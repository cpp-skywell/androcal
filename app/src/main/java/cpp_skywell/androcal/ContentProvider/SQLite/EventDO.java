package cpp_skywell.androcal.ContentProvider.SQLite;

import java.util.Date;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventDO {
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_CANCEL = 2;

    protected int id;
    protected String name;
    protected Date start;
    protected Date end;
    protected int statue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }
}
