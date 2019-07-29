package io.github.tyeolrik.hangmanqrcode;

import io.realm.RealmObject;

/**
 * Created by Roshita on 2017-12-18.
 */

public class SanitationHistory extends RealmObject {

    private String bottleCode;

    private int testCount;
    private String date;

    public SanitationHistory() {
        // Empty Constructor
    }

    public SanitationHistory(String bottleCode, int testCount, String contents) {
        this.bottleCode = bottleCode;
        this.testCount = testCount;
        this.date = contents;
    }

    public String getBottleCode() {
        return bottleCode;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTestDateInString() {
        return date.substring(0, 4) + "년 " + date.substring(4, 6) + "월 " + date.substring(6, 8) + "일";
    }

}
