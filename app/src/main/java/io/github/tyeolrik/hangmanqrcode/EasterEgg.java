package io.github.tyeolrik.hangmanqrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EasterEgg extends Activity {

    @BindView(R.id.myNameKor)
    TextView myNameKor;
    @BindView(R.id.myNameEng)
    TextView myNameEng;
    @BindView(R.id.contactUS)
    TextView contactUS;
    @BindView(R.id.tyeolRik_Character)
    ImageView tyeolRik_Character;
    @BindView(R.id.easterEgg_Slogan)
    TextView easterEgg_Slogan;
    @BindView(R.id.emailImageView)
    ImageView emailImageView;
    @BindView(R.id.githubImageView)
    ImageView githubImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easter_egg);
        ButterKnife.bind(this);

        Typeface NanumBarunGothicBold = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothicBold.ttf");
        Typeface NanumBarunGothic = Typeface.createFromAsset(this.getAssets(), "fonts/NanumBarunGothic.ttf");

        myNameKor.setTypeface(NanumBarunGothicBold);
        myNameEng.setTypeface(NanumBarunGothic);
        contactUS.setTypeface(NanumBarunGothicBold);
        easterEgg_Slogan.setTypeface(NanumBarunGothic);

    }

    @OnClick(R.id.homepageImageView)
    public void onClickedHomepageImageView() {
        Intent gotoMyHomePage = new Intent(Intent.ACTION_VIEW, Uri.parse("https://TyeolRik.github.io/"));
        startActivity(gotoMyHomePage);
    }

    @OnClick(R.id.emailImageView)
    public void onClickedEmailImageView() {
        Intent sendEmail = new Intent(Intent.ACTION_SEND);
        sendEmail.setType("plain/text");
        // email setting 배열로 해놔서 복수 발송 가능
        String[] address = {"kino6147@naver.com"};
        sendEmail.putExtra(Intent.EXTRA_EMAIL, address);
        sendEmail.putExtra(Intent.EXTRA_SUBJECT,"제목예시 : 항만공병관리시스템 개발자, TyeolRik에게");
        sendEmail.putExtra(Intent.EXTRA_TEXT,"내용을 적어주세요!\n");
        startActivity(sendEmail);

    }

    @OnClick(R.id.githubImageView)
    public void onClickedGithubImageView() {
        Intent gotoMyGithub = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tyeolrik"));
        startActivity(gotoMyGithub);
    }
}
