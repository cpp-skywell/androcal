package cpp_skywell.androcal.ContentProvider.SQLite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventDAO {
    public void add(EventDO event) {

    }

    public EventDO get(int id) {
        EventDO event = new EventDO();
        event.setId(id);
        event.setName("test event");
        event.setStatue(EventDO.STATUS_NORMAL);
        event.setStart(new Date());
        event.setEnd(new Date());

        return event;
    }

    public void delete(int id) {

    }

    public void update(EventDO newEvent) {

    }

    public List<EventDO> getByDateRange(Date start, Date end) {
        List<EventDO> result = new ArrayList<EventDO>();
        for (int i = 0; i < 5; i ++) {
            EventDO event = new EventDO();
            event.setId(i);
            event.setName("test event " + i);
            event.setStatue(EventDO.STATUS_NORMAL);
            event.setStart(new Date());
            event.setEnd(new Date());

            result.add(event);
        }

        return result;
    }
}
