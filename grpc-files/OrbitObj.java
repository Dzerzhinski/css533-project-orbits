package io.grpc.examples.orbitxfer;

public class OrbitObj {


    private Orbit orbit;
    private String name;

    OrbitObj() {
        orbit = null;
        name = "";
    };

    OrbitObj(String n, Orbit o) {
        this.name = n;
        this.orbit = o;
    }

    public String getName() { return this.name; }
    public Orbit getOrbit() { return this.orbit; }

    public double[][] getSoln(double time, double[] stateVector) {
        return this.orbit.getImpulse(time, stateVector);
    }
}

    

