package org.processmining.causalityinference.evaluation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.processmining.causalityinference.noiseFunction.NoiseFunctionEstimator;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

/**
 * for a given data and sem, we check which portion of the data is 
 * Producible by the sem.
 * 
 * subtask
 * 		for each instance check if it is producible by sem.
 * 
 * @author qafari
 *
 */

public class Evaluation {
	
	private LinkedList<Map<String, Object>> testData;
	private LinkedList<Map<String, Object>> trainData;
	
	private Graph dag;
	private Map<String, Double> edgeCoefficients;
	
	private Map<String, Map<String, Double>> parentCoeff;
	private Map<String, Double[]> noiseMinMax;
	
	public Evaluation(LinkedList<Map<String, Object>> trainData, LinkedList<Map<String, Object>> testData) {
		this.trainData = trainData;
		this.testData = testData;
	}
	
	public void initiate() {
		setParentCoeff();
		setNoiseMinMax();
	}
	

	/**
	 * create a map of the non source nodes.
	 * For each non source node a mapping of its parents and their coefficient is stored as the value.
	 */
	private void setParentCoeff() {
		parentCoeff = new HashMap<>();
		List<Node> nodes = dag.getNodes();
		for (Node node : nodes) {
			List<Node> parents = dag.getParents(node);
			if (parents != null && parents.size() > 0) {
				Map<String, Double> map = new HashMap<>();
				for (Node n : parents)
					map.put(n.getName(), getEdgeCoefficient(n.getName(), node.getName()));
				parentCoeff.put(node.getName(), map);
			}
		}
	}
	
	/**
	 * create a map in which 
 	 * key is an attName 
 	 * value in a double array {min, max} of the minimum and maximum noise value of the attribute
	 */
	private void setNoiseMinMax() {
		noiseMinMax = new HashMap<>();
		NoiseFunctionEstimator estimator = new NoiseFunctionEstimator(trainData, dag, this);
		for (Node node : dag.getNodes()) {
			double[] vec = estimator.turn2vec(node);
			Double[] minMax = {vec[0], vec[0]};
			for (int i = 0; i < vec.length; i++) {
				if (minMax[0] > vec[i])
					minMax[0] = vec[i];
				else if (minMax[1] < vec[i])
					minMax[1] = vec[i];
			}
			noiseMinMax.put(node.getName(), minMax);
		}
		
	}

	/**
	 * 
	 * @param instance
	 * @return a vector of noises. One noise value for each attribute.
	 */
	public Map<String, Double> noiseValues(Map<String, Object> instance) {
		Map<String, Double> noises = new HashMap<>();
		
		for (String attName : instance.keySet()) {
			if(parentCoeff.containsKey(attName))
				noises.put(attName, getNoise(attName, instance));
			else
				noises.put(attName, doubleValue(instance.get(attName)));
		}
		
		return noises;
	}
	
	/**
	 * compute the noise value of a non source node
	 * @param attName 
	 * @param instance
	 * @return the noise value
	 */
	private double getNoise(String attName, Map<String, Object> instance) {
		double noise = doubleValue(instance.get(attName));
		Map<String, Double> map = parentCoeff.get(attName);
		for (String parentName : map.keySet())
			if (instance.containsKey(parentName))
				noise = noise - (doubleValue(instance.get(parentName)) * map.get(parentName));
		
		return noise;
	}

	/**
	 * true if the instance is possible considering the sem
	 * false o.w
	 * @param instance
	 * @return if instance comply with sem
	 */
	public boolean ifPossible(Map<String, Object> instance) {
		Map<String, Double> noises = noiseValues(instance);
		for (String attName : noises.keySet()) 
			if (noises.get(attName) <= noiseMinMax.get(attName)[0] && noises.get(attName) >= noiseMinMax.get(attName)[1])
				return false;
		
		return true;
	}
	
	/**
	 * 
	 * @param testData
	 * @return portion of the instances in the test data that comply with sem
	 */
	public double accuracy() {
		double num = 0d;
		
		for (Map<String, Object> instance : testData) 
			if (ifPossible(instance))
				num = num + 1;
		
		return num / testData.size();
	}
	
	/**
	 * Convert object (integer, long, double) into double.
	 * @param o 
	 * @return double value of o
	 */
	private Double doubleValue(Object o) {
		if (o instanceof Double)
			return (Double) o;
		if (o instanceof Integer)
			return Double.valueOf((int) o);
		if (o instanceof Long)
			return Double.valueOf((long) o);
			
		return null;
	}
	
	public double getEdgeCoefficient(String first, String second) {
		for (String edge : edgeCoefficients.keySet()) {
			String[] parts = edge.split(" ");
			if (first.equals(parts[0]) && second.equals(parts[2]))
				return edgeCoefficients.get(edge);
		}
		return 0;
	}


	public void setGraphAndCoeff(Graph dag, Map<String, Double> edgeCoefficients) {
		this.dag = dag;
		this.edgeCoefficients = edgeCoefficients;
	}
	
}
