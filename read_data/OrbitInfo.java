

/*
 * Class encapsulates target orbit state data.  Basically 
 *      a struct for relevant data.  Includes, 
 *          - id (a unique identifier within the dataset)
 *          - name of target
 *          - state vector
 *          - Kepler elements
 *      Can generate the Orbit object that implements the 
 *      functionality to find orbit intercept solutions.
 */
class OrbitInfo {

    int id;
    String name;
    double[] pos;
    double[] vel;
    double axis;            // semi major axis
    double ecc;             // eccentricity
    double inc;             // inclination
    double r_asc;           // node of right ascension
    double arg_peri;        // argument of periapse
    double f_true;          // true anomaly
    double M_mean;          // mean anomaly

    OrbitInfo() {}

    OrbitInfo(int nmbr) { this.id = nmbr; }


    /*
     * Setter methods.
     */
    public void setName(String n) { this.name = n; }
    public void setPos(double x, double y, double z) {
        this.pos = new double[]{x, y, z};
    }
    public void setVel(double x, double y, double z) {
        this.vel = new double[]{x, y, z};
    }
    public void setAxis(double a) { this.axis = a; }
    public void setEcc(double e) { this.ecc = e; }
    public void setInclin(double i) { this.inc = i; }
    public void setRAsc(double O) { this.r_asc = O; }
    public void setArgPeri(double o) { this.arg_peri = o; }
    public void setF(double f) { this.f_true = f; }
    public void setM(double M) { this.M_mean = M; }

    /*
     * Getter methods.
     */
    public int getId() { return this.id; }
    public String getName() { return this.name; }

    /*
     * Essentially a getter method for orbit data as well 
     *      as a quasi-factory class.
     */
    public Orbit makeOrbit() {
        Orbit o = new Orbit(
                this.pos, 
                this.vel, 
                this.axis, 
                this.ecc, 
                this.inc, 
                this.r_asc, 
                this.arg_peri, 
                this.f_true, 
                this.M_mean);
        return o;
    }

    public double[] getState() {
        double[] s = new double[]{pos[0], pos[1], pos[2], vel[0], vel[1], vel[2]};
        return s;
    }

}
