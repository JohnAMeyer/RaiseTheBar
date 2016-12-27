package edu.nd.raisethebar;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static android.R.attr.data;
import static org.apache.commons.math3.stat.StatUtils.variance;

/**
 * Created by jack1 on 12/26/2016.
 */

public class DataAnalysis {
    static RealMatrix covarianceMatrix(RealMatrix data) {
        return new Covariance(data).getCovarianceMatrix();
    }

    static double[] eigenvalues(RealMatrix matrix) {
        return new EigenDecomposition(matrix).getRealEigenvalues();
    }

    static RealVector eigenvectorFromValue(RealMatrix matrix, int i) {
        return new EigenDecomposition(matrix).getEigenvector(i);
    }

    static double[] components(RealMatrix matrix, RealVector norm) {
        double[] out = new double[matrix.getRowDimension()];
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            out[i] = matrix.getRowVector(i).dotProduct(norm);
        }
        return out;
    }

    static int counter(double[] data) {
        int n = 0;
        double z = 1D;
        boolean above = false;
        DescriptiveStatistics ds = new DescriptiveStatistics(10);
        for (int i = 0; i < data.length; i++) {
            ds.addValue(data[i]);
            if (i >= 10) {
                if((data[i]-ds.getMean())/ds.getStandardDeviation() > z && !above){
                    above = true;
                    n++;
                } else if((data[i]-ds.getMean())/ds.getStandardDeviation() < -z && above){
                    above = false;
                    n++;
                }
            }
        }
        return n / 2;
    }

    static Vector3D[] normalize(Vector3D[] data) {
        //does not function currently
        return null;
    }

    static Vector2D[] planarize(RealMatrix data, RealVector norm) {//might need to break in two statements
        //moves all values onto plane centered at 0,0
        Plane p = new Plane(new Vector3D(norm.toArray()));//TODO look at tolerances
        Vector2D[] out = new Vector2D[data.getRowDimension()];
        for (int i = 0; i < data.getRowDimension(); i++) {
            out[i] = p.toSubSpace(new Vector3D(data.getRowVector(i).toArray()));
        }
        return out;
    }

    static double rVariance(Vector2D[] data) {
        double[] values = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = data[i].distance(Vector2D.ZERO);
        }
        return variance(values);
    }
}
