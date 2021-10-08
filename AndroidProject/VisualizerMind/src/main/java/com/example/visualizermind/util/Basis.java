package com.example.visualizermind.util;

public class Basis {
    public Vector3 v_x;
    public Vector3 v_y;
    public Vector3 v_z;

    public Basis() {
        v_x = new Vector3(1, 0, 0);
        v_y = new Vector3(0, 1, 0);
        v_z = new Vector3(0, 0, 1);
    }

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

    public boolean equals(Basis b) {
        return (v_x.equals(b.v_x) && v_y.equals(b.v_y) && v_z.equals(b.v_z));
    }

    public Basis rotateQuat(float x, float y, float z, float w) {
        /*
            Returns this basis rotated by the given quaternion
        */

        Vector3 new_v_x = v_x.rotateQuat(x, y, z, w);
        Vector3 new_v_y = v_y.rotateQuat(x, y, z, w);
        Vector3 new_v_z = v_z.rotateQuat(x, y, z, w);

        return new Basis(new_v_x, new_v_y, new_v_z);
    }

    public Vector3 vectorToFrameOfReference(Vector3 w) {
        /*
            Given that this basis is represented in w's frame of reference, returns w
            expressed in this basis's frame of reference. Effectively, we are solving the
            system

            w = c_1 v_x + c_2 v_y + c_3 v_z

            and returning [c_1, c_2, c_3]
        */

        float D = v_x.x * v_y.y * v_z.z + v_y.x * v_z.y * v_x.z + v_z.x * v_x.y * v_y.z - v_z.x * v_y.y * v_x.z - v_y.x * v_x.y * v_z.z - v_x.x * v_z.y * v_y.z;

        if (D == 0) {
            return new Vector3(0, 0, 0);
        }

        float c_1 = (w.x * v_y.y * v_z.z + v_y.x * v_z.y * w.z + v_z.x * w.y * v_y.z - v_z.x * v_y.y * w.z - v_y.x * w.y * v_z.z - w.x * v_z.y * v_y.z)/D;
        float c_2 = (v_x.x * w.y * v_z.z + w.x * v_z.y * v_x.z + v_z.x * v_x.y * w.z - v_z.x * w.y * v_x.z - w.x * v_x.y * v_z.z - v_x.x * v_z.y * w.z)/D;
        float c_3 = (v_x.x * v_y.y * w.z + v_y.x * w.y * v_x.z + w.x * v_x.y * v_y.z - w.x * v_y.y * v_x.z - v_y.x * v_x.y * w.z - v_x.x * w.y * v_y.z)/D;

        return new Vector3(c_1, c_2, c_3);
    }

    public Basis orthonormalized() {
        /*
            Returns an orthonormalized basis produced thorugh the Gram-Schmidt Process
        */

        Vector3 new_v_x = v_x.copy();
        Vector3 new_v_y = v_y.projectToPlane(new_v_x);
        Vector3 new_v_z = (v_z.projectToPlane(v_x)).projectToPlane(new_v_y);

        new_v_x = new_v_x.normalized();
        new_v_y = new_v_y.normalized();
        new_v_z = new_v_z.normalized();

        return new Basis(new_v_x, new_v_y, new_v_z);
    }

    @Override
    public String toString() {
        return "[" + v_x + ", " + v_y + ", " + v_z + "]";
    }
}
