package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventsDAO {
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Table.NAME + " (" +
                    Table._ID + " INTEGER PRIMARY KEY," +
                    Table.COLUMN_NAME_NAME + " TEXT," +
                    Table.COLUMN_NAME_START + " INTEGER," +
                    Table.COLUMN_NAME_END + " INTEGER," +
                    Table.COLUMN_NAME_RECURRENCE + " TEXT," +
                    Table.COLUMN_NAME_STATUS + " INTEGER)";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + Table.NAME;

    private SQLiteOpenHelper mOpenHelper = null;

    private EventsDAO() {}

    public static EventsDAO getInstance() {
        return Loader.INSTANCE;
    }

    public void init(SQLiteOpenHelper openHelper) {
        this.mOpenHelper = openHelper;
    }

    protected SQLiteDatabase getReader() {
        return this.mOpenHelper.getReadableDatabase();
    }

    protected SQLiteDatabase getWriter() {
        return this.mOpenHelper.getWritableDatabase();
    }

    public long add(EventsDO event) {
        SQLiteDatabase db = this.getWriter();

        ContentValues values = new ContentValues();
        values.put(Table.COLUMN_NAME_NAME, event.getName());
        values.put(Table.COLUMN_NAME_START, event.getStart().getTime());
        values.put(Table.COLUMN_NAME_END, event.getEnd().getTime());
        values.put(Table.COLUMN_NAME_STATUS, event.getStatue());

        return db.insert(Table.NAME, null, values);
    }

    public EventsDO get(long id) {
        SQLiteDatabase db = getReader();

        // Columns to return
        String[] columns = {
                Table._ID,
                Table.COLUMN_NAME_NAME,
                Table.COLUMN_NAME_START,
                Table.COLUMN_NAME_START,
                Table.COLUMN_NAME_END,
                Table.COLUMN_NAME_STATUS
        };

        // Filters
        String selection = Table._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        // Execute SQL
        Cursor cursor = db.query(
                Table.NAME, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                null, // group
                null, // having
                null // order by
        );

        // Fetch result
        EventsDO event = null;
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            event = this.mapColumns(cursor);
        }
        cursor.close();

        return event;
    }

    public void delete(long id) {
        SQLiteDatabase db = this.getWriter();

        // Filters
        String selection = Table._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        db.delete(Table.NAME, selection, selectionArgs);
    }

    public void update(EventsDO newEvent) {
        // TODO
    }

    public List<EventsDO> getByDateRange(Date start, Date end) {
        SQLiteDatabase db = this.getReader();

        // Columns to return
        String[] columns = {
                Table._ID,
                Table.COLUMN_NAME_NAME,
                Table.COLUMN_NAME_START,
                Table.COLUMN_NAME_START,
                Table.COLUMN_NAME_END,
                Table.COLUMN_NAME_STATUS
        };

        // Filters
        String tsStart = String.valueOf(start.getTime());
        String tsEnd = String.valueOf(end.getTime());
        String selection = Table.COLUMN_NAME_START + " BETWEEN ? AND ? OR " + Table.COLUMN_NAME_END + " BETWEEN ? AND ?";
        String[] selectionArgs = {tsStart, tsEnd, tsStart, tsEnd};

        // Sort order
        String order = Table.COLUMN_NAME_START + " ASC";

        // Execute SQL
        Cursor cursor = db.query(
                Table.NAME, // Table name
                columns, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                null, // group
                null, // having
                null // order by
        );

        // Fetch result
        List<EventsDO> result = new ArrayList<EventsDO>();
        while(cursor.moveToNext()) {
            EventsDO event = this.mapColumns(cursor);
            result.add(event);
        }
        cursor.close();

        return result;
    }

    public void dropTable() {
        this.getWriter().execSQL(SQL_DROP_TABLE);
    }

    public void createTable() {
        this.getWriter().execSQL(SQL_CREATE_TABLE);
    }

    private EventsDO mapColumns(Cursor cursor) {
        EventsDO event = new EventsDO();

        int index = cursor.getColumnIndex(Table._ID);
        event.setId(index == -1? 0: cursor.getLong(index));

        index = cursor.getColumnIndex(Table.COLUMN_NAME_NAME);
        event.setName(index == -1? null: cursor.getString(index));

        index = cursor.getColumnIndex(Table.COLUMN_NAME_START);
        event.setStart(index == -1? null: new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(Table.COLUMN_NAME_END);
        event.setEnd(index == -1? null: new Date(cursor.getLong(index)));

        index = cursor.getColumnIndex(Table.COLUMN_NAME_STATUS);
        event.setStatue(index == -1? EventsDO.STATUS_NORMAL: cursor.getInt(index));

        return event;
    }

    private static class Loader {
        static EventsDAO INSTANCE = new EventsDAO();
    }

    public static class Table implements BaseColumns {
        public static final String NAME = "events";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_END = "end";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_RECURRENCE = "recurrence";
    }
}
