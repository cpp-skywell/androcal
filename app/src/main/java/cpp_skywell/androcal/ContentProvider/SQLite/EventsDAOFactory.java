package cpp_skywell.androcal.ContentProvider.SQLite;

import android.content.Context;

/**
 * Created by zhangliang on 5/21/17.
 * @deprecated 
 */

public class EventsDAOFactory {
    public static EventsDAO create(Context context) {
        return new EventsDAO(context.getContentResolver());
    }
}
