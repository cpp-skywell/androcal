package cpp_skywell.androcal.ContentProvider.Google;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
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
    static final String SYNC_TOKEN_KEY = "google_sync_token";

    private Calendar mService = null;
    private SharedPreferences mDataStore = null;

    public void init(GoogleAccountCredential credential, SharedPreferences dataStore) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.mService = new Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(APP_NAME)
                .build();
        mDataStore = dataStore;
    }

    private void EventsDAO() {}

    public static GEventsDAO getInstance() {
        return Loader.INSTANCE;
    }

    public void checkAuth() throws IOException {
        // Call a simple API to trigger UserRecoverableAuthIOException
        this.mService.calendars().get(CALENDAR_ID).execute();
    }

    public EventsDO add(EventsDO event) throws IOException {
        Event gevent = new Event();
        gevent.setSummary(event.getName());
        gevent.setStart(new EventDateTime()
                .setDateTime(new DateTime(event.getStart().getTime()))
                .setTimeZone(TimeZone.getDefault().getID())
        );
        gevent.setEnd(new EventDateTime()
                .setDateTime(new DateTime(event.getEnd().getTime()))
                .setTimeZone(TimeZone.getDefault().getID())
        );

        Event geventNew = this.mService.events().insert(CALENDAR_ID, gevent).execute();
        EventsDO eventNew = this.mapValues(geventNew);
        eventNew.setId(event.getId());
        eventNew.setDirty(false);

        return eventNew;
    }

    public void deleteBatch(List<EventsDO> eventList) throws IOException {
        if (eventList == null || eventList.size() == 0) {
            return;
        }

        JsonBatchCallback<Void> callback = new JsonBatchCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
            }

            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                Log.d("delBatch", e.getMessage());
            }
        };
        BatchRequest batch = this.mService.batch();

        Iterator<EventsDO> itRequest = eventList.iterator();
        while (itRequest.hasNext()) {
            EventsDO event = itRequest.next();
            this.mService.events().delete(CALENDAR_ID, event.getRefId()).queue(batch, callback);
        }
        batch.execute();
    }

    public List<EventsDO> updateBatch(List<EventsDO> eventList) throws IOException {
        List<EventsDO> events = new ArrayList<EventsDO>();
        if (eventList == null || eventList.size() == 0) {
            return events;
        }

        BatchCallback callback = new BatchCallback();
        BatchRequest batch = this.mService.batch();

        Iterator<EventsDO> itRequest = eventList.iterator();
        while(itRequest.hasNext()) {
            EventsDO event = itRequest.next();

            Event gevent = new Event();
            gevent.setSummary(event.getName());
            gevent.setStart(new EventDateTime()
                    .setDateTime(new DateTime(event.getStart().getTime()))
                    .setTimeZone(TimeZone.getDefault().getID())
            );
            gevent.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(event.getEnd().getTime()))
                    .setTimeZone(TimeZone.getDefault().getID())
            );
            this.mService.events().update(CALENDAR_ID, event.getRefId(), gevent).queue(batch, callback);
        }
        batch.execute();

        // The data on Google is exactly the same as local
        // So just set dirty => false
        itRequest = eventList.iterator();
        while(itRequest.hasNext()) {
            EventsDO event = itRequest.next();
            event.setDirty(false);
            events.add(event);
        }

        return events;
    }

    public List<EventsDO> addBatch(List<EventsDO> eventList) throws IOException {
        List<EventsDO> events = new ArrayList<EventsDO>();
        if (eventList == null || eventList.size() == 0) {
            return events;
        }

        BatchCallback callback = new BatchCallback();
        BatchRequest batch = this.mService.batch();

        Map<Integer, EventsDO> requestMap = new HashMap<Integer, EventsDO>();
        Iterator<EventsDO> itRequest = eventList.iterator();
        while(itRequest.hasNext()) {
            EventsDO event = itRequest.next();
            requestMap.put(event.localHash(), event);
//            Log.d("RequestHash", String.valueOf(event.localHash()) + " : " + event.toString());

            Event gevent = new Event();
            gevent.setSummary(event.getName());
            gevent.setStart(new EventDateTime()
                    .setDateTime(new DateTime(event.getStart().getTime()))
                    .setTimeZone(TimeZone.getDefault().getID())
            );
            gevent.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(event.getEnd().getTime()))
                    .setTimeZone(TimeZone.getDefault().getID())
            );
            this.mService.events().insert(CALENDAR_ID, gevent).queue(batch, callback);
        }
        batch.execute();

        // No refId in local data, so we need to copy it from Google response
        Iterator<Event> itReturn = callback.result.iterator();
        while(itReturn.hasNext()) {
            EventsDO eventNew = this.mapValues(itReturn.next());
            EventsDO eventRequest = requestMap.get(eventNew.localHash());
            if (eventRequest == null) {
                // Should not happen, something wrong with localHash
                Log.e("GEventsDAO.addBatch", "Request event not found:" + eventNew.toString());
//                Log.d("ReturnHash", String.valueOf(eventNew.localHash()) + " : " + eventNew.toString());
                continue;
            }
            eventNew.setId(eventRequest.getId());
            eventNew.setDirty(false);

            events.add(eventNew);
        }

        return events;
    }

    public List<EventsDO> getUpdatedList(int batchSize) throws IOException {
        List<EventsDO> eventList = new ArrayList<EventsDO>();
        Calendar.Events.List request = this.mService.events().list(CALENDAR_ID);

        // Look up the saved sync token
        String syncToken = this.loadDataStore(SYNC_TOKEN_KEY);
        if (syncToken == null) { // Full sync
            Log.d("GEventsDAO", "full sync");
        } else { // Incremental sync
            Log.d("GEventsDAO", "incr sync");
            request.setSyncToken(syncToken);
        }

        // Filters
        // Google don't allow specity filters with sync token

        // Page size
        request.setMaxResults(batchSize);

        // Retrieve events, batch by batch
        String pageToken = null;
        Events events = null;
        do {
            try {
                events = request.execute();
            } catch(GoogleJsonResponseException e) {
                if (e.getStatusCode() == 410) { // sync token invalid
                    this.removeDataStore(SYNC_TOKEN_KEY);
                    throw new InvalidSyncTokenException();
                } else {
                    throw e;
                }
            }

            Iterator<Event> it = events.getItems().iterator();
            while(it.hasNext()) {
                Event item = it.next();
//                Log.d("GEventsDAO", item.toString());
                eventList.add(this.mapValues(item));
            }
        } while(pageToken != null);

        // Save the latest sync token
        this.saveDataStore(SYNC_TOKEN_KEY, events.getNextSyncToken());

        return eventList;
    }

    private String loadDataStore(String key) {
        return mDataStore.getString(key, null);
    }

    private void saveDataStore(String key, String value) {
        SharedPreferences.Editor editor = mDataStore.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void removeDataStore(String key) {
        SharedPreferences.Editor editor = mDataStore.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clearDataStore() {
        this.removeDataStore(SYNC_TOKEN_KEY);
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
            eventList.add(this.mapValues(item));
        }

        return eventList;
    }

    private EventsDO mapValues(Event item) {
        EventsDO event = new EventsDO();
        event.setSource(EventsDO.Source.GOOGLE);
        event.setRefId(item.getId());
        if (Status.fromString(item.getStatus()) != Status.CANCELLED) {
            event.setName(item.getSummary());

            EventDateTime eDateTime = item.getStart();
            if (eDateTime.getDateTime() != null) {
                event.setStart(new Date(eDateTime.getDateTime().getValue()));
            } else { // All day event
                int tzShift = TimeZone.getDefault().getOffset(eDateTime.getDate().getValue());
                event.setStart(new Date(eDateTime.getDate().getValue() - tzShift));
            }

            eDateTime = item.getEnd();
            if (eDateTime.getDateTime() != null) {
                event.setEnd(new Date(eDateTime.getDateTime().getValue()));
            } else { // All day event, add 1day-0.001s
                int tzShift = TimeZone.getDefault().getOffset(eDateTime.getDate().getValue());
                event.setEnd(new Date( eDateTime.getDate().getValue() - tzShift ));
            }
            event.setStatus(EventsDO.STATUS_NORMAL);
        } else {
            event.setStatus(EventsDO.STATUS_CANCEL);
        }

        return event;
    }

    public Calendar getCalendarInstance(){
        return mService;
    }

    public Events getEventsList (){
        Events events = null;
        try {
            events = this.mService.events().list(CALENDAR_ID).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static class InvalidSyncTokenException extends IOException { }

    static class Fields {
        static final String FIELD_NAME_START_TIME = "startTime";
    }

    static enum Status {
        CONFIRMED("confirmed"), TENTATIVE("tentative"), CANCELLED("cancelled");
        private String value = null;

        private Status(String value) {
            this.value = value;
        }

        public static Status fromString(String value) {
            for (Status s: Status.values()) {
                if (s.value.equals(value)) {
                    return s;
                }
            }
            return null;
        }
    }

    static class BatchCallback extends JsonBatchCallback<Event> {
        List<Event> result = new ArrayList<Event>();

        @Override
        public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
            Log.d("BatchCallback", e.getMessage());
        }

        @Override
        public void onSuccess(Event event, HttpHeaders responseHeaders) throws IOException {
            result.add(event);
        }
    }


}
