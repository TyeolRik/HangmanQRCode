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

import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_PRESSURETEST;

/**
 * Created by Roshita on 2017-12-18.
 */

public class PressureTestListViewAdapter extends BaseAdapter {

    Context mContext;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<PressureTest> listViewItemList = new ArrayList<PressureTest>() ;

    public PressureTestListViewAdapter() {
        // Empty Constructor
    }

    public PressureTestListViewAdapter(Context context) {
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

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pressure_list_item, parent, false);
        }

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(parent.getContext().getAssets(), "fonts/NanumBarunGothic.ttf");

        TextView pressureListItemCount = (TextView) convertView.findViewById(R.id.pressureListItemCount);
        TextView pressureListItemDate  = (TextView) convertView.findViewById(R.id.pressureListItemDate);

        final PressureTest listViewItem = listViewItemList.get(position);

        pressureListItemCount.setText(String.valueOf(listViewItem.getTestCount()) + "차");
        pressureListItemDate .setText(listViewItem.getTestDateInString());
        pressureListItemCount.setTypeface(nanumBarunGothicFontBold);
        pressureListItemDate .setTypeface(nanumBarunGothicFont);

        LinearLayout pressureTestLinearLayout = (LinearLayout) convertView.findViewById(R.id.pressureTestLinearLayout);
        ImageButton pressureDeleteButton = (ImageButton) convertView.findViewById(R.id.pressureDeleteButton);
        ImageButton pressureEditButton = (ImageButton) convertView.findViewById(R.id.pressureEditButton);

        int sp_to_pixel_100sp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, parent.getResources().getDisplayMetrics());

        final Spring imageButtonPopUp = SpringSystem.create().createSpring();
        imageButtonPopUp.addListener(new MapPerformer(pressureTestLinearLayout, View.TRANSLATION_X, 0 , 1, 0, sp_to_pixel_100sp * -1));

        imageButtonPopUp.setEndValue(0);

        pressureTestLinearLayout.setOnClickListener(new View.OnClickListener() {
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

        pressureDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "pressureDeleteButton is Clicked \t\t " + listViewItem.getTestCount() + "\t\t" + listViewItem.getTestDateInString());
                if(mContext instanceof OxygenBottleActivity){
                    imageButtonPopUp.setEndValue(0);
                    Log.d("Check", "Invisible");
                    ((OxygenBottleActivity) mContext).deleteOnGoogleSheets(AppGlobalInstance.WHATTOADD_PRESSURETEST, String.valueOf(listViewItem.getTestCount()), listViewItem.getDate(), position);
                }
            }
        });

        pressureEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick", "pressureEditButton is Clicked \t\t " + listViewItem.getTestCount() + "\t\t" + listViewItem.getTestDateInString());

                Intent openActivity = new Intent(mContext, SettingDialogActivity.class);
                openActivity.putExtra("BottleCode", listViewItem.getBottleCode());
                openActivity.putExtra("WhatToAdd", REQUEST_DIALOG_EDITING_PRESSURETEST);

                openActivity.putExtra("editDate", listViewItem.getDate());
                openActivity.putExtra("editCount", listViewItem.getTestCount());
                openActivity.putExtra("position", position);

                imageButtonPopUp.setEndValue(0);
                Log.d("Check", "Invisible");
                ((OxygenBottleActivity) mContext).startActivityForResult(openActivity, REQUEST_DIALOG_EDITING_PRESSURETEST);
            }
        });

        return convertView;
    }

    public void addItem(String bottleCode, int count, String date) {
        PressureTest item = new PressureTest(bottleCode, count, date);
        listViewItemList.add(item);
    }
}
