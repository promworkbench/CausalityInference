package org.processmining.causalityinference.noiseFunction;

import net.sourceforge.jdistlib.disttest.NormalityTest;

public class GaussianDistTest {

	double[] data;

	
	public GaussianDistTest(double [] d) {
		data = d;
//		normalization(0, 1);
	}
	
	public double p_value_normal_dist() {
	//	AndersonDarlingDist test = new AndersonDarlingDist(data.length);
		NormalityTest test = new NormalityTest();
		double p_value = test.shapiro_wilk_pvalue(test.shapiro_wilk_statistic(data), data.length);
		return p_value;
	}
	
	
	/**
	 * t is the significance threshold.
	 * True : p_value >= t 
	 * false : o.w.
	 * @return
	 */
	public boolean ifGaussianDist(double t) {
		if (p_value_normal_dist() >= t)
			return true;
		
		return false;
	}
	
	public double getMean() {
		double sum = data[0];
		for (int i = 1; i < data.length; i++)
			sum += data[i];
		
		return sum / data.length;
	}
	
	public double getSTDev() {
		
		double mean = getMean();
		
		double variance = 0;
		for (int i = 0; i < data.length; i++) {
		    variance += Math.pow(data[i] - mean, 2);
		}
		variance /= data.length;

		// Standard Deviation
		return Math.sqrt(variance);
	}
	
	public static void main(String[] args) {
		double[] data = {1, 2.2, 5, 5,5, 5.75, 6, 6.25, 7, 8, 11};
		GaussianDistTest test = new GaussianDistTest(data);
		System.out.println("p_value : " + test.p_value_normal_dist());
	}
}
