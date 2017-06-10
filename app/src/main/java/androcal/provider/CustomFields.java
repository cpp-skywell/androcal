package androcal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jlouie on 2017/06/10.
 */
public class CustomFields implements BaseColumns {
    public static final String NAME = "custom_fields";
    public static final Uri CONTENT_URI = Uri.parse("content://" + LocalCalendarProvider.AUTHORITY + "/" + NAME);

    public static final String COLUMN_NAME_EVENT_ID = "event_id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_VALUE = "value";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_EVENT_ID + " INTEGER," +
                    COLUMN_NAME_NAME + " TEXT," +
                    COLUMN_NAME_VALUE + " TEXT)";
    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + NAME;
}
