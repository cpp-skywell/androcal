package androcal.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import androcal.provider.EventsDO;
import androcal.provider.EventsDAO;
import cpp_skywell.androcal.R;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        long id = intent.getLongExtra(ListEventsActivity.EVENT_ID, 0);

//        EventsDAO dao = EventsDAOFactory.create(this);
        EventsDAO dao = new EventsDAO(this.getContentResolver());
        EventsDO event = dao.get(id);

        ((TextView) findViewById(R.id.textView)).setText(event.getName());
        ((TextView) findViewById(R.id.textView2)).setText("Start: " + event.getStart().toString());
        ((TextView) findViewById(R.id.textView3)).setText("End: " + event.getEnd().toString());

        // DEBUG custom fields
//        Map<String, String> customFields = dao.getCustomFields(id);
//        Iterator<Map.Entry<String, String>> itFields = customFields.entrySet().iterator();
//        while(itFields.hasNext()) {
//            Map.Entry<String, String> entry = itFields.next();
//            Log.d("EventActivity", entry.getKey() + ":" + entry.getValue());
//        }

    }

}
