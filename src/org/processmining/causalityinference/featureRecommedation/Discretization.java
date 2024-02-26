package org.processmining.causalityinference.featureRecommedation;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

public class Discretization {
	
	/**
	 * discretize the numerical features except the class feature.
	 */
	
	private LinkedList<Map<String, Object>> data;
	
	private Map<String, Type> types;
	
	private Map<String, Object[]> minMax;
	
	/**
	 * the set of attribute names to be discretize
	 */
	private Set<String> attNames2Disc;
	
	/**
	 * Number of bins when the attribute in numerical.
	 */
	private int numBins;
	
	/**
	 * for each numerical value attribute includes a list of interval.
	 * Discretization is done based on the given number of bins;
	 */
	private Map<String, LinkedList<String>> intervals;
	/**
	 * for each numerical value attribute includes a list of bounds of intervals.
	 * Lower value is included in the interval but the upper value is not.
	 */
	private Map<String, LinkedList<Double[]>> intervalBorders;
	
	
	/**
	 * Discrete numerical features based on the given number of bins.
	 * @return Discretized data
	 */
	public LinkedList<Map<String, Object>> discretize() {
		LinkedList<Map<String, Object>> newData = new LinkedList<>();
		
		//if all the attributes are nominal, no need for discretization.
		boolean flag = true;
		for (String attName : types.keySet()) 
			if (!types.get(attName).equals(Type.BOOLEAN) && !types.get(attName).equals(Type.LITERAL))
				flag = false;
		if (flag) 
			return data;
		
		setBins();
		
		// find the set of the features to be discretized. 
		// Those discrete features with cardinality less than numBins should not be discretized.
		
		for (Map<String, Object> instance : data) {
			Map<String, Object> newInstance = new HashMap<>();
			for (String attName : instance.keySet()) 
				if (attNames2Disc.contains(attName)) {
					if (types.get(attName).equals(Type.BOOLEAN) || types.get(attName).equals(Type.LITERAL))
						newInstance.put(attName, instance.get(attName));
					else
						newInstance.put(attName, getBin(attName, instance.get(attName)));
				} else
					newInstance.put(attName, instance.get(attName));
			
			newData.add(newInstance);
					
		}
		
		return newData;
	}
	
	private void setBins() {
		intervals = new HashMap<>();
		intervalBorders = new HashMap<>();
		
		for (String attName : types.keySet()) 
			if (!types.get(attName).equals(Type.BOOLEAN) && !types.get(attName).equals(Type.LITERAL))
				discretizeOneFeature(attName);
	}
	/**
	 * @param attName
	 * for the given attribute add the string interval to "intervals"
	 * and add the borders in long format to the "intervalBoreders"
	 */
	private void discretizeOneFeature(String attName) {
		
		LinkedList<String> list = new LinkedList<>();
		LinkedList<Double[]> borders  = new LinkedList<>();
		
		//TODO customize for timestamp
		double min = getDoubleValue(minMax.get(attName)[0]);
		double max = getDoubleValue(minMax.get(attName)[1]);
		
		Double length = (max - min) / numBins;
		
		Double i =  round(min, 2);
		Double j = min;
		for (; j <= max; round(i += length, 2)) {
//			System.out.println(round(i, 2));
			if (j != max) {
				j = round(i + length, 2);
				list.add("[" + round(i, 2) + ", " + round(j, 2) + ")" );
				Double[] b = {round(i, 2), round(j, 2)};
				borders.add(b);
			} else
				break;
		}
		
		if (i != Math.round(max)) {
			list.removeLast();
			list.add("[" + round(i-length, 2) + ", " + (Math.round(max)) + "]");
			borders.removeLast();
			Double[] b = {round(i-length, 2), round(max + 1, 2)};
			borders.add(b);
		}
			
		
		intervals.put(attName, list);
		intervalBorders.put(attName,borders);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public double getDoubleValue(Object v) {
		double d = 0;
		if (v instanceof Integer)
			d = ((Integer) v).doubleValue();
		else if (v instanceof Long)
			d = ((Long) v).doubleValue();
		else if (v instanceof Date)
			d = ((Date) v).getTime();
		else
			d = (double) v;
		
		return d;
	}
	private Object getBin(String attName, Object value) {
		Double v = getDoubleValue(value);
		
		int i = 0;
		for (Double[] borders : intervalBorders.get(attName)) {
			if (borders[0] <= v && borders[1] > v) {
//				System.out.println(intervals.get(attName).get(i));
				return intervals.get(attName).get(i);
			}
			i++;
		}
		
		return null;
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
	
	public void setNumBins(int num) {
		numBins = num;
	}
	
	public void setTypes(Map<String, Type> t) {
		types = t;
	}
	
	public Map<String, Type> getTypes() {
		return types;
	}
	
	
	public void setData(LinkedList<Map<String, Object>> d) {
		data = d;
	}
	
	public void setMinMax(Map<String, Object[]> map) {
		minMax = map;
	}
	
	public void setAttNames2Disc(Set<String> set) {
		attNames2Disc = set;
	}
	
	public Map<String, LinkedList<String>> getIntervals() {
		return intervals;
	}
	
	/**	private int whichBin(String attName, double d) {

		double min = getDoubleValue(minMax.get(attName)[0]);
		double max = getDoubleValue(minMax.get(attName)[1]);
		
		if (d == max)
			return numBin - 1;
		
		double d1 = (max - min) / numBin;
		
		return (int) Math.floor((d - min) / d1);
	}
	*/

	
	private void testDataConversion() {
		// Set feature names and types
		Map<String, Type> testType = new HashMap<>();
		testType.put("item", Type.LITERAL);
		testType.put("damaged", Type.BOOLEAN);
		testType.put("timestamp", Type.TIMESTAMP);
		testType.put("price", Type.CONTINUOS);
		testType.put("level", Type.DISCRETE);
		setTypes(testType);
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
			row.put("price", i*10 + randInt(0,9) + Math.random());
			row.put("level", i%3 + 1);
			
			testData.add(row);	
		}
		data = testData;
		
		Set<String> set = new HashSet<>();
		set.add("timestamp");
		set.add("level");
		attNames2Disc = set;
		
		System.out.println("Original data");
		for (Map<String, Object> instance : data) {
			String str = new String();
			for (String attName : types.keySet())
				str = str + "  " + attName + ": " + instance.get(attName);
			System.out.println(str);
		}	
		// Set minMax
		minMax = new HashMap<>();
		for (Map<String, Object> instance : data) {
			for (String attName : getTypes().keySet()) {
				if (types.get(attName).equals(Type.CONTINUOS) || types.get(attName).equals(Type.DISCRETE)) {
					if (minMax.containsKey(attName)) {
						if (getDoubleValue(minMax.get(attName)[0]) > getDoubleValue(attName, instance))
							minMax.get(attName)[0] = getDoubleValue(attName, instance);
						else if (getDoubleValue(minMax.get(attName)[1]) < getDoubleValue(attName, instance))
							minMax.get(attName)[1] = getDoubleValue(attName, instance);
					} else {
						Object[] mm = new Object[2];
						mm[0] = getDoubleValue(attName, instance);
						mm[1] = getDoubleValue(attName, instance);
						minMax.put(attName, mm);
					}
						
				}
				
				if (types.get(attName).equals(Type.TIMESTAMP)) {
					long val = ((Date) instance.get(attName)).getTime();
					if (minMax.containsKey(attName)) {
						if (getDoubleValue(minMax.get(attName)[0]) > getDoubleValue(attName, instance))
							minMax.get(attName)[0] = getDoubleValue(attName, instance);
						else if (getDoubleValue(minMax.get(attName)[1]) < getDoubleValue(attName, instance))
							minMax.get(attName)[1] = getDoubleValue(attName, instance);
					} else {
						Object[] mm = new Object[2];
						mm[0] = val;
						mm[1] = val;
						minMax.put(attName, mm);
					}
				} 
			}
		}
	}

	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	public static void main(String[] args) {	
		Discretization disc = new Discretization();
		disc.setNumBins(5);
		disc.testDataConversion();
		
		// Discretize 
		LinkedList<Map<String, Object>> disceretizedTestData = disc.discretize();
		
		System.out.println("\n\n ***********************");
		System.out.println("Discretized data");
		for (Map<String, Object> instance : disceretizedTestData) {
			String str = new String();
			for (String attName : disc.getTypes().keySet())
				str = str + "  " + attName + ": " + instance.get(attName);
			System.out.println(str);
		}		
	}
}
