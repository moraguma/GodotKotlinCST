package com.example.visualizermind.util;

public class IMUDataPack {
    public Vector3 linearAcceleration;
    public Vector3 gravity;
    public Vector3 magnetomer;
    public Vector3 gyroscope;

    public IMUDataPack(Vector3 linearAcceleration, Vector3 gravity, Vector3 magnetomer, Vector3 gyroscope) {
        this.linearAcceleration = linearAcceleration;
        this.gravity = gravity;
        this.magnetomer = magnetomer;
        this.gyroscope = gyroscope;
    }
}
