package com.example.visualizermind;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.util.ArraySet;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.visualizermind.util.Basis;
import com.example.visualizermind.util.Vector3;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class VisualizerMind extends GodotPlugin {

    private static VisualizerMind instance = null;

    private final float DISTANCE_PER_DEGREE = 111000;
    private final float ACCELEROMETER_STANDARD_DEVIATION = 0.001f;

    private SensorManager sensorManager;

    private Sensor accelerationSensor;
    private Sensor magneticSensor;

    private SensorEventListener accelerationListener;
    private SensorEventListener magneticListener;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Vector3 worldPosition;
    private float posVar;
    private Vector3 worldVelocity;
    private float velVar;
    private Basis basis;

    private Vector3 linearAcceleration;
    private Vector3 gravity;
    private Vector3 predictedPosition;
    private Vector3 predictedVelocity;
    private float predictedPosVar;
    private float predictedSpeedVar;

    private Vector3 GPSBasePosition;
    private Vector3 GPSPosition;
    private Vector3 GPSVelocity;
    private float GPSPositionVar;
    private float GPSVelocityVar;

    private float lastAccelCallTime;

    private boolean accelerationCalled = false;

    DecimalFormat df = new DecimalFormat();

    public VisualizerMind(Godot godot) {
        super(godot);
        instance = this;

        df.setMaximumFractionDigits(5);
    }

    public static VisualizerMind getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "VisualizerMind";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList("start", "getBasis", "getWorldPosition");
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("update_value", String.class, String.class));
        signals.add(new SignalInfo("create_notification", String.class));

        return signals;
    }

    public void start() {
        /*
        STARTING UP SENSORS
        */

        worldPosition = new Vector3(0, 0, 0);
        posVar = 1;
        worldVelocity = new Vector3(0, 0, 0);
        velVar = 1;
        basis = new Basis(new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(0, 0, 1));

        linearAcceleration = new Vector3(0, 0, 0);
        gravity = new Vector3(0, 0, 0);
        predictedPosition = new Vector3(0, 0, 0);
        predictedVelocity = new Vector3(0, 0, 0);
        predictedPosVar = 1;
        predictedSpeedVar = 1;

        GPSBasePosition = null;
        GPSPosition = new Vector3(0, 0, 0);
        GPSVelocity = new Vector3(0, 0, 0);
        GPSPositionVar = 1;
        GPSVelocityVar = 1;

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        lastAccelCallTime = System.nanoTime();

        initializeAccelerometer();
        initializeMagnetometer();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        initializeLocation();
    }

    private void initializeAccelerometer() {
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.

                float alpha = 0.8f;

                // Isolate the force of gravity with the low-pass filter.
                gravity = new Vector3(alpha * gravity.x + (1 - alpha) * event.values[0], alpha * gravity.y + (1 - alpha) * event.values[1], alpha * gravity.z + (1 - alpha) * event.values[2]);

                // Remove the gravity contribution with the high-pass filter.
                linearAcceleration = new Vector3(event.values[0] - gravity.x, event.values[1] - gravity.y, event.values[2] - gravity.z);

                float deltaT = (System.nanoTime() - lastAccelCallTime)/1000000000;
                lastAccelCallTime = System.nanoTime();

                Vector3 worldAccel = basis.v_x.multiply(linearAcceleration.x).add(basis.v_y.multiply(linearAcceleration.y)).add(basis.v_z.multiply(linearAcceleration.z));
                predictedPosition = worldPosition.add(worldVelocity.multiply(deltaT)).add(worldAccel.multiply((float) Math.pow(deltaT, 2)/2));
                predictedPosVar = (float) (Math.pow(posVar, 2) + Math.pow(deltaT * Math.pow(velVar, 0.5), 2) + Math.pow(Math.pow(deltaT, 2) * ACCELEROMETER_STANDARD_DEVIATION / 2, 2));
                predictedVelocity = worldVelocity.add(worldAccel.multiply(deltaT));
                predictedSpeedVar = (float) (Math.pow(velVar, 2) + Math.pow(ACCELEROMETER_STANDARD_DEVIATION * deltaT, 2));

                if (GPSBasePosition != null && !accelerationCalled) {
                emitSignal("create_notification", "PRE\nposVar: " + posVar + "\nworldPosition: " + worldPosition + "\npredictedPosVar: " + predictedPosVar + "\npredictedPosition: " + predictedPosition + "\nGPSPosVar: " + GPSPositionVar + "\nGPSPosition: " + GPSPosition);
                }
                float[] aux = combine(posVar, worldPosition.x, predictedPosVar, predictedPosition.x, GPSPositionVar, GPSPosition.x);
                worldPosition.x = aux[1];
                aux = combine(posVar, worldPosition.y, predictedPosVar, predictedPosition.y, GPSPositionVar, GPSPosition.y);
                worldPosition.y = aux[1];
                aux = combine(posVar, worldPosition.z, predictedPosVar, predictedPosition.z, GPSPositionVar, GPSPosition.z);
                worldPosition.z = aux[1];
                posVar = aux[0];

                aux = combine(velVar, worldVelocity.x, predictedSpeedVar, predictedVelocity.x, GPSVelocityVar, GPSVelocity.x);
                worldVelocity.x = aux[1];
                aux = combine(velVar, worldVelocity.y, predictedSpeedVar, predictedVelocity.y, GPSVelocityVar, GPSVelocity.y);
                worldVelocity.y = aux[1];
                aux = combine(velVar, worldVelocity.z, predictedSpeedVar, predictedVelocity.z, GPSVelocityVar, GPSVelocity.z);
                worldVelocity.z = aux[1];
                velVar = aux[0];

                if (GPSBasePosition != null && !accelerationCalled) {
                    emitSignal("create_notification", "POST\nposVar: " + posVar + "\nworldPosition: " + worldPosition + "\npredictedPosVar: " + predictedPosVar + "\npredictedPosition: " + predictedPosition + "\nGPSPosVar: " + GPSPositionVar + "\nGPSPosition: " + GPSPosition);
                    accelerationCalled = true;
                }

                emitSignal("update_value", worldPosition.toString(), "POSITION");
                emitSignal("update_value", worldVelocity.toString(), "VELOCITY");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(accelerationListener, accelerationSensor, 1);

    }

    private void initializeMagnetometer() {
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magneticListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] R = {basis.v_x.x, basis.v_x.y, basis.v_x.z, basis.v_y.x, basis.v_y.y, basis.v_y.z, basis.v_z.x, basis.v_z.y, basis.v_z.z};
                float[] I = new float[9];
                float[] g = {gravity.x, gravity.y, gravity.z};
                SensorManager.getRotationMatrix(R, I, g, event.values);

                basis = new Basis(new Vector3(R[0], R[3], R[6]), new Vector3(R[1], R[4], R[7]), new Vector3(R[2], R[5], R[8]));

                emitSignal("update_value", basis.toString(), "BASIS");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(magneticListener, magneticSensor, 1);
    }

    private Vector3 posFromLocation(Location location) {
        /*
            Returns a Vector3 that represents an absolute position from latitude and longitude
        */

        return new Vector3((float) location.getLatitude() * DISTANCE_PER_DEGREE, (float) location.getLongitude() * DISTANCE_PER_DEGREE, (float) location.getAltitude());
    }

    private void initializeLocation() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Vector3 pastPos = new Vector3(GPSPosition.x, GPSPosition.y, GPSPosition.z);

                if (GPSBasePosition == null) {
                    GPSBasePosition = posFromLocation(location);
                    GPSPosition = new Vector3(0f, 0f, 0f);

                    if (location.hasAccuracy()) {
                        posVar = location.getAccuracy();
                    } else {
                        posVar = 2f;
                    }

                    if (location.hasSpeedAccuracy()) {
                        velVar = location.getSpeedAccuracyMetersPerSecond();
                    } else {
                        velVar = 0.5f;
                    }

                } else {
                    GPSPosition = (posFromLocation(location)).sub(GPSBasePosition);
                }

                if (location.hasAccuracy()) {
                    GPSPositionVar = location.getAccuracy();
                }
                GPSVelocity = GPSPosition.sub(pastPos).normalized().multiply(location.getSpeed());
                if (location.hasSpeedAccuracy()) {
                    GPSVelocityVar = location.getSpeedAccuracyMetersPerSecond();
                }

                //emitSignal("create_notification", GPSBasePosition + " - " + GPSPosition.toString());
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setWaitForAccurateLocation(true);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getActivity().getMainLooper());
    }

    private float[] predict(float var1, float mean1, float var2, float mean2) {
        float[] r = {var1 + var2, mean1 + mean2};
        return r;
    }

    private float[] correct(float var1, float mean1, float var2, float mean2) {
        if (var1 == 0) {
            var1 = 0.01f;
        }
        if (var2 == 0) {
            var2 = 0.01f;
        }
        float[] r = {(var1 * mean2 + var2 * mean1)/(var1 + var2), 1/(1/var1 + 1/var2)};
        return r;
    }

    private float[] combine(float var, float mean, float predictedVar, float predictedMean, float measuredVar, float measuredMean) {
        float[] aux = predict(var, mean, predictedVar, predictedMean);
        return correct(aux[0], aux[1], measuredVar, measuredMean);
    }

    public String getWorldPosition() {
        return worldPosition.x + ";" + worldPosition.y + ";" + worldPosition.z;
    }

    public String getBasis() {
        return basis.v_x.x + ";" + basis.v_x.y + ";" + basis.v_x.z + ";" + basis.v_y.x + ";" + basis.v_y.y + ";" + basis.v_y.z + ";" + basis.v_z.x + ";" + basis.v_z.y + ";" + basis.v_z.z;
    }

    public Activity getActivityOutside() {
        return getActivity();
    }
}
