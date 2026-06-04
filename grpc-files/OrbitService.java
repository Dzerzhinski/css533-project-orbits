package io.grpc.examples.orbitxfer;

import java.util.HashMap;
import java.util.Map;


class OrbitService {

    public static void main(String args[]) {
        if(args.length < 1) {
            System.err.println("need orbit files.");
            System.exit(-1);
        }

        String fname;
        if(args[0].equals("0")) {
            fname = "/home/john/Documents/533/project/data/test_data.txt";
        } else {
            fname = args[0];
        }


        Map<Integer, OrbitObj> map = new HashMap<Integer, OrbitObj>();
        DataReader reader = new DataReader(fname);
        OrbitInfo entry = reader.getOrbitData();
        int ctr = 0;
        while(entry != null) {
            ctr++;
            OrbitObj orb = new OrbitObj(entry.getName(), entry.makeOrbit());
            map.put(entry.getId(), orb);
            entry = reader.getOrbitData();
        }
        System.out.println("Read in " + ctr + " orbits.");

        OrbitXferServer.launchService(map);
        
    }
}

