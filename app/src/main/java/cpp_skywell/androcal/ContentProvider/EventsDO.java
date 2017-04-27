package cpp_skywell.androcal.ContentProvider;

import java.util.Date;
import java.util.Objects;

/**
 * Created by zhangliang on 4/23/17.
 */

public class EventsDO {
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_CANCEL = 2;
    public static enum Source {
        NONE, GOOGLE, OTHER;
    };


    protected long id;
    protected String name;
    protected Date start;
    protected Date end;
    protected int status;
    protected Source source;
    protected String refId;
    protected boolean dirty = false;

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
        this.start = new Date(start.getTime()/1000*1000);
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = new Date(end.getTime()/1000*1000);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int localHash() {
        int result = name.hashCode();
        result = result * 37 + start.hashCode();
        result = result * 37 + end.hashCode();
        result = result * 37 + status;
        return result;
    }

    public String toString() {
        return "id=" + this.id + "; " +
                "name=" + (this.name == null? "null": this.name) + "; " +
                "start=" + (this.start == null? "null": this.start.toString()) + "; " +
                "end=" + (this.end == null? "null": this.end.toString()) + "; " +
                "refId=" + (this.refId == null? "null": this.refId) + "; " +
                "source=" + (this.source == null? "null": this.source) + "; " +
                "dirty=" + (this.dirty? "1": "0") + "; " +
                "status=" + this.status;
    }
}
