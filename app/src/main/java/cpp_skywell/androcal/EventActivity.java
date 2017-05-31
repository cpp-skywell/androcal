package cpp_skywell.androcal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAOFactory;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        long id = intent.getLongExtra(ListEventsActivity.EVENT_ID, 0);

        EventsDAO dao = EventsDAOFactory.create(this);
        EventsDO event = dao.get(id);

        ((TextView) findViewById(R.id.textView)).setText(event.getName());
        ((TextView) findViewById(R.id.textView2)).setText("Start: " + event.getStart().toString());
        ((TextView) findViewById(R.id.textView3)).setText("End: " + event.getEnd().toString());

    }

}
