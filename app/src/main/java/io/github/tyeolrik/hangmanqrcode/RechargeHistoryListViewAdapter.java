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

import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_RECHARGEHISTORY;

/**
 * Created by Roshita on 2017-12-19.
 */

public class RechargeHistoryListViewAdapter extends BaseAdapter {

    Context mContext;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<RechargeHistory> listViewItemList = new ArrayList<RechargeHistory>() ;

    public RechargeHistoryListViewAdapter() {
        // Empty Constructor
    }

    public RechargeHistoryListViewAdapter(Context context) {
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
            convertView = inflater.inflate(R.layout.recharge_list_item, parent, false);
        }

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        TextView rechargeHistoryItemCount = (TextView) convertView.findViewById(R.id.rechargeHistoryItemCount);
        TextView rechargeHistoryItemDate  = (TextView) convertView.findViewById(R.id.rechargeHistoryItemDate);

        final RechargeHistory listViewItem = listViewItemList.get(position);

        rechargeHistoryItemCount.setText(String.valueOf(listViewItem.getRechargeCount()) + "회");       // 0차가 아니라 1차로 보여주기 위해서
        rechargeHistoryItemDate .setText(listViewItem.getTestDateInString());
        rechargeHistoryItemCount.setTypeface(nanumBarunGothicFontBold);
        rechargeHistoryItemDate .setTypeface(nanumBarunGothicFont);


        LinearLayout rechargeLinearLayout = (LinearLayout) convertView.findViewById(R.id.rechargeLinearLayout);
        ImageButton rechargeDeleteButton = (ImageButton) convertView.findViewById(R.id.rechargeDeleteButton);
        ImageButton rechargeEditButton = (ImageButton) convertView.findViewById(R.id.rechargeEditButton);

        int sp_to_pixel_100sp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, parent.getResources().getDisplayMetrics());

        final Spring imageButtonPopUp = SpringSystem.create().createSpring();
        imageButtonPopUp.addListener(new MapPerformer(rechargeLinearLayout, View.TRANSLATION_X, 0 , 1, 0, sp_to_pixel_100sp * -1));

        imageButtonPopUp.setEndValue(0);

        rechargeLinearLayout.setOnClickListener(new View.OnClickListener() {
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

        rechargeDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "rechargeDeleteButton is Clicked \t\t " + listViewItem.getRechargeCount() + "\t\t" + listViewItem.getDate());
                if(mContext instanceof OxygenBottleActivity){
                    imageButtonPopUp.setEndValue(0);
                    Log.d("Check", "Invisible");
                    ((OxygenBottleActivity) mContext).deleteOnGoogleSheets(AppGlobalInstance.WHATTOADD_RECHARGEHISTORY, String.valueOf(listViewItem.getRechargeCount()), listViewItem.getDate(), position);
                }
            }
        });

        rechargeEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "rechargeEditButton is Clicked \t\t " + listViewItem.getRechargeCount() + "\t\t" + listViewItem.getDate());

                Intent openActivity = new Intent(mContext, SettingDialogActivity.class);
                openActivity.putExtra("BottleCode", listViewItem.getBottleCode());
                openActivity.putExtra("WhatToAdd", REQUEST_DIALOG_EDITING_RECHARGEHISTORY);

                openActivity.putExtra("editDate", listViewItem.getDate());
                openActivity.putExtra("editCount", listViewItem.getRechargeCount());
                openActivity.putExtra("position", position);

                imageButtonPopUp.setEndValue(0);
                Log.d("Check", "Invisible");
                ((OxygenBottleActivity) mContext).startActivityForResult(openActivity, REQUEST_DIALOG_EDITING_RECHARGEHISTORY);
            }
        });

        return convertView;
    }

    public void addItem(String bottleCode, int rechargeCount, String date) {
        RechargeHistory rechargeHistory = new RechargeHistory(bottleCode, rechargeCount, date);
        listViewItemList.add(rechargeHistory);
    }

}
