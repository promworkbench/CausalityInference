package org.processmining.causalityinference.noiseFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.processmining.causalityinference.evaluation.Evaluation;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

public class NoiseFunctionEstimator {
/**
 * Continuous data
 * 
 * input: data + sem
 * estimate the noise function in the form of a uniform or a gaussian 
 * 
 * Steps:
 * 
 * 	for source nodes
 * 		determine the distribution and the parameters of the error vector
 * 
 *  for non-source node, 
 *  	1. determine its parents.
 * 		2. determine the coefficient.
 * 		3. generate the error vector
 * 		4. determine the distribution and the parameters of the error vector
 */
	
	
	private LinkedList<Map<String, Object>> data;
	private Graph dag;
	
	/**
	 * threshold for p-value
	 */
	private double threshold;
	private Evaluation eval; 
	
	public NoiseFunctionEstimator(LinkedList<Map<String, Object>> data, Graph dag, Evaluation eval) {
		this.data = data;
		this.dag = dag;
		this.eval = eval;
	}
	
	/**
	 * Estimate the noise function of the given node.
	 * Determines if the noise function is normal or gaussian.
	 * Normal distribution: key = "Normal" val = min, max, -1, -1
	 * Gaussian distribution: key = "Gaussian" val = min, max, mean, variance
	 * Neither of the above distributions: key = "non" val = min, max, -1, -1
	 * @param node
	 * @return 
	 */
	public Object[] estimateNoiseFunction(Node node) {	
		Object[] res = new Object[3];
		
		double[] vec = turn2vec(node);
		GaussianDistTest gaussianTest = new GaussianDistTest(vec);
		if (gaussianTest.ifGaussianDist(threshold)) {
			res[0] = "Gaussian";
			res[1] = gaussianTest.getMean();
			res[2] = gaussianTest.getSTDev();
			res[3] = gaussianTest.getMean();
			res[4] = gaussianTest.getSTDev();
			return res;
		} 
		
		UniformDistTest uniformTest = new UniformDistTest(vec);
		if (uniformTest.isUniform(threshold)) {
			res[0] = "Uniform";
			res[1] = uniformTest.minValue();
			res[2] = uniformTest.maxValue();
			res[3] = -1;
			res[4] = -1;
			return res;
		}
		
		res[0] = "non";
		res[1] = uniformTest.minValue();
		res[2] = uniformTest.maxValue();
		res[3] = -1;
		res[4] = -1;
			 
		return res;
	}
	
	/**
	 * Computes the vector of the noise values
	 * @param node
	 * @return the noise vector
	 */
	public double[] turn2vec(Node node) {
		double[] vec = null;
		String attName = node.getName();
		LinkedList<Double> vecList = new LinkedList<>();
		
		if (isSourceNode(node)) {
			
			for (Map<String, Object> row : data) {
				if (row.containsKey(attName))
					if (row.get(attName) != null)
						vecList.add(getDoubleVal(row.get(attName)));
			}
			
		} else {
			List<Node> parents = dag.getParents(node);
			for (Map<String, Object> row : data) {
				if (getNoiseVal(row, node, parents) != null)
					vecList.add(getNoiseVal(row, node, parents));
			}
		}
		
		vec = new double[vecList.size()];
		for (int i = 0; i < vecList.size(); i++)
			vec[i] = vecList.get(i);

		return vec;
	}
	
	/**
	 * for the given row, return the noise value of the node. 
	 * @param row
	 * @param node
	 * @param parents
	 * @return the noise value for node in row 
	 */
	private Double getNoiseVal(Map<String, Object> row, Node node, List<Node> parents) {
		String attName = node.getName();
		if (!row.containsKey(attName))
			return null;
		for (Node p : parents) 
			if (!row.containsKey(p.getName()))
				return null;
		
		double noiseValue = getDoubleVal(row.get(attName)); 
		for (Node p : parents) {
			double c = eval.getEdgeCoefficient(p.getName(), attName);
			noiseValue = noiseValue - c * getDoubleVal(row.get(p.getName()));
		}
		
		return noiseValue;
	}

	/**
	 * Convert object (integer, long, double) into double.
	 * @param o 
	 * @return double value of o
	 */
	private Double getDoubleVal(Object o) {
		if (o instanceof Double)
			return (Double) o;
		if (o instanceof Integer)
			return Double.valueOf((int) o);
		if (o instanceof Long)
			return Double.valueOf((long) o);
			
		return null;
	}
	
	/**
	 * true : node is a source node
	 * false : o.w.
	 * @param node
	 * @return if node is a source node 
	 */
	private boolean isSourceNode(Node node) {
		List<Node> parents = dag.getParents(node);
		if (parents == null || parents.size() == 0)
			return true;
		
		return false;
	}
}
