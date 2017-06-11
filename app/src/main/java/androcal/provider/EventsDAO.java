package androcal.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This an abstraction of the LocalCalendarProvider class. It allows us to use EventsDO directly
 * instead of doing SQL queries.
 *
 * @author Liang Zhang
 * @author Johnathan Louie
 * @version 0.3.1
 * @since 0.0.0
 */
public class EventsDAO {

    protected ContentResolver mContentResolver = null;

    public EventsDAO(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    public EventsDAO(Context context) {
        this(context.getContentResolver());
    }

    // this is redundant
    protected ContentResolver getResolver() {
        return mContentResolver;
    }

    /**
     * Store a new event locally.
     *
     * @param event The object containing event data.
     * @return The _ID of the row.
     */
    public long add(EventsDO event) {
        ContentValues values = new ContentValues();
        values.put(EventsContract.EVENT_TITLE, event.getName());
        values.put(EventsContract.START, event.getStart().getTime());
        values.put(EventsContract.END, event.getEnd().getTime());
        values.put(EventsContract.STATUS, event.getStatus());
        values.put(EventsContract.WEB_ID, event.getRefId());
        values.put(EventsContract.WEB_CALENDAR, event.getSource().name());
        values.put(EventsContract.DIRTY, event.isDirty() ? 1 : 0);
        Uri result = getResolver().insert(EventsContract.CONTENT_URI, values);
        addCustomFields(event);
        return ContentUris.parseId(result);
    }

    /**
     *
     * @param id The ID of the event.
     * @return The number of rows deleted.
     */
    private int deleteCustomFields(long id)
    {
        String selection = CustomFieldsContract.LOCAL_ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        return getResolver().delete(CustomFieldsContract.CONTENT_URI, selection, selectionArgs);
    }

    private void addCustomFields(EventsDO event) {
        long eventId = event.getId();
        Iterator<Map.Entry<String, String>> itFields = event.getCustomFields().entrySet().iterator();
        while (itFields.hasNext()) { // TODO: use bulk insert
            Map.Entry<String, String> entry = itFields.next();
            ContentValues values = new ContentValues();
            values.put(CustomFieldsContract.LOCAL_ID, eventId);
            values.put(CustomFieldsContract.FIELD_NAME, entry.getKey());
            values.put(CustomFieldsContract.VALUE, entry.getValue());
            getResolver().insert(CustomFieldsContract.CONTENT_URI, values);
        }
    }

    public EventsDO get(long id) {
        // Columns to return
        String[] columns = {
                EventsContract._ID,
                EventsContract.EVENT_TITLE,
                EventsContract.START,
                EventsContract.END,
                EventsContract.STATUS,
                EventsContract.WEB_CALENDAR,
                EventsContract.WEB_ID,
                EventsContract.DIRTY
        };

        // Filters
        String selection = EventsContract._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute SQL
        Cursor cursor = getResolver().query(
                EventsContract.CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                null
        );

        // Fetch result
        EventsDO event = null;
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            event = mapEventsColumns(cursor);
        }
        cursor.close();

        event.setCustomFields(getCustomFields(id));

        return event;
    }

    private Map<String, String> getCustomFields(long eventId) {
        // Columns to return
        String[] columns = {
                CustomFieldsContract.FIELD_NAME,
                CustomFieldsContract.VALUE
        };
        // Filters
        String selection = CustomFieldsContract.LOCAL_ID + "=?";
        String[] selectionArgs = {String.valueOf(eventId)};
        // Execute SQL
        Cursor cursor = getResolver().query(
                CustomFieldsContract.CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                null
        );
        // Fetch result
        Map<String, String> customFields = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(CustomFieldsContract.FIELD_NAME);
            String name = cursor.getString(index);

            index = cursor.getColumnIndex(CustomFieldsContract.VALUE);
            String value = cursor.getString(index);

            customFields.put(name, value);
        }
        cursor.close();

        return customFields;
    }

    public int cancel(long eventId) {
        return updateStatusByLocalId(eventId, EventsDO.STATUS_CANCEL);
    }

    public int updateStatusByLocalId(long id, int status) {
        // New values
        ContentValues values = new ContentValues();
        values.put(EventsContract.DIRTY, 1);
        values.put(EventsContract.STATUS, status);

        // Filters
        String selection = EventsContract._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute
        return getResolver().update(
                EventsContract.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public int deleteByLocalId(long id) {
        // Filters
        String selection = EventsContract._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute
        deleteCustomFields(id);
        return getResolver().delete(EventsContract.CONTENT_URI, selection, selectionArgs);
    }

    public int deleteByWebId(EventsDO.Source source, String webId) {
        // Filters
        String selection = EventsContract.WEB_CALENDAR + "=? AND " + EventsContract.WEB_ID + "=?";
        String[] selectionArgs = {source.name(), webId};

        // Execute
        return getResolver().delete(EventsContract.CONTENT_URI, selection, selectionArgs);
    }

    public int updateByLocalId(EventsDO event) {
        // New values
        ContentValues values = new ContentValues();
        values.put(EventsContract.EVENT_TITLE, event.getName());
        values.put(EventsContract.START, event.getStart().getTime());
        values.put(EventsContract.END, event.getEnd().getTime());
        values.put(EventsContract.STATUS, event.getStatus());
        values.put(EventsContract.WEB_ID, event.getRefId());
        values.put(EventsContract.WEB_CALENDAR, event.getSource().name());
        values.put(EventsContract.DIRTY, event.isDirty() ? 1 : 0);

        // Filters
        String selection = EventsContract._ID + "=?";
        String[] selectionArgs = {String.valueOf(event.getId())};

        deleteCustomFields(event.getId());
        addCustomFields(event);

        // Execute
        return getResolver().update(
                EventsContract.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public int updateByWebId(EventsDO newEvent) {
        // New values
        ContentValues values = new ContentValues();
        values.put(EventsContract.EVENT_TITLE, newEvent.getName());
        values.put(EventsContract.START, newEvent.getStart().getTime());
        values.put(EventsContract.END, newEvent.getEnd().getTime());
        values.put(EventsContract.STATUS, newEvent.getStatus());
        values.put(EventsContract.DIRTY, newEvent.isDirty() ? 1 : 0);

        // Filters
        String selection = EventsContract.WEB_CALENDAR + "=? AND " + EventsContract.WEB_ID + "=?";
        String[] selectionArgs = {newEvent.getSource().name(), newEvent.getRefId()};

        // Execute
        return getResolver().update(
                EventsContract.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public List<EventsDO> getByDirty(boolean dirty) {
        // Columns to return
        String[] columns = {
                EventsContract._ID,
                EventsContract.EVENT_TITLE,
                EventsContract.START,
                EventsContract.END,
                EventsContract.STATUS,
                EventsContract.WEB_CALENDAR,
                EventsContract.WEB_ID,
                EventsContract.DIRTY
        };

        // Filters
        String selection = EventsContract.DIRTY + "=?";
        String[] selectionArgs = {dirty ? "1" : "0"};

        // Execute
        Cursor cursor = getResolver().query(
                EventsContract.CONTENT_URI, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                null // order by
        );

        // Fetch result
        List<EventsDO> result = new ArrayList<EventsDO>();
        while (cursor.moveToNext()) {
            EventsDO event = mapEventsColumns(cursor);
            result.add(event);
        }
        cursor.close();

        return result;
    }

    public List<EventsDO> getByDateRange(Date start, Date end) {
        // Columns to return
        String[] columns = {
                EventsContract._ID,
                EventsContract.EVENT_TITLE,
                EventsContract.START,
                EventsContract.END,
                EventsContract.STATUS,
                EventsContract.WEB_CALENDAR,
                EventsContract.WEB_ID,
                EventsContract.DIRTY

        };

        // Filters
        long now = new Date().getTime();
        long defaultDateStart = now - 365 * 86400 * 1000; // One year before
        long defaultDataEnd = now + 365 * 86400 * 1000; // One year after
        String tsStart = String.valueOf(start != null ? start.getTime() : defaultDateStart);
        String tsEnd = String.valueOf(end != null ? end.getTime() : defaultDataEnd);
        String selection = EventsContract.STATUS + "<>? AND (" + EventsContract.START + " BETWEEN ? AND ? OR " + EventsContract.END + " BETWEEN ? AND ?)";
        String[] selectionArgs = {String.valueOf(EventsDO.STATUS_CANCEL), tsStart, tsEnd, tsStart, tsEnd};

        // Sort order
        String order = EventsContract.START + " ASC";

        // Execute SQL
        Cursor cursor = getResolver().query(
                EventsContract.CONTENT_URI, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                order // order by
        );

        // Fetch result
        List<EventsDO> result = new ArrayList<EventsDO>();
        while (cursor.moveToNext()) {
            EventsDO event = mapEventsColumns(cursor);
            result.add(event);
        }
        cursor.close();

        return result;
    }

    public void dropTable() {
        getResolver().call(EventsContract.CONTENT_URI, LocalCalendarProvider.CALL_DROP_EVENTS, null, null);
        getResolver().call(CustomFieldsContract.CONTENT_URI, LocalCalendarProvider.CALL_DROP_CUSTOMFIELDS, null, null);
    }

    public void createTable() {
        getResolver().call(EventsContract.CONTENT_URI, LocalCalendarProvider.CALL_CREATE_EVENTS, null, null);
        getResolver().call(CustomFieldsContract.CONTENT_URI, LocalCalendarProvider.CALL_CREATE_CUSTOMFIELDS, null, null);
    }

    private EventsDO mapEventsColumns(Cursor cursor) {
        EventsDO event = new EventsDO();

        int index = cursor.getColumnIndex(EventsContract._ID);
        event.setId(index == -1 ? 0 : cursor.getLong(index));

        index = cursor.getColumnIndex(EventsContract.EVENT_TITLE);
        event.setName(index == -1 ? null : cursor.getString(index));

        index = cursor.getColumnIndex(EventsContract.START);
        event.setStart(index == -1 ? null : new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(EventsContract.END);
        event.setEnd(index == -1 ? null : new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(EventsContract.STATUS);
        event.setStatus(index == -1 ? EventsDO.STATUS_NORMAL : cursor.getInt(index));

        index = cursor.getColumnIndex(EventsContract.WEB_CALENDAR);
        event.setSource(index == -1 ? EventsDO.Source.NONE : EventsDO.Source.valueOf(cursor.getString(index)));

        index = cursor.getColumnIndex(EventsContract.WEB_ID);
        event.setRefId(index == -1 ? null : cursor.getString(index));

        index = cursor.getColumnIndex(EventsContract.DIRTY);
        event.setDirty(index == -1 ? false : (cursor.getInt(index) == 1));

        return event;
    }
}
