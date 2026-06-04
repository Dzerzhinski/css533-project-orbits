package io.grpc.examples.orbitxfer;

import java.lang.Math;
import java.util.Random;


class TestVessel {

    private double[] pos; 
    private double[] vel; 
    private int orbit_id;
    private String orbit_name;

    private Random rng;

    TestVessel() { }

    TestVessel(OrbitInfo o) {
        double[] state = o.getState();
        this.pos = new double[]{state[0], state[1], state[2]};
        this.vel = new double[]{state[3], state[4], state[5]};
        this.orbit_id = o.getId();
        this.orbit_name = o.getName();
        this.rng = new Random();
    }

    public int getOrbitId() { return this.orbit_id; }

    public double[] randStateVector() {
        double[] r_pos = randVector(0.01, this.pos);
        double[] r_vel = randVector(1.0, this.vel);
        double[] res = new double[6];
        for(int i = 0; i < 3; i++) {
            res[i] = r_pos[i];
            res[i + 3] = r_vel[i];
        }
        return res;
    }

    private double[] randVector(double factor, double[] vec) {
        double norm = getNorm(vec);
        double[] rand_v = randomUnitVector();
        double rand_scal = (norm * factor) * rng.nextDouble();
        rand_v = scaleVector(rand_scal, rand_v);
        rand_v = addVector(vec, rand_v);
        return rand_v;
    }




    private double[] addVector(double[] v1, double[] v2) {
        double[] res = new double[v1.length];
        for(int i = 0; i < v1.length; i++) {
            res[i] = v1[i] + v2[i];
        }
        return res;
    }

    private double getNorm(double[] v) {
        double res = 0;
        for(int i = 0; i < v.length; i++) {
            res += v[i] * v[i];
        }
        return Math.sqrt(res);
    }

    private double[] scaleVector(double x, double[] vec) {
        double[] res = new double[vec.length];
        for(int i = 0; i < vec.length; i++) {
            res[i] = vec[i] * x;
        }
        return res;
    }

    private double[] randomUnitVector() {
        double[] rand_v = new double[3];
        for(int i = 0; i < 3; i++) {
            rand_v[i] = rng.nextDouble();
        }
        double norm = getNorm(rand_v);
        rand_v = scaleVector((1 / norm), rand_v);
        return rand_v;
    }

}
