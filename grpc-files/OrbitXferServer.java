package io.grpc.examples.orbitxfer;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Map;

public class OrbitXferServer {

    private static final Logger logger = Logger.getLogger(OrbitXferServer.class.getName());
    
    private Server server;
    private Map<Integer, OrbitObj> orbit_m;

    OrbitXferServer() {
        orbit_m = null;
    }

    OrbitXferServer(Map<Integer, OrbitObj> map) {
        this.orbit_m = map;
    }

    private void blockUntilShutdown() {
        if(server != null) {
            try {
                server.awaitTermination();
            } catch(InterruptedException ie) {
            }
        }
    }

    public static void launchService(Map<Integer, OrbitObj> m) {
        try {
            final OrbitXferServer s = new OrbitXferServer(m);
            s.start();
            s.blockUntilShutdown();
        } catch(Exception e) {
            System.err.println("service exception: " + e);
            e.printStackTrace();
        }
    }




    private void start() throws IOException {

        int port = 28030;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                            .executor(executor)
                            .addService(new OrbitXferSolnService(this.orbit_m))
                            .build()
                            .start();
    }

    static class OrbitXferSolnService extends OrbitXferSolnGrpc.OrbitXferSolnImplBase {

        private final Map<Integer, OrbitObj> map;

        OrbitXferSolnService(Map<Integer, OrbitObj> orb_map) {
            boolean empty_map = true;
            if(orb_map != null) {
                empty_map = orb_map.isEmpty();
            }
            if(empty_map) {
                System.err.println("Empty orbit map!");
            }
            this.map = orb_map;
        }

        @Override
        public void getImpulseSoln(Craft req, StreamObserver<Soln> responseObserver) {
            int oid = req.getId();
            //System.err.println("orbit id: " + oid);
            //System.out.println();
            // get vector parameters
            double time = req.getTf();
            double[] stateVec = new double[]{
                                req.getRx(),
                                req.getRy(),
                                req.getRz(),
                                req.getVx(),
                                req.getVy(),
                                req.getVz(),
            };
            // find orbit in list
            //
            // if not found
            if(this.map.containsKey(oid) == false) {
                Soln reply = invalidReqReply();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            }
            OrbitObj orb = this.map.get(oid);
            // get orbit transfer solution
            double[][] xferSoln = orb.getSoln(time, stateVec); 
            Soln reply = Soln.newBuilder()
                            .setValid(true)
                            .setTi(xferSoln[0][0])
                            .setDvix(xferSoln[0][1])
                            .setDviy(xferSoln[0][2])
                            .setDviz(xferSoln[0][3])
                            .setTf(xferSoln[1][0])
                            .setDvfx(xferSoln[1][1])
                            .setDvfy(xferSoln[1][2])
                            .setDvfz(xferSoln[1][3])
                            .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    private static Soln invalidReqReply() {
        Soln reply= Soln.newBuilder()
            .setValid(false)
            .setTi(0.0)
            .setDvix(0.0)
            .setDviy(0.0)
            .setDviz(0.0)
            .setTf(0.0)
            .setDvfx(0.0)
            .setDvfy(0.0)
            .setDvfz(0.0)
            .build();
        return reply;
    }
}
            
