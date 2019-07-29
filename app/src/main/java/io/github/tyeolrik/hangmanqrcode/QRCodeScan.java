package io.github.tyeolrik.hangmanqrcode;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class QRCodeScan extends Activity {

    @BindView(R.id.relativeLayoutInQRCode)
    RelativeLayout relativeLayoutInQRCode;
    @BindView(R.id.cameraFocusQR)
    ImageView cameraFocusQR;
    @BindView(R.id.cameraSurfaceView)
    SurfaceView cameraSurfaceView;

    // Fragment
    @BindView(R.id.tipFrameLayout)
    FrameLayout tipFrameLayout;
    @BindView(R.id.tipConstraint)
    ConstraintLayout tipConstraint;

    CameraSource cameraSource;

    Context thisContext;

    Realm realm;

    private final String nowStatusLogTag = "Now Status is";

    public static boolean isBarcodeScanned; // Barcode 동시에 여러번 인식이 안되는 Flag

    private final int REQUEST_CODE_QR_CODE_SCAN = 100;
    private static final String INVAILED_URL = "FAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        ButterKnife.bind(this);

        thisContext = this;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Granted");
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            Log.d("Permission", "Denied");
        } else {
            Log.e("Permission", "ERROR : " + String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)));
        }

        realm = Realm.getInstance(AppGlobalInstance.USER_SETTING);

        Log.d("Realm Check", realm.where(PersonalData.class).findFirst().toString());

        if(realm.where(PersonalData.class).findFirst().isFirstUser()) {
            Fragment_AreYouFirstUserAsk fragmentFirst = new Fragment_AreYouFirstUserAsk();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.add(R.id.tipConstraint, fragmentFirst);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        isBarcodeScanned = false; // Barcode 동시에 여러번 인식이 안되는 Flag
        startCameraBarcode();

    }

    public void startCameraBarcode() {

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        Log.d(nowStatusLogTag, "BarcodeDetector Build Complete");

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(29.8f)
                .setRequestedPreviewSize(1080, 1920)
                .setAutoFocusEnabled(true)
                .build();
        Log.d(nowStatusLogTag, "CameraSource Build Complete");

        cameraSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permission", "Granted");

                        cameraSource.start(cameraSurfaceView.getHolder());

                        cameraFocusQR.invalidate();

                        // drawInitial(); // 그린 뒤에 갱신 :: 필요없는 것 같음.

                        return;
                    }
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
                Log.d(nowStatusLogTag, "SurfaceView Destroyed and CameraSource Stopped");
            }
        });
        Log.d(nowStatusLogTag, "SurfaceView Callback Added");

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Log.d(nowStatusLogTag, "BarcodeDetector SetProcessor Released");
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                if (!isBarcodeScanned) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if(barcodes.size() != 0) {

                        String barcodeContents = barcodes.valueAt(0).displayValue;

                        isBarcodeScanned = true;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable(){   // UI Thread 호출 ERROR :: Only the original thread that created a view hierarchy can touch its views.
                                    @Override
                                    public void run() {
                                        // 해당 작업을 처리함
                                        relativeLayoutInQRCode.removeView(tipConstraint);
                                    }
                                });
                            }
                        }).start();

                        Realm getRealm = Realm.getInstance(AppGlobalInstance.USER_SETTING);

                        if(getRealm.where(PersonalData.class).findFirst().isFirstUser()) {
                            getRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    PersonalData user = realm.where(PersonalData.class).findFirst();
                                    user.setFirstUser(false);
                                }
                            });
                        }

                        if(!getRealm.isClosed()) {
                            getRealm.close();
                        }

                        // Redirect 된 주소 찾기 : 원주소 :: https://docs.google.com/spreadsheets/d/1z7IEFNASoZBmpdZD8XmyaBl1zE4L_Jnk5ETIA0KSQ1E/edit?usp=sharing
                        try {
                            String realBarcodeContents = getRedirectionURL(barcodeContents);
                            if(realBarcodeContents.equals(INVAILED_URL)) {
                                Log.e("INVAILED_URL", "Url is not Vailed :: " + barcodeContents);
                                Toast.makeText(getApplicationContext(), "사용할 수 없는 QR코드 입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent openOxygenBottleActivity = new Intent(getApplicationContext(), OxygenBottleActivity.class);
                                openOxygenBottleActivity.putExtra("SpreadSheetID", getSpreadSheetID(realBarcodeContents));
                                Log.d("Intent", "To: " + String.valueOf(realBarcodeContents));
                                startActivity(openOxygenBottleActivity);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "RequestCode : " + String.valueOf(requestCode) + "\tResultCode : " + String.valueOf(resultCode) + "\tIntent : " + String.valueOf(data));

        if (requestCode == REQUEST_CODE_QR_CODE_SCAN) {

            if(realm.isClosed()) {
                realm.close();
            }

            if (resultCode == RESULT_CANCELED) { // 웹브라우저 선택 안함 (선택창에서 취소 누름)
                Toast.makeText(this, "웹 브라우저를 선택해주세요!", Toast.LENGTH_LONG).show();
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                isBarcodeScanned = false;
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final String getRedirectionURL(String inputURL) throws Exception {
        return getRedirectionURL(inputURL, 0);
    }

    public static final String getRedirectionURL(String inputURL, int redirectionCount) throws Exception {

        if(redirectionCount == 10) {
            return INVAILED_URL;
        }

        HttpURLConnection ucon = (HttpURLConnection) new URL(inputURL).openConnection();
        ucon.setInstanceFollowRedirects(false);
        URL oneStepToOriginalURL = new URL(ucon.getHeaderField("Location"));

        if(oneStepToOriginalURL.toString().contains("docs.google.com/spreadsheets")) {
            return oneStepToOriginalURL.toString();
        } else {
            redirectionCount++;
            return getRedirectionURL(oneStepToOriginalURL.toString(), redirectionCount);
        }

    }

    public static final String getSpreadSheetID(String inputURL) throws Exception {
        // https://docs.google.com/spreadsheets/d/1z7IEFNASoZBmpdZD8XmyaBl1zE4L_Jnk5ETIA0KSQ1E/edit?usp=sharing
        String[] validationCheck = inputURL.split("/");
        for(int idx = 0; idx < validationCheck.length; idx++) {
            if(validationCheck[idx].equals("spreadsheets")) {
                if(validationCheck[idx+1].equals("d")) {
                    return validationCheck[idx+2];
                }
            }
        }
        return inputURL.split("/")[5];
    }
}