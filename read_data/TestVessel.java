import java.lang.Math;
import java.util.Random;

/*
 * This class generates test data for a given target 
 *      orbit state.
 * Given a target orbit's identifier and state vector, 
 *      the class generates state vectors with offsets 
 *      which are then solved for.  The generated state 
 *      vectors are generated with a random offset that 
 *      is within the bounds for a valid application of 
 *      the CW equations.
 * Basically, for a target orbit (e.g. space station) 
 *      it generates reasonable requests for an 
 *      state transition solution, e.g. a spacecraft 
 *      requesting intercept impulse values.
 */

class TestVessel {

    private double[] pos;           // target position
    private double[] vel;           // target velocity
    private int orbit_id;           // target id
    private String orbit_name;      // target name

    private Random rng;             // Random generator

    TestVessel() { }

    TestVessel(OrbitInfo o) {
        double[] state = o.getState();
        this.pos = new double[]{state[0], state[1], state[2]};
        this.vel = new double[]{state[3], state[4], state[5]};
        this.orbit_id = o.getId();
        this.orbit_name = o.getName();
        this.rng = new Random();
    }

    public int getOrbitId() { return this.orbit_id; }

    /*
     * Generates a random state vector that is sufficiently 
     *      near that the CW equations generate a valid 
     *      intercept solution.
     * @return state vector in the vicinity of the target
     */
    public double[] randStateVector() {
        // position distance should be within 1% of the 
        //      orbit radius
        double[] r_pos = randVector(0.01, this.pos);
        // not actually sure what valid scaling for 
        //      velocity is
        double[] r_vel = randVector(1.0, this.vel);
        // return value
        double[] res = new double[6];
        for(int i = 0; i < 3; i++) {
            res[i] = r_pos[i];
            res[i + 3] = r_vel[i];
        }
        return res;
    }

    /*
     * Create a vector with a random offset from the given 
     *      parameter.  Magnitude of difference is less 
     *      than the scaling factor given.
     * For input vector v1 and input factor a, a random unit 
     *      vector u is chosen so, 
     *          (a|v1|) * u + v1 = v2
     *      where v2 is the output vector.
     * @param factor scaling factor for the upper bound of 
     *      the magnitude of the random vector added to the 
     *      argument
     * @param vec random vector is based on this given vector
     * @return vector with a random offset
     */
    private double[] randVector(double factor, double[] vec) {
        // get magnitude of given vector
        double norm = getNorm(vec);
        double[] rand_v = randomUnitVector();
        double rand_scal = (norm * factor) * rng.nextDouble();
        rand_v = scaleVector(rand_scal, rand_v);
        rand_v = addVector(vec, rand_v);
        return rand_v;
    }

    /*
     * Vector math helper functions.  I really should have found 
     *      a good numerical methods library so I don't have to 
     *      keep re-implementing these basic operations.
     */

    private double[] addVector(double[] v1, double[] v2) {
        double[] res = new double[v1.length];
        for(int i = 0; i < v1.length; i++) {
            res[i] = v1[i] + v2[i];
        }
        return res;
    }

    private double getNorm(double[] v) {
        double res = 0;
        for(int i = 0; i < v.length; i++) {
            res += v[i] * v[i];
        }
        return Math.sqrt(res);
    }

    private double[] scaleVector(double x, double[] vec) {
        double[] res = new double[vec.length];
        for(int i = 0; i < vec.length; i++) {
            res[i] = vec[i] * x;
        }
        return res;
    }

    private double[] randomUnitVector() {
        double[] rand_v = new double[3];
        for(int i = 0; i < 3; i++) {
            rand_v[i] = rng.nextDouble();
        }
        double norm = getNorm(rand_v);
        rand_v = scaleVector((1 / norm), rand_v);
        return rand_v;
    }

}
