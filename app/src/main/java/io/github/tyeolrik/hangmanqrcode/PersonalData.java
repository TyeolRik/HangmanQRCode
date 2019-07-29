package io.github.tyeolrik.hangmanqrcode;

import io.realm.RealmObject;

/**
 * Created by Roshita on 2017-11-29.
 */

public class PersonalData extends RealmObject {

    private boolean isFirstUser;

    public boolean isFirstUser() {
        return isFirstUser;
    }

    public void setFirstUser(boolean firstUser) {
        isFirstUser = firstUser;
    }
}
