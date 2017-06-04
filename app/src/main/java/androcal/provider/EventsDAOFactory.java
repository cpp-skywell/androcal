package androcal.provider;

import android.content.Context;

import androcal.provider.EventsDAO;

/**
 * Created by zhangliang on 5/21/17.
 * @deprecated 
 */

public class EventsDAOFactory {
    public static EventsDAO create(Context context) {
        return new EventsDAO(context.getContentResolver());
    }
}
