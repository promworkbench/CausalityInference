package org.processmining.causalityinference.featureRecommedation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.causalityinference.parameters.FeatureSelectionMethod;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.dataTable.AggregatedDataExtraction;
import org.processmining.datadiscovery.estimators.Type;

import com.espertech.esper.collection.Pair;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Utils;
/**
 *  1 - A target sensitive tabular data with as much features as possible is created.
 *  2 - For each feature
 *  		- for each value v count 
 *  				- the number of rows with value v  (nA)
 *  				- the number of rows with undesirable result with value v (nAB)
 *  				- percentage of all the problematic cases with value v (deltaPB)
 *  
 *  3 - Remove the cases that happens less than tA times
 *  4 - Sort the remaining ones based on nAB
 * @author Mahnaz
 *
 */
public class FeatureRecommendation {
	
	private AggregatedDataExtraction de;
	
	private LinkedList<Map<String, Object>> data;
	
	private Instances dataWEKA;
	
	private Map<String, Type> types;
	
	private String classAttName;
	
	private Map<String, Object[]> minMax;
	
	private FeatureSelectionMethod fsMethod;
	
//	private Map<String, Map<Object, Integer>> nA;
	
//	private Map<String, Map<Object, Integer>> nAB;
	
//	private Map<String, Map<Object, Double>> deltaPB;
	
	/**
	 * Number of bins when the attribute in numerical.
	 */
	private int numBin;
	
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
	 * The set of features selected by the feature selection method.
	 */
	private Set<String> selectedAtts;
	
	LinkedList<Pair<Pair<String, String>, Double>> finalListFeatureValues;
	
	
	 Parameters params;
	
	
	private LinkedList<Object[]> sortedList;
	
	public FeatureRecommendation(AggregatedDataExtraction d, Parameters params) {
		de = d;
		data = d.getTabularData();
		types = d.getAttTypes();
		classAttName = params.getClassAttNameReplaced();
		minMax = de.getMinMax();
		fsMethod = params.getFeatureSelectionMethod();
		this.params = params;
//		initiate();
	}

	/**
	 * for each value v of attribute at compute the number of 
	 * instances for which the value of at is v (nA). Also, computes
	 * the number of instances for which the value of at is v and
	 * the class value is bad (nAB).
	 
	public void generate_nA_nAB() {
		for (Map<String, Object> instance : data) {
			for (String attName : instance.keySet()) 
				update_nA_nAB(attName, instance);
		}
	}
	
	private void update_nA_nAB(String attName, Map<String, Object> instance) {
		if (types.get(attName).equals(Type.BOOLEAN) || types.get(attName).equals(Type.LITERAL))
			handelNominal(attName, instance);
		else {
			handelNumerical(attName, instance);
		}
	}
	
	/**
	 * Update nA and nAB when the feature is nominal.
	 * @param attName
	 * @param instance
	
	private void handelNumerical(String attName, Map<String, Object> instance) {
		Object value = instance.get(attName);
		if (value != null) {
			double d = getDoubleValue(attName, instance);
			int bin = whichBin(attName, d);
			boolean isGood = isUnDesirableInstance(instance);
			if (nA.containsKey(bin)) {
				Integer numA = nA.get(attName).get(bin) + 1;
				nA.get(attName).put(bin, numA);
				if (!isGood) {
					Integer numAB = nAB.get(attName).get(bin) + 1;
					nAB.get(attName).put(bin, numAB);
				}
			} else {
				nA.get(attName).put(bin, 1);
				if (isGood) 
					nAB.get(attName).put(bin, 0);
				else 
					nAB.get(attName).put(bin, 1);
			}
		}
	} */
	
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

	/**
	 * Update nA and nAB when the feature is nominal.
	 * @param attName
	 * @param instance
	 
	private void handelNominal(String attName, Map<String, Object> instance) {
		Object value = instance.get(attName);
		if (value != null) {
			boolean isGood = isUnDesirableInstance(instance);
			if (nA.get(attName).containsKey(value)) {
				Integer numA = nA.get(attName).get(value) + 1;
				nA.get(attName).put(value, numA);
				if (!isGood) {
					Integer numAB = nAB.get(attName).get(value) + 1;
					nAB.get(attName).put(value, numAB);
				}
			} else {
				nA.get(attName).put(value, 1);
				if (isGood) 
					nAB.get(attName).put(value, 0);
				else 
					nAB.get(attName).put(value, 1);
			}
		}
	} */
	
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
	/**
	public void initiate() {
		//initiate nA
		nA = new HashMap<>();
		nAB = new HashMap<>();
		for(String attName : types.keySet()) {
			Map<Object, Integer> mapA = new HashMap<>();
			nA.put(attName, mapA);
			Map<Object, Integer> mapAB = new HashMap<>();
			nAB.put(attName, mapAB);
		}		
	}
	
	public void generateList() {
		generate_nA_nAB();
		
		double numBad = numBadInstances();
		
		LinkedList<Object[]> list = new LinkedList<>();
		
		Set<String> attNames = nA.keySet();
		attNames.remove(classAttName);
		
		for (String attName : attNames) {
			for (Object value : nA.get(attName).keySet()) {
				if (nAB.get(attName).get(value) != null && nAB.get(attName).get(value) != 0) {
					Object[] array = new Object[3];
					array[0] = attName;
					array[1] = value;
					array[2] = nAB.get(attName).get(value) / numBad;
					list.add(array);
				}
			}
		}
		
		Collections.sort(list, new Comparator<Object[]>() { 
			@Override 
			public int compare(Object[] o1, Object[] o2) { 
				if ((double) o1[2] >= (double) o2[2])
					return -1;
				else 
					return 1;
			} 
		} );
		
		sortedList = list;
	}
	
	
	/**
	 * In this method a random forest is used to measure the feature importance and
	 * then entropy is used to find the critical values.
	 * For each pair of feature values the following info are computed:
	 *   - information gain 
	 *   - percentage of cases with that value
	 *   - percentage of the cases with that value and an undesirable outcome.
	
	public void featureRecommendationEntropy() {
		generate_nA_nAB();
		
		double numBad = numBadInstances();
		
		LinkedList<Object[]> list = new LinkedList<>();
		
		Set<String> featuresToRecommend = findFeatures();
		
		Set<String> attNames = nA.keySet();
		attNames.remove(classAttName);
		
		for (String attName : attNames) {
			for (Object value : nA.get(attName).keySet()) {
				if (nAB.get(attName).get(value) != null && nAB.get(attName).get(value) != 0) {
					Object[] array = new Object[3];
					array[0] = attName;
					array[1] = value;
					array[2] = nAB.get(attName).get(value) / numBad;
					list.add(array);
				}
			}
		}
		
		Collections.sort(list, new Comparator<Object[]>() { 
			@Override 
			public int compare(Object[] o1, Object[] o2) { 
				if ((double) o1[2] >= (double) o2[2])
					return -1;
				else 
					return 1;
			} 
		} );
		
		sortedList = list;
	}  */
	
	public void generateList() {
		Discretization disc = new Discretization();
		disc.setData(data);
		disc.setMinMax(minMax);
		disc.setTypes(types);
		if (!types.get(classAttName).equals(Type.BOOLEAN) && !types.get(classAttName).equals(Type.LITERAL))
			selectedAtts.add(classAttName);
		disc.setAttNames2Disc(selectedAtts);
		disc.setNumBins(params.getNumBinsFR());
		
		InfoGain ig = new InfoGain(params);
		ig.setNonDiscData(data);
		ig.setDiscretizedData(disc.discretize());
		
		ig.compute(selectedAtts);
		finalListFeatureValues =  ig.generateList(0d);
		
		Set<String> set = new HashSet<>();
		if (fsMethod.equals(FeatureSelectionMethod.OM)) {
			for (Pair<Pair<String, String>, Double> item : finalListFeatureValues) {
				Pair<String, String> attNameValue = item.getFirst();
				if (item.getSecond() >= params.getInfoGainThreshold())
					set.add(attNameValue.getFirst());
			}
		}
		
		if (!set.isEmpty())
			selectedAtts = set;
	} 

	/**
	 * Returns the features with the order of their influence on the class at based on a random forest.
	 * @return
	 */
	public void findFeatures() {
		convertDataForWEKA();
		if (fsMethod.equals(FeatureSelectionMethod.RF))
			attSelection_RF();
		else if (fsMethod.equals(FeatureSelectionMethod.IG))
			attSelection_IG();
		else if (fsMethod.equals(FeatureSelectionMethod.OM))
			attSelection_OM();
		else
			attSelection_Corr();
	}
	
	public void attSelection_OM() {
		selectedAtts = new HashSet<>();
		for (String attName:types.keySet())
			selectedAtts.add(attName);
		
		selectedAtts.remove(classAttName);
	}

	private void visualizeIGs() {
		// TODO Auto-generated method stub
		
	}

	public void attSelection_RF() { 
	    AttributeSelection fs = new AttributeSelection();
	    WrapperSubsetEval wrapper = new WrapperSubsetEval();

	    try {
			wrapper.buildEvaluator(dataWEKA);
		} catch (Exception e) {
			System.out.println(" wrapper build failed!");
		}
	    wrapper.setClassifier(new RandomForest());
	    wrapper.setFolds(10);
	    wrapper.setThreshold(0.001);

	    try {
			fs.SelectAttributes(dataWEKA);
		} catch (Exception e) {
			System.out.println("Selection attribute failed!");
		}  
	    fs.setEvaluator(wrapper);
	    fs.setSearch(new BestFirst());
	    int[] attIndexes = null;
	    try {
			attIndexes = fs.selectedAttributes();
		} catch (Exception e) {
			System.out.println("Attribute selection has faild! ");
			e.printStackTrace();
		}
	    
	    setSelectedAtts(attIndexes);
	    System.out.println(fs.toResultsString());
	}

	public AttributeSelection attSelection_IG(){
		Instances newDataWEKA = discretizeAtts();
		AttributeSelection selector = new AttributeSelection();
		InfoGainAttributeEval evaluator = new InfoGainAttributeEval();
		Ranker ranker = new Ranker();
		ranker.setNumToSelect(Math.min(500, newDataWEKA.numAttributes() - 1));
		selector.setEvaluator(evaluator);
		selector.setSearch(ranker);
		
		int[] attIndexes = null;
		int num = 0;
		try {
			selector.SelectAttributes(newDataWEKA);
			attIndexes = selector.selectedAttributes();
			double[][] result = selector.rankedAttributes();
			for (int i =0; i < result.length; i++) 
				if (result[i][1]>= params.getInfoGainThreshold())
					num++;
		} catch (Exception e) {
			System.out.println(" InfoGain att selection failed! ");
			e.printStackTrace();
		}
		
		int[] newAttIndex = new int[num];
		if (num > 0)
			for (int i = 0; i < num; i++)
				newAttIndex[i] = attIndexes[i];
		attIndexes = newAttIndex;
		
		setSelectedAtts(attIndexes);
		System.out.println(selector.toResultsString());
		
		return selector;
	}
	
	private Instances discretizeAtts() {
		Set<String> set = new HashSet<>();
		LinkedList<Map<String, Object>> newData = new LinkedList<>();
		Map<String, Type> newTypes = new HashMap<>();
		for (String attName : types.keySet()) 
			if (!types.get(attName).equals(Type.BOOLEAN) && !types.get(attName).equals(Type.LITERAL)) {
				set.add(attName);
				newTypes.put(attName, Type.LITERAL);
			} else 
				newTypes.put(attName, types.get(attName));
		Map<String, Set<String>> literalValues = de.getLiteralValues();
		
		if (set.size() > 0) {
			Discretization disc = new Discretization();
			disc.setData(data);
			disc.setMinMax(minMax);
			disc.setTypes(types);
			disc.setAttNames2Disc(set);
			disc.setNumBins(params.getNumBinsFR());
			newData = disc.discretize();
			Map<String, LinkedList<String>> values = disc.getIntervals();
			for (String attName : set) {
				Set<String> vals = new HashSet<>();
				for (String item : values.get(attName)) 
					vals.add(item);
				literalValues.put(attName, vals);
			}
			
			for (String attName : de.getLiteralValues().keySet())
				if (!literalValues.containsKey(attName))
					literalValues.put(attName, de.getLiteralValues().get(attName));
				
		} else
			newData = data;
		
		Convertor convertor = new Convertor();
		convertor.setLitelValues(literalValues);
		convertor.setAttributeTypes(newTypes);
		convertor.setInstanceSet(newData);
		convertor.setClassAttName(classAttName);
		convertor.createWEKAinstances();
		Instances newDataWEKA = convertor.getData();	
		
		return newDataWEKA;
	}

	public AttributeSelection attSelection_Corr(){
		AttributeSelection selector = new AttributeSelection();
	/**	CorrelationAttributeEval evaluator = new CorrelationAttributeEval();
		Ranker ranker = new Ranker();
		ranker.setNumToSelect(Math.min(500, dataWEKA.numAttributes() - 1));
		selector.setEvaluator(evaluator);
		selector.setSearch(ranker);
		
		int[] attIndexes = null;
		try {
			selector.SelectAttributes(dataWEKA);
			attIndexes = selector.selectedAttributes();
		} catch (Exception e) {
			System.out.println(" InfoGain att selection failed! ");
			e.printStackTrace();
		} */
		CfsSubsetEval eval = new CfsSubsetEval();
	    GreedyStepwise search = new GreedyStepwise();
	    //Performs a greedy forward or backward search through the space of attribute subsets.
	    //May start with no/all attributes or from an arbitrary point in the space. 
	    //Stops when the addition/deletion of any remaining attributes results in a decrease in evaluation.
	    //Can also produce a ranked list of attributes by traversing the space from one side to the other and
	    //recording the order that attributes are selected.
	    search.setSearchBackwards(true);
	    selector.setEvaluator(eval);
	    selector.setSearch(search);
	    int[] attIndexes = null;
	    try {
			selector.SelectAttributes(dataWEKA);
			attIndexes = selector.selectedAttributes();
		} catch (Exception e) {
			System.out.println("Correlation based feature recommendation has failed");
			e.printStackTrace();
		}
	    
	    System.out.println("selected attribute indices (starting with 0):\n" + Utils.arrayToString(attIndexes));
		
		setSelectedAtts(attIndexes);
		System.out.println(selector.toResultsString());
		
		return selector;
	}
	
	public void setSelectedAtts(int[] attIndexes) {
		selectedAtts = new HashSet<String>();
	    if (attIndexes != null && attIndexes.length > 0) 
	    	for (int i = 0; i < attIndexes.length; i++)
	    		if (attIndexes[i] != dataWEKA.classIndex())
	    			selectedAtts.add(dataWEKA.attribute(attIndexes[i]).name());
	}
	
	public void attSelection_Cor() { 
	//	InfoGainAttributeEval fs = new InfoGainAttributeEval();
	    AttributeSelection fs = new AttributeSelection();
	    WrapperSubsetEval wrapper = new WrapperSubsetEval();

	    try {
			wrapper.buildEvaluator(dataWEKA);
		} catch (Exception e) {
			System.out.println(" wrapper build failed!");
		}
	    wrapper.setClassifier(new RandomForest());
	    wrapper.setFolds(10);
	    wrapper.setThreshold(0.001);

	    try {
			fs.SelectAttributes(dataWEKA);
		} catch (Exception e) {
			System.out.println("Selection attribute failed!");
		}  
	    fs.setEvaluator(wrapper);
	    fs.setSearch(new BestFirst());
	    int[] attIndexes = null;
	    try {
			attIndexes = fs.selectedAttributes();
		} catch (Exception e) {
			System.out.println("Attribute selection has faild! ");
			e.printStackTrace();
		}
	    
	    selectedAtts = new HashSet<String>();
	    if (attIndexes != null && attIndexes.length > 0) 
	    	for (int i = 0; i < attIndexes.length; i++)
	    		if (attIndexes[i] != dataWEKA.classIndex())
	    			selectedAtts.add(dataWEKA.attribute(attIndexes[i]).name());
	    	
	    
	    System.out.println(fs.toResultsString());
	}

	
	/**
	 * Converts the data to the WEKA format.
	 */
	public void convertDataForWEKA() {		
		LinkedList<String> attNames = new LinkedList<>();
		for (String attName : types.keySet())
			if (!attName.equals(classAttName))
				attNames.add(attName);
		
		Convertor convertor = new Convertor();
		convertor.setLitelValues(de.getLiteralValues());
		convertor.setAttributeTypes(types);
		convertor.setInstanceSet(data);
		convertor.setClassAttName(classAttName);
		convertor.createWEKAinstances();
		dataWEKA = convertor.getData();			
	}
	
/**	private void convertDataForWEKAString() {
		String arffString = "@relation RelationName\n\n";
		
		for (String attName : types.keySet()) {
			arffString = arffString + getARFFHeader(attName);
		}
		
		arffString = arffString + "\\n\\n@data\\n";
		
		for (Map<String, Object> instance : data)
			arffString = arffString + instance2String(instance);
		//"@relation RelationName\n\n@attribute attributeName1\n@attribute attributeName2 {No,Yes}\n\n@data\n'atribute1value',?\n";
		try {
			dataWEKA = new Instances(
			new BufferedReader(new StringReader(arffString)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	
	private String instance2String(Map<String, Object> instance) {
		String str = new String();
		
		for (String attName : types.keySet()) {
			if (instance.containsKey(attName))
				if (types.get(attName).equals(Type.TIMESTAMP)) {
					
				}
		}
		return null;
	}

	private String getARFFHeader(String attName) {
		if (types.get(attName).equals(Type.DISCRETE))
			return "@ATTRIBUTE attName NUMERIC\n";
		if (types.get(attName).equals(Type.CONTINUOS))
			return "@ATTRIBUTE attName real\n";
		if (types.get(attName).equals(Type.TIMESTAMP))
			return "@ATTRIBUTE attName real\n";
		if (types.get(attName).equals(Type.BOOLEAN))
			return "@ATTRIBUTE attName {true, false}\n";
		if (types.get(attName).equals(Type.LITERAL))
			return "@ATTRIBUTE attName " + stringOfValues(attName) + "\n";
		return null;
	}

	private String stringOfValues(String attName) {
		String str = new String();
		
		Set<String> values = new HashSet<>();
		for (Map<String, Object> instance : data)
			values.add(instance.get(attName).toString());
		
		for (String value : values)
			str = str + value + ", ";
		
		return str.substring(0, str.length()-2);
	} */

	

	
	
	
	/**
	 * @return The number of instances with bad result.
	 */
	private double numBadInstances() {
		double num = 0;
		for (Map<String, Object> instance : data)
			if (!isUnDesirableInstance(instance))
				num++;
		
		return num;
	}
	
	public void setNumBin(int n) {
		numBin = n;
	}

	public LinkedList<Object[]> getList() {
		return sortedList;
	}
	
	public void setIsLowerUnDesirable(boolean b) {
		isLowerUnDesirable = b;
	}
	
	public void setThreshold(double d) {
		threshold = d;
	}
	
	public void setUnDesirableValues(Set<String> values) {
		undesirableValues = values;
	}
	
	//----------------------- To directly read data from a file ---------------------
	public FeatureRecommendation(boolean b) {
		String fileName = "1day1percent duration0replaced.txt";
		setDataAndTypesFromFile(fileName);
//		initiate();  //TODO
	}
	
	public FeatureRecommendation(String fileName, int numBins) {
		setDataAndTypesFromFile(fileName, numBins);
//		initiate();  //TODO
	}
	
	/**
	 * Read the data from a CSV file (fileName.txt).
	 */
	public void setDataAndTypesFromFile(String fileName) {
		data = readFromFile(fileName);
		setNumBin(100);
		setTypes();		
		setMinMax();
	}
	
	/**
	 * Read the data from a CSV file (fileName.txt).
	 */
	public void setDataAndTypesFromFile(String fileName, int numBins) {
		data = readFromFile(fileName);
		setNumBin(numBins);
		setTypes();		
		setMinMax();
	}

	
	private void setMinMax() {
		minMax = new HashMap<String, Object[]>();
		
		for (Map<String, Object> instance : data)
			for (String attName : instance.keySet())
				if (types.get(attName).equals(Type.CONTINUOS))
					updateMinMaxCont(attName, instance);
				else if (types.get(attName).equals(Type.DISCRETE))
					updateMinMaxDisc(attName, instance);
	}

	private void updateMinMaxDisc(String attName, Map<String, Object> instance) {
		if (minMax.containsKey(attName)) {
			if (Double.valueOf(minMax.get(attName)[0].toString()) > Double.valueOf(instance.get(attName).toString()))
				minMax.get(attName)[0] = instance.get(attName);
			else if (Long.valueOf(minMax.get(attName)[1].toString()) < Long.valueOf(instance.get(attName).toString()))
				minMax.get(attName)[1] = instance.get(attName);
		} else {
			Object[] mm = {instance.get(attName), instance.get(attName)};
			minMax.put(attName, mm);
		}
	}

	private void updateMinMaxCont(String attName, Map<String, Object> instance) {
		if (minMax.containsKey(attName)) {
			if (Double.valueOf(minMax.get(attName)[0].toString()) > Double.valueOf(instance.get(attName).toString()))
				minMax.get(attName)[0] = instance.get(attName);
			else if (Double.valueOf(minMax.get(attName)[1].toString()) < Double.valueOf(instance.get(attName).toString()))
				minMax.get(attName)[1] = instance.get(attName);
		} else {
			Object[] mm = {instance.get(attName), instance.get(attName)};
			minMax.put(attName, mm);
		}
	}

	/**
	 * Set the data type using the data that have been read from a file.
	 */
	private void setTypes() {
		types = new HashMap<String, Type>();
		
		for (Map<String, Object> instance : data)
			for (String attName : instance.keySet())
				types.put(attName, generateDataElement(instance.get(attName)));	
	}
	
	private static Type generateDataElement(Object value) {

		if (value instanceof Boolean) {
			return Type.BOOLEAN;
		} else if (value instanceof Long || value instanceof Integer) {
			return Type.DISCRETE;
		} else if (value instanceof Double || value instanceof Float) {
			return Type.CONTINUOS;
		} else if (value instanceof Date) {
			return Type.TIMESTAMP;
		} else if (value instanceof String) {
			return Type.LITERAL;
		}
		
		return null;	
	}
	
	/**
	 * Read the data in the form of instances from the file.
	 * @return data
	 */
	private LinkedList<Map<String, Object>> readFromFile(String path) {
		
		LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
		
		// Open the file
		//String path = "1day1percent duration0replaced.txt";
		FileInputStream fstream = null;
		String strLine = null;
		LinkedList<String> header = null;
		try {
			fstream = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			//header
			strLine = br.readLine();
			if (strLine != null)
				header = readHeader(strLine);
			else
				System.out.println("Empty file!");
			// process line by line
			while ((strLine = br.readLine()) != null)   {
				  list.add(rowToInstance(strLine, header));
				}
			//Close the input stream
			fstream.close();
		} catch (FileNotFoundException e) {
			System.out.println("file reading problem");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("file closing problem");
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * Turn each line of the file to an instance.
	 * @param line  (csv format)
	 * @param header  (order of attributes)
	 * @return an instance.
	 */
	private Map<String, Object> rowToInstance(String line, LinkedList<String> header) {
		Map<String, Object> instance = new HashMap<>();
		String[] values = line.split(",");
		int i = 0;
		for (String attName : header) {
			instance.put(attName, values[i]);
			i++;
		}
		
		return instance;
	}
	
	/**
	 * Generate the header from the first line of the file
	 * @param line
	 * @return header
	 */
	private LinkedList<String> readHeader(String line) {
		LinkedList<String> header = new LinkedList<>();
		String[] attNames = line.split(",");
		for (int i = 0; i < attNames.length; i++)
			header.add(attNames[i]);
		
		return header;
	}
	
	public Set<String> getSelectedAtts() {
		return selectedAtts;
	}
	
	//----------------------- Table ------------------------
	public void showResults() {
		String[] header = {"Trace or activity", "Attribute","value", "Percent"};
		Object[][] body = createBody();
		showPupup(body, header);
	}
	
	public void showPupup(Object[][] body, String[] header) {
		JFrame frame = new JFrame("Recommended Situation Features and Values");
		frame.setSize(new Dimension(500, 500));
		
		JTable table = new JTable(body, header);
		table.setBackground(Color.lightGray);
		JScrollPane sPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sPane.setPreferredSize(new Dimension(400, 450));
		sPane.setMinimumSize(new Dimension(400, 450));
		frame.getContentPane().add(sPane, BorderLayout.CENTER);
		
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		frame.show();  
	}
	
	public JScrollPane getTableForView() {
		String[] header = {"Trace or activity name", "Attribute","value", "Information gain"};
		Object[][] body = createBody();
		JTable table = new JTable(body, header);
		table.setBackground(Color.lightGray);
		JScrollPane sPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sPane.setPreferredSize(new Dimension(400, 450));
		sPane.setMinimumSize(new Dimension(400, 450));
		
		return sPane;
	}
	
	public Object[][] createBody() {
		LinkedList<Object[]> list = new LinkedList<>();

		for (int i = 0; i < finalListFeatureValues.size(); i++) {
			String attName = finalListFeatureValues.get(i).getFirst().getFirst();
			String attValue = finalListFeatureValues.get(i).getFirst().getSecond();
			if (attValue.equals("*"))
				attValue = "NOT SET";
//			if (!attValue.equals("0") && !attValue.equals("NOT SET")) {
				Object[] row = new Object[4];
				if (attName.contains("_:_")) {
					String[] parts = attName.split("_:_");
					row[0] = parts[1];
					row[1] = parts[0];
					//TODO add choice
				} else {
					row[0] = "Trace";
					row[1] = attName.toString();
				}
				row[2] =attValue;
				row[3] = finalListFeatureValues.get(i).getSecond();
				list.add(row);
//			}
		}
			

		Object[][] body = new Object[list.size()][4];
		
		for (int j = 0; j < list.size(); j++) {
			body[j][0] = list.get(j)[0];
			body[j][1] = list.get(j)[1];
			body[j][2] = list.get(j)[2];
			body[j][3] = list.get(j)[3];
		}
		
		return body;
	} 
	
	public LinkedList<Pair<Pair<String, String>, Double>> getFinalListFeatureValues() {
		return finalListFeatureValues;
	}
	
	public void setFeatureSelectionMethod(FeatureSelectionMethod fsm) {
		fsMethod = fsm;
	}

	
/**	public Object[][] createBody() {
		LinkedList<Object[]> list = new LinkedList<>();

		for (int i = 0; i < sortedList.size(); i++) {
			if (!sortedList.get(i)[1].toString().equals("0") && !sortedList.get(i)[1].toString().equals("NOT SET")) {
				Object[] row = new Object[4];
				if (sortedList.get(i)[0].toString().contains(" : ")) {
					String[] parts = sortedList.get(i)[0].toString().split(" : ");
					row[0] = parts[1];
					row[1] = parts[0];
					//TODO add choice
				} else {
					row[0] = "Trace";
					row[1] = sortedList.get(i)[0].toString();
				}
				row[2] = sortedList.get(i)[1];
				row[3] = sortedList.get(i)[2].toString().subSequence(2, 4);
				list.add(row);
			}
		}
			

		Object[][] body = new Object[list.size()][4];
		
		for (int j = 0; j < list.size(); j++) {
			body[j][0] = list.get(j)[0];
			body[j][1] = list.get(j)[1];
			body[j][2] = list.get(j)[2];
			body[j][3] = list.get(j)[3];
		}
		
		return body;
	} */
	
	//--------------------- Test Code ----------------------
	
	
	public void setDataAndTypes() {
		LinkedList<Map<String, Object>> sampleData = new LinkedList<>();
		String[] names = {"a", "c", "c", "b", "c", "b", "a", "a"};
		Integer[] nums = {5, 6, 3, 4, 1, 2, 7, 8};
		Boolean[] res = {true, true, true, true, false, false, false, false};
		
		for (int i = 0; i < names.length; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("name", names[i]);
			map.put("num", nums[i]);
			map.put("res", res[i]);
			sampleData.add(map);
		}
		
		data = sampleData;
		
		types = new HashMap<String, Type>();
		types.put("name", Type.LITERAL);
		types.put("num", Type.DISCRETE);
		types.put("res", Type.BOOLEAN);
		
		numBin = 4;
		
		classAttName = "res";
		
		undesirableValues = new HashSet<>();
		undesirableValues.add("true");
		
		minMax = new HashMap<String, Object[]>();
		Object[] m = {1, 8};
		minMax.put("num", m);
	}

	public FeatureRecommendation() {
		setDataAndTypes();
//TODO		initiate();
	}
	
	public void setClassAttName(String name) {
		classAttName = name;
	}
	
	/**
	 * expected output nominal class feature:
	 * num 0 0.25
	 * num 3 0.25
	 * name b 0.25
	 * name c 0.25
	 * name a 0.5
	 * 
	 * expected output numerical class feature:
	 * res false 0.5
	 * res true 0.5
	 * name b 0.5
	 * name c 0.5
	
	
	public static void  tryFR() {
		FeatureRecommendation fr = new FeatureRecommendation(true);
		fr.initiate();
		fr.setClassAttName("Trace Delay");
		Set<String> undesirableValues = new HashSet<>();
		undesirableValues.add("delayed");
		fr.setUnDesirableValues(undesirableValues);
		fr.generateList();
		
		LinkedList<Object[]> list = fr.getList();
		System.out.println("  numerical class attribute");
		for (Object l[] : list) {
			String str = l[0].toString() + " " + l[1].toString() + " " + l[2].toString();
			System.out.println(str);
		}
		
		fr.showResults();
	}  */
	
	public static void tryFRwrapper() {
		FeatureRecommendation fr = new FeatureRecommendation();
		fr.testDataConversion();
//		testFeatureRecommendation();
	}
	
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
			row.put("level", randInt(1,3));
			
			testData.add(row);	
		}
		data = testData;
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
		
		Set<String> set = new HashSet<>();
		set.add("timestamp");
		set.add("level");
		
		// Set classAttName
		classAttName = "level";
		
		// Set num bins
		setNumBin(5);
		
		// Set Discretizer 
		Discretization disc = new Discretization();
		disc.setTypes(testType);
		disc.setMinMax(minMax);
		disc.setData(data);
		disc.setAttNames2Disc(set);
		disc.setNumBins(5);
		
		// Discretize 
		LinkedList<Map<String, Object>> disceretizedTestData = disc.discretize();
		
		System.out.println("\n\n ***********************");
		for (Map<String, Object> instance : disceretizedTestData) {
			String str = new String();
			for (String attName : types.keySet())
				str = str + "  " + attName + ": " + instance.get(attName);
			System.out.println(str);
		}
		// Convert to WEKA format
		convertDataForWEKA();
		
		// Print
		
		System.out.println(dataWEKA);
	}
	
	private void setTypes(Map<String, Type> t) {
		types = t;
	}
	
	public Map<String, Type> getTypes() {
		return types;
	}

	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	public static void main(String[] args) {
/**		FeatureRecommendation fr = new FeatureRecommendation();
		fr.generateList();
		LinkedList<Object[]> list = fr.getList();
		System.out.println("  nominal class attribute");
		for (Object l[] : list) {
			String str = l[0].toString() + " " + l[1].toString() + " " + l[2].toString();
			System.out.println(str);
		}
		
		fr.initiate();
		fr.setClassAttName("num");
		fr.setIsLowerUnDesirable(false);
		fr.setThreshold(4.5);
		fr.generateList();
		
		list = fr.getList();
		System.out.println("  numerical class attribute");
		for (Object l[] : list) {
			String str = l[0].toString() + " " + l[1].toString() + " " + l[2].toString();
			System.out.println(str);
		}
		
		tryFR(); */
		
		
		System.out.println(" \n\n **************************");
		tryFRwrapper();
	}

	public LinkedList<Map<String, Object>> getInstances() {
		return data;
	}

	public Map<String, Object[]> geMinMax() {
		return minMax;
	}
}
