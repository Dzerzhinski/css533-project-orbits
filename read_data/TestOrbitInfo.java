import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class TestOrbitInfo {

    public static void main(String args[]) {
        DataReader reader = new DataReader("../data/test_data.txt");
        Map<Integer, OrbitInfo> map = new HashMap<Integer, OrbitInfo>();
        OrbitInfo oi = reader.getOrbitData();
        while(oi != null) {
            System.out.println("orbit id: " + (oi.getId()));
            System.out.println("object name: " + oi.getName());
            map.put(oi.getId(), oi);
            oi = reader.getOrbitData();
        }
        List<SpaceObject> spaceObjL = new ArrayList<SpaceObject>();
        DataReader objReader = new DataReader("../data/random-objects.txt");
        SpaceObject spaceObj = objReader.getSpaceObject();
        while(spaceObj != null) {
            spaceObjL.add(spaceObj);
            spaceObj = objReader.getSpaceObject();
        }

        for(SpaceObject so : spaceObjL) {
            int orbId = so.getTargetId();
            OrbitInfo oInfo = map.get(orbId);
            if(oInfo != null) {
                getSoln(oInfo, so);
            } else { 
                System.err.println("can't find object: " + orbId);
            }
        }
    }

    private static void getSoln(OrbitInfo oi, SpaceObject so) {
        Orbit o = oi.makeOrbit();
        System.out.println(oi.getName());
        // o.printFields();
        System.out.println();
        double[] rState = so.getStateVector();
        printStateVector(rState);
        System.out.println();
        double[][] xfer = o.getImpulse(0.0, rState);
        printResults(xfer);
        System.out.println();
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
