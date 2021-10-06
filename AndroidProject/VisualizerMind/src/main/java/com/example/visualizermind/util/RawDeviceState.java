package com.example.visualizermind.util;

import android.location.Location;

public class RawDeviceState {
    public IMUDataPack IMUData;
    public Location location;

    public RawDeviceState(IMUDataPack IMUData, Location location) {
        this.IMUData = IMUData;
        this.location = location;
    }
}
