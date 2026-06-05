import java.io.BufferedReader;
import java.io.FileReader;

import java.lang.Integer;
import java.lang.Double;
import java.lang.NumberFormatException;
import java.io.IOException;
import java.io.FileNotFoundException;

/*
 * Class to read data from files and create OrbitInfo or 
 *      SpaceObject objects.  Performs no real validation 
 *      or graceful error recovery.
 *
 * Files have one object per row, with fields split by ";".
 *      Fields are identified by index in line.
 */
class DataReader {

    BufferedReader reader;      // reader object

    DataReader() {}

    /*
     * Read from file given as parameter.
     * @param fname Filename to read from.
     */
    DataReader(String fname) {
        try {
            reader = new BufferedReader(new FileReader(fname));
        } catch(FileNotFoundException fnf) {
            System.err.println("Can't open file: " + fnf);
            fnf.printStackTrace();
        }
    }

    /*
     * Close reader object.  I think this 
     *      is unnecessary?
     */
    public void close() {
        try {
            reader.close();
        } catch(IOException ioe) {
        }
    }

    /*
     * Create a SpaceObject object from a line of the 
     *      file.  These represent objects that are in 
     *      proximity to target orbit states (e.g. 
     *      spacecraft near rendezvous points).
     *      Returns null if EOF.  Skips lines that can't 
     *      be parsed.
     * @return SpaceObject from file.
     */
    public SpaceObject getSpaceObject() {
        SpaceObject object = null;
        // keep reading the line can't be parsed.
        //      Hopefully can recover from a mangled 
        //      line.
        while(object == null) {
            try {
                String line = reader.readLine();
                // if EOF
                if(line == null) {
                    reader.close();
                    return null;
                }
                String[] objectL = line.split(";");
                if(objectL.length != 8) {
                    continue;
                }
                // Target orbit identifier
                int o_id = Integer.parseInt(objectL[0]);
                // Name of target (e.g. space station name)
                String o_name = objectL[1];
                // state vector
                double[] o_state = new double[6];
                for(int i = 0; i < 6; i++) {
                    o_state[i] = Double.parseDouble(objectL[2 + i]);
                }
                object = new SpaceObject(o_id, o_name, o_state);
            // if parsing failure, skip line and hope for the 
            //      best.
            } catch (NumberFormatException nfe) {
                continue;
            } catch(Exception e) {
                System.err.println("error reading objects: " + e);
                e.printStackTrace();
            }
        }
        return object;
    }

    /*
     * Read target orbit state objects (e.g. space stations) 
     *      from file.  These include identifiers, the state 
     *      vector, and Kepler elements.
     * @return OrbitInfo object of a target, or null if EOF.
     */
    public OrbitInfo getOrbitData() {
        try {
            String line = reader.readLine();
            // if EOF
            if(line == null) {
                reader.close();
                return null;
            }
            String[] orbitL = line.split(";");
            // ID number
            int o_id = Integer.parseInt(orbitL[0]);
            OrbitInfo orbit = new OrbitInfo(o_id);
            // Name of target  
            orbit.setName(orbitL[1]);
            double[] elements = new double[13];
            // next elements are all floating point values
            for(int i = 0; i < 13; i++) {
                elements[i] = Double.parseDouble(orbitL[2 + i]);
            }

            // set state vector elements
            double x = elements[0];
            double y = elements[1];
            double z = elements[2];
            orbit.setPos(x, y, z);
            x = elements[3];
            y = elements[4];
            z = elements[5];
            orbit.setVel(x, y, z);
            // set Kepler elements
            orbit.setAxis(elements[6]);
            orbit.setEcc(elements[7]);
            orbit.setInclin(elements[8]);
            orbit.setRAsc(elements[9]);
            orbit.setArgPeri(elements[10]);
            orbit.setF(elements[11]);
            orbit.setM(elements[12]);
            return orbit;
        // if can't parse
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


