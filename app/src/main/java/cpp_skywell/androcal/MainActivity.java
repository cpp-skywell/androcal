package cpp_skywell.androcal;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAOFactory;
import cpp_skywell.androcal.ContentProvider.SQLite.LocalCalendarProvider;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClickModify(View view) {
        Intent intent = new Intent(this, ModifyCalendarEventActivity.class);
        startActivity(intent);
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

    public void onClickListEvents(View view) {
        Intent intent = new Intent(this, ListViewLoader.class);
        startActivity(intent);
    }
}
