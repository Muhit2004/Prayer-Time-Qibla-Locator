package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.DatabaseHelper;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.R;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.UserPreference;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrayerTimeFragment extends Fragment {

    //  UI Elements The XML TextViews
    private TextView tvCity, tvDate;
    private TextView tvNextPrayer, tvNextTime, tvCountdown;
    private TextView tvFajr, tvDhuhr, tvAsr, tvMaghrib, tvIsha;

    //  Logic & State Variables
    private DatabaseHelper dbHelper;
    private CountDownTimer countDownTimer;

    // Arrays to store raw 24h timings ("HH:mm") for comparison algorithms
    private String[] prayerNames = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private String[] prayerTimes24h = new String[5];



    // The Wiring
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_prayer_time, container, false);

        dbHelper = new DatabaseHelper(getActivity());

        // Bind Header & Gold Next Prayer Box
        tvCity       = (TextView) view.findViewById(R.id.tv_city);
        tvDate       = (TextView) view.findViewById(R.id.tv_date);
        tvNextPrayer = (TextView) view.findViewById(R.id.tv_next_prayer);
        tvNextTime   = (TextView) view.findViewById(R.id.tv_next_time);
        tvCountdown  = (TextView) view.findViewById(R.id.tv_countdown);

        // Bind Table Layout Rows
        tvFajr       = (TextView) view.findViewById(R.id.tv_fajr_time);
        tvDhuhr      = (TextView) view.findViewById(R.id.tv_dhuhr_time);
        tvAsr        = (TextView) view.findViewById(R.id.tv_asr_time);
        tvMaghrib    = (TextView) view.findViewById(R.id.tv_maghrib_time);
        tvIsha       = (TextView) view.findViewById(R.id.tv_isha_time);

        // Load cached city profile info immediately
        UserPreference prefs = dbHelper.getPreferences();
        tvCity.setText(prefs.getCity());

        // GET Prayer Times from API SERVER IN A BACKGROUND THREAD
        fetchPrayerTimesFromServer();
        return view;
    }

    //  The API Network Call
    private void fetchPrayerTimesFromServer() {
        UserPreference prefs = dbHelper.getPreferences();
        final double lat = prefs.getLatitude();
        final double lon = prefs.getLongitude();
        Log.e("PrayerTimeFragment", "Fetching prayer times for lat: " + lat + ", lon: " + lon);
        //  Get whatever string is saved in the database
        String savedMethodStr = prefs.getCalculationMethod();

        // 2. Translate the text into the correct Aladhan API ID
        int tempMethodId = 8; // Default to UAE Awqaf
        if (savedMethodStr != null) {
            if (savedMethodStr.contains("World League") ) {
                tempMethodId = 3;
            } else if (savedMethodStr.contains("Awqaf") || savedMethodStr.contains("Gulf") ) {
                tempMethodId = 8;
            }
        }

        // Needs to be final to be used inside the background thread
        final int finalMethodId = tempMethodId;

        // Safe async background background context tracking thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 3. Inject the clean integer into the URL
                    String urlString = "https://api.aladhan.com/v1/timings/today?latitude="
                            + lat + "&longitude=" + lon + "&method=" + finalMethodId;

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    final String jsonResponse = response.toString();

                    Log.println(Log.INFO, "PrayerTimeFragment", "API Response: " + jsonResponse);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseJsonAndUpdateUI(jsonResponse);
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Network error. Showing offline backups.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    // ── 6. The JSON Parser ──
    private void parseJsonAndUpdateUI(String jsonString) {
        try {
            JSONObject mainObject = new JSONObject(jsonString);
            JSONObject dataObject = mainObject.getJSONObject("data");
            JSONObject timings    = dataObject.getJSONObject("timings");
            JSONObject dateObj    = dataObject.getJSONObject("date");

            // Extract and map human readable calendar layout date
            String apiDate = dateObj.getString("readable");
            tvDate.setText(apiDate);

            // Extract values into comparison index arrays
            prayerTimes24h[0] = timings.getString("Fajr");
            prayerTimes24h[1] = timings.getString("Dhuhr");
            prayerTimes24h[2] = timings.getString("Asr");
            prayerTimes24h[3] = timings.getString("Maghrib");
            prayerTimes24h[4] = timings.getString("Isha");

            // Format strings from 24-Hour text to 12-Hour display layout blocks
            tvFajr.setText(formatTo12Hour(prayerTimes24h[0]));
            tvDhuhr.setText(formatTo12Hour(prayerTimes24h[1]));
            tvAsr.setText(formatTo12Hour(prayerTimes24h[2]));
            tvMaghrib.setText(formatTo12Hour(prayerTimes24h[3]));
            tvIsha.setText(formatTo12Hour(prayerTimes24h[4]));

            // Fire real time logical clock sequencing handlers
            setupNextPrayerAndCountdown();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "JSON Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }

    //  24h to 12h Time Formatting Helper
    private String formatTo12Hour(String time24) {
        try {
            SimpleDateFormat h24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat h12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = h24.parse(time24);
            return date != null ? h12.format(date) : time24;
        } catch (Exception e) {
            return time24;
        }
    }

    // ── 8. Next Prayer Logic & Countdown Engine ──
    private void setupNextPrayerAndCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();

        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        int targetIndex = 0;
        int targetMinutes = 0;

        // 1. Convert API times to "Minutes Since Midnight" to find the next prayer
        for (int i = 0; i < prayerTimes24h.length; i++) {
            String[] parts = prayerTimes24h[i].split(":");
            int prayerMinutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);

            if (prayerMinutes > currentMinutes) {
                targetIndex = i;
                targetMinutes = prayerMinutes;
                break;
            }
        }

        // 2. Edge-Case: If past Isha, next target is Fajr tomorrow (+24 hours)
        if (targetMinutes == 0) {
            targetIndex = 0;
            String[] parts = prayerTimes24h[0].split(":");
            targetMinutes = (Integer.parseInt(parts[0]) + 24) * 60 + Integer.parseInt(parts[1]);
        }

        // 3. Update the UI
        tvNextPrayer.setText(prayerNames[targetIndex]);
        tvNextTime.setText(formatTo12Hour(prayerTimes24h[targetIndex]));

        // 4. Calculate exact milliseconds left (adjusting for current seconds)
        long diffMillis = (targetMinutes - currentMinutes) * 60000L - (now.get(Calendar.SECOND) * 1000L);

        // 5. Start the ultra-short countdown
        countDownTimer = new CountDownTimer(diffMillis, 1000) {
            public void onTick(long ms) {
                long s = ms / 1000;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60));
            }
            public void onFinish() {
                tvCountdown.setText("00:00:00");
                setupNextPrayerAndCountdown();
            }
        }.start();
    }

    private int getPrayerIndex(String name) {
        for (int i = 0; i < prayerNames.length; i++) {
            if (prayerNames[i].equalsIgnoreCase(name)) return i;
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent active window thread leaks on navigation context cleanups
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}