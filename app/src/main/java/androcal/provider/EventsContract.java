package androcal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jlouie on 2017/06/10.
 */
public class EventsContract implements BaseColumns {
    public static final String TABLE_NAME = "events";
    public static final Uri CONTENT_URI = Uri.parse("content://" + LocalCalendarProvider.AUTHORITY + "/" + TABLE_NAME);

    public static final String EVENT_TITLE = "name";
    public static final String START = "start";
    public static final String END = "end";
    public static final String STATUS = "status";
    public static final String RECURRENCE = "recurrence";
//    public static final String WEB_CALENDAR = "source";
//    public static final String WEB_ID = "ref_id";
    public static final String DIRTY = "dirty";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    EVENT_TITLE + " TEXT," +
                    START + " INTEGER," +
                    END + " INTEGER," +
                    RECURRENCE + " TEXT," +
//                    WEB_CALENDAR + " TEXT," +
//                    WEB_ID + " TEXT," +
                    DIRTY + " INTEGER," +
                    STATUS + " INTEGER)";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
