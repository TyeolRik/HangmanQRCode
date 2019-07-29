package io.github.tyeolrik.hangmanqrcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tumblr.backboard.performer.MapPerformer;

import java.util.ArrayList;

import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_REPAIRHISTORY;

/**
 * Created by Roshita on 2017-12-18.
 */

public class RepairHistoryListViewAdapter extends BaseAdapter {

    Context mContext;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<RepairHistory> listViewItemList = new ArrayList<RepairHistory>() ;

    public RepairHistoryListViewAdapter() {
        // Empty Constructor
    }

    public RepairHistoryListViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // inflate 하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.repair_list_item, parent, false);
        }

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        TextView repairListItemDate = (TextView) convertView.findViewById(R.id.repairListItemDate);
        TextView repairListItemContents  = (TextView) convertView.findViewById(R.id.repairListItemContents);

        final RepairHistory listViewItem = listViewItemList.get(position);

        repairListItemDate          .setText(listViewItem.getDate());
        repairListItemContents      .setText(listViewItem.getContents());
        repairListItemDate          .setTypeface(nanumBarunGothicFontBold);
        repairListItemContents      .setTypeface(nanumBarunGothicFont);

        LinearLayout repairHistoryLinearLayout = (LinearLayout) convertView.findViewById(R.id.repairHistoryLinearLayout);
        ImageButton repairHistoryDeleteButton = (ImageButton) convertView.findViewById(R.id.repairHistoryDeleteButton);
        ImageButton repairHistoryEditButton = (ImageButton) convertView.findViewById(R.id.repairHistoryEditButton);

        int sp_to_pixel_100sp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, parent.getResources().getDisplayMetrics());

        final Spring imageButtonPopUp = SpringSystem.create().createSpring();
        imageButtonPopUp.addListener(new MapPerformer(repairHistoryLinearLayout, View.TRANSLATION_X, 0 , 1, 0, sp_to_pixel_100sp * -1));

        imageButtonPopUp.setEndValue(0);

        repairHistoryLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageButtonPopUp.getEndValue() == 0) {
                    imageButtonPopUp.setEndValue(1);
                    Log.d("Check", "Visible");
                } else {
                    imageButtonPopUp.setEndValue(0);
                    Log.d("Check", "Invisible");
                }
            }
        });

        repairHistoryDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "repairHistoryDeleteButton is Clicked \t\t " + listViewItem.getContents() + "\t\t" + listViewItem.getDate());
                if(mContext instanceof OxygenBottleActivity){
                    imageButtonPopUp.setEndValue(0);
                    Log.d("Check", "Invisible");
                    ((OxygenBottleActivity) mContext).deleteOnGoogleSheets(AppGlobalInstance.WHATTOADD_REPAIRHISTORY, String.valueOf(listViewItem.getContents()), listViewItem.getDate(), position);
                }
            }
        });

        repairHistoryEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "repairHistoryEditButton is Clicked \t\t " + listViewItem.getContents() + "\t\t" + listViewItem.getDate());

                Intent openActivity = new Intent(mContext, SettingDialogActivity.class);
                openActivity.putExtra("BottleCode", listViewItem.getBottleCode());
                openActivity.putExtra("WhatToAdd", REQUEST_DIALOG_EDITING_REPAIRHISTORY);

                openActivity.putExtra("editDate", listViewItem.getDate());
                openActivity.putExtra("editContentBefore", listViewItem.getContents());
                openActivity.putExtra("position", position);

                imageButtonPopUp.setEndValue(0);
                Log.d("Check", "Invisible");
                ((OxygenBottleActivity) mContext).startActivityForResult(openActivity, REQUEST_DIALOG_EDITING_REPAIRHISTORY);
            }
        });

        return convertView;
    }

    public void addItem(String bottleCode, String date, String contents) {
        RepairHistory item = new RepairHistory(bottleCode, date, contents);
        listViewItemList.add(item);
    }
}
