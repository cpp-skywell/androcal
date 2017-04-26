package cpp_skywell.androcal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.SQLite.DBOpenHelper;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDO;

public class MainActivity extends AppCompatActivity {
    private DBOpenHelper mDbHelper = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close database
        this.mDbHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init database and DAO
        this.mDbHelper = new DBOpenHelper(this.getApplicationContext());
        EventsDAO.getInstance().init(this.mDbHelper);

        // Check login status
        if (!this.isLoggedIn()) {
            // Go to Login activity
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        }
    }

    private boolean isLoggedIn() {
        // TODO: check login status
        return false;
    }

    public void testClick(View view) {
        testDatabase();
    }

    private void testDatabase() {
        EventsDAO dao = EventsDAO.getInstance();
        Date now = new Date();

        // Init database
        dao.dropTable();
        dao.createTable();

        // Test INSERT, SELECT by _ID
        for (int i = 0; i < 10; i ++) {
            EventsDO event = new EventsDO();
            event.setName("test event#" + i);
            event.setStart(new Date(now.getTime() + i*86400*1000));
            event.setEnd(new Date(now.getTime() + (i + 1)*86400*1000)); // 1 days later
            event.setStatue(EventsDO.STATUS_NORMAL);

            // INSERT
            long rowId = dao.add(event);
            Log.d("db_test", "rowId = " + rowId);

            // SELECT by _ID
            event = dao.get(rowId);
            Log.d("db_test", event.toString());
        }

        // Test SELECT by time range
        Log.d("db_test", now.toString());
        List<EventsDO> eventList = dao.getByDateRange(
                now,
                new Date(now.getTime() + 2*86400*1000)
        );
        Iterator<EventsDO> it = eventList.iterator();
        while(it.hasNext()) {
            Log.d("db_test", it.next().toString());
        }

        // Test DELETE
        dao.delete(dao.get(1).getId());
        if (dao.get(1) == null) {
            Log.d("db_test", "DELETE successfully");
        }

        // TODO: Test UPDATE
    }
}
