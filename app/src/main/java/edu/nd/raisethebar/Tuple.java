package edu.nd.raisethebar;

import java.util.Arrays;

/**
 * Created by jack1 on 10/2/2016.
 */

public class Tuple {
    long time;
    double[] data;

    public Tuple(double[] data, long time) {
        this.data = data;
        this.time = time;
    }
    public Tuple(float[] data, long time) {
        this.data = new double[data.length];
        for(int i = 0; i < data.length; i++){
            this.data[i] = data[i];
        }
        this.time = time;
    }
    @Override
    public String toString(){
        return "{" +time + ":" + Arrays.toString(data) + "}";
    }
}
