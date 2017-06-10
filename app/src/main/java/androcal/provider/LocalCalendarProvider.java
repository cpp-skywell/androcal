package androcal.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangliang on 5/21/17.
 */

public class LocalCalendarProvider extends ContentProvider {
    public static final String AUTHORITY = "androcal.provider";
    public static final String CALL_DROP_EVENTS = "drop_events";
    public static final String CALL_CREATE_EVENTS = "create_events";
    public static final String CALL_DROP_CUSTOMFIELDS = "drop_cust";
    public static final String CALL_CREATE_CUSTOMFIELDS = "create_cust";

    private static final Map<Uri, String> mTableMap = new HashMap<Uri, String>();

    static {
        mTableMap.put(Events.CONTENT_URI, Events.NAME);
        mTableMap.put(CustomFields.CONTENT_URI, CustomFields.NAME);
    }

    private OpenHelper mOpenHelper = null;

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
        mOpenHelper = new OpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = getReader();
        return db.query(
                getTableName(uri), // Table name
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
        long id = db.insert(getTableName(uri), null, values);
        return ContentUris.withAppendedId(Events.CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = this.getWriter();
        return db.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = this.getWriter();
        return db.update(
                getTableName(uri),
                values,
                selection,
                selectionArgs);
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (method.equals(CALL_DROP_EVENTS)) {
            this.getWriter().execSQL(Events.SQL_DROP_TABLE);
        } else if (method.equals(CALL_CREATE_EVENTS)) {
            this.getWriter().execSQL((Events.SQL_CREATE_TABLE));
        } else if (method.equals(CALL_DROP_CUSTOMFIELDS)) {
            this.getWriter().execSQL(CustomFields.SQL_DROP_TABLE);
        } else if (method.equals(CALL_CREATE_CUSTOMFIELDS)) {
            this.getWriter().execSQL(CustomFields.SQL_CREATE_TABLE);
        }
        return null;
    }

    protected String getTableName(@NonNull Uri uri) {
        // This method is frequently called, so use hash map for performance consideration
        return mTableMap.get(uri);
    }

    public void execSQL(String sql) {
        this.getWriter().execSQL(sql);
    }

}
