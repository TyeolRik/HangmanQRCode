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
public class Fragment_HowToUse extends Fragment {

    @BindView(R.id.findQRCodeBig)
    TextView findQRCodeBig;
    @BindView(R.id.findQRCodeSmall)
    TextView findQRCodeSmall;

    @BindView(R.id.findQRCodeNo)
    Button findQRCodeNo;
    @BindView(R.id.findQRCodeYes)
    Button findQRCodeYes;

    public Fragment_HowToUse() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__how_to_use, container, false);
        ButterKnife.bind(this, view);

        Typeface NanumBarunGothic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunGothic.ttf");
        findQRCodeBig.setTypeface(NanumBarunGothic);
        findQRCodeSmall.setTypeface(NanumBarunGothic);
        findQRCodeNo.setTypeface(NanumBarunGothic);
        findQRCodeYes.setTypeface(NanumBarunGothic);

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

        findQRCodeBig.setAnimation(alphaChange);
        findQRCodeSmall.setAnimation(upAlpha);

        return view;
    }

    @OnClick(R.id.findQRCodeYes)
    public void onClicked_findQRCodeYes() {
        Log.d("Current Status", "On Clicked findQRCodeYes");
        getFragmentManager().beginTransaction().replace(R.id.tipConstraint, new getQRCodeOnCamera()).commit();
    }

    @OnClick(R.id.findQRCodeNo)
    public void onClicked_findQRCodeNo() {
        Log.d("Current Status", "On Clicked findQRCodeNo");
        Realm realm = Realm.getInstance(AppGlobalInstance.USER_SETTING);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PersonalData.class).findFirst().setFirstUser(false);
            }
        });
        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
