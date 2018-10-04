package com.fabiosomaglia.onlinebookstore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.fabiosomaglia.onlinebookstore.fragments.PrestitiAttiviFragment;
import com.fabiosomaglia.onlinebookstore.fragments.PrestitiPassatiFragment;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    String[] tabarray = new String[]{"Prestiti Attivi", "Prestiti Passati"};
    Integer tabnumber = 2;

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public CharSequence getPageTitle(int position) {
        return tabarray[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                PrestitiAttiviFragment attiviFragment = new PrestitiAttiviFragment();
                return attiviFragment;
            case 1:
                PrestitiPassatiFragment passatiFragment = new PrestitiPassatiFragment();
                return passatiFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabnumber;
    }
}