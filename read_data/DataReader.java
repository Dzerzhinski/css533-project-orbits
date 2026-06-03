import java.io.BufferedReader;
import java.io.FileReader;

import java.lang.Integer;
import java.lang.Double;
import java.lang.NumberFormatException;
import java.io.IOException;
import java.io.FileNotFoundException;

class DataReader {

    BufferedReader reader;

    DataReader() {}

    DataReader(String fname) {
        try {
            reader = new BufferedReader(new FileReader(fname));
        } catch(FileNotFoundException fnf) {
            System.err.println("Can't open file: " + fnf);
            fnf.printStackTrace();
        }
    }

    public OrbitInfo getOrbitData() {
        try {
            String line = reader.readLine();
            if(line == null) {
                reader.close();
                return null;
            }
            String[] orbitL = line.split(";");
            int o_id = Integer.parseInt(orbitL[0]);
            OrbitInfo orbit = new OrbitInfo(o_id);
            orbit.setName(orbitL[1]);
            double[] elements = new double[13];
            for(int i = 0; i < 13; i++) {
                elements[i] = Double.parseDouble(orbitL[2 + i]);
            }

            double x = elements[0];
            double y = elements[1];
            double z = elements[2];
            orbit.setPos(x, y, z);
            x = elements[3];
            y = elements[4];
            z = elements[5];
            orbit.setVel(x, y, z);
            orbit.setAxis(elements[6]);
            orbit.setEcc(elements[7]);
            orbit.setInclin(elements[8]);
            orbit.setRAsc(elements[9]);
            orbit.setArgPeri(elements[10]);
            orbit.setF(elements[11]);
            orbit.setM(elements[12]);
            return orbit;
        } catch(NumberFormatException nfe) {
            return null;
        } catch(IOException ioe) {
            System.err.println("Can't read file: " + ioe);
            ioe.printStackTrace();
            try{ 
                reader.close();
            } catch(Exception e) {}
            return null;
        }
    }
}


