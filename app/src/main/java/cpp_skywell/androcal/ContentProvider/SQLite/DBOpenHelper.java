package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhangliang on 4/23/17.
 */

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "androcal";
    private static final String SQL_CREATE_TABLE_EVENTS =
            "CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY, name TEXT";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables if not exist
        db.execSQL(SQL_CREATE_TABLE_EVENTS);
    }
}
