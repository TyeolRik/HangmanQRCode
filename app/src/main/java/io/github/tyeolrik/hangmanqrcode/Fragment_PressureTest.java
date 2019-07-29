package io.github.tyeolrik.hangmanqrcode;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_PressureTest extends Fragment {

    @BindView(R.id.pressureListView)
    ListView pressureListView;

    Realm realm;
    Unbinder unbinder;

    public Fragment_PressureTest() {
        // Required empty public constructor
    }

    public static Fragment_PressureTest newInstance() {
        Bundle args = new Bundle();

        Fragment_PressureTest fragment = new Fragment_PressureTest();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment_PressureTest newInstance(Bundle input) {
        Fragment_PressureTest fragment = new Fragment_PressureTest();
        Log.d("Arguments", input.toString());
        fragment.setArguments(input);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pressure_test, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Adapter
        PressureTestListViewAdapter pressureTestListViewAdapter = new PressureTestListViewAdapter(this.getContext());
        pressureListView.setAdapter(pressureTestListViewAdapter);

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        // Input
        Realm.init(this.getContext());
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);

        if(getArguments() != null) {
            OxygenBottle bottle = realm.where(OxygenBottle.class).equalTo("bottleCode", getArguments().getString("BOTTLE_CODE")).findFirst();
            if((bottle == null) || (bottle.getBottleCode().isEmpty())) {

            } else {
                if(bottle.getPressureTest().size() > 0) {
                    for(int idx = 0; idx < bottle.getPressureTest().size(); idx++) {
                        pressureTestListViewAdapter.addItem(bottle.getBottleCode(), bottle.getPressureTest().get(idx).getTestCount(), bottle.getPressureTest().get(idx).getDate());
                    }
                } else {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.pressureTestFrameLayout);
                    ListView listView = view.findViewById(R.id.pressureListView);
                    frameLayout.removeView(listView);

                    TextView textView = new TextView(this.getContext());
                    textView.setText("자료가 없습니다");
                    textView.setTypeface(nanumBarunGothicFontBold);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(20);
                    textView.setGravity(Gravity.CENTER);

                    frameLayout.addView(textView);
                }
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
