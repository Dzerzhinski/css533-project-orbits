
class TestOrbitInfo {

    public static void main(String args[]) {
        DataReader reader = new DataReader("../data/test_data.txt");
        while(true) {
            
            OrbitInfo oi = reader.getOrbitData();
            if(oi == null) {
                break;
            }
            System.out.println("orbit id: " + (oi.getId()));
            System.out.println("object name: " + oi.getName());
            Orbit o = oi.makeOrbit();
            o.printFields();
            System.out.println();
        }
    }
}
