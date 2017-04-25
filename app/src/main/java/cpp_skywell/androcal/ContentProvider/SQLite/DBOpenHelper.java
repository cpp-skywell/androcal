package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by zhangliang on 4/23/17.
 */

public class DBOpenHelper extends SQLiteOpenHelper {
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
        db.execSQL(EventsDAO.SQL_CREATE_TABLE);
        Log.d("DBOpenHelper", "Table " + EventsDAO.Table.NAME + " created");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBOpenHelper", "Downgrade database");
        this.initDatabase(db);
    }

    private void initDatabase(SQLiteDatabase db) {
        db.execSQL(EventsDAO.SQL_DROP_TABLE);
        db.execSQL(EventsDAO.SQL_CREATE_TABLE);
    }
}
