package io.github.tyeolrik.hangmanqrcode;


import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_AreYouFirstUserAsk#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_AreYouFirstUserAsk extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Variable
    // Fragment views
    @BindView(R.id.tipBG)
    ConstraintLayout tipBG;
    @BindView(R.id.areYouFirstTextView)
    TextView areYouFirstTextView;
    @BindView(R.id.areYouFirstSubTitle)
    TextView areYouFirstSubTitle;
    @BindView(R.id.firstUserNoButton)
    Button firstUserNoButton;
    @BindView(R.id.firstUserYesButton)
    Button firstUserYesButton;


    public Fragment_AreYouFirstUserAsk() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_AreYouFirstUserAsk.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_AreYouFirstUserAsk newInstance(String param1, String param2) {
        Fragment_AreYouFirstUserAsk fragment = new Fragment_AreYouFirstUserAsk();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_fragment__are_you_first_user_ask, container, false);
        ButterKnife.bind(this, view);

        Typeface NanumBarunGothic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunGothic.ttf");
        areYouFirstTextView.setTypeface(NanumBarunGothic);
        areYouFirstSubTitle.setTypeface(NanumBarunGothic);
        firstUserNoButton.setTypeface(NanumBarunGothic);
        firstUserYesButton.setTypeface(NanumBarunGothic);

        Animation textAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.tip_text_flow);
        final Animation textAnimTopToBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.tip_text_flow_top_to_bottom);

        textAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                areYouFirstSubTitle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                areYouFirstSubTitle.setAnimation(textAnimTopToBottom);
                areYouFirstSubTitle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        areYouFirstTextView.setAnimation(textAnim);

        return view;
    }


    @OnClick(R.id.firstUserYesButton)
    public void firstUserTipShow() {
        // 처음 사용자 팁 시작하기
        Log.d("Current Status", "On Clicked firstUserYesButton");
        getFragmentManager().beginTransaction().replace(R.id.tipConstraint, new Fragment_HowToUse()).commit();
    }

    @OnClick(R.id.firstUserNoButton)
    public void firstUserTipHide() {
        // 처음 사용자 팁 숨기기
        Log.d("Current Status", "On Clicked firstUserNoButton");
        Realm realm = Realm.getInstance(AppGlobalInstance.USER_SETTING);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PersonalData.class).findFirst().setFirstUser(false);
            }
        });
        getFragmentManager().beginTransaction().remove(this).commit();
        // getActivity().getFragmentManager().popBackStack();
    }
}
