
class TestOrbitInfo {

    public static void main(String args[]) {
        DataReader reader = new DataReader("../data/test_data.txt");
        while(true) {
            
            OrbitInfo oi = reader.getOrbitData();
            if(oi == null) {
                break;
            }
            TestVessel foo = new TestVessel(oi);

            
            System.out.println("orbit id: " + (oi.getId()));
            System.out.println("object name: " + oi.getName());
            Orbit o = oi.makeOrbit();
            o.printFields();
            System.out.println();
            double[] rState = foo.randStateVector();
            printStateVector(rState);
            System.out.println();
            double[][] xfer = o.getImpulse(0.0, rState);
            printResults(xfer);
        }
    }

    private static void printStateVector(double[] v) {
        System.out.println("Vector: ");
        System.out.print("[ ");
        for(int i = 0; i < 5; i++) {
            System.out.printf("%4.4f, ", v[i]);
        }
        System.out.printf("%4.4f ]\n\n", v[5]);
    }

    private static void printResults(double[][] res) {
        System.out.println("Xfer: ");
        System.out.printf("time: %4.4f, [ %4.4f, %4.4f, %4.4f ]\n", 
                        res[0][0], res[0][1], res[0][2], res[0][3]);
        System.out.printf("time: %4.4f, [ %4.4f, %4.4f, %4.4f ]\n", 
                        res[1][0], res[1][1], res[1][2], res[1][3]);
    }


}
