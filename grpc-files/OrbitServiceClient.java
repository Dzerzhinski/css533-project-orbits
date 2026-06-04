package io.grpc.examples.orbitxfer;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.StringBuilder;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

class OrbitServiceClient {

    private static final Logger logger = Logger.getLogger(OrbitServiceClient.class.getName());
    private final OrbitXferSolnGrpc.OrbitXferSolnBlockingStub blockingStub;

    private List<OrbitInfo> orb_l;
    private List<SpaceObject> spaceObj_l;

    OrbitServiceClient(Channel channel, String orbFile, String objFile) {
        blockingStub = OrbitXferSolnGrpc.newBlockingStub(channel);
        orb_l = getOrbList(orbFile);
        spaceObj_l = getSpaceObjectList(objFile);
    }

    private List<OrbitInfo> getOrbList(String fname) {
        List<OrbitInfo> list = new ArrayList<OrbitInfo>();
        DataReader reader = new DataReader(fname);
        OrbitInfo o = reader.getOrbitData();
        int ctr = 0;
        while(o != null) {
            ctr++;
            list.add(o);
            o = reader.getOrbitData();
        }
        System.out.println("Read " + ctr + " orbits.");
        return (ctr == 0) ? null : list;
    }

    private List<SpaceObject> getSpaceObjectList(String fname) {
        List<SpaceObject> list = new ArrayList<SpaceObject>();
        DataReader reader = new DataReader(fname);
        SpaceObject so = reader.getSpaceObject();
        int ctr = 0;
        while(so != null) {
            ctr++;
            list.add(so);
            so = reader.getSpaceObject();
        }
        System.out.println("Read " + ctr + "objects.");
        return list;
    }

    private void requestSolns() {
        Date startTime = new Date();
        String output = "";
        int ctr = 0;
        for(SpaceObject obj : spaceObj_l) {
            ctr++;
            if(ctr % 50 == 0) {
                System.out.print(output);
                output = "";
            }
            output += getOrbitSoln(obj);
        }
        System.out.println(output);
        Date endTime = new Date();
        System.out.printf("Found " + ctr + " solutions in " + 
                (endTime.getTime() - startTime.getTime()) + "ms.\n\n");

    }

    private String getOrbitSoln(SpaceObject spaceObj) {
        //OrbitInfo o = orb_l.get(0);
        // TestVessel tv = new TestVessel(o);
        double[] state = spaceObj.getStateVector();
        Craft req = Craft.newBuilder()
                        .setId(spaceObj.getTargetId())
                        .setTf(0.0)
                        .setRx(state[0])
                        .setRy(state[1])
                        .setRz(state[2])
                        .setVx(state[3])
                        .setVy(state[4])
                        .setVz(state[5])
                        .build();
        Soln res;
        try {
            res = blockingStub.getImpulseSoln(req);
        } catch(StatusRuntimeException sre) {
            System.err.println("client err: " + sre);
            sre.printStackTrace();
            return "";
        }
        String output = solnOutput(spaceObj, state, res);
        return output;
    }

    private String solnOutput(SpaceObject oi, double[] initState, Soln res) {
        StringBuilder sb = new StringBuilder();
        Formatter fs = new Formatter(sb);
        fs.format("Orbit: %d\tName: %s\n", oi.getTargetId(), oi.getTargetName());
        fs.format("\tr: [ %4.4f, %4.4f, %4.4f ], v: [ %4.4f, %4.4f, %4.4f ]\n", 
                        initState[0], initState[1], initState[2], 
                        initState[3], initState[4], initState[5]);
        fs.format("\t\tinitial: time: %4.4f, dv: [ %4.4f, %4.4f, %4.4f ]\n", 
                        res.getTi(), res.getDvix(), res.getDviy(), res.getDviz());
        fs.format("\t\tfinal: time: %4.4f, dv: [ %4.4f, %4.4f, %4.4f ]\n", 
                        res.getTf(), res.getDvfx(), res.getDvfy(), res.getDvfz());
        return sb.toString();
    }





    public static void main(String args[]) {

        if(args.length < 3) {
            System.err.println("Need <server:port> <orbit filename>");
        }
        String target = args[0];
        String orbitFile = args[1];
        String objectFile = args[2];
        if(args[0].equals("0")) {
            target = "localhost:28030";
        }
        if(args[1].equals("0")) {
            orbitFile = "/home/john/Documents/533/project/data/test_data.txt";
        }
        if(args[2].equals("0")) {
            objectFile = "/home/john/Documents/533/project/data/rand-obj-small.txt";
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, 
                            InsecureChannelCredentials.create()).build();

        
        try {
            OrbitServiceClient client = new OrbitServiceClient(channel, orbitFile, objectFile);
            client.requestSolns();
        } finally {
            try {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch(InterruptedException ie) {
            }
        }

    }
}

