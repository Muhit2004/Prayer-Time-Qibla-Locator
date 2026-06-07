package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.SettingsFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.PrayerTimeFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.QiblaFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.TasbihFragment;

public class MainActivity extends Activity {
//main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onBottomNavClicked(View view) {
        Fragment selectedFragment = null;

        // Check which dashboard card the user tapped
        int id = view.getId();
        if (id == R.id.nav_prayer) {
            selectedFragment = new PrayerTimeFragment();
        } else if (id == R.id.nav_qibla) {
             selectedFragment = new QiblaFragment();
        } else if (id == R.id.nav_tasbih) {
             selectedFragment = new TasbihFragment();
        } else if (id == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
        }

        if (selectedFragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, selectedFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}