package io.github.tyeolrik.hangmanqrcode;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Information extends Fragment {

    @BindView(R.id.division)
    TextView division;
    @BindView(R.id.divisionText)
    TextView divisionText;
    @BindView(R.id.bottleCode)
    TextView bottleCode;
    @BindView(R.id.bottleCodeText)
    TextView bottleCodeText;
    @BindView(R.id.manufacturedDate)
    TextView manufacturedDate;
    @BindView(R.id.manufacturedDateText)
    TextView manufacturedDateText;
    @BindView(R.id.expiryDate)
    TextView expiryDate;
    @BindView(R.id.expiryDateText)
    TextView expiryDateText;
    @BindView(R.id.expectedPressureTestDate)
    TextView expectedPressureTestDate;
    @BindView(R.id.expectedPressureTestDateText)
    TextView expectedPressureTestDateText;
    @BindView(R.id.lastPressureTestDate)
    TextView lastPressureTestDate;
    @BindView(R.id.lastPressureTestDateText)
    TextView lastPressureTestDateText;

    Realm realm;
    Unbinder unbinder;

    private static String BOTTLE_CODE;

    public Fragment_Information() {
        // Required empty public constructor
    }

    public static Fragment_Information newInstance() {
        Bundle args = new Bundle();

        Fragment_Information fragment = new Fragment_Information();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment_Information newInstance(Bundle input) {
        Fragment_Information fragment = new Fragment_Information();
        Log.d("Arguments", input.toString());
        fragment.setArguments(input);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__information, container, false);
        unbinder = ButterKnife.bind(this, view);

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        // 항목
        division.setTypeface(nanumBarunGothicFontBold);
        bottleCode.setTypeface(nanumBarunGothicFontBold);
        manufacturedDate.setTypeface(nanumBarunGothicFontBold);
        expiryDate.setTypeface(nanumBarunGothicFontBold);
        expectedPressureTestDate.setTypeface(nanumBarunGothicFontBold);
        lastPressureTestDate.setTypeface(nanumBarunGothicFontBold);

        // 실제 내용

        Realm.init(getContext());
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);
        Log.d("FragmentBundle", getArguments().toString());

        if(getArguments() != null) {
            OxygenBottle bottle = realm.where(OxygenBottle.class).equalTo("bottleCode", getArguments().getString("BOTTLE_CODE")).findFirst();
            if((bottle == null) || (bottle.getBottleCode().isEmpty())) {

            } else {

                divisionText.setText(bottle.getDivision());
                bottleCodeText.setText(bottle.getBottleCode());
                manufacturedDateText.setText(getFormattedDateKorean(bottle.getManufacturedDate()));
                expiryDateText.setText(getFormattedDateKorean(bottle.getExpiryDate()));
                expectedPressureTestDateText.setText(getFormattedDateKorean(bottle.getExpectedPressureTestDate()));
                lastPressureTestDateText.setText(getFormattedDateKorean(bottle.getLastPressureTestDate()));
            }
        }
        divisionText.setTypeface(nanumBarunGothicFont);
        bottleCodeText.setTypeface(nanumBarunGothicFont);
        manufacturedDateText.setTypeface(nanumBarunGothicFont);
        expiryDateText.setTypeface(nanumBarunGothicFont);
        expectedPressureTestDateText.setTypeface(nanumBarunGothicFont);
        lastPressureTestDateText.setTypeface(nanumBarunGothicFont);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public String getFormattedDateKorean(final String YYYYMMDD) {
        return YYYYMMDD.substring(0, 4) + "년 " + YYYYMMDD.substring(4, 6) + "월 " + YYYYMMDD.substring(6, 8) + "일";
    }
}
