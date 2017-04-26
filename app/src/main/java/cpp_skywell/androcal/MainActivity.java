package cpp_skywell.androcal;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
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
import java.util.Iterator;
import java.util.List;

import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;
import cpp_skywell.androcal.ContentProvider.SQLite.DBOpenHelper;
import cpp_skywell.androcal.ContentProvider.SQLite.EventsDAO;
import cpp_skywell.androcal.ContentProvider.EventsDO;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

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
                break;
        }
    }

    private void setAccountName(String accountName) {
        mCredential.setSelectedAccountName(accountName);
        GEventsDAO.getInstance().init(mCredential);
        Log.d("setAccount", accountName);
    }

    public void testClick(View view) {
        testGoogle();
    }

    private void testGoogle() {
        new TestCaller().execute();
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
            event.setRefId(String.valueOf(i));
            event.setSource(EventsDO.Source.GOOGLE);
            event.setStatus(EventsDO.STATUS_NORMAL);

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

    private class TestCaller extends AsyncTask<Void, Void, Void> {

        public TestCaller() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            GEventsDAO dao = GEventsDAO.getInstance();

            try {
                dao.checkAuth();
            } catch(UserRecoverableAuthIOException e) {
                startActivityForResult(
                        e.getIntent(),
                        REQUEST_AUTHORIZATION);

            } catch (Exception e) {
                Log.d("TestCaller", "", e);
            }

            try {
                // Test getByDateRange
                Date now = new Date();
                List<EventsDO> eventList = dao.getByDateRange(
                        new Date(now.getTime() - 10 * 86400 * 1000),
                        new Date(now.getTime() + 10 * 86400 * 1000)
                );
                Iterator<EventsDO> it = eventList.iterator();
                while(it.hasNext()) {
                    EventsDO event = it.next();
                    Log.d("TestCaller", event.toString());
                }

            } catch (Exception e) {
                Log.d("TestCaller", "", e);
            }
            return null;
        }

    }
}
