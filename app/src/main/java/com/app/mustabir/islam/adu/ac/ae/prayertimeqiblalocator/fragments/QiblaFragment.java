package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.fragments;

import android.app.Fragment;
import android.content.Context;

//sensors
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.DatabaseHelper;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.R;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.UserPreference;


public class QiblaFragment extends Fragment implements SensorEventListener, View.OnTouchListener {

    //  UI Elements user interecton
    private ImageView ivCompassRose, ivQiblaNeedle;
    private TextView  tvQiblaAngle, tvQiblaDirection, tvCity, tvSwipeHint;
    private View compassContainer;

    //hardware sensors , the physical chips inside the phone
    private SensorManager sensorManager;
    private Sensor accelerometer , magnetometer;

    // Arrays to hold the raw data coming from the chips
    private float[] gravity     = new float[3];
    private float[] geomagnetic = new float[3];
    private boolean sensorsCalibrated = false;

    // Math & State Variables
    private float currentAzimuth = 0f; // Where the top of the phone is currently pointing
    private float qiblaAngle     = 0f; // The calculated angle to Mecca
    private final float NEEDLE_OFFSET = 45f; // The math fix for crooked needle image

    // Low-Pass Filter Constant (Lower = smoother but slower, Higher = faster but shakier)
    private static final float ALPHA = 0.05f;

    // Database & Gestures
    private DatabaseHelper dbHelper;
    private GestureDetector gestureDetector;
    private boolean compassViewVisible = true; // Toggles between compass and text-only mode

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qibla, container, false);


        // A. Initialize the Database
        dbHelper = new DatabaseHelper(getActivity());

        // B. Bind the UI elements (Connect Java variables to your XML IDs)
        ivCompassRose    = (ImageView) view.findViewById(R.id.iv_compass_rose);
        ivQiblaNeedle    = (ImageView) view.findViewById(R.id.iv_compass_needle);
        tvQiblaAngle     = (TextView) view.findViewById(R.id.tv_qibla_angle);
        tvQiblaDirection = (TextView) view.findViewById(R.id.tv_qibla_direction);
        tvCity           = (TextView) view.findViewById(R.id.tv_city_qibla);
        tvSwipeHint      = (TextView) view.findViewById(R.id.tv_swipe_hint);
        compassContainer = view.findViewById(R.id.compass_container);

        // C. Run our custom logic to load the Qibla data
        loadQiblaData();
        setupSensors();

        //  Register the standard Touch Listener
        // We attach it to the main 'view' so swiping works anywhere on the screen.
        // Because our class says "implements View.OnTouchListener", we just pass "this".
        compassContainer.setOnTouchListener(this);

        // to make sure Android knows this box is allowed to be touched
        compassContainer.setClickable(true);
        compassContainer.setFocusable(true);

        //  Set up the advanced Gesture Detector
        // We create our custom doubletap/fling handler and hand it to Android.
        DoubleTapHandler handler = new DoubleTapHandler();
        gestureDetector = new GestureDetector(getActivity(), handler);

        return view;
    }

    //  Math & Database
    private void loadQiblaData() {
        // Grab the saved preferences from  database
        UserPreference prefs = dbHelper.getPreferences();
        tvCity.setText("From: " + prefs.getCity());

        // Creating a Location object for the user
        // For example, when this runs using  Abu Dhabi coordinates (Lat: 24.437, Lon: 54.438),
        // Android will use this exact point to start the calculation.
        Location userLoc = new Location("User");
        userLoc.setLatitude(prefs.getLatitude());
        userLoc.setLongitude(prefs.getLongitude());

        // Create a Location object for the Kaaba in Mecca

        Location meccaLoc = new Location("Mecca");
        meccaLoc.setLatitude(21.422487);
        meccaLoc.setLongitude(39.826206);

        // Calculate the shortest path over the Earth's curve
        float bearing = userLoc.bearingTo(meccaLoc);

        // Android can sometimes return negative degrees. This math trick forces it into a clean 0-360 circle.
        qiblaAngle = (bearing + 360) % 360;

        tvQiblaAngle.setText(String.format("Qibla Direction: %.1f°", qiblaAngle));
        tvQiblaDirection.setText(getDirectionText(qiblaAngle));


    }

    private String getDirectionText(float angle) {
        // A simple helper method to translate raw degrees into readable text for the user
        if (angle >= 337.5 || angle < 22.5) return "Face North ";
        if (angle >= 22.5 && angle < 67.5) return "Face North-East ";
        if (angle >= 67.5 && angle < 112.5) return "Face East ";
        if (angle >= 112.5 && angle < 157.5) return "Face South-East ";
        if (angle >= 157.5 && angle < 202.5) return "Face South ";
        if (angle >= 202.5 && angle < 247.5) return "Face South-West ";
        if (angle >= 247.5 && angle < 292.5) return "Face West ";
        if (angle >= 292.5 && angle < 337.5) return "Face North-West ";
        return "";
    }

    //  Sensor Lifecycle (Battery Management)

    private void setupSensors() {
        // Ask the Android system for access to the physical chips
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
       if(sensorManager != null) {
           accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
           magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
       }
    }
    @Override
    public void onResume() {
        super.onResume();
        // This fires the second the screen is visible to the user.
        // We turn the sensors ON here at a high refresh rate (SENSOR_DELAY_GAME)
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer,  SensorManager.SENSOR_DELAY_GAME);
    }}
    @Override
    public void onPause() {
        super.onPause();
        // This fires the millisecond the user clicks away to the Tasbih or Settings tab.
        // We turn the sensors OFF immediately so we don't kill their battery in the background.
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    // ── 9. Low-Pass Filter Math ──
    private float[] lowPass(float[] input, float[] output) {
        //if output array is null
        if (output == null) return input.clone();
        for(int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    //  Real Time Hardware Math (The Compass Spin)
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Run the raw hardware data through the Low-Pass Filter
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = lowPass(event.values, gravity);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = lowPass(event.values, geomagnetic);
        }

        //R is the rotation matrix, I is the inclination matrix
        float[] R = new float[9];
        float[] I = new float[9];

// it takes clean, low-pass-filtered gravity and geomagnetic arrays and mixes them together.
// It strips out the tilt of my hand and uses the Earth's gravity vectors to
// figure out how to accurately align the phone's compass direction
// with the actual horizon.
        if(SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            // a flag i set to track i have live coordinate tracking data ready
            sensorsCalibrated = true;

            //orientation[0] = Azimuth
            // Rotation around the Z-axis points to my compass heading.
            //orientation[1] = Pitch Tilted forward/backward
            //orientation[2]=Roll Tilted left/right.
            float[] orientation = new float[3];
            SensorManager.getOrientation(R, orientation);

            //instead od having the angle of azimuth -180 to +180
            //we transfrom it to 0 to 360
            float azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth + 360) % 360;

            if (compassViewVisible) {
                // Spin the dial
                RotateAnimation roseAnim = new RotateAnimation(
                        -currentAzimuth, -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                roseAnim.setDuration(210);
                roseAnim.setFillAfter(true);
                ivCompassRose.startAnimation(roseAnim);

                // Spin the needle (with your offset)
                RotateAnimation needleAnim = new RotateAnimation(
                        (qiblaAngle - currentAzimuth - NEEDLE_OFFSET),
                        (qiblaAngle - azimuth - NEEDLE_OFFSET),
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                needleAnim.setDuration(210);
                needleAnim.setFillAfter(true);
                ivQiblaNeedle.startAnimation(needleAnim);

            }
            currentAzimuth = azimuth;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    //touch and gestures
    @Override
    public boolean onTouch(View v, MotionEvent event) {

// let  Gesture Engine analyze the finger movement FIRST
        boolean gestureHandled = gestureDetector.onTouchEvent(event);

        // Do your custom check when they lift their finger
        if (event.getAction() == MotionEvent.ACTION_UP && !sensorsCalibrated) {
            Toast.makeText(getActivity(), "Hold device flat to calibrate", Toast.LENGTH_SHORT).show();
        }

        // If the Gesture Engine recognized a swipe or double tap, return true.
        // Otherwise return true anyway so we don't drop the finger tracking
        return true;
    }

    private  class DoubleTapHandler extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                ivCompassRose.clearAnimation();
                ivQiblaNeedle.clearAnimation();
                currentAzimuth = 0f;
                sensorsCalibrated = false;
                Toast.makeText(getActivity(), "Compass reset", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();

            if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(velocityX) > 300) {
                compassViewVisible = !compassViewVisible;

                if (compassViewVisible) {
                    compassContainer.setVisibility(View.VISIBLE);
                    tvSwipeHint.setText("Swipe for text only vieww");
                } else {
                    compassContainer.setVisibility(View.INVISIBLE);
                    tvSwipeHint.setText("Swipe for compass vieww");
                }
                return true;
            }
            return false;
        }


    }
}