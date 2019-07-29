package io.github.tyeolrik.hangmanqrcode;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class StartActivity extends Activity {

    private static int getDeveloperAuthority;
    private final static int CAMERA_PERMISSIONS_GRANTED = 123;

    @BindView(R.id.Signiture119Image)
    ImageView signiture119Image;
    @BindView(R.id.titleTextView)
    TextView titleTextView;
    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.contestName)
    TextView contestName;
    @BindView(R.id.imageName)
    TextView imageName;
    @BindView(R.id.copyright)
    TextView copyright;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        getDeveloperAuthority = 0;          // For EasterEgg

        /* Get Permission */
        getCameraPermission(); // Camera

        Realm.init(this);
        realm = Realm.getInstance(AppGlobalInstance.USER_SETTING);

        if(realm.where(PersonalData.class).findFirst() == null) {
            Log.d("RealmNullCheck", "User is Null");
            // No data
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    PersonalData user = new PersonalData();
                    user.setFirstUser(true);
                    realm.copyToRealm(user);
                }
            });
        }

        Log.d("Realm Check", realm.where(PersonalData.class).findFirst().toString());

        // Font
        Typeface nanumBarunGothicBoldFont = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothic.ttf");
        titleTextView.setTypeface(nanumBarunGothicBoldFont);
        startButton.setTypeface(nanumBarunGothicBoldFont);
        contestName.setTypeface(nanumBarunGothicFont);
        imageName.setTypeface(nanumBarunGothicFont);
        copyright.setTypeface(nanumBarunGothicFont);

        AlphaAnimation alphaChange = new AlphaAnimation(0.0f, 1.0f);
        alphaChange.setDuration(2000);

        titleTextView.setAnimation(alphaChange);
    }


    private boolean getCameraPermission() {
        int cameraPermissionFlag = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {

                Toast.makeText(this, "카메라 사용을 위해 확인버튼을 눌러주세요!", Toast.LENGTH_SHORT).show();

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                return true;

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_PERMISSIONS_GRANTED);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

                return true;
            }
        }
    }


    @OnClick(R.id.startButton)
    public void onClickedStartButton() {
        Intent goQRCodeScanActivity = new Intent(this, QRCodeScan.class);
        startActivity(goQRCodeScanActivity);
    }

    @OnClick(R.id.Signiture119Image)
    public void goEasterEgg() {
        getDeveloperAuthority++;
        if(getDeveloperAuthority == 7) {
            Intent goEasterEgg = new Intent(this, EasterEgg.class);
            startActivity(goEasterEgg);
        }
    }

    @OnClick(R.id.areYouFirstText)
    public void onClickAreYouFirstText() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PersonalData user = realm.where(PersonalData.class).findFirst();
                user.setFirstUser(true);
            }
        });
        onClickedStartButton();
    }

    @OnClick(R.id.copyright)
    public void onClickCopyRight() {
        Intent goEasterEgg = new Intent(this, EasterEgg.class);
        startActivity(goEasterEgg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSIONS_GRANTED: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
