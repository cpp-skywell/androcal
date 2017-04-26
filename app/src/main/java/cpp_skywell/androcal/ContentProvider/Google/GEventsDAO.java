package cpp_skywell.androcal.ContentProvider.Google;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.MainActivity;

/**
 * Created by zhangliang on 4/26/17.
 */

public class GEventsDAO {
    static class Loader {
        static GEventsDAO INSTANCE = new GEventsDAO();
    }

    static final String CALENDAR_ID = "primary";
    static final String APP_NAME = "cpp_skywell.androcal";

    private Calendar mService = null;

    public void init(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.mService = new Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(APP_NAME)
                .build();
    }

    private void EventsDAO() {}

    public static GEventsDAO getInstance() {
        return Loader.INSTANCE;
    }

    public void checkAuth() throws IOException {
        // Call a simple API to trigger UserRecoverableAuthIOException
        this.mService.calendars().get(CALENDAR_ID).execute();
    }

    public List<EventsDO> getByDateRange(Date timeMin, Date timeMax) throws IOException {
        List<EventsDO> eventList = new ArrayList<EventsDO>();
        Events events = this.mService.events().list(CALENDAR_ID)
                .setTimeMin(new DateTime(timeMin.getTime()))
                .setTimeMax(new DateTime(timeMax.getTime()))
                .setOrderBy(Fields.FIELD_NAME_START_TIME)
                .setSingleEvents(true)
                .execute();

        Iterator<Event> it = events.getItems().iterator();
        while(it.hasNext()) {
            Event item = it.next();
            EventsDO event = new EventsDO();

            event.setSource(EventsDO.Source.GOOGLE);
            event.setRefId(item.getId());
            event.setName(item.getSummary());
            event.setStart(new Date(item.getStart().getDateTime().getValue()));
            event.setEnd(new Date(item.getEnd().getDateTime().getValue()));
            event.setStatus(EventsDO.STATUS_NORMAL); // TODO: need mapping

            Log.d(APP_NAME, item.toString());
            eventList.add(event);
        }

        return eventList;
    }

    static class Fields {
        static final String FIELD_NAME_START_TIME = "startTime";
    }
}
