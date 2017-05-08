package cpp_skywell.androcal;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;
import java.util.Date;

import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.DBOpenHelper;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.Service.GoogleSyncService;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR };

    private DBOpenHelper mDbHelper = null;
    GoogleAccountCredential mCredential;

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

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());   // Check login status

        this.chooseAccount();

    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName == null) {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            } else {
                this.setAccountName(accountName);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /**
     * Callback for startActivityForResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.setAccountName(accountName);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (requestCode == RESULT_OK) {
                    setupSyncService();
                }
                break;
        }
    }

    private void setAccountName(String accountName) {
        mCredential.setSelectedAccountName(accountName);
        GEventsDAO.getInstance().init(mCredential, getPreferences(Context.MODE_PRIVATE));
        Log.d("setAccount", accountName);

        // Check Google Auth
        new TaskCheckAuth().execute();
    }

    private void setupSyncService() {
        // Setup syncup service
        GoogleSyncService.register(this.getApplicationContext());
    }

    public void onClickModify(View view) {
        EventsDAO ldao = EventsDAO.getInstance();

        // Add event
//        Date now = new Date();
//        EventsDO event = new EventsDO();
//        event.setName("test batch sync 1");
//        event.setStart(now);
//        event.setEnd(new Date(now.getTime() + 3600*1000)); // 1 hour
//        event.setRefId("");
//        event.setSource(EventsDO.Source.NONE);
//        event.setStatus(EventsDO.STATUS_NORMAL);
//        event.setDirty(true);
//        ldao.add(event);
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
        EventsDO event = ldao.get(3);
        event.setEnd( new Date(event.getEnd().getTime() + 86400*1000) );
        event.setDirty(true);
        ldao.updateById(event);
    }

    public void onClickSync(View view) {
//        new TestCaller().execute();
    }

    public void onClickClearDB(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setMessage("Clear Database and SyncToken?");
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GEventsDAO.getInstance().clearDataStore();
                EventsDAO.getInstance().dropTable();
                EventsDAO.getInstance().createTable();
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

    private class TaskCheckAuth extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // Trigger Google authentication
            try {
                GEventsDAO.getInstance().checkAuth();
            } catch(UserRecoverableAuthIOException e) {
                startActivityForResult(
                        e.getIntent(),
                        REQUEST_AUTHORIZATION);
                return false;

            } catch (Exception e) {
                Log.d("TaskCheckAuth", "", e);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setupSyncService();
            }
        }
    }
}
