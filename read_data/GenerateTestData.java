import java.lang.StringBuilder;
import java.util.Formatter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/*
 * Create test data for testing different implementations 
 *      or regression testing.  Reads in targets (e.g. 
 *      space stations) and generates SpaceObject objects 
 *      near them with random vectors added to their state 
 *      vectors.  This simulates objects near enough to 
 *      get a state transition solution.  It then writes 
 *      these objects to file.  Very little validation or 
 *      error recovery.  Hard-coded parameters produce 
 *      way too many.
 *
 * Usage: java GenerateTestData <input file> <output file>
 *
 * Output file is optional, has a default output filename.
 */
public class GenerateTestData {

    public static void main(String args[]) {
        try {
            DataReader reader = new DataReader(args[0]);
            String outFName;
            if(args.length <= 1) {
                // default output filename
                outFName = "randomObjects.txt";
            } else {
                outFName = args[1];
            }

            // writes output to file
            BufferedWriter writer = 
                new BufferedWriter(new FileWriter(outFName));

            // generate lines of output
            StringBuilder sb = new StringBuilder();
            Formatter sf = new Formatter(sb);

            // read data in
            OrbitInfo oi = reader.getOrbitData();
            while(oi != null) {
                // TestVessel generates random state vectors
                //      for a given target orbit
                TestVessel tv = new TestVessel(oi);
                String oName = oi.getName();
                for(int i = 0; i < 100; i++) {
                    double[] rv = tv.randStateVector();
                    // format: 
                    //  "<id#>;<name>;<r_x>;<r_y>;...;<v_y>;<v_z>\n"
                    sf.format("%d;%s;%f;%f;%f;%f;%f;%f\n", 
                                tv.getOrbitId(), oName, 
                                rv[0], rv[1], rv[2], rv[3], rv[4], rv[5]);
                    System.out.print(sb.toString());
                    writer.write(sb.toString());
                }
                oi = reader.getOrbitData();
            }
            writer.close();
            reader.close();
        } catch(IOException ioe) {
            System.err.println("file except; " + ioe);
            ioe.printStackTrace();
        }
    }
}
