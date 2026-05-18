package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.PrayerTimeFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.QiblaFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.SettingsFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.TasbihFragment;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    private PrayerTimeFragment prayerTimesFragment;
    private QiblaFragment qiblaFragment;
    private TasbihFragment tasbihFragment;
    private SettingsFragment settingsFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        prayerTimesFragment = new PrayerTimeFragment();
        qiblaFragment = new QiblaFragment();
        tasbihFragment = new TasbihFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment);
            ft.add(R.id.fragment_container, tasbihFragment, "tasbih").hide(tasbihFragment);
            ft.add(R.id.fragment_container, qiblaFragment, "qibla").hide(qiblaFragment);
            ft.add(R.id.fragment_container, prayerTimesFragment, "prayer");

            ft.commit();
            activeFragment = prayerTimesFragment;
        }
    }

    public void onBottomNavClicked(View v) {
        int id = v.getId();

        if (id == R.id.nav_prayer) {
            switchFragment(prayerTimesFragment);
        } else if (id == R.id.nav_qibla) {
            switchFragment(qiblaFragment);
        } else if (id == R.id.nav_tasbih) {
            switchFragment(tasbihFragment);
        } else if (id == R.id.nav_settings) {
            switchFragment(settingsFragment);
        }
    }

    private void switchFragment(Fragment target) {
        if (target == activeFragment) {
            return;
        }

        fragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }
}