package io.github.tyeolrik.hangmanqrcode;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.tumblr.backboard.performer.MapPerformer;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.FLOATING_ACTION_BUTTON_MAIN_COORDINATE;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_PRESSURETEST;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_RECHARGEHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_SANITATIONHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_EDIT_PRESSURETEST;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_EDIT_RECHARGEHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_EDIT_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_EDIT_SANITATIONHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_PRESSURETEST;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_RECHARGEHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_SANITATIONHISTORY;


/***********************************************************************
 * Copyright Because of Apache license terms.
 * I am not sure whether this is right way but, I try to do my duty for coder who made Backboard_Tumblr
 * URL : https://github.com/tumblr/Backboard#license
 *
 * Copyright 2015-2016 Tumblr, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************/           //      Copyright 2015-2016 Tumblr, Inc.         Apache License, Version 2.0
public class OxygenBottleActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    @BindView(R.id.tabLayout)
    TabLayout tablayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.floatingActionButtonInOxygenBottleActivity)
    FloatingActionButton floatingActionButtonInOxygenBottleActivity;

    @BindViews({R.id.floatingActionButtonEditPressure, R.id.floatingActionButtonEditSanitation, R.id.floatingActionButtonEditRepairHistory, R.id.floatingActionButtonEditRechargeHistory})
    List<FloatingActionButton> miniButtons;

    Realm realm;

    boolean fab_isOpen;

    private int whatToAdd;
    private int numberPickerFirstSettingValue;

    LocalDate today;
    LocalDate select;     // Initialize

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final int REQUEST_DIALOG_ADDING_PRESSURE = 2001;
    private static final int REQUEST_DIALOG_ADDING_SANITATION = 2002;
    private static final int REQUEST_DIALOG_ADDING_REPAIR = 2003;
    private static final int REQUEST_DIALOG_ADDING_RECHARGE = 2004;

    private static String SHEETS_ID;
    private static String BOTTLE_CODE;
    private static Bundle bundleToFragment;

    // Spring Animation
    private static final int DIAMETER = 50;
    private static final int RING_DIAMETER = 7 * DIAMETER;
    private static final int CLOSE = 0;
    private static final int OPEN = 1;

    SpringSystem FAB_SpringSystem;
    Spring FAB_Spring;

    int px_miniButton;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fab_isOpen = false;

        Realm.init(this);
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);
        SHEETS_ID = getIntent().getStringExtra("SpreadSheetID");
        Log.d("SHEETS_ID", SHEETS_ID);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("공기호흡기 옮기는 중...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        setupAPI();

        setContentView(R.layout.activity_oxygen_bottle);
        ButterKnife.bind(this);

        tablayout.setTabTextColors(Color.argb(128, 242, 229, 41), Color.WHITE);

        // Spring(Rebound) For Floating Action Button
        FAB_SpringSystem = SpringSystem.create();
        FAB_Spring = FAB_SpringSystem.createSpring();

        px_miniButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getApplicationContext().getResources().getDisplayMetrics());
        int firstMiniButton = px_miniButton/56*64;

        FAB_Spring.addListener(new MapPerformer(miniButtons.get(0), View.TRANSLATION_Y, 0, 1, 0, (float) firstMiniButton * -1));
        FAB_Spring.addListener(new MapPerformer(miniButtons.get(1), View.TRANSLATION_Y, 0, 1, 0, (float) (firstMiniButton + px_miniButton) * -1));
        FAB_Spring.addListener(new MapPerformer(miniButtons.get(2), View.TRANSLATION_Y, 0, 1, 0, (float) (firstMiniButton + px_miniButton * 2) * -1));
        FAB_Spring.addListener(new MapPerformer(miniButtons.get(3), View.TRANSLATION_Y, 0, 1, 0, (float) (firstMiniButton + px_miniButton * 3) * -1));

        FAB_Spring.setEndValue(CLOSE);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
        } else {
            // new MakeReadTask(mCredential).execute();
        }
    }

    private void testWriteInSheet() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
        } else {
            // new MakeWriteTask(mCredential, "TEST").execute();
        }
    }

    private void setupAPI() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
            Toast.makeText(this, "인터넷 연결을 확인해주세요", Toast.LENGTH_LONG).show();
        } else {
            Log.d("Setup API", "COMPLETE");
            new MakeReadTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                setupAPI();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
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

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.e("ERROR",
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    setupAPI();
                }
                break;
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
                        mCredential.setSelectedAccountName(accountName);
                        setupAPI();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    setupAPI();
                }
                break;

            case REQUEST_DIALOG_ADDING_PRESSURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            writeOnGoogleSheets(whatToAdd, String.valueOf(count) + "차", String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2]));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_ADDING_PRESSURE - RESULT_OK - if()");
                        }
                        Log.d("onActivityResult", "getIntent Result Data :: expect => YYYY년 MM월 DD일\t\t" + dateIntArrayToString(date));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_EDITING_PRESSURETEST:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("RESULT", "REQUEST_DIALOG_EDITING_PRESSURETEST OK in OxygenBottleActivity");
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            modifyOnGoogleSheets(WHATTOADD_EDIT_PRESSURETEST
                                    , data.getStringExtra("contentsBefore")
                                    , data.getStringExtra("editDateBefore")
                                    , String.valueOf(count)
                                    , String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2])
                                    , data.getIntExtra("position", -1));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_EDITING_PRESSURETEST - RESULT_OK - if()");
                        }
                        Log.d("onActivityResult", "getIntent Result Data :: expect => YYYY년 MM월 DD일\t\t" + dateIntArrayToString(date));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_ADDING_SANITATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            writeOnGoogleSheets(whatToAdd, String.valueOf(count) + "차", String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2]));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_ADDING_PRESSURE - RESULT_OK - if()");
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_EDITING_SANITATIONHISTORY:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("RESULT", "REQUEST_DIALOG_EDITING_SANITATIONHISTORY OK in OxygenBottleActivity");
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            modifyOnGoogleSheets(WHATTOADD_EDIT_SANITATIONHISTORY
                                    , data.getStringExtra("contentsBefore")
                                    , data.getStringExtra("editDateBefore")
                                    , String.valueOf(count)
                                    , String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2])
                                    , data.getIntExtra("position", -1));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_EDITING_SANITATIONHISTORY - RESULT_OK - if()");
                        }
                        Log.d("onActivityResult", "getIntent Result Data :: expect => YYYY년 MM월 DD일\t\t" + dateIntArrayToString(date));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_ADDING_REPAIR:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        int[] date = data.getIntArrayExtra("ResultDate");
                        String repairContents = data.getStringExtra("ResultRepairContents");
                        writeOnGoogleSheets(whatToAdd, String.valueOf(repairContents), String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2]));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_EDITING_REPAIRHISTORY:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("RESULT", "REQUEST_DIALOG_EDITING_REPAIRHISTORY OK in OxygenBottleActivity");
                        int[] date = data.getIntArrayExtra("ResultDate");
                        if(date.length > 0) {
                            modifyOnGoogleSheets(WHATTOADD_EDIT_REPAIRHISTORY
                                    , data.getStringExtra("contentsBefore")
                                    , data.getStringExtra("editDateBefore")
                                    , data.getStringExtra("ResultRepairContents")
                                    , String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2])
                                    , data.getIntExtra("position", -1));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_EDITING_REPAIRHISTORY - RESULT_OK - if()");
                        }
                        Log.d("onActivityResult", "getIntent Result Data :: expect => YYYY년 MM월 DD일\t\t" + dateIntArrayToString(date));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_ADDING_RECHARGE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            writeOnGoogleSheets(whatToAdd, String.valueOf(count) + "회", String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2]));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_ADDING_RECHARGE - RESULT_OK - if()");
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case REQUEST_DIALOG_EDITING_RECHARGEHISTORY:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("RESULT", "REQUEST_DIALOG_EDITING_RECHARGEHISTORY OK in OxygenBottleActivity");
                        int[] date = data.getIntArrayExtra("ResultDate");
                        int count = data.getIntExtra("ResultCount", -1);
                        if(count != -1) {           // Check Validation
                            modifyOnGoogleSheets(WHATTOADD_EDIT_RECHARGEHISTORY
                                    , data.getStringExtra("contentsBefore")
                                    , data.getStringExtra("editDateBefore")
                                    , String.valueOf(count)
                                    , String.valueOf(date[0]) + String.format(Locale.KOREA, "%02d", date[1]) + String.format(Locale.KOREA, "%02d", date[2])
                                    , data.getIntExtra("position", -1));
                        } else {
                            Toast.makeText(this, "에러발생.", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "REQUEST_DIALOG_EDITING_RECHARGEHISTORY - RESULT_OK - if()");
                        }
                        Log.d("onActivityResult", "getIntent Result Data :: expect => YYYY년 MM월 DD일\t\t" + dateIntArrayToString(date));
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                OxygenBottleActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeReadTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        Realm threadRealm;

        MakeReadTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("HangmanQRCode")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = SHEETS_ID;
            String range = "A2:Z";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (checkSheetValidation(values) && (values != null)) {
                writeOnRealm(values);
                BOTTLE_CODE = values.get(0).get(1).toString();
                bundleToFragment = new Bundle();
                bundleToFragment.putString("BOTTLE_CODE", values.get(0).get(1).toString());
                results.add("용기번호: "            + values.get(0).get(1));
                results.add("제조일: "              + values.get(1).get(1));
                results.add("압력검사예정일: "      + values.get(2).get(1));
                results.add("부서명: "              + values.get(0).get(3));
                results.add("사용기한: "            + values.get(1).get(3));
                results.add("최종압력검사일: "      + values.get(2).get(3));
                if(values.get(5).size() > 0) {
                    for(int idx = 0; idx < values.get(5).size(); idx++) {
                        results.add("내압검사 이력 || "     + values.get(4).get(idx) + " : " + values.get(5).get(idx));
                    }
                } else {
                    Log.d("PressureTest", "No data retrieved");
                }
            }
            return results;
        }

        protected boolean checkSheetValidation(final List<List<Object>> values) {

            boolean result = false;
            boolean checkItem = false;
            boolean checkPressureTest = false;
            boolean checkSanitationHistory = false;
            boolean checkRepairHistory = false;
            boolean checkRechargeHistory = false;

            if(        String.valueOf("용기번호")              .equals(String.valueOf(values.get(0).get(0)))
                    && String.valueOf("제조일")                .equals(String.valueOf(values.get(1).get(0)))
                    && String.valueOf("압력검사예정일")        .equals(String.valueOf(values.get(2).get(0)))
                    && String.valueOf("부서명")                .equals(String.valueOf(values.get(0).get(2)))
                    && String.valueOf("사용기한")              .equals(String.valueOf(values.get(1).get(2)))
                    && String.valueOf("최종압력검사일")        .equals(String.valueOf(values.get(2).get(2)))) {
                checkItem = true;
            }

            if(String.valueOf("내압검사 이력").equals(String.valueOf(values.get(3).get(0)))) {
                Log.v("CheckSheet", String.valueOf(values.get(3).get(0)));
                checkPressureTest = true;
            }

            if(String.valueOf("위생검사 내역").equals(String.valueOf(values.get(6).get(0)))) {
                Log.v("CheckSheet", String.valueOf(values.get(6).get(0)));
                checkSanitationHistory = true;
            }

            if(String.valueOf("수리내역").equals(String.valueOf(values.get(9).get(0)))) {
                Log.v("CheckSheet", String.valueOf(values.get(9).get(0)));
                checkRepairHistory = true;
            }

            if(String.valueOf("충전내역").equals(String.valueOf(values.get(16).get(0)))) {
                Log.v("CheckSheet", String.valueOf(values.get(16).get(0)));
                checkRechargeHistory = true;
            }

            if(checkItem && checkPressureTest && checkSanitationHistory && checkRepairHistory && checkRechargeHistory) {
                result = true;
            }

            if(result) {
                Log.v("checkSheetValidation", "checkItem: \t\t"              + String.valueOf(checkItem));
                Log.v("checkSheetValidation", "checkPressureTest: \t\t"      + String.valueOf(checkPressureTest));
                Log.v("checkSheetValidation", "checkSanitationHistory: \t\t" + String.valueOf(checkSanitationHistory));
                Log.v("checkSheetValidation", "checkRepairHistory: \t\t"     + String.valueOf(checkRepairHistory));
                Log.v("checkSheetValidation", "checkRechargeHistory: \t\t"   + String.valueOf(checkRechargeHistory));
            } else {
                Log.e("checkSheetValidation", "checkItem: \t\t"              + String.valueOf(checkItem));
                Log.e("checkSheetValidation", "checkPressureTest: \t\t"      + String.valueOf(checkPressureTest));
                Log.e("checkSheetValidation", "checkSanitationHistory: \t\t" + String.valueOf(checkSanitationHistory));
                Log.e("checkSheetValidation", "checkRepairHistory: \t\t"     + String.valueOf(checkRepairHistory));
                Log.e("checkSheetValidation", "checkRechargeHistory: \t\t"   + String.valueOf(checkRechargeHistory));
            }
            return result;
        }

        protected void writeOnRealm(final List<List<Object>> values) {

            Log.d("RealmWrite", "Start Writing Bottle");
            Realm.init(getApplication().getApplicationContext());
            threadRealm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);
            threadRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Log.d("RealmWrite", "Basic Information Start to Write");
                    OxygenBottle bottle = new OxygenBottle(values.get(0).get(1).toString()      // 용기번호
                                                         , values.get(0).get(3).toString()      // 부서명
                                                         , values.get(1).get(1).toString()      // 제조일
                                                         , values.get(1).get(3).toString()      // 사용기한
                                                         , values.get(2).get(1).toString()      // 압력검사예정일
                                                         , values.get(2).get(3).toString());    // 최종압력검사일

                    Log.d("RealmWrite", "Basic Information End to Write with Constructor");

                    // 내압검사 이력 작성
                    Log.d("RealmWrite", "Pressure Test History Start to Write with getPressureTest().add()");
                    if(values.get(5).size() > 0) {
                        for(int idx = 0; idx < values.get(5).size(); idx++) {
                            bottle.getPressureTest().add(new PressureTest(values.get(0).get(1).toString(), getOnlyIntegerCount(values.get(4).get(idx).toString()), getFormattedDate(values.get(5).get(idx).toString())));
                        }
                    }
                    Log.d("RealmWrite", "Pressure Test History End to Write with getPressureTest().add()");

                    // 위생검사 이력 작성
                    Log.d("RealmWrite", "Sanitation History Start to Write with getSanitationHistory().add()");
                    if(values.get(8).size() > 0) {
                        for(int idx = 0; idx < values.get(8).size(); idx++) {
                            bottle.getSanitationHistory().add(new SanitationHistory(values.get(0).get(1).toString(), getOnlyIntegerCount(values.get(7).get(idx).toString()), getFormattedDate(values.get(8).get(idx).toString())));
                        }
                    }
                    Log.d("RealmWrite", "Sanitation History End to Write with getSanitationHistory().add()");

                    // 수리내역 작성
                    Log.d("RealmWrite", "Repair History Start to Write with getRepairHistorys().add(new RepairHistory())");
                    if(values.get(11).size() > 0) {
                        int columnCount = values.get(11).size() / 2;
                        int allCount = 0;
                        int nowCount = 0;
                        for(int row = 11; row < 16; row++) {
                            allCount = allCount + values.get(row).size() / 2;
                        }
                        for(int column = 0; column < columnCount; column++) {
                            for(int index = 11; index < 16; index++) {
                                // 여기서 에러
                                if(!TextUtils.isEmpty(String.valueOf(values.get(index).get(column*2))) && !TextUtils.isEmpty(String.valueOf(values.get(index).get(column*2 + 1)))) {
                                    bottle.getRepairHistorys()
                                            .add(new RepairHistory(
                                                      values.get(0).get(1).toString()                               // 용기번호
                                                    , values.get(index).get(column*2)       .toString()             // 일자
                                                    , values.get(index).get(column*2 + 1)   .toString()));          // 수리내용

                                    /* Getting Log whether This is right Index of List.
                                    Log.d("RealmWrite"
                                            , "This is Complete :: "
                                                    + values.get(0).get(1).toString() + "\t\t"
                                                    + values.get(index).get(column*2).toString()  + "\t\t"
                                                    + values.get(index).get(column*2 + 1)   .toString());
                                    */
                                }
                                nowCount++;
                                if(nowCount == allCount) {
                                    break;
                                }
                            }
                        }
                    }
                    Log.d("RealmWrite", "Repair History End to Write with getRepairHistorys().add(new RepairHistory())");

                    // 충전내역 작성
                    Log.d("RealmWrite", "Recharge History Start to Write with getRechargeHistory().add()");
                    // 횟수 미리 적힌 것 체크
                    if(values.get(18).size() > 0) {     // 유효한 문서인지에 대한 서식 확인

                        int totalNumber = 0;
                        // 충전 전체 횟수 구하기
                        for (int row = 18; row < values.size(); row++) {
                            for(Object each : values.get(row)) {
                                Log.d("181105", "row Number :: " + String.valueOf(row));
                                if(!each.toString().equals("")) {   // 첫줄 빈칸 세기
                                    totalNumber++;
                                }
                                Log.d("Check", "" + each.toString() + "\t\tTotalNumber :: " + totalNumber);
                                if(each.toString().contains("위생")) {        // 위생검사는 제외
                                    Log.d("Check", "위생검사 체크 - 제거완료");
                                    totalNumber = totalNumber - 2;
                                }
                            }
                            /*
                            for(Object each : values.get(row)) {
                                if(each.toString().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                                    // 각 칸에 한글이 포함되어 있다 -> N회, N차, 위생검사 등
                                } else {
                                    // 각 칸에 한글이 없다 -> 순수 숫자 == 날짜
                                    if(values.get(row).get(values.get(row).indexOf(each) - 1).toString().contains("회")) {
                                        bottle.getRechargeHistory().add(
                                                new RechargeHistory(
                                                        values.get(0).get(1).toString()
                                                        , Integer.valueOf(values.get(row).get(
                                                                values.get(row).indexOf(each) - 1).toString().substring(
                                                                        0, values.get(row).get(values.get(row).indexOf(each) - 1).toString().length() - 1))
                                                        , each.toString()));
                                    }
                                }
                            }
                            */
                        }
                        if(totalNumber%2 != 0) {
                            Log.e("RealmWrite", "충전내역 갯수 오류 :: totalNumber == " + String.valueOf(totalNumber));
                        } else {
                            Log.d("RealmWrite", "TotalNumber == " + String.valueOf(totalNumber));
                        }
                        totalNumber = totalNumber / 2;      // 충전내역 전체 갯수 파악완료


                        int tempCount = 0;
                        FOR_COLUMN : for(int column = 0; column < (int)(totalNumber / 5) * 2 + 1; column++) {
                            FOR_ROW : for(int row = 18; row < 23; row++) {
                                Log.d("RealmWrite", "tempCount :: " + tempCount);
                                bottle.getRechargeHistory().add(
                                        new RechargeHistory(
                                                  values.get(0).get(1).toString()               // BottleCode
                                                , Integer.valueOf(values.get(row).get(column*2).toString().replaceAll("[^0-9]", ""))
                                                , values.get(row).get(column*2 + 1).toString()));
                                Log.d("RealmWrite", "Bottle RechargeHistory Added :: " + values.get(row).get(column*2).toString().replaceAll("[^0-9]", "") + "\t\t" + values.get(row).get(column*2 + 1).toString());

                                tempCount++;
                                if(tempCount == totalNumber) {
                                    break FOR_COLUMN;
                                }
                            }
                        }
                        Log.d("RealmWrite", "RechargeHistory size :: " + bottle.getRechargeHistory().size());
                    }
                    Log.d("RealmWrite", "Recharge History End to Write with getRechargeHistory().add()");


                    realm.copyToRealmOrUpdate(bottle);
                    Log.d("RealmWrite", "COMPLETE Writing Bottle");
                }
            });

            if(!threadRealm.isClosed()) {
                threadRealm.close();
            }
            Log.d("RealmWrite", "End Writing Bottle");
        }

        @Override
        protected void onPreExecute() {
            Log.v("Status","Waiting");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {

            // QRCodeScan Activity - BarcodeScan Flag modify.
            QRCodeScan.isBarcodeScanned = false;

            mProgress.hide();
            if (output == null || output.size() == 0) {
                Log.d("Data","No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                Log.d("Data", TextUtils.join("\n", output));

                OxygenBottlePagerAdapter adapter = new OxygenBottlePagerAdapter(getSupportFragmentManager(), bundleToFragment);
                setTitle("용기번호 :: " + bundleToFragment.getString("BOTTLE_CODE"));
                viewPager.setAdapter(adapter);

                tablayout.setupWithViewPager(viewPager, true);

                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tablayout));
                tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            OxygenBottleActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("ERROR","The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.e("ERROR","Request cancelled.");
            }
        }

        // YYYYMMDD 형식으로 return
        public String getFormattedDate(String input) {
            if(input.contains("/")) { // MM/DD/YYYY
                String[] parse = input.split("/");
                String result = String.format(Locale.KOREA, "%04d%02d%02d", Integer.valueOf(parse[2]), Integer.valueOf(parse[0]), Integer.valueOf(parse[1]));
                Log.d("DateFormatter", result);
                return result;
            } else if(input.contains(".")) { // YYYY. MM. DD.
                String[] parse = input.split(".");
                String result = String.format(Locale.KOREA, "%04d%02d%02d", Integer.valueOf(parse[0]), Integer.valueOf(parse[1]), Integer.valueOf(parse[2]));
                Log.d("DateFormatter", result);
                return result;
            } else {    // YYYYMMDD
                return input;
            }
        }

        public int getOnlyIntegerCount(String input) {
            return Integer.valueOf(input.replaceAll("[^0-9]", ""));
        }

    }

    private class MakeWriteTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        private String contents;
        private String editDate;
        private String range;

        /**
         *
         * @param credential
         * @param whatToAdd Select one. {WRITE_RANGE_PRESSURE, WRITE_RANGE_SANITATION, WRITE_RANGE_REPAIR, WRITE_RANGE_RECHARGE}
         * @param editDate
         */
        MakeWriteTask(GoogleAccountCredential credential, int whatToAdd, String contents, String editDate) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("HangmanQRCode")
                    .build();

            this.contents = contents;
            this.editDate = editDate;

            switch (whatToAdd) {
                case WHATTOADD_PRESSURETEST:
                    int pressureTestLength = realm.where(OxygenBottle.class).equalTo("bottleCode", BOTTLE_CODE).findFirst().getPressureTest().size();
                    range = ColumnNumberToLetter(pressureTestLength + 1) + "6:" + ColumnNumberToLetter(pressureTestLength + 1) + "7";   // 뒤에 무조건 추가
                    break;
                case WHATTOADD_SANITATIONHISTORY:
                    int sanitationHistorySize = realm.where(OxygenBottle.class).equalTo("bottleCode", BOTTLE_CODE).findFirst().getSanitationHistory().size();
                    range = ColumnNumberToLetter(sanitationHistorySize + 1) + "9:" + ColumnNumberToLetter(sanitationHistorySize + 1) + "10";   // 뒤에 무조건 추가
                    break;
                case WHATTOADD_REPAIRHISTORY:
                    int repairHistorySize = realm.where(OxygenBottle.class).equalTo("bottleCode", BOTTLE_CODE).findFirst().getRepairHistorys().size();
                    if(repairHistorySize == 0) {
                        range = "A13:B13";
                    } else {
                        range = ColumnNumberToLetter(((int)(repairHistorySize)/5 + 1) * 2 - 1) + ((int)(repairHistorySize)%5 + 13)
                                + ":" + ColumnNumberToLetter(((int)(repairHistorySize)/5 + 1) * 2) + ((int)(repairHistorySize)%5 + 13);
                        Log.d("SheetRange", "Write :: \t\t" + range);
                    }
                    break;
                case WHATTOADD_RECHARGEHISTORY:
                    int rechargeHistorySize = realm.where(OxygenBottle.class).equalTo("bottleCode", BOTTLE_CODE).findFirst().getRechargeHistory().size();
                    if(rechargeHistorySize == 0) {
                        range = "A20:B20";
                    } else {
                        range = ColumnNumberToLetter(((int)(rechargeHistorySize)/5 + 1) * 2 - 1) + ((int)(rechargeHistorySize)%5 + 20)
                                + ":" + ColumnNumberToLetter(((int)(rechargeHistorySize)/5 + 1) * 2) + ((int)(rechargeHistorySize)%5 + 20);
                        Log.d("SheetRange", "Write :: \t\t" + range);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                writeDataFromApi();
                return null;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private void writeDataFromApi() throws IOException {
            String spreadsheetId = SHEETS_ID;

            List<List<Object>> values;
            if(whatToAdd == WHATTOADD_REPAIRHISTORY) {
                values = Arrays.asList(
                        Arrays.asList(
                                // Cell values ...
                                (Object) editDate, contents
                        )
                        // Additional rows ...
                );
            } else if(whatToAdd == WHATTOADD_RECHARGEHISTORY) {
                values = Arrays.asList(
                        Arrays.asList(
                                // Cell values ...
                                (Object) contents, editDate
                        )
                        // Additional rows ...
                );
            } else {
                values = Arrays.asList(
                        Arrays.asList(
                                // Cell values ...
                                (Object) contents
                        ),
                        // Additional rows ...
                        Arrays.asList(
                                (Object) editDate
                        )
                );
            }


            ValueRange body = new ValueRange()
                    .setValues(values);

            UpdateValuesResponse results = this.mService.spreadsheets().values().update(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            Log.d("WriteSheets", String.valueOf(results.getUpdatedCells()) + " cells updated.");

            return;
        }

        protected boolean checkSheetValidation(final List<List<Object>> values) {

            boolean result = false;
            boolean checkItem = false;
            boolean checkPressureTest = false;
            boolean checkSanitationHistory = false;
            boolean checkRepairHistory = false;
            boolean checkRechargeHistory = false;

            if(        String.valueOf("용기번호")              .equals(String.valueOf(values.get(0).get(0)))
                    && String.valueOf("제조일")                .equals(String.valueOf(values.get(1).get(0)))
                    && String.valueOf("압력검사예정일")        .equals(String.valueOf(values.get(2).get(0)))
                    && String.valueOf("부서명")                .equals(String.valueOf(values.get(0).get(2)))
                    && String.valueOf("사용기한")              .equals(String.valueOf(values.get(1).get(2)))
                    && String.valueOf("최종압력검사일")        .equals(String.valueOf(values.get(2).get(2)))) {
                checkItem = true;
            }

            if(String.valueOf("내압검사 이력").equals(String.valueOf(values.get(3).get(0)))) {
                checkPressureTest = true;
            }

            if(String.valueOf("위생검사 내역").equals(String.valueOf(values.get(6).get(0)))) {
                checkSanitationHistory = true;
            }

            if(String.valueOf("수리내역").equals(String.valueOf(values.get(9).get(0)))) {
                checkRepairHistory = true;
            }

            if(String.valueOf("충전내역").equals(String.valueOf(values.get(16).get(0)))) {
                checkRechargeHistory = true;
            }

            if(checkItem && checkPressureTest && checkSanitationHistory && checkRepairHistory && checkRechargeHistory) {
                result = true;
            }

            if(result) {
                Log.v("checkSheetValidation", "checkItem: \t\t"              + String.valueOf(checkItem));
                Log.v("checkSheetValidation", "checkPressureTest: \t\t"      + String.valueOf(checkPressureTest));
                Log.v("checkSheetValidation", "checkSanitationHistory: \t\t" + String.valueOf(checkSanitationHistory));
                Log.v("checkSheetValidation", "checkRepairHistory: \t\t"     + String.valueOf(checkRepairHistory));
                Log.v("checkSheetValidation", "checkRechargeHistory: \t\t"   + String.valueOf(checkRechargeHistory));
                return result;
            } else {
                Log.e("checkSheetValidation", "checkItem: \t\t"              + String.valueOf(checkItem));
                Log.e("checkSheetValidation", "checkPressureTest: \t\t"      + String.valueOf(checkPressureTest));
                Log.e("checkSheetValidation", "checkSanitationHistory: \t\t" + String.valueOf(checkSanitationHistory));
                Log.e("checkSheetValidation", "checkRepairHistory: \t\t"     + String.valueOf(checkRepairHistory));
                Log.e("checkSheetValidation", "checkRechargeHistory: \t\t"   + String.valueOf(checkRechargeHistory));
                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.v("Status","Waiting");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid){
            new MakeReadTask(mCredential).execute();
            mProgress.hide();
        }

        /* Original Code
        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Log.d("Data","No results returned.");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                Log.d("Data", TextUtils.join("\n", output));
            }
        }
        */

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            OxygenBottleActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("ERROR","The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.e("ERROR","Request cancelled.");
            }
        }
    }

    private class ModifyGoogleSheets extends AsyncTask<Void, Void, Void> {

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        private String contentsBefore;
        private String editDateBefore;
        private String contentsAfter;
        private String editDateAfter;
        private int position;

        private String targetRange;
        private List<List<Object>> inputValues;

        private Realm modifyGoogleSheetCheckRealm;

        private int inputWhatToAdd;

        ModifyGoogleSheets(GoogleAccountCredential credential, int whatToAdd, String contentsBefore, String editDateBefore, String contentsAfter, String editDateAfter, int position) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("HangmanQRCode")
                    .build();

            this.contentsBefore = contentsBefore;
            this.editDateBefore = editDateBefore;
            this.contentsAfter = contentsAfter;
            this.editDateAfter = editDateAfter;
            this.inputWhatToAdd = whatToAdd;
            this.position = position;

        }

        @Override
        protected void onPreExecute() {
            Log.v("Status","Waiting");
            mProgress.setMessage("수정하는 중...");
            mProgress.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<List<Object>> dataFromAPI;
            try {
                modifyGoogleSheetCheckRealm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);

                dataFromAPI = getDataFromApi();

                switch (inputWhatToAdd) {

                    case WHATTOADD_EDIT_PRESSURETEST:
                        PressureTest foundedPressureTest
                                = modifyGoogleSheetCheckRealm.where(PressureTest.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("testCount", Integer.valueOf(contentsBefore))
                                .equalTo("date", editDateBefore)
                                .findFirst();
                        Log.d("RealmFound", "PressureTest \t\t" + foundedPressureTest.getTestCount() + "회\t\t" + foundedPressureTest.getTestDateInString());
                        Log.d("APIFound", dataFromAPI.get(4).get(position).toString().replaceAll("[^0-9]", ""));
                        Log.d("APIFound", dataFromAPI.get(5).get(position).toString());

                        if(dataFromAPI.get(4).get(position).toString().replaceAll("[^0-9]", "").equals(String.valueOf(foundedPressureTest.getTestCount()))
                                && dataFromAPI.get(5).get(position).toString().equals(foundedPressureTest.getDate())) {
                            targetRange = ColumnNumberToLetter(position + 1) + "6:" + ColumnNumberToLetter(position + 1) + "7";

                            inputValues = Arrays.asList(
                                    Arrays.asList(
                                            // Cell values ...
                                            (Object) String.valueOf(contentsAfter + "차")
                                    ),
                                    // Additional rows ...
                                    Arrays.asList(
                                            (Object) editDateAfter
                                    )
                            );
                        } else {
                            Log.e("ERROR", "Position ERROR :: position == " + (position));
                            for(int idx = 0; idx < dataFromAPI.get(4).size(); idx++) {
                                if(dataFromAPI.get(4).get(idx).toString().replaceAll("[^0-9]", "").equals(String.valueOf(foundedPressureTest.getTestCount()))
                                        && dataFromAPI.get(5).get(idx).toString().equals(foundedPressureTest.getDate())) {
                                    targetRange = ColumnNumberToLetter(idx + 1) + "6:" + ColumnNumberToLetter(idx + 1) + "7";
                                    inputValues = Arrays.asList(
                                            Arrays.asList(
                                                    // Cell values ...
                                                    (Object) String.valueOf(contentsAfter + "차")
                                            ),
                                            // Additional rows ...
                                            Arrays.asList(
                                                    (Object) editDateAfter
                                            )
                                    );
                                }
                            }
                        }

                        /*
                        // Google Sheets Data
                        ArrayList<String> date = new ArrayList<>();
                        ArrayList<Integer> count = new ArrayList<>();
                        int targetIndex = -1;
                        for(int idx = 0; idx < dataFromAPI.get(4).size(); idx++) {
                            if(Integer.valueOf(dataFromAPI.get(4).get(idx).toString().replaceAll("[^0-9]", "")).equals(foundedPressureTest.getTestCount())
                                    && dataFromAPI.get(5).get(idx).toString().equals(foundedPressureTest.getDate())) {
                                targetIndex = idx;
                            }
                        }
                        targetRange = ColumnNumberToLetter(targetIndex + 1) + "6:" + ColumnNumberToLetter(targetIndex + 1) + "7";

                        inputValues = Arrays.asList(
                                Arrays.asList(
                                        // Cell values ...
                                        (Object) contentsAfter
                                ),
                                // Additional rows ...
                                Arrays.asList(
                                        (Object) editDateAfter
                                )
                        );
                        */

                        break;

                    case WHATTOADD_EDIT_SANITATIONHISTORY:
                        SanitationHistory foundedSanitationHistory
                                = modifyGoogleSheetCheckRealm.where(SanitationHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("testCount", Integer.valueOf(contentsBefore))
                                .equalTo("date", editDateBefore)
                                .findFirst();
                        Log.d("RealmFound", "SanitationHistory \t\t" + foundedSanitationHistory.getTestCount() + "회\t\t" + foundedSanitationHistory.getTestDateInString());
                        Log.d("APIFound", dataFromAPI.get(7).get(position).toString().replaceAll("[^0-9]", ""));
                        Log.d("APIFound", dataFromAPI.get(8).get(position).toString());

                        if(dataFromAPI.get(7).get(position).toString().replaceAll("[^0-9]", "").equals(String.valueOf(foundedSanitationHistory.getTestCount()))
                                && dataFromAPI.get(8).get(position).toString().equals(foundedSanitationHistory.getDate())) {
                            targetRange = ColumnNumberToLetter(position + 1) + "9:" + ColumnNumberToLetter(position + 1) + "10";

                            inputValues = Arrays.asList(
                                    Arrays.asList(
                                            // Cell values ...
                                            (Object) String.valueOf(contentsAfter + "차")
                                    ),
                                    // Additional rows ...
                                    Arrays.asList(
                                            (Object) editDateAfter
                                    )
                            );
                        } else {
                            Log.e("ERROR", "Position ERROR :: position == " + (position));
                            for(int idx = 0; idx < dataFromAPI.get(7).size(); idx++) {
                                if(dataFromAPI.get(7).get(idx).toString().replaceAll("[^0-9]", "").equals(String.valueOf(foundedSanitationHistory.getTestCount()))
                                        && dataFromAPI.get(8).get(idx).toString().equals(foundedSanitationHistory.getDate())) {
                                    targetRange = ColumnNumberToLetter(idx + 1) + "9:" + ColumnNumberToLetter(idx + 1) + "10";
                                    inputValues = Arrays.asList(
                                            Arrays.asList(
                                                    // Cell values ...
                                                    (Object) String.valueOf(contentsAfter + "차")
                                            ),
                                            // Additional rows ...
                                            Arrays.asList(
                                                    (Object) editDateAfter
                                            )
                                    );
                                }
                            }
                        }
                        break;

                    case WHATTOADD_EDIT_REPAIRHISTORY:
                        Log.d("Received", BOTTLE_CODE);
                        Log.d("Received", contentsBefore);
                        Log.d("Received", editDateBefore);
                        RepairHistory foundedRepairHistory
                                = modifyGoogleSheetCheckRealm.where(RepairHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("contents", contentsBefore)
                                .equalTo("date", editDateBefore)
                                .findFirst();
                        Log.d("RealmFound", "RepairHistory \t\t" + foundedRepairHistory.getDate() + "\t\t" + foundedRepairHistory.getContents());
                        Log.d("APIFound", dataFromAPI.get((position % 5) + 11).get((int)(position / 5) * 2).toString());
                        Log.d("APIFound", dataFromAPI.get((position % 5) + 11).get((int)(position / 5) * 2 + 1).toString());

                        if(dataFromAPI.get((position % 5) + 11).get((int)(position / 5) * 2).toString().equals(String.valueOf(foundedRepairHistory.getDate()))
                                && dataFromAPI.get((position % 5) + 11).get((int)(position / 5) * 2 + 1).toString().equals(foundedRepairHistory.getContents())) {
                            targetRange = ColumnNumberToLetter((int)(position / 5) * 2) + String.valueOf((position % 5) + 13) + ":"
                                    + ColumnNumberToLetter((int)(position / 5) * 2 + 1) + String.valueOf((position % 5) + 13);

                            inputValues = Arrays.asList(
                                    Arrays.asList(
                                            // Cell values ...
                                            (Object) editDateAfter, (Object) String.valueOf(contentsAfter)
                                    )
                                    // Additional rows ...
                            );
                        } else {
                            Log.e("ERROR", "Position ERROR :: position == " + (position));
                        }

                        break;

                    case WHATTOADD_EDIT_RECHARGEHISTORY:
                        RechargeHistory foundedRechargeHistory
                                = modifyGoogleSheetCheckRealm.where(RechargeHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("rechargeCount", Integer.valueOf(contentsBefore))
                                .equalTo("date", editDateBefore)
                                .findFirst();
                        Log.d("RealmFound", "PressureTest \t\t" + foundedRechargeHistory.getRechargeCount() + "회\t\t" + foundedRechargeHistory.getTestDateInString());
                        Log.d("APIFound", dataFromAPI.get((position % 5) + 18).get((int)(position / 5) * 2).toString());
                        Log.d("APIFound", dataFromAPI.get((position % 5) + 18).get((int)(position / 5) * 2 + 1).toString());

                        if(dataFromAPI.get((position % 5) + 18).get((int)(position / 5) * 2).toString().replaceAll("[^0-9]", "").equals(String.valueOf(foundedRechargeHistory.getRechargeCount()))
                                && dataFromAPI.get((position % 5) + 18).get((int)(position / 5) * 2 + 1).toString().equals(foundedRechargeHistory.getDate())) {
                            targetRange = ColumnNumberToLetter((int)(position / 5) * 2) + String.valueOf((position % 5) + 20) + ":"
                                    + ColumnNumberToLetter((int)(position / 5) * 2 + 1) + String.valueOf((position % 5) + 20);

                            inputValues = Arrays.asList(
                                    Arrays.asList(
                                            // Cell values ...
                                            (Object) (String.valueOf(contentsAfter) + "회"), (Object) editDateAfter
                                            )
                                    // Additional rows ...
                            );
                        } else {
                            Log.e("ERROR", "Position ERROR :: position == " + (position));
                        }
                        break;

                    default:

                        break;

                }

                ValueRange body = new ValueRange()
                        .setValues(inputValues);

                UpdateValuesResponse results = this.mService.spreadsheets().values().update(SHEETS_ID, targetRange, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private List<List<Object>> getDataFromApi() throws IOException {
            String spreadsheetId = SHEETS_ID;
            String range = "A2:Z";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new MakeReadTask(mCredential).execute();
            mProgress.hide();
            Log.d("ModifyGoogleSheets", "Modifying is Over");
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            OxygenBottleActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("ERROR","The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.e("ERROR","Request cancelled.");
            }
        }
    }

    private class DeleteGoogleSheets extends AsyncTask<Void, Void, Void> {

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        private String deleteContentsTarget;
        private String deleteDateTarget;

        private String targetRange;
        private List<List<Object>> inputValues;

        private Realm modifyGoogleSheetCheckRealm;

        private int inputWhatToAdd;

        private int targetIndex;
        private int duplicateDiscriminant;
        private int arrayPosition;

        DeleteGoogleSheets(GoogleAccountCredential credential, int whatToAdd, String deleteContentsTarget, String deleteDateTarget, int arrayPosition) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("HangmanQRCode")
                    .build();

            this.deleteContentsTarget = deleteContentsTarget;
            this.deleteDateTarget = deleteDateTarget;
            this.inputWhatToAdd = whatToAdd;
            this.arrayPosition = arrayPosition;


        }

        @Override
        protected void onPreExecute() {
            Log.v("Status","Waiting");
            mProgress.setMessage("수정하는 중...");
            mProgress.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<List<Object>> dataFromAPI;
            try {
                modifyGoogleSheetCheckRealm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);

                dataFromAPI = getDataFromApi();

                switch (inputWhatToAdd) {

                    case WHATTOADD_PRESSURETEST:
                        PressureTest foundedPressureTest
                                = modifyGoogleSheetCheckRealm.where(PressureTest.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("testCount", Integer.valueOf(deleteContentsTarget))
                                .equalTo("date", deleteDateTarget)
                                .findFirst();
                        Log.d("RealmFound", "PressureTest \t\t" + foundedPressureTest.getTestCount() + "회\t\t" + foundedPressureTest.getTestDateInString());

                        // Google Sheets Data
                        targetIndex = -1;
                        for(int idx = 0; idx < dataFromAPI.get(4).size(); idx++) {
                            if(Integer.valueOf(dataFromAPI.get(4).get(idx).toString().replaceAll("[^0-9]", "")).equals(foundedPressureTest.getTestCount())
                                    && dataFromAPI.get(5).get(idx).toString().equals(foundedPressureTest.getDate())) {
                                targetIndex = idx;
                            }
                        }

                        targetRange = "A6:" + ColumnNumberToLetter(dataFromAPI.get(4).size()) + "7";

                        dataFromAPI.get(4).remove(dataFromAPI.get(4).get(targetIndex));
                        dataFromAPI.get(5).remove(dataFromAPI.get(5).get(targetIndex));
                        dataFromAPI.get(4).add(String.valueOf(""));
                        dataFromAPI.get(5).add(String.valueOf(""));

                        inputValues = Arrays.asList(dataFromAPI.get(4), dataFromAPI.get(5));

                        break;

                    case WHATTOADD_SANITATIONHISTORY:
                        SanitationHistory foundedSanitationHistory
                                = modifyGoogleSheetCheckRealm.where(SanitationHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("testCount", Integer.valueOf(deleteContentsTarget))
                                .equalTo("date", deleteDateTarget)
                                .findFirst();
                        Log.d("RealmFound", "SanitationHistory \t\t" + foundedSanitationHistory.getTestCount() + "회\t\t" + foundedSanitationHistory.getTestDateInString());

                        // Google Sheets Data
                        targetIndex = -1;
                        for(int idx = 0; idx < dataFromAPI.get(7).size(); idx++) {
                            if(Integer.valueOf(dataFromAPI.get(7).get(idx).toString().replaceAll("[^0-9]", "")).equals(foundedSanitationHistory.getTestCount())
                                    && dataFromAPI.get(8).get(idx).toString().equals(foundedSanitationHistory.getDate())) {
                                targetIndex = idx;
                            }
                        }

                        targetRange = "A9:" + ColumnNumberToLetter(dataFromAPI.get(7).size()) + "10";

                        dataFromAPI.get(7).remove(dataFromAPI.get(7).get(targetIndex));
                        dataFromAPI.get(8).remove(dataFromAPI.get(8).get(targetIndex));
                        dataFromAPI.get(7).add(String.valueOf(""));
                        dataFromAPI.get(8).add(String.valueOf(""));

                        inputValues = Arrays.asList(dataFromAPI.get(7), dataFromAPI.get(8));

                        break;

                    case WHATTOADD_REPAIRHISTORY:
                        RepairHistory foundedRepairHistory
                                = modifyGoogleSheetCheckRealm.where(RepairHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("contents", deleteContentsTarget)
                                .equalTo("date", deleteDateTarget)
                                .findFirst();
                        Log.d("RealmFound", "RepairHistory \t\t" + foundedRepairHistory.getDate() + "\t\t" + foundedRepairHistory.getContents());

                        targetIndex = -1;
                        duplicateDiscriminant = 0;      // For checking duplicate. If this variable is larger than 1 (duplicateDiscriminant > 1), have to think which data should be deleted.

                        /**
                        * For Making pair coordination like below example.
                        * ●    ●    ●    ●
                        * ●    ●    ●    ●
                        * ●    ●    ○    ○
                        * ●    ●    ○    ○
                        * ●    ●    ○    ○
                        * For example, if each row doesn't have same column, it could make some error because of index problem.
                        */
                        for(int row = 1; row < 5; row++) {
                            if(dataFromAPI.get(row + 11).size() == dataFromAPI.get(11).size()) {
                                continue;
                            } else {
                                // Each set has 2 values. For example, (20180222 - AirPressure Problem)
                                dataFromAPI.get(row + 11).add(String.valueOf(""));
                                dataFromAPI.get(row + 11).add(String.valueOf(""));
                            }
                        }

                        // Checking Data.
                        for(int idx = 0; idx < dataFromAPI.get(11).size() * 5 / 2; idx++) {
                            // Log.d("Comparing", dataFromAPI.get(idx % 5 + 11).get((int)(idx / 5) * 2) + "\t\t" + foundedRepairHistory.getDate());
                            // Log.d("Comparing", dataFromAPI.get(idx % 5 + 11).get((int)(idx / 5) * 2 + 1) + "\t\t" + foundedRepairHistory.getContents());
                            if(dataFromAPI.get(idx % 5 + 11).get((int)(idx / 5) * 2).equals(foundedRepairHistory.getDate())
                                    && dataFromAPI.get(idx % 5 + 11).get((int)(idx / 5) * 2 + 1).equals(foundedRepairHistory.getContents())) {
                                targetIndex = idx;
                                duplicateDiscriminant++;
                            }
                        }

                        // If this variable is larger than 1 (duplicateDiscriminant > 1), have to think which data should be deleted.
                        if(duplicateDiscriminant > 1) {
                            // In this project, I decide to delete position.
                            targetIndex = arrayPosition;
                        }

                        // Overwriting for delete Data
                        for(int idx = targetIndex; idx < dataFromAPI.get(11).size() * 5 / 2 - 1; idx++) {
                            // Log.d("Current", "Now Idx == " + String.valueOf(idx));
                            dataFromAPI.get(idx % 5 + 11).set((int)(idx / 5) * 2    , dataFromAPI.get((idx + 1) % 5 + 11).get((int)((idx + 1) / 5) * 2));
                            dataFromAPI.get(idx % 5 + 11).set((int)(idx / 5) * 2 + 1, dataFromAPI.get((idx + 1) % 5 + 11).get((int)((idx + 1) / 5) * 2 + 1));
                        }

                        dataFromAPI.get((dataFromAPI.get(11).size() * 5 / 2 - 1) % 5 + 11).set((int)((dataFromAPI.get(11).size() * 5 / 2 - 1) / 5) * 2, String.valueOf(""));
                        dataFromAPI.get((dataFromAPI.get(11).size() * 5 / 2 - 1) % 5 + 11).set((int)((dataFromAPI.get(11).size() * 5 / 2 - 1) / 5) * 2 + 1, String.valueOf(""));

                        targetRange = "A13:" + ColumnNumberToLetter(dataFromAPI.get(11).size()) + "17";

                        inputValues = new ArrayList<>();

                        inputValues.add(dataFromAPI.get(11));
                        inputValues.add(dataFromAPI.get(12));
                        inputValues.add(dataFromAPI.get(13));
                        inputValues.add(dataFromAPI.get(14));
                        inputValues.add(dataFromAPI.get(15));

                        break;

                    case WHATTOADD_RECHARGEHISTORY:
                        RechargeHistory foundedRechargeHistory
                                = modifyGoogleSheetCheckRealm.where(RechargeHistory.class)
                                .equalTo("bottleCode", BOTTLE_CODE)
                                .equalTo("rechargeCount", Integer.valueOf(deleteContentsTarget))
                                .equalTo("date", deleteDateTarget)
                                .findFirst();
                        Log.d("RealmFound", "RechargeHistory \t\t" + foundedRechargeHistory.getRechargeCount() + "회\t\t" + foundedRechargeHistory.getTestDateInString());

                        targetIndex = -1;
                        duplicateDiscriminant = 0;      // For checking duplicate. If this variable is larger than 1 (duplicateDiscriminant > 1), have to think which data should be deleted.

                        boolean isThereSanitationHistory = false;
                        if(dataFromAPI.get(22).contains(String.valueOf("위생검사"))) {
                            dataFromAPI.get(22).remove(3);
                            dataFromAPI.get(22).remove(2);
                            isThereSanitationHistory = true;
                        }

                        for(int row = 1; row < 5; row++) {
                            if(dataFromAPI.get(row + 18).size() == dataFromAPI.get(18).size()) {
                                continue;
                            } else {
                                dataFromAPI.get(row + 18).add(String.valueOf(""));
                                dataFromAPI.get(row + 18).add(String.valueOf(""));
                            }
                        }

                        // Checking Data.
                        for(int idx = 0; idx < dataFromAPI.get(18).size() * 5 / 2; idx++) {
                            Log.d("Comparing", dataFromAPI.get(idx % 5 + 18).get((int)(idx / 5) * 2).toString() + "\t\t" + foundedRechargeHistory.getRechargeCount());
                            Log.d("Comparing", dataFromAPI.get(idx % 5 + 18).get((int)(idx / 5) * 2 + 1).toString() + "\t\t" + foundedRechargeHistory.getDate());
                            if(dataFromAPI.get(idx % 5 + 18).get((int)(idx / 5) * 2).toString().equals("")) {
                                continue;
                            } else {
                                if(Integer.valueOf(dataFromAPI.get(idx % 5 + 18).get((int)(idx / 5) * 2).toString().replaceAll("[^0-9]", "")).equals(foundedRechargeHistory.getRechargeCount())
                                        && dataFromAPI.get(idx % 5 + 18).get((int)(idx / 5) * 2 + 1).toString().equals(foundedRechargeHistory.getDate())) {
                                    targetIndex = idx;
                                    duplicateDiscriminant++;
                                }
                            }
                        }

                        // If this variable is larger than 1 (duplicateDiscriminant > 1), have to think which data should be deleted.
                        if(duplicateDiscriminant > 1) {
                            // In this project, I decide to delete position.
                            targetIndex = arrayPosition;
                        }

                        // Overwriting for delete Data
                        for(int idx = targetIndex; idx < dataFromAPI.get(18).size() * 5 / 2 - 1; idx++) {
                            // Log.d("Current", "Now Idx == " + String.valueOf(idx));
                            dataFromAPI.get(idx % 5 + 18).set((int)(idx / 5) * 2    , dataFromAPI.get((idx + 1) % 5 + 18).get((int)((idx + 1) / 5) * 2));
                            dataFromAPI.get(idx % 5 + 18).set((int)(idx / 5) * 2 + 1, dataFromAPI.get((idx + 1) % 5 + 18).get((int)((idx + 1) / 5) * 2 + 1));
                        }

                        dataFromAPI.get((dataFromAPI.get(18).size() * 5 / 2 - 1) % 5 + 18).set((int)((dataFromAPI.get(18).size() * 5 / 2 - 1) / 5) * 2, String.valueOf(""));
                        dataFromAPI.get((dataFromAPI.get(18).size() * 5 / 2 - 1) % 5 + 18).set((int)((dataFromAPI.get(18).size() * 5 / 2 - 1) / 5) * 2 + 1, String.valueOf(""));

                        if(isThereSanitationHistory) {
                            if(dataFromAPI.get(22).size() < 4) {
                                dataFromAPI.get(22).add(String.valueOf(""));
                                dataFromAPI.get(22).add(String.valueOf(""));
                            }
                            dataFromAPI.get(22).set(2, String.valueOf("10회"));
                            dataFromAPI.get(22).set(3, String.valueOf("위생검사"));
                        }

                        if(dataFromAPI.get(18).size() > dataFromAPI.get(22).size()) {
                            targetRange = "A20:" + ColumnNumberToLetter(dataFromAPI.get(18).size()) + "24";
                        } else {
                            targetRange = "A20:" + ColumnNumberToLetter(dataFromAPI.get(22).size()) + "24";
                        }

                        Log.d("targetRange", targetRange);

                        inputValues = new ArrayList<>();

                        inputValues.add(dataFromAPI.get(18));
                        inputValues.add(dataFromAPI.get(19));
                        inputValues.add(dataFromAPI.get(20));
                        inputValues.add(dataFromAPI.get(21));
                        inputValues.add(dataFromAPI.get(22));

                        break;

                    default:

                        break;

                }

                ValueRange body = new ValueRange()
                        .setValues(inputValues);

                UpdateValuesResponse results = this.mService.spreadsheets().values().update(SHEETS_ID, targetRange, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private List<List<Object>> getDataFromApi() throws IOException {
            String spreadsheetId = SHEETS_ID;
            String range = "A2:Z";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new MakeReadTask(mCredential).execute();
            mProgress.hide();
            Log.d("ModifyGoogleSheets", "Modifying is Over");
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            OxygenBottleActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("ERROR","The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.e("ERROR","Request cancelled.");
            }
        }
    }

    @OnClick({ R.id.floatingActionButtonInOxygenBottleActivity
            , R.id.floatingActionButtonEditPressure
            , R.id.floatingActionButtonEditSanitation
            , R.id.floatingActionButtonEditRepairHistory
            , R.id.floatingActionButtonEditRechargeHistory})
    public void fabClicked(View view) {

        // Floating Action Bar Animation
        Animation fab_open  = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_open);
        Animation fab_close = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_close);
        TranslateAnimation goUP = new TranslateAnimation(0.0f, 0.0f, 0.0f, 1.0f);
        TranslateAnimation goDOWN = new TranslateAnimation(0.0f, 0.0f, 0.0f, -1.0f);

        FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0] = floatingActionButtonInOxygenBottleActivity.getLeft() + (px_miniButton / 2);
        FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1] = floatingActionButtonInOxygenBottleActivity.getTop() + (px_miniButton / 2);
        Log.d("Coordinate", "FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0] == " + FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0]
                + "\nFLOATING_ACTION_BUTTON_MAIN_COORDINATE[1] == " + FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1]);

        Intent openActivity = new Intent(this, SettingDialogActivity.class);

        Log.d("onClickID", String.valueOf(view.getId()));

        switch (view.getId()) {

            case R.id.floatingActionButtonInOxygenBottleActivity:

                if(FAB_Spring.getEndValue() == OPEN) {      // OPEN -> CLOSE
                    FAB_Spring.setEndValue(CLOSE);
                } else {                                    // CLOSE -> OPEN
                    FAB_Spring.setEndValue(OPEN);
                }
                break;

            case R.id.floatingActionButtonEditPressure:

                Log.d("onClicked", "floatingActionButtonEditPressure Clicked");

                whatToAdd = AppGlobalInstance.WHATTOADD_PRESSURETEST;

                FAB_Spring.setEndValue(CLOSE);
                openActivity.putExtra("BottleCode", bundleToFragment.getString("BOTTLE_CODE"));
                openActivity.putExtra("WhatToAdd", whatToAdd);

                startActivityForResult(openActivity, REQUEST_DIALOG_ADDING_PRESSURE);
                break;

            case R.id.floatingActionButtonEditSanitation:

                Log.d("onClicked", "floatingActionButtonEditSanitation Clicked");

                whatToAdd = AppGlobalInstance.WHATTOADD_SANITATIONHISTORY;

                FAB_Spring.setEndValue(CLOSE);
                openActivity.putExtra("BottleCode", bundleToFragment.getString("BOTTLE_CODE"));
                openActivity.putExtra("WhatToAdd", whatToAdd);

                startActivityForResult(openActivity, REQUEST_DIALOG_ADDING_SANITATION);
                break;

            case R.id.floatingActionButtonEditRepairHistory:

                Log.d("onClicked", "floatingActionButtonEditRepairHistory Clicked");

                whatToAdd = AppGlobalInstance.WHATTOADD_REPAIRHISTORY;

                FAB_Spring.setEndValue(CLOSE);
                openActivity.putExtra("BottleCode", bundleToFragment.getString("BOTTLE_CODE"));
                openActivity.putExtra("WhatToAdd", whatToAdd);

                startActivityForResult(openActivity, REQUEST_DIALOG_ADDING_REPAIR);
                break;

            case R.id.floatingActionButtonEditRechargeHistory:

                Log.d("onClicked", "floatingActionButtonEditRechargeHistory Clicked");

                whatToAdd = AppGlobalInstance.WHATTOADD_RECHARGEHISTORY;

                FAB_Spring.setEndValue(CLOSE);
                openActivity.putExtra("BottleCode", bundleToFragment.getString("BOTTLE_CODE"));
                openActivity.putExtra("WhatToAdd", whatToAdd);

                startActivityForResult(openActivity, REQUEST_DIALOG_ADDING_RECHARGE);
                break;

        }



    }

    protected void writeOnGoogleSheets(int whatToAdd, String contents, String editDate) {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
        } else {
            new MakeWriteTask(mCredential, whatToAdd, contents, editDate).execute();
        }
    }

    protected void modifyOnGoogleSheets(int whatToAdd, String contentsBefore, String editDateBefore, String contentsAfter, String editDateAfter, int position) {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
        } else {
            new ModifyGoogleSheets(mCredential, whatToAdd, contentsBefore, editDateBefore, contentsAfter, editDateAfter, position).execute();
        }
    }

    protected void deleteOnGoogleSheets(int whatToAdd, String deleteContentsTarget, String deleteDateTarget, int arrayPosition) {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Log.e("ERROR","No network connection available.");
        } else {
            new DeleteGoogleSheets(mCredential, whatToAdd, deleteContentsTarget, deleteDateTarget, arrayPosition).execute();
        }
    }

    public String dateIntArrayToString(int[] date) {
        if(Integer.valueOf(date.length).equals(3)) {
            return String.valueOf(date[0]) + "년 " + String.valueOf(date[1]) + "월 " +String.valueOf(date[2]) + "일";
        } else {
            Log.e("Error", "Failed! : dateIntArrayToString(int[] date) Error");
            return "Failed! : dateIntArrayToString(int[] date) Error";
        }
    }

    private String ColumnNumberToLetter(int inputColumnNumber) {
        String outputColumnName = "";
        int Base = 26;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int TempNumber = inputColumnNumber;
        while (TempNumber > 0) {
            int position = TempNumber % Base;
            outputColumnName = (position == 0 ? 'Z' : chars.charAt(position > 0 ? position - 1 : 0)) + outputColumnName;
            TempNumber = (TempNumber - 1) / Base;
        }
        return outputColumnName;
    }
}
