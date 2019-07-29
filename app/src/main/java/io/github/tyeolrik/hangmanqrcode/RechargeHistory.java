package io.github.tyeolrik.hangmanqrcode;

import io.realm.RealmObject;

/**
 * Created by Roshita on 2017-12-19.
 */

public class RechargeHistory extends RealmObject {

    private String bottleCode;
    private int rechargeCount;

    private String date;

    public RechargeHistory() {
        // Empty Constructor
    }

    public RechargeHistory(String bottleCode, int rechargeCount, String date) {
        this.bottleCode = bottleCode;
        this.rechargeCount = rechargeCount;
        this.date = date;
    }

    public int getRechargeCount() {
        return rechargeCount;
    }

    public void setRechargeCount(int rechargeCount) {
        this.rechargeCount = rechargeCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBottleCode() {
        return bottleCode;
    }

    public String getTestDateInString() {
        return date.substring(0, 4) + "년 " + date.substring(4, 6) + "월 " + date.substring(6, 8) + "일";
    }
}
