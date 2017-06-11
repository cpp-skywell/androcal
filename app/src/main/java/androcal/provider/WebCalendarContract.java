package androcal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jlouie on 2017/06/10.
 */
public class WebCalendarContract implements BaseColumns {
    public static final String TABLE_NAME = "web_calendar";
    public static final Uri CONTENT_URI = Uri.parse("content://" + LocalCalendarProvider.AUTHORITY + "/" + TABLE_NAME);

    public static final String LOCAL_ID = "local";
    public static final String GOOGLE_ID = "google";
    public static final String OUTLOOK_ID = "outlook";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    LOCAL_ID + " INTEGER," +
                    GOOGLE_ID + " TEXT," +
                    OUTLOOK_ID + " TEXT)";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
