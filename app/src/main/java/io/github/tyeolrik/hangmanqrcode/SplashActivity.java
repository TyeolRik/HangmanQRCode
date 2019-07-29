package io.github.tyeolrik.hangmanqrcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Roshita on 2017-11-28.
 */

public class SplashActivity extends AppCompatActivity {

    private RealmConfiguration userCfg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Realm Initialization */
        Realm.init(getApplicationContext());

        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);

        finish();
    }
}
