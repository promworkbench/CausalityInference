package org.processmining.causalityinference.featureRecommedation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.datadiscovery.estimators.Type;

import com.espertech.esper.collection.Pair;

public class InfoGain {
	private LinkedList<Map<String, Object>> discretizedData;
	
	private String classAttName;
	
	private Map<String, Type> types;
	
	/**
	 * a sorted list of information gain for all the values of features.
	 */
	private Map<String, LinkedList<Pair<String, Double>>> infoGain;
	
	/**
	 * In case of nominal target feature, it is the set of desirable
	 * values.
	 */
	private Set<String> undesirableValues;
	
	/**
	 * In case of numerical target feature, it is the threshold for
	 * desirable values.
	 */
	private double threshold;
	
	/**
	 * In case of numerical target feature, shows if lower or 
	 * higher than threshold is desirable.
	 */
	private boolean isLowerUnDesirable;
	
	
	/**
	 * The set of values for the features in attNames.
	 */
	private Map<String, Set<String>> literalValues;
	
	/**
	 * number of desirable class att value in the data. For computing the initial entropy.
	 */
	private Double numGood = 0d;
	
	/**
	 * This is the data before the discretization for checking if an instance is desirable
	 * in case of discrete class attribute name
	*/
	private LinkedList<Map<String, Object>> nonDiscData; 
	
	 Parameters params;
	
	 public InfoGain() {
		 
	 }
	
	public InfoGain(Parameters params) {
		this.params = params;
		classAttName = params.getClassAttNameReplaced();
		types = params.getAttTypes();
		undesirableValues = params.getUndesirableValuesFR();
		threshold = params.getThresholdFR();
		isLowerUnDesirable = params.getIsLowerUnDesirableFR();
		
	}

	public void compute(Set<String> attNames) {
		
		// set literal values
		setLiteralValues(attNames);

		// initiate the infoGain map
		initiateInfoGain(attNames);
		
		// Compute counts
		Map<String, Map<String, Double[]>> counts = computeCounts(attNames);
		
		for (String attName : counts.keySet()) {
			String str = attName + ": ";
			for (String value : counts.get(attName).keySet())
				str = str + value + " " + counts.get(attName).get(value)[0]+ 
				" " + counts.get(attName).get(value)[1];
			System.out.println(str);
		}
		
		// Compute information gain
		ComputeInfoGain(counts);
		
	}
	
	private void ComputeInfoGain(Map<String, Map<String, Double[]>> counts) {
		double num = discretizedData.size();
		double numBad = num - numGood;
		Double entropyWhole = entropy(numGood, numBad);
		
		for (String attName : counts.keySet()) {
			for (String value : counts.get(attName).keySet()) {
				double numVG = counts.get(attName).get(value)[0]; // number of instances with (value and goodResult)
				double numNVG = numGood - numVG;  // number of instances with (!value and goodResult)
				double numVB = counts.get(attName).get(value)[1];   // number of instances with (Value and badResult)
				double numNVB = numBad - numVB;
				
				double entropyValue = ((numVG + numVB) / num) * entropy(numVG, numVB) + 
						((numNVG + numNVB) / num) * entropy(numNVG, numNVB);
				
				infoGain.get(attName).add(new Pair(value, entropyWhole - entropyValue));
			}
		}	
		
		sort();
	}
	
	public void sort() {
		for (String attName : infoGain.keySet())
			Collections.sort(infoGain.get(attName), new pairComparator());
	}
	
	static class pairComparator implements Comparator<Pair<String, Double>>
	 {
	     public int compare(Pair<String, Double> p1, Pair<String, Double> p2)
	     {
	         return p1.getSecond().compareTo(p2.getSecond());
	     }
	 }
	
	public LinkedList<Pair<Pair<String, String>, Double>> generateList(Double threshold){
		LinkedList<Pair<Pair<String, String>, Double>> finalList = new LinkedList<>();
		for (String attName: infoGain.keySet()) {
			if (!infoGain.get(attName).isEmpty()) {
				int i = 0;
				while (i < infoGain.get(attName).size() && infoGain.get(attName).get(i).getSecond() >= threshold) {
					finalList.add(new Pair(new Pair(attName, infoGain.get(attName).get(i).getFirst()),
							infoGain.get(attName).get(i).getSecond()));
					i++;
				}
			}
		}
		
		sort(finalList);
		
		return finalList;
	}
	
	public Set<String> getRecommendedFeatures() {
		return infoGain.keySet();
	}
	
	private void sort(LinkedList<Pair<Pair<String, String>, Double>> list) {
		Collections.sort(list, new listPairComparator());
	}
	
	static class listPairComparator implements Comparator<Pair<Pair<String, String>, Double>>
	 {
	     public int compare(Pair<Pair<String, String>, Double> p1, Pair<Pair<String, String>, Double> p2)
	     {
	         return -(p1.getSecond().compareTo(p2.getSecond()));
	     }
	 }

	
	public double entropy(double n1, double n2) {
		if (n1 == 0d || n2 == 0d)
			return 0d;
		
		double n = n1+ n2;
		return - ((n1/n) * (Math.log(n1/n)/Math.log(2)) + (n2/n) * (Math.log(n2/n)/Math.log(2)));
	}

	/**
	 * attName, value, [0]: good+value; [1]: bad+value;
	 * @param attNames
	 * @return count of the 
	 */
	private Map<String, Map<String, Double[]>> computeCounts(Set<String> attNames) {
		Map<String, Map<String, Double[]>> counts = new HashMap<>(); // 
		for (String attName : attNames) { // initiate count
			Map<String, Double[]> oneFeature = new HashMap<>();
			for (String value : literalValues.get(attName)) {
				Double[] arr = new Double[2];
				arr[0] = 0d; arr[1] = 0d;
				oneFeature.put(value, arr);
			}
			counts.put(attName, oneFeature);
		}
		
		for (int i = 0; i < discretizedData.size(); i++) {
			Map<String, Object> instance = discretizedData.get(i);
			Map<String, Object> nonDiscInstance = nonDiscData.get(i);
			if (isUnDesirableInstance(nonDiscInstance)) {
				for (String attName : attNames) {
					if (instance.containsKey(attName) && instance.get(attName) != null) {
						Double[] arr = counts.get(attName).get(instance.get(attName).toString());
						arr[1]++;
						counts.get(attName).put(instance.get(attName).toString(), arr);
					}
				}
			} else {
				numGood++;
				for (String attName : attNames) {
					if (instance.containsKey(attName) && instance.get(attName) != null) {
//						System.out.println(attName);
//						System.out.println(" " + instance.get(attName));
//						System.out.println(" " +  counts.get(attName).get(instance.get(attName).toString()));
						Double[] arr = counts.get(attName).get(instance.get(attName).toString());
						arr[0]++;
						counts.get(attName).put(instance.get(attName).toString(), arr);
					}
				}
			}
		}
		return counts;
	}

	private void initiateInfoGain(Set<String> attNames) {
		infoGain = new HashMap<>();
		for (String attName : attNames) {
			LinkedList<Pair<String, Double>> list = new LinkedList<>();
			infoGain.put(attName, list);
		}
	}

	private void setLiteralValues(Set<String> attNames) {
		literalValues = new HashMap<>();
		for (String attName : attNames)
			literalValues.put(attName, new HashSet<String>());
		for (Map<String, Object> instance : discretizedData) 
			for (String attName : attNames) {
			//	if (!literalValues.containsKey(attName))
			//		System.out.println("here");
				if (instance.containsKey(attName) && instance.get(attName) != null)
					literalValues.get(attName).add(instance.get(attName).toString());
			}
	}

	/**
	 * 
	 * @param instance
	 * @return true if the class value is desirable and false o.w.
	 */
	private boolean isUnDesirableInstance(Map<String, Object> instance) {
		if (types.get(classAttName).equals(Type.BOOLEAN) || types.get(classAttName).equals(Type.LITERAL)) {
			if (undesirableValues.contains(instance.get(classAttName).toString()))
				return true;
			else
				return false;
		} else {
			double d = getDoubleValue(classAttName, instance);
			if (isLowerUnDesirable && d <= threshold)
				return true;
			else if (!isLowerUnDesirable && d >= threshold)
				return true;
			else
				return false;
		}
	}
	
	/**
	 * In case of numerical feature
	 * @param attName
	 * @param instance
	 * @return return the double value of the feature value.
	 */
	private double getDoubleValue(String attName, Map<String, Object> instance) {
		if (types.get(attName).equals(Type.CONTINUOS))
			return (double) instance.get(attName);
		if (types.get(attName).equals(Type.DISCRETE)) {
			Object v = instance.get(attName);
			if (v instanceof Integer)
				return ((Integer) v).doubleValue();
			else if (v instanceof Long)
				return ((Long) v).doubleValue();
		}
		
		if (types.get(attName).equals(Type.TIMESTAMP)) {
			Date d = (Date) instance.get(attName);
			long l = d.getTime();
			return ((Long) l).doubleValue();
		}
			 
		return 0;
	}

	public void setDiscretizedData(LinkedList<Map<String, Object>> data) {
		discretizedData = data;
	}
	
	public void setNonDiscData(LinkedList<Map<String, Object>> data) {
		nonDiscData = data;
	}
	
	// ----------------------- Test code -------------------------
	private void testInfoGain() {
		// Set feature names and types
		Map<String, Type> testType = new HashMap<>();
		testType.put("item", Type.LITERAL);
		testType.put("damaged", Type.BOOLEAN);
		testType.put("timestamp", Type.TIMESTAMP);
		testType.put("price", Type.CONTINUOS);
		testType.put("level", Type.DISCRETE);
		types = testType;
		// Set data
		LinkedList<Map<String, Object>> testData = new LinkedList<>();
		String[] items = {"phone", "laptop", "phone", "plant", "glass", "phone", "laptop", "phone", "plant", "glass"};
		for (int i = 0; i < 10; i++) {
			Map<String, Object> row = new HashMap<>();
			row.put("item", items[i]);
			if (i%2 == 0)
				row.put("damaged", true);
			else
				row.put("damaged", false);
			Date date = new Date(1982, i, i, i, i, i);
			row.put("timestamp", date);
			row.put("price", i*10 + 4 + Math.random());
			row.put("level", i%3 + 1);
			
			testData.add(row);	
		}
		discretizedData = testData;
		
		System.out.println("\n\nData: ");
		for (Map<String, Object> instance : discretizedData) {
			String str = new String();
			for (String attName : types.keySet())
				str = str + "  " + attName + ": " + instance.get(attName);
			System.out.println(str);
		}
	
		classAttName = "price";
		threshold = 40d;
		isLowerUnDesirable = false;
		
		Set<String> set = new HashSet<>();
		set.add("item");
		set.add("damaged");
		compute(set);
		
		System.out.println("\n\ninfoGain: ");
		for (String attName : infoGain.keySet()) {
			System.out.println(attName);
			String str = new String();
			for (Pair<String, Double> value : infoGain.get(attName)) {
				str = str + "   " + value.getFirst() + ": " + value.getSecond();
			}
			System.out.println(str);
		}
			
	}
	
	public static void main(String args[]) {
		InfoGain ig = new InfoGain();
		System.out.println("entropy : ");
		System.out.println("5 and 5 : " + ig.entropy(5d, 5d));
		System.out.println("10 and 0 : " + ig.entropy(10d, 0d));
		
		ig.testInfoGain();
		
		
	}
}
