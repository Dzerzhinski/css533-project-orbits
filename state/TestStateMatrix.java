
// import java.io.System;
// import StateMatrix;

class TestStateMatrix {

    private static void printVector(double[] v) {
        System.out.printf("x: %.2f, y: %.2f, z: %.2f\n", v[0], v[1], v[2]);
        return;
    }

    public static void main(String args[]) {

        StateMatrix test = new StateMatrix(Math.PI / 2);

        double[] dr0 = new double[]{1, 1, 1};
        double[] dv0 = new double[]{0, 0, 1};

        System.out.print("initial: \t");
        double[] deltaVi = test.initialImpulse(dr0, dv0);
        printVector(deltaVi);
        System.out.print("final: \t");
        double[] deltaVf = test.endImpulse(dr0);
        printVector(deltaVf);

        return;
    }
}




