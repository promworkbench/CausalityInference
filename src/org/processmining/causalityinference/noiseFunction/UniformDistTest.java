package org.processmining.causalityinference.noiseFunction;

import static java.lang.Math.pow;
import static java.util.Arrays.stream;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;
 
public class UniformDistTest {
	
	double[] data;
	
	public UniformDistTest(double[] data) {
		this.data = data;
	}
	
 
    public double dist() {
        double avg = stream(data).sum() / data.length;
        double sqs = stream(data).reduce(0, (a, b) -> a + pow((b - avg), 2));
        return sqs / avg;
    }
 
    static double prob(double dof, double distance) {
        return Gamma.regularizedGammaQ(dof / 2, distance / 2);
    }
 
    public boolean isUniform(double significance) {
        return prob(data.length - 1.0, dist()) > significance;
    }
    
    public double minValue() {
    	double min = data[0];
    	for (int i = 0; i < data.length; i++) 
    		if (data[i] < min) 
    			min = data[i];
    	
    	return min;
    }
    
    
    public double maxValue() {
    	double max = data[0];
    	for (int i = 0; i < data.length; i++) 
    		if (data[i] > max) 
    			max = data[i];
    	
    	return max;
    }
 
    public static void main(String[] a) {
        double[][] dataSets = {{199809, 200665, 199607, 200270, 199649},
        {522573, 244456, 139979, 71531, 21461}};
 
        System.out.printf(" %4s %12s  %12s %8s   %s%n",
                "dof", "distance", "probability", "Uniform?", "dataset");
 
        for (double[] ds : dataSets) {
            int dof = ds.length - 1;
            UniformDistTest test = new UniformDistTest(ds);
            double dist = test.dist();
            double prob = prob(dof, dist);
            System.out.printf("%4d %12.3f  %12.8f    %5s    %6s%n",
                    dof, dist, prob, test.isUniform(0.05) ? "YES" : "NO",
                    Arrays.toString(ds));
        }
    }
}