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
public class Fragment_RepairHistory extends Fragment {

    @BindView(R.id.repairHistoryListView)
    ListView repairHistoryListView;

    Realm realm;
    Unbinder unbinder;

    public Fragment_RepairHistory() {
        // Required empty public constructor
    }

    public static Fragment_RepairHistory newInstance() {
        Bundle args = new Bundle();

        Fragment_RepairHistory fragment = new Fragment_RepairHistory();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment_RepairHistory newInstance(Bundle input) {
        Fragment_RepairHistory fragment = new Fragment_RepairHistory();
        Log.d("Arguments", input.toString());
        fragment.setArguments(input);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repair_history, container, false);
        unbinder = ButterKnife.bind(this, view);

        RepairHistoryListViewAdapter repairHistoryListViewAdapter = new RepairHistoryListViewAdapter(this.getContext());
        repairHistoryListView.setAdapter(repairHistoryListViewAdapter);

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        // Input
        Realm.init(this.getContext());
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);

        if(getArguments() != null) {
            OxygenBottle bottle = realm.where(OxygenBottle.class).equalTo("bottleCode", getArguments().getString("BOTTLE_CODE")).findFirst();
            if((bottle == null) || (bottle.getBottleCode().isEmpty())) {

            } else {
                if(bottle.getRepairHistorys().size() > 0) {
                    for(int idx = 0; idx < bottle.getRepairHistorys().size(); idx++) {
                        RepairHistory history = bottle.getRepairHistorys().get(idx);
                        repairHistoryListViewAdapter.addItem(bottle.getBottleCode(), history.getDate(), history.getContents());
                    }
                } else {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.repairHistoryFrameLayout);
                    ListView listView = view.findViewById(R.id.repairHistoryListView);
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
