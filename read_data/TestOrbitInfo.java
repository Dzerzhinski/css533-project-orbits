
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
            printStateVector(foo.randStateVector());
            System.out.println();
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

}
