
class TestOrbit {

    public static void main(String args[]) {
        double[] r_0 = new double[]{0, 0, 0};
        double[] v_0 = new double[]{0, 0, 0};
        Orbit test = new Orbit(r_0, v_0, 25512, 5.0 / 8.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        double res = test.testFindfAtTime(4.0 * 3600);
        System.out.printf("f: \t%.4f\n", res);

        double[] r1 = new double[]{1622.39, 5305.10, 3717.44};
        double[] v1 = new double[]{-7.29977, 0.492357, 2.48318};
        double[] r2 = new double[]{1612.75, 5310.19, 3750.33};
        double[] v2 = new double[]{-7.35321, 0.463856, 2.46920};

        double[] rv2 = new double[]{1612.75, 5310.19, 3750.33, -7.35321, 0.463856, 2.46920};
        Orbit test2 = new Orbit(r1, v1, 300.0 + 6.37812e3, 1.0, 40.0, 20.0, 0.0, 0.0, 0.0);
        // test2.testBasis();
        // test2.testTransform(r2, v2);
        double t1 = 8.0 * 3600;
        double[][] res1 = test2.getImpulse(t1, rv2);
        double dv_val = 0;
        double dv_val2 = 0;
        for(int i = 0; i < 3; i++) {
            dv_val += res1[0][i] * res1[0][i];
            dv_val2 += res1[1][i] * res1[1][i];
        }
        dv_val = Math.sqrt(dv_val);
        dv_val = Math.sqrt(dv_val2);
        System.out.printf("delta v initial: %4.4f\n", dv_val);
        System.out.printf("delta v final: %4.4f\n", dv_val2);

        


    }


}
