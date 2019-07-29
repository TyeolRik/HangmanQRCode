package io.github.tyeolrik.hangmanqrcode;

import io.realm.RealmConfiguration;

/**
 * Created by Roshita on 2017-12-14.
 */

public class AppGlobalInstance {

    public static final int WHATTOADD_PRESSURETEST = 710;
    public static final int WHATTOADD_SANITATIONHISTORY = 720;
    public static final int WHATTOADD_REPAIRHISTORY = 730;
    public static final int WHATTOADD_RECHARGEHISTORY = 740;

    public static final int WHATTOADD_EDIT_PRESSURETEST = 810;
    public static final int WHATTOADD_EDIT_SANITATIONHISTORY = 820;
    public static final int WHATTOADD_EDIT_REPAIRHISTORY = 830;
    public static final int WHATTOADD_EDIT_RECHARGEHISTORY = 840;

    public static final int REQUEST_DIALOG_EDITING_PRESSURETEST = 8100;
    public static final int REQUEST_DIALOG_EDITING_SANITATIONHISTORY = 8200;
    public static final int REQUEST_DIALOG_EDITING_REPAIRHISTORY = 8300;
    public static final int REQUEST_DIALOG_EDITING_RECHARGEHISTORY = 8400;

    public static int[] FLOATING_ACTION_BUTTON_MAIN_COORDINATE = {0, 0};        // (X, Y)

    public static RealmConfiguration USER_SETTING = new RealmConfiguration.Builder()
                                                    .name("hangman_mydata.realm")
                                                    .schemaVersion(1)
                                                    .deleteRealmIfMigrationNeeded()
                                                    .build();

    public static RealmConfiguration BOTTLE_INFORMATION = new RealmConfiguration.Builder()
            .name("bottle_information.realm")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build();

}
