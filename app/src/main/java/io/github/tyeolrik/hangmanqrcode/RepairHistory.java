package io.github.tyeolrik.hangmanqrcode;

import io.realm.RealmObject;

/**
 * Created by Roshita on 2017-12-15.
 */

public class RepairHistory extends RealmObject {

    String bottleCode;

    String date;
    String contents;

    public RepairHistory() {
        // Empty Constructor.
    }

    public RepairHistory(String bottleCode, String date, String contents) {
        this.bottleCode = bottleCode;
        this.date = date;
        this.contents = contents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getBottleCode() {
        return bottleCode;
    }
}
