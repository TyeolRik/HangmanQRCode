package io.github.tyeolrik.hangmanqrcode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Roshita on 2017-12-14.
 */

public class OxygenBottlePagerAdapter extends FragmentPagerAdapter {

    private static int PAGE_NUMBER = 5;
    private static Bundle bundle;

    public OxygenBottlePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public OxygenBottlePagerAdapter(FragmentManager fm, Bundle inputBundle) {
        super(fm);
        this.bundle = inputBundle;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return Fragment_Information.newInstance(this.bundle);
            case 1:
                return Fragment_PressureTest.newInstance(this.bundle);
            case 2:
                return Fragment_SanitationHistory.newInstance(this.bundle);
            case 3:
                return Fragment_RepairHistory.newInstance(this.bundle);
            case 4:
                return Fragment_RechargeHistory.newInstance(this.bundle);
            default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "정보";
            case 1:
                return "내압";
            case 2:
                return "위생";
            case 3:
                return "수리";
            case 4:
                return "충전";
            default:
                return null;
        }
    }
}
