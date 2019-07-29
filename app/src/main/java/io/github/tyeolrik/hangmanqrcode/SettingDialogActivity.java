package io.github.tyeolrik.hangmanqrcode;

import android.animation.Animator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tumblr.backboard.performer.MapPerformer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;

import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.FLOATING_ACTION_BUTTON_MAIN_COORDINATE;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_PRESSURETEST;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_RECHARGEHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.REQUEST_DIALOG_EDITING_SANITATIONHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_EDIT_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_PRESSURETEST;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_RECHARGEHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_REPAIRHISTORY;
import static io.github.tyeolrik.hangmanqrcode.AppGlobalInstance.WHATTOADD_SANITATIONHISTORY;

public class SettingDialogActivity extends AppCompatActivity {

    @BindView(R.id.settingDialogLayout)
    ConstraintLayout settingDialogLayout;
    @BindView(R.id.settingDialogContent)
    ConstraintLayout settingDialogContent;
    @BindView(R.id.settingTitle)
    TextView settingTitle;
    @BindView(R.id.closeButton)
    ImageButton closeButton;

    @BindView(R.id.textView_Calendar)
    TextView textView_Calendar;
    @BindView(R.id.textView_Counter)
    TextView textView_Counter;
    @BindView(R.id.editText_RepairContents)
    EditText editText_RepairContents;

    @BindView(R.id.question1_when)
    TextView question1_when;
    @BindView(R.id.question2_howMany)
    TextView question2_howMany;

    @BindView(R.id.datePickerFrameLayout)
    FrameLayout datePickerFrameLayout;
    @BindView(R.id.settingDatePicker)
    DatePicker settingDatePicker;
    @BindView(R.id.numberPickerFrameLayout)
    FrameLayout numberPickerFrameLayout;
    @BindView(R.id.settingNumberPicker)
    NumberPicker settingNumberPicker;

    Unbinder unbinder;
    Intent writeIntent;
    int WhatToAdd;

    LocalDate today;
    LocalDate select;

    Realm realm;
    OxygenBottle bottle;

    private static String bottleCode;
    private static String TEXTVIEW_settingTitle_TITLE;

    private static final int CIRCULAR_REVEAL_ANIMATION_DURATION = 200;

    private static final int RIGHT_SIDE = 1;
    private static final int CENTER_SIDE = 0;

    private LocalDate RECOMMEND_DATE;           // 추천 날짜
    private int RECOMMEND_COUNT;                // 추천 횟수
    private LocalDate outputDate;               // 결과 날짜
    private int outputCount;                    // 결과 횟수

    // Spring Animation
    Spring datePickerSpring;
    Spring numberPickerSpring;

    // For Editing
    String previousDate;

    String contentsBefore;
    String editDateBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);

        setContentView(R.layout.activity_setting_dialog);
        unbinder = ButterKnife.bind(this);

        if (savedInstanceState == null) {
            settingDialogLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = settingDialogLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            settingDialogLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            settingDialogLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }

        writeIntent = getIntent();
        bottleCode = getIntent().getStringExtra("BottleCode");
        WhatToAdd = getIntent().getIntExtra("WhatToAdd", -1);

        Realm.init(this);
        realm = Realm.getInstance(AppGlobalInstance.BOTTLE_INFORMATION);
        bottle = realm.where(OxygenBottle.class).equalTo("bottleCode", bottleCode).findFirstAsync();

        Typeface nanumBarunGothicFontBold   = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface nanumBarunGothicFont       = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothic.ttf");

        settingTitle.setTypeface(nanumBarunGothicFontBold);

        datePickerSpring = SpringSystem.create().createSpring();
        numberPickerSpring = SpringSystem.create().createSpring();

        datePickerFrameLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryFireFighting));

        today = new LocalDate(new Date());
        select = today;     // Initialize
        settingDatePicker.init(today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth(), new DatePicker.OnDateChangedListener() { // MonthOfYear - 1 :: datePicker는 1월이 0월
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
                select = new LocalDate(year, month + 1, dayOfMonth);
                textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                Log.d("DateChanged", select.toString());
            }
        });

        // NumberPicker
        settingNumberPicker.setMaxValue(20);
        settingNumberPicker.setMinValue(1);

        settingNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                textView_Counter.setText(String.valueOf(numberPicker.getValue()) + "번째");
            }
        });

        RECOMMEND_DATE = new LocalDate(new Date());
        RECOMMEND_COUNT = 1;

        switch (WhatToAdd) {

            case WHATTOADD_PRESSURETEST:

                TEXTVIEW_settingTitle_TITLE = "내압검사이력 추가";
                question1_when.setText("언제 내압검사를 했나요?");
                question2_howMany.setText("몇 번째 내압검사 인가요?");
                textView_Counter.setVisibility(View.VISIBLE);

                break;

            case REQUEST_DIALOG_EDITING_PRESSURETEST:

                TEXTVIEW_settingTitle_TITLE = "내압검사이력 수정";
                question1_when.setText("언제로 수정할까요?");
                question2_howMany.setText("몇 번째 내압검사 였나요?");
                textView_Counter.setVisibility(View.VISIBLE);

                previousDate = getIntent().getStringExtra("editDate");
                RECOMMEND_DATE = new LocalDate(Integer.valueOf(previousDate.substring(0, 4)), Integer.valueOf(previousDate.substring(4, 6)), Integer.valueOf(previousDate.substring(6, 8)));
                RECOMMEND_COUNT = getIntent().getIntExtra("editCount", -1);
                contentsBefore = String.valueOf(RECOMMEND_COUNT);
                editDateBefore = previousDate;

                if(RECOMMEND_COUNT == -1) {
                    Log.e("ERROR", "Intent ERROR :: -1 received. there is no available intent");
                }

                break;

            case WHATTOADD_SANITATIONHISTORY:

                TEXTVIEW_settingTitle_TITLE = "위생검사이력 추가";
                question1_when.setText("언제 위생검사를 했나요?");
                question2_howMany.setText("몇 번째 위생검사 인가요?");
                textView_Counter.setVisibility(View.VISIBLE);

                break;

            case REQUEST_DIALOG_EDITING_SANITATIONHISTORY:

                TEXTVIEW_settingTitle_TITLE = "위생검사이력 수정";
                question1_when.setText("언제로 수정할까요?");
                question2_howMany.setText("몇 번째 위생검사 였나요?");
                textView_Counter.setVisibility(View.VISIBLE);

                previousDate = getIntent().getStringExtra("editDate");
                RECOMMEND_DATE = new LocalDate(Integer.valueOf(previousDate.substring(0, 4)), Integer.valueOf(previousDate.substring(4, 6)), Integer.valueOf(previousDate.substring(6, 8)));
                RECOMMEND_COUNT = getIntent().getIntExtra("editCount", -1);
                contentsBefore = String.valueOf(RECOMMEND_COUNT);
                editDateBefore = previousDate;

                if(RECOMMEND_COUNT == -1) {
                    Log.e("ERROR", "Intent ERROR :: -1 received. there is no available intent");
                }

                break;

            case WHATTOADD_REPAIRHISTORY:

                TEXTVIEW_settingTitle_TITLE = "수리내역 추가";
                question1_when.setText("언제 수리를 했나요?");
                question2_howMany.setText("무엇을 수리 했나요?");
                editText_RepairContents.setVisibility(View.VISIBLE);

                break;

            case REQUEST_DIALOG_EDITING_REPAIRHISTORY:

                TEXTVIEW_settingTitle_TITLE = "수리내역 수정";
                question1_when.setText("언제로 수정할까요?");
                question2_howMany.setText("무엇을 수리 했나요?");
                editText_RepairContents.setVisibility(View.VISIBLE);

                previousDate = getIntent().getStringExtra("editDate");
                RECOMMEND_DATE = new LocalDate(Integer.valueOf(previousDate.substring(0, 4)), Integer.valueOf(previousDate.substring(4, 6)), Integer.valueOf(previousDate.substring(6, 8)));
                RECOMMEND_COUNT = getIntent().getIntExtra("editCount", -1);
                editDateBefore = previousDate;
                editText_RepairContents.setHint("(수정전) " + getIntent().getStringExtra("editContentBefore"));

                break;

            case WHATTOADD_RECHARGEHISTORY:

                TEXTVIEW_settingTitle_TITLE = "충전내역 추가";
                question1_when.setText("언제 충전을 했나요?");
                question2_howMany.setText("몇 번째 충전 인가요?");
                textView_Counter.setVisibility(View.VISIBLE);

                break;

            case REQUEST_DIALOG_EDITING_RECHARGEHISTORY:

                TEXTVIEW_settingTitle_TITLE = "충전내역 수정";
                question1_when.setText("언제로 수정할까요?");
                question2_howMany.setText("몇 번째 충전이었나요?");
                textView_Counter.setVisibility(View.VISIBLE);

                previousDate = getIntent().getStringExtra("editDate");
                RECOMMEND_DATE = new LocalDate(Integer.valueOf(previousDate.substring(0, 4)), Integer.valueOf(previousDate.substring(4, 6)), Integer.valueOf(previousDate.substring(6, 8)));
                RECOMMEND_COUNT = getIntent().getIntExtra("editCount", -1);
                contentsBefore = String.valueOf(RECOMMEND_COUNT);
                editDateBefore = previousDate;

                if(RECOMMEND_COUNT == -1) {
                    Log.e("ERROR", "Intent ERROR :: -1 received. there is no available intent");
                }

                break;

            default:

                TEXTVIEW_settingTitle_TITLE = "ERROR : 다시 시도";
                Log.e("ERROR", "Failed to get WhatToAdd which is intent");

                break;

        }

        outputDate = RECOMMEND_DATE;
        outputCount = RECOMMEND_COUNT;

        settingTitle.setText(TEXTVIEW_settingTitle_TITLE);

        textView_Calendar.setText(RECOMMEND_DATE.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
        textView_Counter.setText(RECOMMEND_COUNT + "번째");

        /* Handling EditText Focus and Keyboard problem. */
        if ((WhatToAdd == WHATTOADD_REPAIRHISTORY) || (WhatToAdd == WHATTOADD_EDIT_REPAIRHISTORY)) {
            /* Remove Keyboard(Soft Keyboard) when user press outside of EditText. (Except the view that I manually set onClickListener. In this Activity, textView_Calendar) */
            settingDialogLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    editText_RepairContents.clearFocus();
                    return true;
                }
            });

            /* Remove Focus(Exactly Cursor) after pressing Done key in keyboard */
            editText_RepairContents.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_DONE){

                        // Clear Keyboard. Not clearing automatically after setting this listener.
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                        // Clear focus here from edittext
                        editText_RepairContents.clearFocus();
                    }
                    return false;
                }
            });
        }
    }

    private void circularRevealActivity() {

        // int cx = settingDialogLayout.getWidth() / 2;
        // int cy = settingDialogLayout.getHeight() / 2;

        float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], 0, finalRadius);
        circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);

        datePickerSpring.addListener(new MapPerformer(datePickerFrameLayout, View.TRANSLATION_X, 0 , 1, 0, settingDialogLayout.getWidth()));
        numberPickerSpring.addListener(new MapPerformer(numberPickerFrameLayout, View.TRANSLATION_X, 0 , 1, 0, settingDialogLayout.getWidth()));
        numberPickerSpring.setEndValue(RIGHT_SIDE);
        datePickerSpring.setEndValue(RIGHT_SIDE);

        // make the view visible and start the animation
        settingDialogLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    @OnClick(R.id.textView_Calendar)
    public void onClickedCalendarTextView() {

        if((WhatToAdd == WHATTOADD_REPAIRHISTORY) || (WhatToAdd == WHATTOADD_EDIT_REPAIRHISTORY)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            editText_RepairContents.clearFocus();
        }

        // If NumberPicker is at the Center side,
        if(numberPickerSpring.getEndValue() == CENTER_SIDE) {
            numberPickerSpring.setEndValue(RIGHT_SIDE);         // Get OFF!
        }

        datePickerFrameLayout.setVisibility(View.VISIBLE);
        datePickerSpring.setEndValue(CENTER_SIDE);

    }

    @OnClick(R.id.textView_Counter)
    public void onClickedCounterTextView() {

        // If DatePicker is at the Center side,
        if(datePickerSpring.getEndValue() == CENTER_SIDE) {
            datePickerSpring.setEndValue(RIGHT_SIDE);         // Get OFF!
        }

        numberPickerFrameLayout.setVisibility(View.VISIBLE);
        numberPickerSpring.setEndValue(CENTER_SIDE);
        Log.d("SpringCheck", "numberPickerSpring.setEndValue(CENTER_SIDE)");

    }

    @OnClick(R.id.editText_RepairContents)
    public void onClickedEditText() {

    }

    @OnClick(R.id.checkButton)
    public void onClickedCheckButton() {

        switch (WhatToAdd) {

            case WHATTOADD_PRESSURETEST:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            case REQUEST_DIALOG_EDITING_PRESSURETEST:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    writeIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                    writeIntent.putExtra("contentsBefore", contentsBefore);
                    writeIntent.putExtra("editDateBefore", editDateBefore);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            case WHATTOADD_SANITATIONHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            case REQUEST_DIALOG_EDITING_SANITATIONHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    writeIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                    writeIntent.putExtra("contentsBefore", contentsBefore);
                    writeIntent.putExtra("editDateBefore", editDateBefore);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            case WHATTOADD_REPAIRHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE) {
                    // Check whether there is no contents in EditText
                    if(TextUtils.isEmpty(editText_RepairContents.getText().toString().trim())) {
                        Toast.makeText(this, "어떤 수리를 하셨나요?", Toast.LENGTH_SHORT).show();
                    } else {
                        int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                        writeIntent.putExtra("ResultDate", date);
                        writeIntent.putExtra("ResultRepairContents", editText_RepairContents.getText().toString());
                        setResult(Activity.RESULT_OK, writeIntent);
                        // Get Out of Activity.
                        float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                        Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                        circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                        circularReveal.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                settingDialogLayout.setVisibility(View.INVISIBLE);
                                finish();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        circularReveal.start();
                    }

                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                }

                break;

            case REQUEST_DIALOG_EDITING_REPAIRHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE) {
                    // Check whether there is no contents in EditText
                    if(TextUtils.isEmpty(editText_RepairContents.getText().toString().trim())) {
                        Toast.makeText(this, "어떤 수리를 하셨나요?", Toast.LENGTH_SHORT).show();
                    } else {
                        int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                        writeIntent.putExtra("ResultDate", date);
                        writeIntent.putExtra("ResultRepairContents", editText_RepairContents.getText().toString());
                        writeIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                        writeIntent.putExtra("contentsBefore", getIntent().getStringExtra("editContentBefore"));
                        writeIntent.putExtra("editDateBefore", editDateBefore);
                        setResult(Activity.RESULT_OK, writeIntent);
                        // Get Out of Activity.
                        float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                        Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                        circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                        circularReveal.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                settingDialogLayout.setVisibility(View.INVISIBLE);
                                finish();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        circularReveal.start();
                    }

                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                }

                break;

            case WHATTOADD_RECHARGEHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            case REQUEST_DIALOG_EDITING_RECHARGEHISTORY:

                if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {
                    int[] date = {outputDate.getYear(), outputDate.getMonthOfYear(), outputDate.getDayOfMonth()};
                    writeIntent.putExtra("ResultDate", date);
                    writeIntent.putExtra("ResultCount", outputCount);
                    writeIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                    writeIntent.putExtra("contentsBefore", contentsBefore);
                    writeIntent.putExtra("editDateBefore", editDateBefore);
                    setResult(Activity.RESULT_OK, writeIntent);
                    // Get Out of Activity.
                    float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
                    circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            settingDialogLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    circularReveal.start();
                } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
                    datePickerFrameLayout.setVisibility(View.VISIBLE);
                    datePickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    outputDate = select;
                } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
                    numberPickerFrameLayout.setVisibility(View.VISIBLE);
                    numberPickerSpring.setEndValue(RIGHT_SIDE);
                    Log.d("Spring", "Set End Value == RIGHT_SIDE");
                    textView_Counter.setText(settingNumberPicker.getValue() + "번째");
                    outputCount = settingNumberPicker.getValue();
                }

                break;

            default:

                Log.e("ERROR", "onClickedCheckButton - Not Found WhatToAdd");

                break;

        }
    }

    @OnClick(R.id.closeButton)
    public void onClickedCloseButton() {

        if(datePickerSpring.getEndValue() != CENTER_SIDE && numberPickerSpring.getEndValue() != CENTER_SIDE) {  // Exit Activity without Save.
            setResult(Activity.RESULT_CANCELED);
            float finalRadius = Math.max(settingDialogLayout.getWidth(), settingDialogLayout.getHeight());
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(settingDialogLayout, FLOATING_ACTION_BUTTON_MAIN_COORDINATE[0], FLOATING_ACTION_BUTTON_MAIN_COORDINATE[1], finalRadius, 0);
            circularReveal.setDuration(CIRCULAR_REVEAL_ANIMATION_DURATION);
            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    settingDialogLayout.setVisibility(View.INVISIBLE);
                    finish();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            circularReveal.start();
        } else if(datePickerSpring.getEndValue() == CENTER_SIDE){
            datePickerFrameLayout.setVisibility(View.VISIBLE);
            datePickerSpring.setEndValue(RIGHT_SIDE);
            textView_Calendar.setText(RECOMMEND_DATE.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));

            // Initialize DatePicker
            settingDatePicker.init(today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth(), new DatePicker.OnDateChangedListener() { // MonthOfYear - 1 :: datePicker는 1월이 0월
                @Override
                public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
                    select = new LocalDate(year, month + 1, dayOfMonth);
                    textView_Calendar.setText(select.toString(DateTimeFormat.forPattern("YYYY년 MM월 dd일")));
                    Log.d("DateChanged", select.toString());
                }
            });

            Log.d("Spring", "Set End Value == RIGHT_SIDE");
        } else if(numberPickerSpring.getEndValue() == CENTER_SIDE){
            numberPickerFrameLayout.setVisibility(View.VISIBLE);
            numberPickerSpring.setEndValue(RIGHT_SIDE);
            textView_Counter.setText(RECOMMEND_COUNT + "번째");
            settingNumberPicker.setValue(RECOMMEND_COUNT);
            Log.d("Spring", "Set End Value == RIGHT_SIDE");
        }
    }

    public void onClickedSaveButton(View view) {
        int[] date = {select.getYear(), select.getMonthOfYear(), select.getDayOfMonth()};
        String dateFormat = String.format(Locale.KOREA, "%4d%2d%2d", date[0], date[1], date[2]);

        // ERROR occured : You need to use a Theme.AppCompat theme (or descendant) with this activity.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        LayoutInflater inflater = this.getLayoutInflater();

        dialogBuilder.setView(inflater.inflate(R.layout.dialog_confirm_setting, null))
                .setTitle(TEXTVIEW_settingTitle_TITLE)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        NumberPicker numberPicker = findViewById(R.id.confirmDialogNumberPicker);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show(); // ERROR

        writeIntent.putExtra("ResultDate", date);
        setResult(Activity.RESULT_OK, writeIntent);
        finish();
    }

    public void onClickedCancelButton(View view) {
        setResult(Activity.RESULT_CANCELED, writeIntent);
        finish();
    }

    private void getCountByComparingDate(OxygenBottle bottle, int[] date) {

        switch (WhatToAdd) {

            case WHATTOADD_PRESSURETEST:

                TEXTVIEW_settingTitle_TITLE = "내압검사이력 추가";

                break;

            case WHATTOADD_SANITATIONHISTORY:

                TEXTVIEW_settingTitle_TITLE = "위생검사이력 추가";

                break;

            case WHATTOADD_REPAIRHISTORY:

                TEXTVIEW_settingTitle_TITLE = "수리내역 추가";

                break;

            case WHATTOADD_RECHARGEHISTORY:

                TEXTVIEW_settingTitle_TITLE = "충전내역 추가";

                break;

            default:

                TEXTVIEW_settingTitle_TITLE = "ERROR : 다시 시도";
                Log.e("ERROR", "Failed to get WhatToAdd which is intent");

                break;

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
