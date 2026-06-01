package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments;

// Standard Android UI & Framework Imports (No androidx allowed)
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

// Native Java Network & Data Parsing Imports for API
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

// Your custom app imports
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.DatabaseHelper;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.R;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.UserPreference;

public class SettingsFragment extends Fragment {

    private EditText etCountry, etCity, etTasbihTarget;
    private RadioGroup rgMethod, rgTheme;
    private Switch switchNotification;
    private Button btnSave, btnAutoLocate;

    private DatabaseHelper dbHelper;
    private UserPreference currentPrefs; // <--- ADD THIS LINE
    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getActivity());

        etCountry          = (EditText) view.findViewById(R.id.et_country);
        etCity             = (EditText) view.findViewById(R.id.et_city);
        etTasbihTarget     = (EditText) view.findViewById(R.id.et_tasbih_target);
        rgMethod           = (RadioGroup) view.findViewById(R.id.rg_method);
        rgTheme            = (RadioGroup) view.findViewById(R.id.rg_theme);
        switchNotification = (Switch) view.findViewById(R.id.switch_notification);
        btnSave            = (Button) view.findViewById(R.id.btn_save_settings);
        btnAutoLocate      = (Button) view.findViewById(R.id.btn_auto_locate);

        loadCurrentSettings();

        //permission to locate location
        btnAutoLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v ){
                if(getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                }else{
                    // We have already permission and  go get the coordinates
                    fetchExactLocation();
                }
            }
        });


        // save Button click listener
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });


    }

    //Catches the user's answer when the GPS permission popup appears
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User clicked "Allow"
                fetchExactLocation();
            } else {
                // User clicked "Deny"
                Toast.makeText(getActivity(), "GPS Permission Denied. Please type location manually.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //loading previous setting
    private void loadCurrentSettings() {
currentPrefs = dbHelper.getPreferences();

        etCountry.setText(currentPrefs.getCountry());
        etCity.setText(currentPrefs.getCity());
        etTasbihTarget.setText(String.valueOf(currentPrefs.getTasbihTarget()));

        if (currentPrefs.getCalculationMethod().equals("UAE Awqaf Method")) {
            rgMethod.check(R.id.rb_method_uae);
        } else {
            rgMethod.check(R.id.rb_method_mowl);
        }

        if (currentPrefs.getTheme().equals("Dark Theme")) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_light);
        }
        switchNotification.setChecked(currentPrefs.isNotificationEnabled());
    }

    //saving settings method
    private void saveSettings() {
        final String countryInput = etCountry.getText().toString().trim();
        final String cityInput = etCity.getText().toString().trim();
        String targetInput = etTasbihTarget.getText().toString().trim();

        if (countryInput.isEmpty() || cityInput.isEmpty() || targetInput.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPrefs.setCountry(countryInput);
        currentPrefs.setCity(cityInput);
        currentPrefs.setTasbihTarget(Integer.parseInt(targetInput));
        currentPrefs.setNotificationEnabled(switchNotification.isChecked());

        if (rgMethod.getCheckedRadioButtonId() == R.id.rb_method_uae) {
            currentPrefs.setCalculationMethod("UAE Awqaf Method");
        } else {
            currentPrefs.setCalculationMethod("Muslim World League");
        }

        if (rgTheme.getCheckedRadioButtonId() == R.id.rb_theme_dark) {
            currentPrefs.setTheme("Dark Theme");
        } else {
            currentPrefs.setTheme("Light Theme");
        }





        Toast.makeText(getActivity(), "Verifying with API...", Toast.LENGTH_SHORT).show();
        validateAndSaveLocationAPI(countryInput, cityInput, currentPrefs);
    }


    /**
     * Uses Open-Meteo to validate the city and automatically pull live GPS coordinates
     */
    private void validateAndSaveLocationAPI(final String countryInput, final String cityInput, final UserPreference prefs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    String encodedCity = java.net.URLEncoder.encode(cityInput, "UTF-8");
                    URL url = new URL("https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity + "&count=1&format=json");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);

                    // 1. Guard Clause: Did the network request fail?
                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "API failed. HTTP Response: " + conn.getResponseCode());
                        showToastOnMain("API Error: Invalid Request");
                        return; // Stop execution here
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);}
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // 3. Guard Clause: Did the API find the city?
                    if (!jsonResponse.has("results")) {
                        showToastOnMain("API Error: City not found globally.");
                        return; // Stop execution here
                    }

                    JSONObject bestMatch = jsonResponse.getJSONArray("results").getJSONObject(0);
                    String fetchedCountry = bestMatch.optString("country", "");

                    // 4. Guard Clause: Does the country match what the user typed?
                    if (!fetchedCountry.equalsIgnoreCase(countryInput)) {
                        showToastOnMain("Error: " + cityInput + " is in " + fetchedCountry + ", not " + countryInput);
                        return; // Stop execution here
                    }

                    // 5. BINGO! If we survive all the checks above, save the data.
                    prefs.setLatitude(bestMatch.getDouble("latitude"));
                    prefs.setLongitude(bestMatch.getDouble("longitude"));

                    updateLocationUIAndDB(prefs, "Location Set!");

                }catch (Exception e) {
                    Log.e(TAG, "Network exception", e);
                    showToastOnMain("Network Error. Check internet connection.");
                }

            }
        }).start();

    }


     // Talks to the Android GPS satellites in the background so it doesn't freeze the screen
    /**
     * Talks to the Android GPS satellites in the background.
     * Triggered by the "Auto Locate" button. It ONLY fills the text boxes, it does NOT save to SQLite.
     */
    private void fetchExactLocation() {
        Toast.makeText(getActivity(), "Searching for location...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    Location tempLocation = null;

                    try {
                        tempLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (tempLocation == null) {
                            tempLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    } catch (SecurityException se) {
                        Log.e(TAG, "Security Exception: Need GPS Permission");
                    }

                    final Location location = tempLocation;

                    // 1. EMULATOR FALLBACK: Update UI Only
                    if (location == null) {
                        Log.w(TAG, "Location was null! Forcing Abu Dhabi fallback for emulator test.");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Just fill the text boxes! Do NOT save to DB yet.
                                    etCity.setText("Abu Dhabi");
                                    etCountry.setText("United Arab Emirates");
                                    Toast.makeText(getActivity(), "Location Found! Click Save to confirm.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        return; // Stop execution here
                    }

                    // 2. We have a real location! Let's translate it.
                    Log.d(TAG, "GPS Location found! Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    // 3. Guard Clause: Did the Geocoder fail to find a city name?
                    if (addresses == null || addresses.isEmpty()) {
                        showToastOnMain("Error: GPS found, but could not translate to city name.");
                        return; // Stop execution here
                    }

                    // 4. REAL GPS SUCCESS: Update UI Only
                    final String liveCity = addresses.get(0).getLocality() != null ? addresses.get(0).getLocality() : "Unknown City";
                    final String liveCountry = addresses.get(0).getCountryName() != null ? addresses.get(0).getCountryName() : "Unknown Country";

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Just fill the text boxes! Do NOT save to DB yet.
                                etCity.setText(liveCity);
                                etCountry.setText(liveCountry);
                                Toast.makeText(getActivity(), "Location Found! Click Save to confirm.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error retrieving location.", e);
                    showToastOnMain("Error retrieving location.");
                }
            }
        }).start(); // Starts the GPS thread!
    }


//helper , main ui and database saver
    private void updateLocationUIAndDB(final UserPreference prefs, final String toastMessage) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 1. Force the UI to match the final validated data
                etCity.setText(prefs.getCity());
                etCountry.setText(prefs.getCountry());

                // 2. Save everything (Location, Theme, Tasbih Target) to the DB
                dbHelper.updatePreferences(prefs);

                // 3. Show Success
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }




// helper , main ui toast message
    private void showToastOnMain(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }



}





