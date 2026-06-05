import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/*
 * Test the logic of the orbit solver to make sure it 
 *      works before incorporating it into RMI/gRPC 
 *      code.
 *
 * Read targets and chasers in, get solutions, print the 
 *      results to stdout.
 */
class TestOrbitInfo {

    public static void main(String args[]) {
        // read in targets
        DataReader reader = new DataReader("../data/test_data.txt");
        Map<Integer, OrbitInfo> map = new HashMap<Integer, OrbitInfo>();
        OrbitInfo oi = reader.getOrbitData();
        while(oi != null) {
            System.out.println("orbit id: " + (oi.getId()));
            System.out.println("object name: " + oi.getName());
            map.put(oi.getId(), oi);
            oi = reader.getOrbitData();
        }
        // read in chasers
        List<SpaceObject> spaceObjL = new ArrayList<SpaceObject>();
        // this particular file is huge
        DataReader objReader = new DataReader("../data/random-objects.txt");
        SpaceObject spaceObj = objReader.getSpaceObject();
        while(spaceObj != null) {
            spaceObjL.add(spaceObj);
            spaceObj = objReader.getSpaceObject();
        }

        // test solutions
        for(SpaceObject so : spaceObjL) {
            // match chaser to target
            int orbId = so.getTargetId();
            OrbitInfo oInfo = map.get(orbId);
            if(oInfo != null) {
                // request solution
                getSoln(oInfo, so);
            } else { 
                System.err.println("can't find object: " + orbId);
            }
        }
    }

    /*
     * Find a state transition solution and print the results
     *      to stdout.
     * @param oi target orbit state (e.g. space station)
     * @param so chaser (e.g. spacecraft)
     */
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

    /*
     * Print state vector to stdout.
     * @param v State vector
     */
    private static void printStateVector(double[] v) {
        System.out.println("Vector: ");
        System.out.print("[ ");
        for(int i = 0; i < 5; i++) {
            System.out.printf("%4.4f, ", v[i]);
        }
        System.out.printf("%4.4f ]\n\n", v[5]);
    }

    /*
     * Print solution for state transtion (i.e. intercept) 
     *      to stdout.  
     * Format: 
     *      "time: <t_i>, [ <initial delta-v vector> ]"
     *      "time: <t_f>, [ <final delta-v vector> ]"
     */
    private static void printResults(double[][] res) {
        System.out.println("Xfer: ");
        System.out.printf("time: %4.4f, [ %4.4f, %4.4f, %4.4f ]\n", 
                        res[0][0], res[0][1], res[0][2], res[0][3]);
        System.out.printf("time: %4.4f, [ %4.4f, %4.4f, %4.4f ]\n", 
                        res[1][0], res[1][1], res[1][2], res[1][3]);
    }


}
