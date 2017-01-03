package edu.nd.raisethebar;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static org.apache.commons.math3.stat.StatUtils.variance;

/**
 * A class that currently holds all the analyzing functions used - this is so that when computations become more complex it is isolated here.
 * Uses Apache Commons Math.
 *
 * @author jack1
 * @since 12/26/2016
 */
public class DataAnalysis {
    private static EigenDecomposition eig;
    public final String TAG = "RTB-analysis";

    /**
     * Gets a covariance matrix of a dataset.
     *
     * @param data the dataset to analyze
     * @return an NxN covariance matrix
     */
    static RealMatrix covarianceMatrix(RealMatrix data) {
        return new Covariance(data).getCovarianceMatrix();
    }

    /**
     * Gets the eigenvalues of a given NxN matrix.
     *
     * @param matrix the NxN matrix
     * @return a list of the values
     */
    static double[] eigenvalues(RealMatrix matrix) {
        assert matrix.getRowDimension() == matrix.getColumnDimension();
        return (eig = new EigenDecomposition(matrix)).getRealEigenvalues();
    }

    /**
     * Gets the eigenvector corresponding to an eigenvalue. MUST BE CALLED AFTER THE CORRESPONDING FUNCTION.
     *
     * @param i the index of the desired eigenvalue
     * @return the eigenvector corresponding to this eigenvalue
     * @see DataAnalysis.eigenvalues(RealMatrix matrix)
     */
    static RealVector eigenvectorFromValue(int i) {
        assert eig != null;
        return eig.getEigenvector(i);
    }

    /**
     * Gets the components of a dataset in the direction of the axis.
     *
     * @param matrix the dataset
     * @param norm   the axis to take the component along
     * @return the set of all components
     */
    static double[] components(RealMatrix matrix, RealVector norm) {
        double[] out = new double[matrix.getRowDimension()];
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            out[i] = matrix.getRowVector(i).dotProduct(norm);
        }
        return out;
    }

    /**
     * Method which attempts to count the number of peaks in the dataset.
     *
     * @param data the 2-D dataset
     * @return the number of wavelengths
     */
    static int counter(double[] data) {
        int n = 0;
        double z = 1D;
        boolean above = false;
        DescriptiveStatistics ds = new DescriptiveStatistics(10);
        for (int i = 0; i < data.length; i++) {
            ds.addValue(data[i]);
            if (i >= 10) {
                if ((data[i] - ds.getMean()) / ds.getStandardDeviation() > z && !above) {
                    above = true;
                    n++;
                } else if ((data[i] - ds.getMean()) / ds.getStandardDeviation() < -z && above) {
                    above = false;
                    n++;
                }
            }
        }
        return n / 2;
    }

    /**
     * Moves the data into the plane described by the axis. Used to remove the motion in the axis.
     *
     * @param data the 3-D dataset
     * @param norm the normal vector for the plane
     * @return the coordinates in 2-D in the plane
     */
    static Vector2D[] planarize(RealMatrix data, RealVector norm) {//might need to break in two statements
        //moves all values onto plane centered at 0,0
        Plane p = new Plane(new Vector3D(norm.toArray()));//TODO look at tolerances
        Vector2D[] out = new Vector2D[data.getRowDimension()];
        for (int i = 0; i < data.getRowDimension(); i++) {
            out[i] = p.toSubSpace(new Vector3D(data.getRowVector(i).toArray()));
        }
        return out;
    }

    /**
     * Gets the variance of the distance from (0,0).
     *
     * @param data the coordinates in a plane.
     * @return the calculated variance
     */
    static double rVariance(Vector2D[] data) {
        double[] values = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = data[i].distance(Vector2D.ZERO);
        }
        return variance(values);
    }
}
