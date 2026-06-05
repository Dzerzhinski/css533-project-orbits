import java.lang.Math;
import java.io.Serializable;

/*
 * Represents a target orbit.  Solves state transtion 
 *      problem for impulse to transition to target 
 *      orbit.
 */
class Orbit 
        implements Serializable {

    private double[] r;
    private double[] v; 

    private double a;       // semi major axis
    private double e;       // eccentricity
    private double i;       // inclination
    private double O;       // Right ascension of asc node
    private double o;       // arg of periapsis 
    private double f;       // true anamoly
    private double M;       // mean anamoly
    private double t_p;     // time of periapsis

    private final double EPSILON = 1e-4;
    private final double MU = 3.986e5;

    private double n_mean;

    private StateMatrix state;

    ///////////////////////////////////////////////////////
    //                                                   // 
    // CONSTRUCTORS                                      //
    //                                                   //
    ///////////////////////////////////////////////////////
    
    Orbit() {

    }

    Orbit(double[] stateVec) {
        this.r = new double[]{
                            stateVec[0],
                            stateVec[1],
                            stateVec[2],
        };

        this.v = new double[]{
                            stateVec[3],
                            stateVec[4],
                            stateVec[5],
        };
    }

    /*
     * Takes state vector and Kepler elements of 
     *      orbit.  Required for correct function.
     */
    Orbit(double[] r_0, 
            double[] v_0, 
            double axis, 
            double eccentricity, 
            double inclination, 
            double right_asc, 
            double arg_peri, 
            double anomaly_t, 
            double anomaly_m) {
        this.r = new double[]{r_0[0], r_0[1], r_0[2]}; 
        this.v = new double[]{v_0[0], v_0[1], v_0[2]};
        this.a = axis;
        this.e = eccentricity; 
        this.i = Math.toRadians(inclination); 
        this.O = Math.toRadians(right_asc);
        this.o = Math.toRadians(arg_peri);
        this.f = Math.toRadians(anomaly_t);
        this.M = Math.toRadians(anomaly_m);
        findTimePeriapsis();

        this.n_mean = Math.sqrt(MU / (a * a * a));
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    // PUBLIC METHODS                                    //
    //                                                   //
    ///////////////////////////////////////////////////////
    
    /*
     * Get impulse vectors (initial and final) for orbit state 
     *      transition.  Defaults to 3/4-orbit period for 
     *      transition.
     * @param stateVec orbit state vector
     * @return array with initial and final impulse vectors
     *          [[time initial, <impulse vector], 
     *           [time final, <impulse vector>]]
     */
    public double[][] getImpulse(double[] stateVec) {
        return getImpulse(0.0, stateVec);
    }

    /*
     * Get impulse vectors (initial and final) for orbit state transition 
     *      at given time.
     * @param t_f time for intercept (from now), if 0.0 replaced 
     *      with 3/4-orbit period
     * @param stateVec orbit state vector ([position velocity])
     * @return array, [[<inital time, <impulse vector>], 
     *                 [<final time, <impulse vector>]]
     */
    public double[][] getImpulse(double tf_req, double[] stateVec) {
        double t_f;
        // if null parameter, replace with 3/4 period
        if(tf_req == 0.0) {
            t_f = getThreeQuarterOrbit();
        } else {
            t_f = tf_req;
        }
        // sanity check to avoid zero divisor when 
        //      calculating state matrix
        if(Math.sin(t_f * n_mean) < EPSILON) {
            t_f += 2 * Math.asin(EPSILON);
        }

        // initialize state transition matrix
        this.state = new StateMatrix(t_f, n_mean);
        // initialize reference frame transform class
        Frame refFrame = new Frame();
        // unnecessary steps splitting state vector
        double[] posVec = new double[]{stateVec[0], stateVec[1], stateVec[2]};
        double[] velVec = new double[]{stateVec[3], stateVec[4], stateVec[5]};
        // transform to target reference frame
        double[] tfmState = refFrame.transformToFrame(posVec, velVec);
        double[] tfmPosVec = new double[3];
        double[] tfmVelVec = new double[3];
        for(int i = 0; i < 3; i++) {
            tfmPosVec[i] = tfmState[i];
            tfmVelVec[i] = tfmState[3 + i];
        }
        // solve for initial impulse
        double[] impulse1 = state.initialImpulse(tfmPosVec, tfmVelVec);
        // transform to ECI
        double[] impulse1t = refFrame.transformdVFromFrame(impulse1);
        double i1fnorm = getNorm(impulse1);
        double i1tnorm = getNorm(impulse1t);

        // get true anomaly at time t_f (point in orbit at time t_f)
        double f_t = findfAtTime(t_f);
        // get reference frame at time t_f
        Frame refFrame2 = new Frame(f_t);
        double[] impulse2 = state.endImpulse(tfmPosVec);
        double[] impulse2t = refFrame2.transformdVFromFrame(impulse2);
        double i2fnorm = getNorm(impulse2);
        double i2tnorm = getNorm(impulse2t);
        double[][] res = new double[2][4];
        res[0][0] = 0.0;
        res[1][0] = t_f;
        for(int i = 0; i < 3; i++) {
            res[0][i + 1] = impulse1t[i];
            res[1][i + 1] = impulse2t[i];
        }
        return res;
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    // TEST METHODS                                      //
    //                                                   //
    ///////////////////////////////////////////////////////
    
    /*
     * Print orbit characteristics to stdout.  For debugging.
     */
    public void printFields() {
        System.out.printf("position: [%4.4f, %4.4f, %4.4f]\n", r[0], r[1], r[2]);
        System.out.printf("velocity: [%4.4f, %4.4f, %4.4f]\n", v[0], v[1], v[2]);
        System.out.printf("semi major axis: %4.4f\n", this.a);
        System.out.printf("eccentricity: %4.4f\n", this.e);
        System.out.printf("inclination: %4.4f\n", this.i);
        System.out.printf("node of right ascension: %4.4f\n", this.O);
        System.out.printf("argument of periapse: %4.4f\n", this.o);
        System.out.printf("true anomaly: %4.4f\n", this.f);
        System.out.printf("mean anomaly: %4.4f\n", this.M);
        return;
    }


    /*
     * Test out solver for f at time t.
     * @param t time parameter
     * @return f (true anomaly) at time t
     */
    public double testFindfAtTime(double t) { return findfAtTime(t); }

    /*
     * Get basis (transform) matrix for orbit reference frame.  
     *      Gets basis matrix and prints to stdout.
     */
    public void testBasis() {
        Frame frame = new Frame();
        double[][] test = frame.testBasisMatrix();
        System.out.println("Basis matrix: ");
        printMatrix(test);
        System.out.println();
    }

    /*
     * Test transform of position and velocity vectors to orbit 
     *      reference frame.  Transforms state vector and 
     *      prints to stdout.
     * @param pos position vector
     * @param vel velocity vector
     */
    public void testTransform(double[] pos, double[] vel) {
        System.out.println("Testing transform: ");
        Frame frame = new Frame();
        double[] orbitVec = frame.transformToFrame(pos, vel);
        printVector(orbitVec);
    }
 
    ///////////////////////////////////////////////////////
    //                                                   //
    // HELPER METHODS                                    //
    //                                                   //
    ///////////////////////////////////////////////////////

    /*
     * Get 3/4 of orbit period.  Good default time.
     * @return 3/4 period
     */
    private double getThreeQuarterOrbit() {
        return (3 * Math.PI) / (2 * n_mean);
    }

    /*
     * Get norm (magnitude) of vector.
     * @param v Vector to norm
     * @return Norm (magnitude) of vector
     */
    private double getNorm(double[] v) {
        double res = 0;
        for(int i = 0; i < v.length; i++) {
            res += v[i] * v[i];
        }
        return Math.sqrt(res);
    }

    /*
     * Get time at periapse (perigee).
     * @return true anomaly
     */
    private double getTimePeriapsisTrue() {
        // tan (E/2) = sqrt(1 - e/ 1 + e) tan(f/2)
        double E = getEfromf(this.f);
        double M_i = E - (e * Math.sin(E));
        return getTimeFromMean(M_i);
    }

    private double getfFromE(double E) {
        // tan (E/2) = sqrt(1 - e / 1 + e) tan(f/2)
        double x = Math.tan(E / 2);
        x = Math.sqrt((1 + e) / (1 - e)) * x;
        x = Math.atan(x);
        return 2 * x;
    }

    private double getEfromf(double f) {
        double x = Math.tan(f / 2);
        x = Math.sqrt((1 - e) / (1 + e)) * x;
        x = Math.atan(x);
        return 2 * x;
    }

    private double getTimeFromMean(double M_i) {
        // t = M/n
        double n = Math.sqrt(MU / (a * a * a));
        return M_i / n;
    }

    private double getMeanFromTime(double t) {
        // M = nt
        double n = Math.sqrt(MU / (a * a * a));
        return t * n;
    }

    private void findTimePeriapsis() {
        double t;
        if(f == 10) {
            t = getTimeFromMean(this.M); 
        } else {
            t = getTimePeriapsisTrue();
        } 
        this.t_p = t;
    }

    private double findfAtTime(double t) {
        double E = findEAtTime(t);
        // System.out.printf("E: \t%.4f\n", E);
        return getfFromE(E);
        
    }

    private double findEAtTime(double t) {
        // Newton's method: 
        //      E_(i+1) = E_i - (F(E) / F'(E))
        //      F(E) = E - e sin(E) - M
        //      F'(E) = 1 - e cos(E)
        // Start M <= E <= M + e, so initial guess 
        //      E_0 = M + (e / 2)
        double M_0 = getMeanFromTime(t);
        // System.out.printf("M: \t%.3f\n", M_0);
        double E = M_0 + (e / 2);
        double F_E = 1.0;
        // int ctr = 0;
        do {
            // ctr++;
            F_E = E - (e * Math.sin(E)) - M_0;
            double dF_dE = 1 - (e * Math.cos(E));
            E = E - (F_E / dF_dE);
        } while(F_E < EPSILON);
        // System.out.printf("ctr: \t%d\n", ctr);
        return E;
    }

    private void printMatrix(double[][] mat) {
        for(int i = 0; i < 3; i++) {
            System.out.print("\t | ");
            for(int j = 0; j < 3; j++) {
                System.out.printf("%3.6f ",  mat[i][j]);
            }
            System.out.println("| ");
        }
    }

    private void printVector(double[] vec) {
        for(int i = 0; i < vec.length; i++) {
            System.out.printf("\t | %3.6f |\n", vec[i]);
        }
        System.out.println();
    }

       



    /*
     * Helper class to encapsulate change of reference 
     *      frame to/from ECI.
     */
    private class Frame {
        // basis vectors
        private double[] x_hat;
        private double[] y_hat;
        private double[] z_hat;

        // target state vector
        private double[] r_f;
        private double[] v_f;

        // store trig function results for reuse
        private double[] eulerAngTrig;
        // true anomaly at given time
        private double fNow;

        Frame() { 
            this(Orbit.this.r, Orbit.this.v);
        }

        /* 
         * Constructor, frame of reference at a point in time 
         *      (based on true anomaly f_0)
         * @param f_0 make frame of reference at this true 
         *      anomaly
         */ 
        Frame(double f_0) {
            this.fNow = f_0;
            setEulerAngles();
            double cO = eulerAngTrig[0];
            double sO = eulerAngTrig[1];
            double cth = eulerAngTrig[2]; 
            double sth = eulerAngTrig[3];
            double ci = eulerAngTrig[4];
            double si = eulerAngTrig[5];
            setFrameExtVec();
            // transpose?
            this.x_hat = new double[3];
            this.y_hat = new double[3];
            this.z_hat = new double[3];
            this.x_hat[0] = (cth * cO) - (ci * sO * sth);
            this.y_hat[0] = -(sth * cO) -(ci * sO * cth);
            this.z_hat[0] = si * sO;
            this.x_hat[1] = (cth * sO) + (ci * cO * sth);
            this.y_hat[1] = -(sth * sO) + (ci * cO * cth);
            this.z_hat[1] = -(si * cO);
            this.x_hat[2] = si * sth;
            this.y_hat[2] = si * cth;
            this.z_hat[2] = ci;
        }
        
        Frame(double[] r, double[] v) {
            this.fNow = Orbit.this.f;
            setEulerAngles();
            this.r_f = r;
            this.v_f = v;
            // System.err.println("Running Frame constructor.");
            // Orbit.this.printVector(r);
            double r_norm = vectorNorm(r);
            this.x_hat = unitVector(r_norm, r);
            // printVector(x_hat);
            double v_norm = vectorNorm(v);
            this.y_hat = unitVector(v_norm, v);
            this.z_hat = vectorCrossProduct(this.x_hat, this.y_hat);
        }

        private void setEulerAngles() {
            this.eulerAngTrig = new double[6];
            double theta = o + fNow;
            eulerAngTrig[0] = Math.cos(O);
            eulerAngTrig[1] = Math.sin(O);
            eulerAngTrig[2] = Math.cos(theta);
            eulerAngTrig[3] = Math.sin(theta);
            eulerAngTrig[4] = Math.cos(i);
            eulerAngTrig[5] = Math.sin(i);
        }
 

        private double[] vectorOp(int op, double[] v1, double[] v2) {
            double[] res = new double[3];
            for(int i = 0; i < 3; i++) {
                res[i] = (op < 0) ? v1[i] - v2[i] : v1[i] + v2[i];
            }
            return res;
        }

        private double[] vectorAdd(double[] v1, double[] v2) {
            return vectorOp(1, v1, v2);
        }

        private double[] vectorSub(double[] v1, double[] v2) {
            return vectorOp(-1, v1, v2);
        }

        private double[] vectorScale(double scal, double[] vec) {
            double[] res = new double[]{
                                vec[0] * scal, 
                                vec[1] * scal, 
                                vec[2] * scal};
            return res;
        }

        /*
         * Get basis matrix for debugging.
         */
        public double[][] testBasisMatrix() {
            return(getBasisMatrix());
        }

        private double[][] getBasisMatrix() {
            double[][] basis = new double[3][3];
            for(int i = 0; i < 3; i++) {
                basis[0][i] = x_hat[i];
                basis[1][i] = y_hat[i];
                basis[2][i] = z_hat[i];
            }
            return basis;
        }

        /*
         * Get basis matrix for target-to-ECI transform.
         */
        private double[][] getReverseBasisMatrix() {
            double[][] basis = new double[3][3];
            for(int i = 0; i < 3; i++) {
                basis[i][0] = x_hat[i];
                basis[i][1] = y_hat[i];
                basis[i][2] = z_hat[i];
            }
            return basis;
        } 

        /*
         * Apply matrix to vector: 
         *          Ax = y
         * @param mat matrix
         * @param vec vector
         * @return product
         */
        private double[] vectorTfm(double[][] mat, double[] vec) {
            double[] res = new double[3];
            for(int i = 0; i < 3; i++) {
                res[i] = 0;
                for(int j = 0; j < 3; j++) {
                    res[i] += mat[i][j] * vec[j];
                }
            }
            return res;
        }

        /*
         * Transform state vectors to orbit frame.
         * @param pos position vector
         * @param vel velocity vector
         * @return state vector (r, v) in orbit frame
         */
        public double[] transformToFrame(double[] pos, double[] vel) {
            double[] dr = vectorSub(pos, r);
            
            double[] dv = vectorSub(vel, v_f);
            double[] rotVector = vectorScale(n_mean, z_hat);
            rotVector = vectorCrossProduct(rotVector, dr);
            dv = vectorSub(dv, rotVector);

            double[][] basis = getBasisMatrix();
            double[] drf = vectorTfm(basis, dr);
            double[] dvf = vectorTfm(basis, dv);

            double[] stateVec = new double[6];
            for(int i = 0; i < 3; i++) {
                stateVec[i] = drf[i];
                stateVec[i + 3] = dvf[i];
            }

            return stateVec;
        }

        /*
         * Take vector cross product.
         * @param v1 First (left) vector
         * @param v2 Second (right) vector
         * @return cross product result
         */
        private double[] vectorCrossProduct(double[] v1, double[] v2) {
            double[] r = new double[3];
            r[0] = (v1[1] * v2[2]) - (v1[2] * v2[1]);
            r[1] = (v1[2] * v2[0]) - (v1[0] * v2[2]);
            r[2] = (v1[0] * v2[1]) - (v1[1] * v2[0]);
            return r;
        }

        /*
         * Get vector norm.
         * @param vec vector to take norm.
         * @return norm of vector
         */
        private double vectorNorm(double[] vec) {
            double res = 0;
            for(int i = 0; i < 3; i++) {
                res += vec[i] * vec[i];
            }
            return Math.sqrt(res);
        }

        /*
         * Helper method, get unit vector by scaling by norm.
         * @param norm vector norm
         * @param vec vector to make unit
         * @return unit vector
         */
        private double[] unitVector(double norm, double[] vec) {
            double[] unit = new double[3];
            for(int i = 0; i < vec.length; i++) {
                unit[i] = vec[i] / norm;
            }
            return unit;
        }


        /*
         * Transform impulse (delta-v) vector from target 
         *      reference frame to ECI.
         */
        public double[] transformdVFromFrame(double[] dv) {
            double[] res;
            double theta = fNow + o;
            double[][] tMatrix = getReverseBasisMatrix();
            res = vectorTfm(tMatrix, dv);
            return res;
        }

        private void setFrameExtVec() {
            this.r_f = new double[3];
            this.v_f = new double[3];

            double theta = fNow + o;
            double cO = eulerAngTrig[0];
            double sO = eulerAngTrig[1];
            double cth = eulerAngTrig[2];
            double sth = eulerAngTrig[3];
            double ci = eulerAngTrig[4];
            double si = eulerAngTrig[5];
            double co = Math.cos(o);
            double so = Math.sin(o);

            // get satellite position at fNow
            // norm of r_f i.e. radius
            double r_f_n = (a * (1 - (e * e))) / (1 + (e * Math.cos(fNow)));
            r_f[0] = r_f_n * ((cO * cth) - (sO * sth * ci));
            r_f[1] = r_f_n * ((sO * cth) + (cO * sth * ci));
            r_f[2] = r_f_n * (sth * si);

            // get magnitude of specific angular momentum h
            double h = Math.sqrt(MU * a * (1 - (e * e)));
            double v_scale = MU / h;

            // reference frame v
            v_f[0] = v_scale * ((cO * (sth + (e * so))) + (sO * ci * (cth + (e * co))));
            v_f[1] = v_scale * ((sO * (sth + (e * so))) - (cO * ci * (cth + (e * so))));
            v_f[2] = v_scale * (si * (cth + (e * co)));
        }

            
    }

}




