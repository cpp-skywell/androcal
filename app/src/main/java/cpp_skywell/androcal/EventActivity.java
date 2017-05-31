package cpp_skywell.androcal;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAOFactory;
import cpp_skywell.androcal.ContentProvider.SQLite.LocalCalendarProvider;

import static cpp_skywell.androcal.ListViewLoader.PROJECTION;
import static cpp_skywell.androcal.ListViewLoader.SELECTION;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        long id = intent.getLongExtra(ListViewLoader.EVENT_ID, 0);

        EventsDAO dao = EventsDAOFactory.create(this);
        EventsDO event = dao.get(id);

        ((TextView) findViewById(R.id.textView)).setText(event.getName());
        ((TextView) findViewById(R.id.textView2)).setText("Start: " + event.getStart().toString());
        ((TextView) findViewById(R.id.textView3)).setText("End: " + event.getEnd().toString());

    }

}
