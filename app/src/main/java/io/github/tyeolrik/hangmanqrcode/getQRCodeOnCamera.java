package io.github.tyeolrik.hangmanqrcode;


import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 */
public class getQRCodeOnCamera extends Fragment {

    @BindView(R.id.getQRCodeBig)
    TextView getQRCodeBig;
    @BindView(R.id.getQRCodeSmall)
    TextView getQRCodeSmall;

    @BindView(R.id.getQRCodeYes)
    Button getQRCodeYes;


    public getQRCodeOnCamera() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_qrcode_on_camera, container, false);
        ButterKnife.bind(this, view);

        Typeface NanumBarunGothic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunGothic.ttf");
        getQRCodeBig.setTypeface(NanumBarunGothic);
        getQRCodeSmall.setTypeface(NanumBarunGothic);
        getQRCodeYes.setTypeface(NanumBarunGothic);

        // Animation 사용
        AlphaAnimation upAlpha = new AlphaAnimation(0.0f, 1.0f);
        upAlpha.setDuration(2000);
        AlphaAnimation downAlpha = new AlphaAnimation(1.0f, 0.0f);
        downAlpha.setStartOffset(1000);
        downAlpha.setDuration(2000);

        AlphaAnimation alphaChange = new AlphaAnimation(0.0f, 1.0f);
        alphaChange.setDuration(2000);
        alphaChange.setRepeatMode(Animation.REVERSE);
        alphaChange.setRepeatCount(Animation.INFINITE);

        getQRCodeBig.setAnimation(alphaChange);
        getQRCodeSmall.setAnimation(upAlpha);

        Realm realm = Realm.getInstance(AppGlobalInstance.USER_SETTING);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PersonalData.class).findFirst().setFirstUser(false);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!QRCodeScan.isBarcodeScanned) {
                        Thread.sleep(100);
                    }
                    getQRCodeYes();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return view;
    }

    @OnClick(R.id.getQRCodeYes)
    public void getQRCodeYes() {
        Log.d("Current Status", "On Clicked findQRCodeNo");
        getFragmentManager().beginTransaction().remove(this).commit();
    }

}
