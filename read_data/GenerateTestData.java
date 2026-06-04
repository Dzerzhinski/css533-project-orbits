import java.lang.StringBuilder;
import java.util.Formatter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateTestData {
    public static void main(String args[]) {
        try {

            DataReader reader = new DataReader(args[0]);
            String outFName;
            if(args.length <= 1) {
                outFName = "randomObjects.txt";
            } else {
                outFName = args[1];
            }


            BufferedWriter writer = new BufferedWriter(new FileWriter(outFName));

            StringBuilder sb = new StringBuilder();
            Formatter sf = new Formatter(sb);

            OrbitInfo oi = reader.getOrbitData();
            while(oi != null) {
                TestVessel tv = new TestVessel(oi);
                String oName = oi.getName();
                for(int i = 0; i < 100; i++) {
                    double[] rv = tv.randStateVector();
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
