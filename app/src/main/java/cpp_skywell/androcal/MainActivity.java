package cpp_skywell.androcal;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

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
            showMessage(account, view);
        }
    }

    private void showMessage(Account account, View view){
        String msg = "";
        if(account != null) {
            msg = "You have successfully synced your calendar!";
        }
        else{
            msg = "Something went wrong, please try again.";
        }
        Snackbar messageSnackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        messageSnackbar.show();
    }

    public void onClickClearDB(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View clearView = view;
        builder.setTitle("Alert")
                .setMessage("Clear Database and SyncToken?");
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EventsDAO eventsDAO = EventsDAOFactory.create(getApplicationContext());
                GEventsDAO.getInstance().clearDataStore();
                eventsDAO.dropTable();
                eventsDAO.createTable();
                Snackbar messageSnackbar = Snackbar.make(clearView, "Database successfully cleared!", Snackbar.LENGTH_SHORT);
                messageSnackbar.show();
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
        Intent intent = new Intent(this, ListEventsActivity.class);
        startActivity(intent);
    }
}
