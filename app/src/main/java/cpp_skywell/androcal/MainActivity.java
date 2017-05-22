package cpp_skywell.androcal;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.Date;
import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAOFactory;
import cpp_skywell.androcal.ContentProvider.SQLite.LocalCalendarProvider;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClickModify(View view) {
        EventsDAO ldao = EventsDAOFactory.create(this.getApplicationContext());

        // Add event
        Date now = new Date();
        EventsDO event = new EventsDO();
        event.setName("test batch sync 1");
        event.setStart(now);
        event.setEnd(new Date(now.getTime() + 3600*1000)); // 1 hour
        event.setRefId("");
        event.setSource(EventsDO.Source.NONE);
        event.setStatus(EventsDO.STATUS_NORMAL);
        event.setDirty(true);
        ldao.add(event);
//
//        now = new Date(now.getTime() + 3600 * 1000);
//        event = new EventsDO();
//        event.setName("test batch sync 2");
//        event.setStart(now);
//        event.setEnd(new Date(now.getTime() + 3600*1000)); // 1 hour
//        event.setRefId("");
//        event.setSource(EventsDO.Source.NONE);
//        event.setStatus(EventsDO.STATUS_NORMAL);
//        event.setDirty(true);
//        ldao.add(event);

        // Cancel event
//        ldao.cancel(3);

        // Update event
//        EventsDO event = ldao.get(3);
//        event.setEnd( new Date(event.getEnd().getTime() + 86400*1000) );
//        event.setDirty(true);
//        ldao.updateById(event);
    }

    public void onClickSync(View view) {
        Account account = createSyncAccount();
        if (account != null) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(account, LocalCalendarProvider.AUTHORITY, settingsBundle);
        }
    }

    public void onClickClearDB(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setMessage("Clear Database and SyncToken?");
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EventsDAO eventsDAO = EventsDAOFactory.create(getApplicationContext());
                GEventsDAO.getInstance().clearDataStore();
                eventsDAO.dropTable();
                eventsDAO.createTable();
                Log.d("Alert", "settings cleared");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onClickAddEvent(View view) {
        Intent intent = new Intent(this, AddCalendarEventActivity.class);
        startActivity(intent);
    }
}
