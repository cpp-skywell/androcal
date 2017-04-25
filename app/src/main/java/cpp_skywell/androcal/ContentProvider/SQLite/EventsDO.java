package cpp_skywell.androcal.ContentProvider.SQLite;

import java.util.Date;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventsDO {
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_CANCEL = 2;


    protected long id;
    protected String name;
    protected Date start;
    protected Date end;
    protected int statue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String toString() {
        return "id=" + this.id + "; " +
                "name=" + this.name + "; " +
                "start=" + this.start.toString() + "; " +
                "end=" + this.end.toString() + "; " +
                "status=" + this.statue + "; ";
    }
}
