package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator;

// Standard Native Android Imports
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.SettingsFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.R;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.PrayerTimeFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.QiblaFragment;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments.TasbihFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This loads your new 2x2 Grid Dashboard XML
        setContentView(R.layout.activity_main);
    }

    /**
     * This method is triggered perfectly by the android:onClick="onBottomNavClicked"
     * attribute you placed on your 4 dashboard cards.
     */
    public void onBottomNavClicked(View view) {
        Fragment selectedFragment = null;

        // Check which dashboard card the user tapped
        int id = view.getId();
        if (id == R.id.nav_prayer) {
            selectedFragment = new PrayerTimeFragment(); // Uncomment when ready
        } else if (id == R.id.nav_qibla) {
             selectedFragment = new QiblaFragment(); // Uncomment when ready
        } else if (id == R.id.nav_tasbih) {
             selectedFragment = new TasbihFragment(); // Uncomment when ready
        } else if (id == R.id.nav_settings) {
            // Initialize the Settings Fragment we just built!
            selectedFragment = new SettingsFragment();
        }

        // If a valid fragment was selected, launch it over the dashboard
        if (selectedFragment != null) {
            getFragmentManager().beginTransaction()
                    // Replace the entire system window content with the Fragment
                    .replace(android.R.id.content, selectedFragment)
                    // This is CRITICAL: It allows the phone's physical "Back" arrow
                    // to close the fragment and reveal the dashboard again
                    .addToBackStack(null)
                    .commit();
        }
    }
}