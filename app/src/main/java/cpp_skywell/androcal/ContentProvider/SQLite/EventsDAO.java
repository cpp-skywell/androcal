package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cpp_skywell.androcal.ContentProvider.EventsDO;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventsDAO {

    protected ContentResolver mContentResolver = null;

    public EventsDAO(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    protected ContentResolver getResolver() {
        return mContentResolver;
    }

    public long add(EventsDO event) {
        ContentValues values = new ContentValues();
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_NAME, event.getName());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_START, event.getStart().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_END, event.getEnd().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_STATUS, event.getStatus());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_REFID, event.getRefId());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_SOURCE, event.getSource().name());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_DIRTY, event.isDirty() ? 1 : 0);
        Uri result = getResolver().insert(LocalCalendarProvider.Events.CONTENT_URI, values);

        return ContentUris.parseId(result);
    }

    public void addCustomFields(EventsDO event) {
        long eventId = event.getId();
        Iterator<Map.Entry<String, String>> itFields = event.getCustomFields().entrySet().iterator();
        while (itFields.hasNext()) { // TODO: use bulk insert
            Map.Entry<String, String> entry = itFields.next();
            ContentValues values = new ContentValues();
            values.put(LocalCalendarProvider.CustomFields.COLUMN_NAME_EVENT_ID, eventId);
            values.put(LocalCalendarProvider.CustomFields.COLUMN_NAME_NAME, entry.getKey());
            values.put(LocalCalendarProvider.CustomFields.COLUMN_NAME_VALUE, entry.getValue());
            getResolver().insert(LocalCalendarProvider.CustomFields.CONTENT_URI, values);
        }
    }

    public EventsDO get(long id) {
        // Columns to return
        String[] columns = {
                LocalCalendarProvider.Events._ID,
                LocalCalendarProvider.Events.COLUMN_NAME_NAME,
                LocalCalendarProvider.Events.COLUMN_NAME_START,
                LocalCalendarProvider.Events.COLUMN_NAME_END,
                LocalCalendarProvider.Events.COLUMN_NAME_STATUS,
                LocalCalendarProvider.Events.COLUMN_NAME_SOURCE,
                LocalCalendarProvider.Events.COLUMN_NAME_REFID,
                LocalCalendarProvider.Events.COLUMN_NAME_DIRTY
        };

        // Filters
        String selection = LocalCalendarProvider.Events._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute SQL
        Cursor cursor = getResolver().query(
                LocalCalendarProvider.Events.CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                null
        );

        // Fetch result
        EventsDO event = null;
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            event = this.mapEventsColumns(cursor);
        }
        cursor.close();

        return event;
    }

    public Map<String, String> getCustomFields(long eventId) {
        // Colums to return
        String[] columns = {
                LocalCalendarProvider.CustomFields.COLUMN_NAME_NAME,
                LocalCalendarProvider.CustomFields.COLUMN_NAME_VALUE
        };
        // Filters
        String selection = LocalCalendarProvider.CustomFields.COLUMN_NAME_EVENT_ID + "=?";
        String[] selectionArgs = {String.valueOf(eventId)};
        // Execute SQL
        Cursor cursor = getResolver().query(
                LocalCalendarProvider.CustomFields.CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                null
        );
        // Fetch result
        Map<String, String> customFields = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(LocalCalendarProvider.CustomFields.COLUMN_NAME_NAME);
            String name = cursor.getString(index);

            index = cursor.getColumnIndex(LocalCalendarProvider.CustomFields.COLUMN_NAME_VALUE);
            String value = cursor.getString(index);

            customFields.put(name, value);
        }
        cursor.close();

        return customFields;
    }

    public int cancel(long eventId) {
        return this.updateStatusById(eventId, EventsDO.STATUS_CANCEL);
    }

    public int updateStatusById(long id, int status) {
        // New values
        ContentValues values = new ContentValues();
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_DIRTY, 1);
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_STATUS, status);

        // Filters
        String selection = LocalCalendarProvider.Events._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute
        return getResolver().update(
                LocalCalendarProvider.Events.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public int deleteById(long id) {
        // Filters
        String selection = LocalCalendarProvider.Events._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute
        return getResolver().delete(LocalCalendarProvider.Events.CONTENT_URI, selection, selectionArgs);
    }

    public int deleteByRefId(EventsDO.Source source, String refId) {
        // Filters
        String selection = LocalCalendarProvider.Events.COLUMN_NAME_SOURCE + "=? AND " + LocalCalendarProvider.Events.COLUMN_NAME_REFID + "=?";
        String[] selectionArgs = {source.name(), refId};

        // Execute
        return getResolver().delete(LocalCalendarProvider.Events.CONTENT_URI, selection, selectionArgs);
    }

    public int updateById(EventsDO newEvent) {
        // New values
        ContentValues values = new ContentValues();
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_NAME, newEvent.getName());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_START, newEvent.getStart().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_END, newEvent.getEnd().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_STATUS, newEvent.getStatus());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_REFID, newEvent.getRefId());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_SOURCE, newEvent.getSource().name());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_DIRTY, newEvent.isDirty() ? 1 : 0);

        // Filters
        String selection = LocalCalendarProvider.Events._ID + "=?";
        String[] selectionArgs = {String.valueOf(newEvent.getId())};

        // Execute
        return getResolver().update(
                LocalCalendarProvider.Events.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public int updateByRefId(EventsDO newEvent) {
        // New values
        ContentValues values = new ContentValues();
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_NAME, newEvent.getName());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_START, newEvent.getStart().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_END, newEvent.getEnd().getTime());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_STATUS, newEvent.getStatus());
        values.put(LocalCalendarProvider.Events.COLUMN_NAME_DIRTY, newEvent.isDirty() ? 1 : 0);
//        values.put(Events.COLUMN_NAME_REFID, newEvent.getRefId());
//        values.put(Events.COLUMN_NAME_SOURCE, newEvent.getSource().name());

        // Filters
        String selection = LocalCalendarProvider.Events.COLUMN_NAME_SOURCE + "=? AND " + LocalCalendarProvider.Events.COLUMN_NAME_REFID + "=?";
        String[] selectionArgs = {newEvent.getSource().name(), newEvent.getRefId()};

        // Execute
        return getResolver().update(
                LocalCalendarProvider.Events.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public List<EventsDO> getByDirty(boolean dirty) {
        // Columns to return
        String[] columns = {
                LocalCalendarProvider.Events._ID,
                LocalCalendarProvider.Events.COLUMN_NAME_NAME,
                LocalCalendarProvider.Events.COLUMN_NAME_START,
                LocalCalendarProvider.Events.COLUMN_NAME_END,
                LocalCalendarProvider.Events.COLUMN_NAME_STATUS,
                LocalCalendarProvider.Events.COLUMN_NAME_SOURCE,
                LocalCalendarProvider.Events.COLUMN_NAME_REFID,
                LocalCalendarProvider.Events.COLUMN_NAME_DIRTY
        };

        // Filters
        String selection = LocalCalendarProvider.Events.COLUMN_NAME_DIRTY + "=?";
        String[] selectionArgs = {dirty ? "1" : "0"};

        // Execute
        Cursor cursor = getResolver().query(
                LocalCalendarProvider.Events.CONTENT_URI, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                null // order by
        );

        // Fetch result
        List<EventsDO> result = new ArrayList<EventsDO>();
        while (cursor.moveToNext()) {
            EventsDO event = this.mapEventsColumns(cursor);
            result.add(event);
        }
        cursor.close();

        return result;
    }

    public List<EventsDO> getByDateRange(Date start, Date end) {
        // Columns to return
        String[] columns = {
                LocalCalendarProvider.Events._ID,
                LocalCalendarProvider.Events.COLUMN_NAME_NAME,
                LocalCalendarProvider.Events.COLUMN_NAME_START,
                LocalCalendarProvider.Events.COLUMN_NAME_END,
                LocalCalendarProvider.Events.COLUMN_NAME_STATUS,
                LocalCalendarProvider.Events.COLUMN_NAME_SOURCE,
                LocalCalendarProvider.Events.COLUMN_NAME_REFID,
                LocalCalendarProvider.Events.COLUMN_NAME_DIRTY

        };

        // Filters
        long now = new Date().getTime();
        long defaultDateStart = now - 365 * 86400 * 1000; // One year before
        long defaultDataEnd = now + 365 * 86400 * 1000; // One year after
        String tsStart = String.valueOf(start != null ? start.getTime() : defaultDateStart);
        String tsEnd = String.valueOf(end != null ? end.getTime() : defaultDataEnd);
        String selection = LocalCalendarProvider.Events.COLUMN_NAME_STATUS + "<>? AND (" + LocalCalendarProvider.Events.COLUMN_NAME_START + " BETWEEN ? AND ? OR " + LocalCalendarProvider.Events.COLUMN_NAME_END + " BETWEEN ? AND ?)";
        String[] selectionArgs = {String.valueOf(EventsDO.STATUS_CANCEL), tsStart, tsEnd, tsStart, tsEnd};

        // Sort order
        String order = LocalCalendarProvider.Events.COLUMN_NAME_START + " ASC";

        // Execute SQL
        Cursor cursor = getResolver().query(
                LocalCalendarProvider.Events.CONTENT_URI, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                order // order by
        );

        // Fetch result
        List<EventsDO> result = new ArrayList<EventsDO>();
        while (cursor.moveToNext()) {
            EventsDO event = this.mapEventsColumns(cursor);
            result.add(event);
        }
        cursor.close();

        return result;
    }

    public void dropTable() {
        getResolver().call(LocalCalendarProvider.Events.CONTENT_URI, LocalCalendarProvider.CALL_DROP_EVENTS, null, null);
        getResolver().call(LocalCalendarProvider.CustomFields.CONTENT_URI, LocalCalendarProvider.CALL_DROP_CUSTOMFIELDS, null, null);
    }

    public void createTable() {
        getResolver().call(LocalCalendarProvider.Events.CONTENT_URI, LocalCalendarProvider.CALL_CREATE_EVENTS, null, null);
        getResolver().call(LocalCalendarProvider.CustomFields.CONTENT_URI, LocalCalendarProvider.CALL_CREATE_CUSTOMFIELDS, null, null);
    }

    private EventsDO mapEventsColumns(Cursor cursor) {
        EventsDO event = new EventsDO();

        int index = cursor.getColumnIndex(LocalCalendarProvider.Events._ID);
        event.setId(index == -1 ? 0 : cursor.getLong(index));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_NAME);
        event.setName(index == -1 ? null : cursor.getString(index));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_START);
        event.setStart(index == -1 ? null : new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_END);
        event.setEnd(index == -1 ? null : new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_STATUS);
        event.setStatus(index == -1 ? EventsDO.STATUS_NORMAL : cursor.getInt(index));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_SOURCE);
        event.setSource(index == -1 ? EventsDO.Source.NONE : EventsDO.Source.valueOf(cursor.getString(index)));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_REFID);
        event.setRefId(index == -1 ? null : cursor.getString(index));

        index = cursor.getColumnIndex(LocalCalendarProvider.Events.COLUMN_NAME_DIRTY);
        event.setDirty(index == -1 ? false : (cursor.getInt(index) == 1));

        return event;
    }
}
