package com.example.visualizermind.util;

public class Basis {
    public Vector3 v_x;
    public Vector3 v_y;
    public Vector3 v_z;

    public Basis(float x, float y, float z, float w) {
        /*
            Creates a basis equivalent to the given quaternion
        */

        v_x = (new Vector3(1, 0, 0).rotateQuat(x, y, z, w));
        v_y = (new Vector3(0, 1, 0).rotateQuat(x, y, z, w));
        v_z = (new Vector3(0, 0, 1).rotateQuat(x, y, z, w));
    }

    public Basis(Vector3 v_x, Vector3 v_y, Vector3 v_z) {
        this.v_x = v_x;
        this.v_y = v_y;
        this.v_z = v_z;
    }

    @Override
    public String toString() {
        return "[" + v_x + ", " + v_y + ", " + v_z + "]";
    }
}
