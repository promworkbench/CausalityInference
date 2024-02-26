package org.processmining.dataTable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

public class TableCreator {
	
	public final static String notAllowedChars=".()&!|=<>-+*/%, ";
	
	private Map<String, Type> attributeType;
	private Map<String, Set<String>> literalValues;
	private Map<Map<String, Object>, Integer> instanceSet ;
	private Map<String, Map<String, Integer>> numericalValueMap;

	
	public TableCreator(String classAttName, Map<String, Type> attributeType, Map<String, Set<String>> literalValues, Map<Map<String, Object>, Integer> instancesWeight) throws Exception {
		this.attributeType = attributeType;
		this.literalValues = literalValues;
		this.instanceSet = instancesWeight;
//		this.classAttName = replaceNotAllowedStrings(getRealAttNameIfChoice(classAttName));
//		this.instancesWeight = instancesWeight;
		makeNumerical();
	}
	
	/**
	 * 
	 *  for each literal attribute, this function assign a nominal value depending on the 
	 *  number of different values that the attribute has taken
	 *  
	 *  
	 */
	public void makeNumerical() {
		
		numericalValueMap = new HashMap<String, Map<String, Integer>>();
		
		Set<String> keys = new HashSet<String>();
		keys = attributeType.keySet();
		
		for (String attName : keys) {
			
			// add assigned values to the map if the attribute is boolean
			if (attributeType.get(attName).equals(Type.BOOLEAN)) {
				Map<String, Integer> intValues = new HashMap<String, Integer>();
				
				intValues.put("true", 1);
				intValues.put("false", 0);
				
				numericalValueMap.put(attName, intValues);
			}
			
			// add assigned values to the map if the attribute is literal
			if (attributeType.get(attName).equals(Type.LITERAL)) {
				Map<String, Integer> intValues = new HashMap<String, Integer>();
				Set<String> strValues = literalValues.get(attName);
				
				int i = 0;
				for (String str : strValues) {
					intValues.put(str, i);
					i++;
				}
				
				numericalValueMap.put(attName, intValues);
			}
		}
		
	}

	
	/**
	 * 
	 *  It save the data in the numerical format in a txt file
	 */
	public void writeCSV() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("tempData.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + ',';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		Set<Map<String, Object>> instances = instanceSet.keySet();
		for(Map<String, Object> instance : instances) {
			for (int i = 1; i <= instanceSet.get(instance); i++) {
				String row = new String();
				row = convertToRow(instance);
				writer.println(row);
			}
		}
		writer.close();
	}    
	

	/**
	 * 
	 *  It save the data in the numerical format in a txt file
	 */
	public void txtDataFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("txtData.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + ',';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		Set<Map<String, Object>> instances = instanceSet.keySet();
		for(Map<String, Object> instance : instances) {
			for (int i = 1; i <= instanceSet.get(instance); i++) {
				String row = new String();
				row = convertToTxtRow(instance);
				writer.println(row);
			}
		}
		writer.close();
	}    
	
	/**
	 * 
	 *  It save the data about Expert department in the numerical format in a txt file
	 */
	public void txtDataJustExpertsFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("txtDataJustExperts.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + ',';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		Set<Map<String, Object>> instances = instanceSet.keySet();
		for(Map<String, Object> instance : instances) {
			for (int i = 1; i <= instanceSet.get(instance); i++) {
				if (instance.get("department").equals("Experts")) {
					String row = new String();
					row = convertToTxtRow(instance);
					writer.println(row);
				}
			}
		}
		writer.close();
	}    
	
	/**
	 * 
	 *  It save the data about other departments but Expert department in the numerical format in a txt file
	 */
	public void txtDataNonExpertsFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("txtDataNonExperts.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + ',';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		Set<Map<String, Object>> instances = instanceSet.keySet();
		for(Map<String, Object> instance : instances) {
			for (int i = 1; i <= instanceSet.get(instance); i++) {
				if (!instance.get("department").equals("Experts")) {
					String row = new String();
					row = convertToTxtRow(instance);
					writer.println(row);
				}
			}
		}
		writer.close();
	}    
	
	/**
	 * 
	 *  It save one instance of the data in the numerical format in a txt file
	 */
	public void writeInstanceInFile(Map<String, Object> instance) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("tempData.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + ',';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		String row = new String();
		row = convertToRow(instance);
		writer.println(row);
		writer.close();
	}
	
	/**
	 * 
	 *  It creates a row of the temporal data file in the right format out of each instance 
	 *  in the instance set.
	 */
	public String convertToRow(Map<String, Object> instance) {
		String row = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			String value = getIntegerValue(attName, instance);
			if (value != null)  {
				row = row + value+'\t';
			} else {
				row = row + "*\t";
			}
		}
		row = row.substring(0, row.length() - 1);
		return row;
	}
	
	/**
	 * 
	 *  It creates a row of the temporal data file in the right format out of each instance 
	 *  in the instance set.
	 */
	public String convertToTxtRow(Map<String, Object> instance) {
		String row = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			String value = getTxtValue(attName, instance);
			if (value != null)  {
				row = row + value+ '\t';
			} else {
				row = row + "*\t,";
			}
		}
		row = row.substring(0, row.length() - 1);
		return row;
	}
	
	/**
	 * 
	 * This function returns the integer value corresponding to the value of the given attribute
	 * name in the given instance
	 */
	public String getTxtValue(String attName, Map<String, Object> instance) {
		
		if (!instance.containsKey(attName)) {
			return null;
		}
		
		Object obj = instance.get(attName);
		Type attType = attributeType.get(attName);
		if (attType.equals(Type.BOOLEAN))
			return obj.toString();
		else if (attType.equals(Type.LITERAL))
			return obj.toString();
		else if (attType.equals(Type.TIMESTAMP)) {
			Long time = ((Date) obj).getTime();
			if (time < 0) 
				System.out.println("time : " + time);
			Integer i = time != null ? time.intValue() : null;
			return i.toString();
		}
		return obj.toString();
	}
	
	/**
	 * 
	 * This function returns the integer value corresponding to the value of the given attribute
	 * name in the given instance
	 */
	public String getIntegerValue(String attName, Map<String, Object> instance) {
		
		if (!instance.containsKey(attName)) {
			return null;
		}
		
		Object obj = instance.get(attName);
		Type attType = attributeType.get(attName);
		if (attType.equals(Type.BOOLEAN))
			return(numericalValueMap.get(attName).get(obj.toString()).toString());
		else if (attType.equals(Type.LITERAL))
			return(numericalValueMap.get(attName).get(obj.toString()).toString());
		else if (attType.equals(Type.TIMESTAMP)) {
			Long time = ((Date) obj).getTime();
			Integer i = time != null ? time.intValue() : null;
			return i.toString();
		}
		return obj.toString();
	}

}
