package androcal.example;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

import androcal.sync.Authenticator;
import androcal.sync.GEventsDAO;
import androcal.provider.LocalCalendarProvider;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by zhangliang on 5/22/17.
 */

public class BaseActivity extends AppCompatActivity {
    protected static final int REQUEST_ACCOUNT_PICKER = 1000;
    protected static final int REQUEST_AUTHORIZATION = 1001;
    protected static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    protected static final String PREF_ACCOUNT_NAME = "accountName";
    protected static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR};

    GoogleAccountCredential mCredential;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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

    protected Account createSyncAccount() {
        Account account = null;
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);

        Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            account = new Account(Authenticator.ACCOUNT_NAME, Authenticator.ACCOUNT_TYPE);
            if (accountManager.addAccountExplicitly(account, null, null)) {
                Log.d("createSyncAccount", "New account");
                return account;
            } else {
                Log.d("createSyncAccount", "Add account error");
                return null;
            }
        } else {
            Log.d("createSyncAccount", "Existing account");
            return accounts[0];
        }
    }

    private void setupSyncService() {
        // Setup syncup service
        Account account = createSyncAccount();
        if (account != null) {
            ContentResolver.setSyncAutomatically(account, LocalCalendarProvider.AUTHORITY, true);
            ContentResolver.addPeriodicSync(
                    account,
                    LocalCalendarProvider.AUTHORITY,
                    Bundle.EMPTY,
                    60 * 15 // at least 15 mins is enforced by framework
            );
        }

    }

    private class TaskCheckAuth extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // Trigger Google authentication
            try {
                GEventsDAO.getInstance().checkAuth();
            } catch (UserRecoverableAuthIOException e) {
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
