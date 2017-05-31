package cpp_skywell.androcal;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.ArrayList;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;

public class ModifyCalendarEventActivity extends AppCompatActivity {
    private Events events;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_calendar_event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new CalendarAsyncOps().execute();
//        ArrayList<String> eventNames = new ArrayList<>();
//        if(events != null && !events.isEmpty()){
//            List<Event> eventsList = events.getItems();
//            for (Event event : eventsList){
//                eventNames.add(event.getSummary());
//            }
//            ArrayAdapter eventAdapter = new ArrayAdapter(this, R.layout.activity_modify_calendar_event, eventNames);
//            ListView eventListView = (ListView) findViewById(R.id.event_list);
//            eventListView.setAdapter(eventAdapter);
//        }
    }

    private class CalendarAsyncOps extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            events = GEventsDAO.getInstance().getEventsList();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            ArrayList<String> eventNames = new ArrayList<>();
            if (events != null && !events.isEmpty()) {
                List<Event> eventsList = events.getItems();
                for (Event event : eventsList) {
                    eventNames.add(event.getSummary());
                }
                ArrayAdapter eventAdapter = new ArrayAdapter(getApplicationContext(), R.layout.activity_listview, eventNames);
                ListView eventListView = (ListView) findViewById(R.id.event_list);
                eventListView.setAdapter(eventAdapter);
            }
        }

    }
}
