package io.github.tyeolrik.hangmanqrcode;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
public class Fragment_RechargeHistory extends Fragment {

    @BindView(R.id.rechargeHistoryListView)
    ListView rechargeHistoryListView;

    Realm realm;
    Unbinder unbinder;

    public Fragment_RechargeHistory() {
        // Required empty public constructor
    }

    public static Fragment_RechargeHistory newInstance() {
        Bundle args = new Bundle();

        Fragment_RechargeHistory fragment = new Fragment_RechargeHistory();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment_RechargeHistory newInstance(Bundle input) {
        Fragment_RechargeHistory fragment = new Fragment_RechargeHistory();
        Log.d("Arguments", input.toString());
        fragment.setArguments(input);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recharge_history, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Adapter
        RechargeHistoryListViewAdapter rechargeHistoryListViewAdapter = new RechargeHistoryListViewAdapter(this.getContext());
        rechargeHistoryListView.setAdapter(rechargeHistoryListViewAdapter);

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        // Input
        Realm.init(this.getContext());
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);

        if(getArguments() != null) {
            OxygenBottle bottle = realm.where(OxygenBottle.class).equalTo("bottleCode", getArguments().getString("BOTTLE_CODE")).findFirst();
            if((bottle == null) || (TextUtils.isEmpty(bottle.getBottleCode()))) {

            } else {
                if(bottle.getRechargeHistory().size() > 0) {
                    for(int idx = 0; idx < bottle.getRechargeHistory().size(); idx++) {
                        rechargeHistoryListViewAdapter.addItem(bottle.getBottleCode(),bottle.getRechargeHistory().get(idx).getRechargeCount() , bottle.getRechargeHistory().get(idx).getDate());
                    }
                } else {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.rechargeHistoryFrameLayout);
                    ListView listView = view.findViewById(R.id.rechargeHistoryListView);
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
