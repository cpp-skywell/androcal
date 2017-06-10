package androcal.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jlouie on 2017/06/10.
 */
public class OpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "androcal";

    public OpenHelper(Context context) {
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
        db.execSQL(CustomFields.SQL_CREATE_TABLE);
        Log.d("DBOpenHelper", Events.NAME + " created");
        Log.d("DBOpenHelper", CustomFields.NAME + " created");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBOpenHelper", "Downgrade database");
        this.initDatabase(db);
    }

    private void initDatabase(SQLiteDatabase db) {
        db.execSQL(Events.SQL_DROP_TABLE);
        db.execSQL(CustomFields.SQL_DROP_TABLE);
        db.execSQL(Events.SQL_CREATE_TABLE);
        db.execSQL(CustomFields.SQL_CREATE_TABLE);
    }
}
