package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zhangliang on 5/21/17.
 */

public class LocalCalendarProvider extends ContentProvider {
    public static final String AUTHORITY = "cpp_skywell.androcal.provider";

    private DBOpenHelper mOpenHelper = null;

    public LocalCalendarProvider() {
        super();
    }

    protected SQLiteDatabase getReader() {
        return this.mOpenHelper.getReadableDatabase();
    }

    protected SQLiteDatabase getWriter() {
        return this.mOpenHelper.getWritableDatabase();
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = getReader();
        return db.query(
                LocalCalendarProvider.Events.NAME, // Table name
                projection, // columns to return
                selection, // filters for WHERE clause
                selectionArgs, // values for selection
                null, // group
                null, // having
                sortOrder // order by
        );
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = this.getWriter();
        long id = db.insert(LocalCalendarProvider.Events.NAME, null, values);
        return ContentUris.withAppendedId(Events.CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = this.getWriter();
        return db.delete(LocalCalendarProvider.Events.NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = this.getWriter();
        return db.update(
                LocalCalendarProvider.Events.NAME,
                values,
                selection,
                selectionArgs);
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (method.equals("drop")) {
            this.getWriter().execSQL(Events.SQL_DROP_TABLE);
        } else if (method.equals("create")) {
            this.getWriter().execSQL((Events.SQL_CREATE_TABLE));
        }
        return null;
    }

    public void execSQL(String sql) {
        this.getWriter().execSQL(sql);
    }

    public static class Events implements BaseColumns {
        public static final String NAME = "events";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NAME);

        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_END = "end";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_RECURRENCE = "recurrence";
        public static final String COLUMN_NAME_SOURCE = "source";
        public static final String COLUMN_NAME_REFID = "ref_id";
        public static final String COLUMN_NAME_DIRTY = "dirty";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_NAME + " TEXT," +
                        COLUMN_NAME_START + " INTEGER," +
                        COLUMN_NAME_END + " INTEGER," +
                        COLUMN_NAME_RECURRENCE + " TEXT," +
                        COLUMN_NAME_SOURCE + " TEXT," +
                        COLUMN_NAME_REFID + " TEXT," +
                        COLUMN_NAME_DIRTY + " INTEGER," +
                        COLUMN_NAME_STATUS + " INTEGER)";
        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + NAME;
    }

    public static class DBOpenHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "androcal";

        public DBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop and Create
            Log.d("DBOpenHelper", "Upgrade database");
            this.initDatabase(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create tables if not exist
            db.execSQL(Events.SQL_CREATE_TABLE);
            Log.d("DBOpenHelper", "Events " + Events.NAME + " created");
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("DBOpenHelper", "Downgrade database");
            this.initDatabase(db);
        }

        private void initDatabase(SQLiteDatabase db) {
            db.execSQL(Events.SQL_DROP_TABLE);
            db.execSQL(Events.SQL_CREATE_TABLE);
        }
    }
}
