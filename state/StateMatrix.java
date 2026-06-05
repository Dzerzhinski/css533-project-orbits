import java.lang.Math;
import java.io.Serializable;


/*
 * Class encapsulates the CW state transition function.
 */

public class StateMatrix 
        implements Serializable {

    private double[][] M_f;     // state transition 
    private double[][] N_f;     //      submatrices
    private double[][] S_f; 
    private double[][] T_f; 

    private double[][] N_inv_f; // inverse of N

    private double t_f;         // time parameter
    private double s;           // sin(nt)
    private double c;           // cos(nt)
    private double n;           // mean anomaly constant
                                // M = nt

    ///////////////////////////////////////////////////////
    //                                                   //
    // CONSTRUCTORS                                      //
    //                                                   //
    ///////////////////////////////////////////////////////
    
    StateMatrix() {
        this(Math.PI, 1.0);
    }

    StateMatrix(double t) {
        this(t, 1.0);
    }

    StateMatrix(double t, double n) {
        this.n = n;
        this.t_f = t;

        // init terms, matrices
        this.c = Math.cos(n * t);
        this.s = Math.sin(n * t);

        this.M_f = initM(t);
        this.N_f = initN(t);
        this.N_inv_f = initNinv(t);
        this.S_f = initS(t);
        this.T_f = initT(t);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    // PUBLIC METHODS                                    //
    //                                                   //
    ///////////////////////////////////////////////////////


    /*
     * Solve for initial impulse (delta v).  Calculates the 
     *      initial impulse for transition to target state 
     *      at time t_f.
     * @param initial position vector
     * @param initial velocity vector
     * @return delta-v vector for interception at time t
     */
    public double[] initialImpulse(double[] dr0, double[] dv0) {
        // delta-v = dv0 - [-N_inv M] dr0
        // temporary result for matrix ops
        double[][] m = new double[3][3];
        // 
        m = matrixMultiply(N_inv_f, M_f);
        // result vector
        double[] v = new double[3];
        v = matrixTransform(m, dr0);
        v = invertVector(v);
        v = vectorSub(v, dv0);
        double dv_test = 0;
        for(int i = 0; i < 3; i++) {
            dv_test += v[i] * v[i];
        }
        dv_test = Math.sqrt(dv_test);
        return v;
    }

    /*
     * Solve for final impulse (delta-v).  Calculate 
     *      the final impulse for transition to 
     *      target state at time t_f.
     * @param dr0 Position vector at time t=0
     * @return final impulse for state transition
     */
    public double[] endImpulse(double[] dr0) {
        // delta-v = [T Ninv M - S] dr0 
        double[][] m = new double[3][3];
        m = matrixMultiply(N_inv_f, M_f);
        m = matrixMultiply(T_f, m);
        m = matrixSub(m, S_f);
        double[] v = new double[3];
        v = matrixTransform(m, dr0);
        return v;
    }
 



    ///////////////////////////////////////////////////////
    //                                                   //
    // HELPER METHODS                                    //
    //                                                   //
    ///////////////////////////////////////////////////////
 
    /*
     * Initialize state transition submatrices.
     *      M, N, S, T, and inverse N.
     * @param t time
     * @return state transition submatrix
     */

    private double[][] initM(double t) {
        double[][] M = new double[3][3];
        M[0][0] = 4.0 - (3 * c);
        M[0][1] = 0;
        M[0][2] = 0;
        M[1][0] = 6 * (s - (n * t));
        M[1][1] = 1; 
        M[1][2] = 0;
        M[2][0] = 0; 
        M[2][1] = 0;
        M[2][2] = c;
        return M;
    }

    private double[][] initN(double t) {
        double[][] N = new double[3][3];
        N[0][0] = s / n;
        N[0][1] = (2 * (1 - c)) / n;
        N[0][2] = 0;
        N[1][0] = - (2 * (1 - c)) / n;
        N[1][1] = ((4 * s) - (3 * n * t)) / n;
        N[1][2] = 0;
        N[2][0] = 0;
        N[2][1] = 0;
        N[2][2] = s / n;
        return N;
    }

    private double[][] initNinv(double t) {
        double[][] Ninv = new double[3][3];
        // for convenience, apply determinant to each term
        double det = n / ((8 * s * s * s) - (3 * s * s * n * t));
        // System.err.printf("det: %8.8f\n\n", det);
        Ninv[0][0] = ((4 * s * s) - (3 * s * n * t)) * det;
        Ninv[0][1] = (-2 * s * (1 - c)) * det;
        Ninv[0][2] = 0;
        Ninv[1][0] = ((2 * s * (1 - c))) * det;
        Ninv[1][1] = (s * s) * det;
        Ninv[1][2] = 0;
        Ninv[2][0] = 0;
        Ninv[2][1] = 0;
        // term mostly cancels determinant
        Ninv[2][2] = n / s;
        return Ninv;
    }

    private double[][] initS(double t) {
        double[][] S = new double[3][3];
        S[0][0] = 3 * n * s;
        S[0][1] = 0; 
        S[0][2] = 0;
        S[1][0] = (-6 * n * (1 - c));
        S[1][1] = 0;
        S[1][2] = 0;
        S[2][0] = 0;
        S[2][1] = 0;
        S[2][2] = -(n * s);
        return S;
    } 

    private double[][] initT(double t) {
        double[][] T = new double[3][3];
        T[0][0] = c;
        T[0][1] = 2 * s;
        T[0][2] = 0;
        T[1][0] = -2 * s;
        T[1][1] = (4 * c) - 3;
        T[1][2] = 0;
        T[2][0] = 0;
        T[2][1] = 0; 
        T[2][2] = c;
        return T;
    }

    /*
     * Multiply two matrices.
     * @param o1 first (left) matrix
     * @param o2 second (right) matrix
     * @return 3x3 matrix result
     */
    private double[][] matrixMultiply(double[][] o1, double[][] o2) {
        double[][] prod = new double[3][3];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                prod[i][j] = 0;
                for(int k = 0; k < 3; k++) {
                    prod[i][j] += o1[i][k] * o2[k][j];
                }
            }
        }
        return prod;
    }

    /*
     * Add or subtract two 3x3 matrices.
     * @param op Subtract if op < 0, add otherwise
     * @param o1 first (left) matrix
     * @param o2 second (right) matrix
     * @return 3x3 matrix result
     */
    private double[][] elementwiseOpMatrix(int op, double[][] o1, double[][] o2) {
        double[][] result = new double[3][3];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                result[i][j] = (op < 0) ? o1[i][j] - o2[i][j] : o1[i][j] + o2[i][j];
            }
        }
        return result;
    }

    /*
     * Add two 3x3 matrices.
     */
    private double[][] matrixAdd(double[][] o1, double[][] o2) {
        return elementwiseOpMatrix(1, o1, o2);
    } 

    /*
     * Subtract two 3x3 matrices.
     */
    private double[][] matrixSub(double[][] o1, double[][] o2) {
        return elementwiseOpMatrix(-1, o1, o2);
    }

    /*
     * Add or subtract two vectors.
     * @param op Subtract if op < 0, add otherwise
     * @param v1 vector operand (left)
     * @param v2 vector operand (right)
     * @return vector result
     */
    private double[] elementwiseOpVector(int op, double[] v1, double[] v2) {
        double[] result = new double[3];
        for(int i = 0; i < 3; i++) {
            result[i] = (op < 1) ? v1[i] - v2[i] : v1[i] + v2[i];
        }
        return result;
    }

    /*
     * Add two vectors.
     * @param v1 vector operand (left) 
     * @param v2 vector operand (right)
     * @return vector sum
     */
    private double[] vectorAdd(double[] v1, double[] v2) {
        return elementwiseOpVector(1, v1, v2);
    }

    /*
     * Subtract two vectors.
     * @param v1 minuend (left)
     * @param v2 subtrahend (right)
     * @return vector difference
     */
    private double[] vectorSub(double[] v1, double[] v2) {
        return elementwiseOpVector(-1, v1, v2);
    }

    /*
     * Apply (multiply) a matrix A to a vector x, i.e. Ax = y.
     * @param A 3x3 matrix
     * @param x a 3-element vector
     * @return the 3-element vector result
     */
    private double[] matrixTransform(double[][] A, double[] x) {
        double[] result = new double[3];
        for(int i = 0; i < 3; i++) {
            result[i] = 0;
            for(int j = 0; j < 3; j++) {
                result[i] += A[i][j] * x[j];
            }
        }
        return result;
    }

    /*
     * Get vector additive inverse (negative).
     * @param vector operand
     * @preturn inverse of vector (-v)
     */
    private double[] invertVector(double[] v) {
        double[] r = new double[3];
        for(int i = 0; i < 3; i++) {
            r[i] = -(v[i]);
        }
        return r;
    }

    /*
     * Print out matrix to stdout.  Used for debugging.
     * @param m Matrix to print
     */
    private void printMatrix(double[][] m) {
        for(int i = 0; i < 3; i++) {
            System.out.print("\t\t| ");
            for(int j = 0; j < 3; j++) {
                System.out.printf("%4.4f ", m[i][j]);
            }
            System.out.println("|");
        }
        System.out.println();
    }

    /*
     * Print out vector to stdout.  Used for debugging.
     * @param v Vector to print.
     */
    private void printVector(double[] v) {
        for(int i = 0; i < v.length; i++) {
            System.out.printf("\t\t| %4.4f |\n", v[i]);
        }
        System.out.println();
    }
}






