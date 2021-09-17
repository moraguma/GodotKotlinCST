package com.example.visualizermind;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class VisualizerMind extends GodotPlugin {

    private SensorManager sensorManager;

    private Sensor linearAccelerationSensor;
    private Sensor gravitySensor;
    private Sensor rotationSensor;

    private SensorEventListener linearAccelerationListener;
    private SensorEventListener gravityListener;
    private SensorEventListener rotationListener;

    private float[] linearAccelerationEventValues;
    private float[] gravityEventValues;
    private float[] rotationEventValues;

    DecimalFormat df = new DecimalFormat();

    public VisualizerMind(Godot godot) {
        super(godot);

        df.setMaximumFractionDigits(5);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "VisualizerMind";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList("start");
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();

        signals.add(new SignalInfo("update_value", String.class, String.class));
        signals.add(new SignalInfo("update_rotation", String.class, String.class, String.class, String.class));
        signals.add(new SignalInfo("update_acceleration", String.class, String.class, String.class));

        return signals;
    }

    public void start() {
        /*
        STARTING UP SENSORS
        */

        linearAccelerationEventValues = new float[] {0, 0, 0};
        gravityEventValues = new float[] {0, 0, 0};
        rotationEventValues = new float[] {0, 0, 0, 0};

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        /*
        GRAVITY
        */

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gravityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                emitSignal("update_value", "["+df.format(event.values[0])+", "+df.format(event.values[1])+", "+df.format(event.values[2])+"]", "GRAVITY");
                gravityEventValues = event.values;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(gravityListener, gravitySensor, 1);


        /*
        LINEAR ACCELERATION
        */

        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        linearAccelerationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                emitSignal("update_value", "["+df.format(event.values[0])+", "+df.format(event.values[1])+", "+df.format(event.values[2])+"]", "ACCELERATION");
                emitSignal("update_acceleration", String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2]));

                linearAccelerationEventValues = event.values;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(linearAccelerationListener, linearAccelerationSensor, 1);

        /*
        ROTATION
        */

        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotationListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                emitSignal("update_value", "["+df.format(event.values[0])+", "+df.format(event.values[1])+", "+df.format(event.values[2])+", "+df.format(event.values[3])+"]", "ROTATION");
                emitSignal("update_rotation", String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2]), String.valueOf(event.values[3]));
                rotationEventValues = event.values;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(rotationListener, rotationSensor, 1);
    }
}
