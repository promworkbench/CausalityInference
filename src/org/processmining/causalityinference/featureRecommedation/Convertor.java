package org.processmining.causalityinference.featureRecommedation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class Convertor {
	
	private Map<String, Type> attributeTypes;
	private int classIndex;
	private String classAttName;
	private LinkedList<Map<String, Object>> instanceSet;
	private Instances dataset;
	private Map<String, Set<String>> literalValues;
	private Map<String, ArrayList<String>> attVals;
	
	// ******************************
	ArrayList<Attribute>    atts;
	String[] header;
	
	/**
	 * Create the header of the data in the WEKA format and create the Instances
	 */
	public void creatHeaderOfInstances() {
		
		// fix the order of attribute names
		Set<String> attNames = attributeTypes.keySet();

		header = new String[attNames.size()];
		int j = 0;
		for (String attName : attNames) {
			if (attName.equals(classAttName))
				classIndex = j;
			header[j] = attName;
			j++;
		}
	
		// initiate attributes
	    atts = new ArrayList<Attribute>();
	    
	    // add attributes
	    for (int i = 0; i < header.length; i++) {
			String attName = header[i];
			switch (attributeTypes.get(attName)) {
				case LITERAL :
					atts.add(new Attribute(attName, attVals.get(attName)));
					break;
				case TIMESTAMP :
					atts.add(new Attribute(attName, "yyyy-MM-dd'T'HH:mm:ss"));;
					break;
				case DISCRETE : // Do the same as case CONTINOUS
					atts.add(new Attribute(attName));
					break;
				case CONTINUOS :
					atts.add(new Attribute(attName));
					break;
				case BOOLEAN :
					atts.add(new Attribute(attName, attVals.get(attName)));
			}
	    }
	    
	    
	    // create the Isntances
	    dataset = new Instances("DataSet", atts, 0);
	    dataset.setClassIndex(classIndex);
	}
	
	public void createWEKAinstances() {
		setLiteralVals();
		creatHeaderOfInstances();
		for (Map<String, Object> item : instanceSet)
			dataset.add(new DenseInstance(1.0, oneInstance(item)));

	}
	
	
	private void setLiteralVals() {
		attVals = new HashMap<>();

		for (String attName : attributeTypes.keySet()) {
			if (attributeTypes.get(attName).equals(Type.LITERAL)) {
				ArrayList<String>  vals = new ArrayList<String>();
				
				for(String value : literalValues.get(attName)) {
					vals.add(value);
				}
				
				attVals.put(attName, vals);
			} else if (attributeTypes.get(attName).equals(Type.BOOLEAN)) {
				ArrayList<String>  vals = new ArrayList<String>();
				vals.add("True");
				vals.add("False");
				attVals.put(attName, vals);
			}
		}
	}
	
	public double [] oneInstance(Map<String, Object> instance) {
		double [] vals = new double[attributeTypes.size()];
		
		for (int i = 0; i < header.length; i++) {
			String attName = header[i];
			if (!instance.containsKey(attName) || instance.get(attName) == null) 
				vals[i] = Double.NaN;
			else 
				switch (attributeTypes.get(attName)) {
					case LITERAL :
						vals[i] = attVals.get(attName).indexOf(instance.get(attName).toString());
						break;
					case TIMESTAMP :
						atts.add(new Attribute(attName, "yyyy-MM-dd'T'HH:mm:ss"));
						vals[i] = ((Date) instance.get(attName)).getTime();
						break;
					case DISCRETE : // Do the same as case CONTINOUS
						vals[i] = getDoubleValue(instance.get(attName));
						break;
					case CONTINUOS :
						vals[i] = getDoubleValue(instance.get(attName));
						break;
					case BOOLEAN :
						vals[i] = attVals.get(attName).indexOf(instance.get(attName).toString());
				}
	    }
		
		return vals;
	}
	
	public void setInstanceSet(LinkedList<Map<String, Object>> instances) {
		instanceSet = instances;
	}

	public void setAttributeTypes(Map<String, Type> at) {
		attributeTypes = at;		
	}

	public void setLitelValues(Map<String, Set<String>> values) {
		literalValues = values;
	}
	
	public Instances getData() {
		return dataset;
	}
	
	public void setClassAttName(String name) {
		classAttName = name;
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
	
	// -------------------- Test Code --------------------
	public static void main(String[] atgs) {
		Map<String, Type> attTypes = new HashMap<>();
		attTypes.put("lit1", Type.LITERAL);
		attTypes.put("bool", Type.BOOLEAN);
		attTypes.put("nom", Type.DISCRETE);
		attTypes.put("con", Type.CONTINUOS);
		
		Map<String, Set<String>> literalValues = new HashMap<>();
		Set<String> vals = new HashSet<>();
		vals.add("A");
		vals.add("B");
		vals.add("C");
		
		literalValues.put("lit1", vals);
		
		LinkedList<Map<String, Object>> instanceSet = new LinkedList<>();
		Map<String, Object> inst1 = new HashMap<>();
		inst1.put("lit1", "A");
		inst1.put("bool", "True");
		inst1.put("nom", 3);
		inst1.put("con", 1.2);
		instanceSet.add(inst1);
		Map<String, Object> inst2 = new HashMap<>();
		inst2.put("lit1", "B");
		inst2.put("bool", "False");
		inst2.put("nom", 6);
//		inst2.put("con",4.2);
		instanceSet.add(inst2);
		
		Convertor convertor = new Convertor();
		convertor.setLitelValues(literalValues);
		convertor.setAttributeTypes(attTypes);
		convertor.setInstanceSet(instanceSet);
		convertor.createWEKAinstances();
		
		System.out.println(convertor.getData());
	}
}
