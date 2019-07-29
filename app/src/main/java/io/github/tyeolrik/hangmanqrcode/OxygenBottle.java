package io.github.tyeolrik.hangmanqrcode;

import android.util.Log;

import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Roshita on 2017-12-14.
 */

public class OxygenBottle extends RealmObject {

    @PrimaryKey
    private String bottleCode;                                      // 용기번호

    private String division;                                        // 부서명
    private String manufacturedDate;                                // 제조일
    private String expiryDate;                                      // 사용기한
    private String expectedPressureTestDate;                        // 압력검사예정일
    private String lastPressureTestDate;                            // 최종압력검사일

    private RealmList<PressureTest> pressureTest;                   // 내압검사이력
    private RealmList<SanitationHistory> sanitationHistory;         // 위생검사이력
    private RealmList<RepairHistory> repairHistorys;                // 수리내역
    private RealmList<RechargeHistory> rechargeHistory;             // 충전내역

    public OxygenBottle() {
        // Empty Constructor which needs for compiler.
    }

    public OxygenBottle(String bottleCode, String division, String manufacturedDate, String expiryDate, String expectedPressureTestDate, String lastPressureTestDate) {

        setBottleCode(bottleCode);
        setDivision(division);
        setManufacturedDate(manufacturedDate);
        setExpiryDate(expiryDate);
        setExpectedPressureTestDate(expectedPressureTestDate);
        setLastPressureTestDate(lastPressureTestDate);

        pressureTest            = new RealmList<PressureTest>();
        sanitationHistory       = new RealmList<SanitationHistory>();
        repairHistorys          = new RealmList<RepairHistory>();
        rechargeHistory         = new RealmList<RechargeHistory>();
    }

    public String getBottleCode() {
        return bottleCode;
    }

    public void setBottleCode(String bottleCode) {
        this.bottleCode = bottleCode;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getManufacturedDate() {
        return manufacturedDate;
    }

    public void setManufacturedDate(String manufacturedDate) {
        this.manufacturedDate = getFormattedDate(manufacturedDate);
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = getFormattedDate(expiryDate);
    }

    public String getExpectedPressureTestDate() {
        return expectedPressureTestDate;
    }

    public void setExpectedPressureTestDate(String expectedPressureTestDate) {
        this.expectedPressureTestDate = getFormattedDate(expectedPressureTestDate);
    }

    public String getLastPressureTestDate() {
        return lastPressureTestDate;
    }

    public void setLastPressureTestDate(String lastPressureTestDate) {
        this.lastPressureTestDate = getFormattedDate(lastPressureTestDate);
    }

    public RealmList<PressureTest> getPressureTest() {
        return pressureTest;
    }

    public RealmList<SanitationHistory> getSanitationHistory() {
        return sanitationHistory;
    }

    public RealmList<RepairHistory> getRepairHistorys() {
        return repairHistorys;
    }

    public RealmList<RechargeHistory> getRechargeHistory() {
        return rechargeHistory;
    }

    // YYYYMMDD 형식으로 return
    public String getFormattedDate(String input) {
        if(input.contains("/")) { // MM/DD/YYYY
            String[] parse = input.split("/");
            String result = String.format(Locale.KOREA, "%04d%02d%02d", Integer.valueOf(parse[2]), Integer.valueOf(parse[0]), Integer.valueOf(parse[1]));
            Log.d("DateFormatter", result);
            return result;
        } else if(input.contains(".")) { // YYYY. MM. DD.
            String[] parse = input.split(".");
            String result = String.format(Locale.KOREA, "%04d%02d%02d", Integer.valueOf(parse[0]), Integer.valueOf(parse[1]), Integer.valueOf(parse[2]));
            Log.d("DateFormatter", result);
            return result;
        } else {    // YYYYMMDD
            return input;
        }
    }

    public void toStringInLog() {
        Log.d("OxygenBottle", "Name: " + getBottleCode());

    }
}
