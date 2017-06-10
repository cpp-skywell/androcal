package androcal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jlouie on 2017/06/10.
 */
public class CustomFieldsContract implements BaseColumns {
    public static final String TABLE_NAME = "custom_fields";
    public static final Uri CONTENT_URI = Uri.parse("content://" + LocalCalendarProvider.AUTHORITY + "/" + TABLE_NAME);

    public static final String EVENT_ID = "event_id";
    public static final String FIELD_NAME = "name";
    public static final String VALUE = "value";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    EVENT_ID + " INTEGER," +
                    FIELD_NAME + " TEXT," +
                    VALUE + " TEXT)";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
