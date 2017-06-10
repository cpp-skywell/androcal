package androcal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jlouie on 2017/06/10.
 */
public class Events implements BaseColumns {
    public static final String NAME = "events";
    public static final Uri CONTENT_URI = Uri.parse("content://" + LocalCalendarProvider.AUTHORITY + "/" + NAME);

    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_START = "start";
    public static final String COLUMN_NAME_END = "end";
    public static final String COLUMN_NAME_STATUS = "status";
    public static final String COLUMN_NAME_RECURRENCE = "recurrence";
    public static final String COLUMN_NAME_SOURCE = "source";
    public static final String WEB_ID = "ref_id";
    public static final String COLUMN_NAME_DIRTY = "dirty";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_NAME + " TEXT," +
                    COLUMN_NAME_START + " INTEGER," +
                    COLUMN_NAME_END + " INTEGER," +
                    COLUMN_NAME_RECURRENCE + " TEXT," +
                    COLUMN_NAME_SOURCE + " TEXT," +
                    WEB_ID + " TEXT," +
                    COLUMN_NAME_DIRTY + " INTEGER," +
                    COLUMN_NAME_STATUS + " INTEGER)";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + NAME;
}
