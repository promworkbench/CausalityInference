package org.processmining.dataTable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.datadiscovery.estimators.Type;

public class TableCreatorOneHot {
	
	public final static String notAllowedChars = " ";
	
	private Map<String, Type> attributeType;
	private Map<String, LinkedList<String>> literalValues;
	private LinkedList<Map<String, Object>> instanceSet ;
	private Map<String, Map<String, Integer>> numericalValueMap;
	private Map<String, Map<Integer, String>> inverseNumericalValueMap;
	private String stringDataTable;
	private AggregatedDataExtraction dataExtraction = null;
	private String classAttName;
	private LinkedList<String> headerAttributeNames;
	private LinkedList<String> headerAttributeNamesNumericl;  //just in case ;)
	private Map<String, Integer> headerAttNameColumnNumberMap;
	private Object[][] body;
	private Object[][] numericalBody;
	private Map <String, Object> minValues;
	private String interpolationMethod = null;
	private int nullValueThresholdInARow = 50;
	private int nullValueThresholdInAColumn = 100;
	private Map<String, Object> meanValues;  // This field keep trace of mean and median of each column
	Object[] lastValues;
	boolean removeNullValues = false;
	private Map<String, Double> mean;
	private Map<String, Double> median;
	private Map<String, Double> variance;
	private Map<String, Double> standardDeviation;
	
	
	public TableCreatorOneHot(String classAttName, Map<String, Type> attributeType, 
			Map<String, Set<String>> literalValues, LinkedList<Map<String, Object>> instancesWeight, AggregatedDataExtraction de) throws Exception {
		this.attributeType = attributeType;
		this.literalValues = new HashMap<String, LinkedList<String>>();
		this.dataExtraction = de;
		this.classAttName = classAttName;
		this.instanceSet = instancesWeight;
		
		replaceNullWithNotSetLiteralAttributes(literalValues);
		
		
		for ( String attName : literalValues.keySet()) {
			LinkedList<String> values = new LinkedList<String>();
			for(String value : literalValues.get(attName)) {
				values.add(value);
			}
			Collections.sort(values);
			this.literalValues.put(attName, values);			
		}
		
		makeNumerical();
		
		setMeanMedian();
		
		setMinValues();
	}
	
	public void replaceNullWithNotSetLiteralAttributes(Map<String, Set<String>> literalValues) {
		for (Map<String, Object> instance : instanceSet)
			for (String attName : attributeType.keySet())
				if (!instance.containsKey(attName) && attributeType.get(attName).equals(Type.LITERAL)) {
					instance.put(attName, "NOT SET");
					if (literalValues.containsKey(attName))
						literalValues.get(attName).add("NOT SET");
					else {
						Set<String> values = new HashSet<String>();
						values.add("NOT SET");
						literalValues.put(attName, values);
					}
				}
	}
	
	public void setMeanMedian() {
		
		Map<String, Object> sumValues = new HashMap<String, Object>();
		Map<String, Integer> numValues = new HashMap<String, Integer>();
		
		for(Map<String, Object> instance : instanceSet) {
			for (String attName : attributeType.keySet()) {
				String value = getIntegerValue(attName, instance);
				if (attributeType.get(attName).equals(Type.BOOLEAN))  {
					if (numValues.containsKey(attName)) {
						if (value.equals("1")) {
							int valNum = numValues.get(attName) + 1;
							numValues.remove(attName);
							numValues.put(attName, valNum);
							
							int valSum = (Integer) sumValues.get(attName) + 1;
							sumValues.remove(attName);
							sumValues.put(attName, valSum);
						} else if (value.equals("0")) {
							int valNum = numValues.get(attName) + 1;
							numValues.remove(attName);
							numValues.put(attName, valNum);
						} 
					} else {
							if (value.equals("1")) {
								numValues.put(attName, 1);
								sumValues.put(attName, 1);
							} else if (value.equals("0")) {
								numValues.put(attName, 1);
								sumValues.put(attName, 0);
							}
						}
				}  // end of boolean
				else if (attributeType.get(attName).equals(Type.LITERAL))  {
					if (value != null && !value.equals("*") && !value.equals("NOT SET")) {
						if (numValues.containsKey(attName)) {
								int valNum = numValues.get(attName) + 1;
								numValues.remove(attName);
								numValues.put(attName, valNum);
								
								long valSum = (Long) sumValues.get(attName) + Long.valueOf(value);
								sumValues.remove(attName);
								sumValues.put(attName, valSum);
							} else {

								numValues.put(attName, 1);
								sumValues.put(attName, Long.valueOf(value));
							} 
					} 
				}  // end of literal
				else if (attributeType.get(attName).equals(Type.TIMESTAMP))  {
					if (value != null) {
						if (numValues.containsKey(attName)) {
								int valNum = numValues.get(attName) + 1;
								numValues.remove(attName);
								numValues.put(attName, valNum);
								
								long valSum = (Long) sumValues.get(attName) + Long.valueOf(value.toString());
								sumValues.remove(attName);
								sumValues.put(attName, valSum);
							}  else {

								numValues.put(attName, 1);
								sumValues.put(attName, Long.valueOf(value.toString()));
							} 
					}  // end of timeStamp and double
				} else {
					if (value != null && !value.equals("*")) {
						if (numValues.containsKey(attName)) {
								int valNum = numValues.get(attName) + 1;
								numValues.remove(attName);
								numValues.put(attName, valNum);
								
								double valSum = (Double) sumValues.get(attName) + Double.valueOf(value.toString());
								sumValues.remove(attName);
								sumValues.put(attName, valSum);
							}  else {

								numValues.put(attName, 1);
								sumValues.put(attName, Double.valueOf(value.toString()));
							} 
					}  // end of timeStamp and double
				}
			}
		}
		
		meanValues = new  HashMap<String, Object>();
		for (String attName : attributeType.keySet()) 
			meanValues.put(attName, getValue(sumValues.get(attName), numValues.get(attName)));
//		System.out.println("mean values" + meanValues);
	}
	
	protected static Object getValue(Object obj, Integer num) 
	{
		if (obj instanceof Double)
			return (double) obj / num;
		else if (obj instanceof Integer)
			return (int) obj / num;
		else if (obj instanceof Float)
			return (float) obj / num;
		else if (obj instanceof Long)
			return (long) obj / num;

		return obj;
	}
	
	/**
	 * this function computes the minimum value for the timestamp attributes.
	 */
	public void setMinValues() {
		minValues = new HashMap<String, Object>();
		Set<String> timestampAttributes = new HashSet<String>();
		for (String attName : attributeType.keySet()) 
			if (attributeType.get(attName).equals(Type.TIMESTAMP))
				timestampAttributes.add(attName);
		for (Map<String, Object> instance : instanceSet) {
			for (String att : instance.keySet())
				if (timestampAttributes.contains(att))
					if (instance.get(att) != null && !instance.get(att).equals("*")) {
						if (minValues.containsKey(att)) {
							if ((Long) minValues.get(att) > ((Date) instance.get(att)).getTime()) {
								minValues.remove(att);
								minValues.put(att, ((Date) instance.get(att)).getTime());
							} 
						} else
							minValues.put(att, ((Date) instance.get(att)).getTime());
					}
		}
		
//		System.out.println(minValues);
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
		inverseNumericalValueMap = new HashMap<String, Map<Integer, String>>();
		
		Set<String> keys = new HashSet<String>();
		keys = attributeType.keySet();
		
		for (String attName : keys) {
			
			// add assigned values to the map if the attribute is boolean
			if (attributeType.get(attName).equals(Type.BOOLEAN)) {
				Map<String, Integer> intValues = new HashMap<String, Integer>();
				intValues.put("true", 1);
				intValues.put("false", 0);
				numericalValueMap.put(attName, intValues);
				
				Map<Integer, String> inverseIntValues = new HashMap<Integer, String>();
				inverseIntValues.put(1, "true");
				inverseIntValues.put(0, "false");
				inverseNumericalValueMap.put(attName, inverseIntValues);
				
			}
			
			// add assigned values to the map if the attribute is literal
			if (attributeType.get(attName).equals(Type.LITERAL)) {
				Map<String, Integer> intValues = new HashMap<String, Integer>();
				HashMap<Integer, String> inverseIntValues = new HashMap<Integer, String>();
				LinkedList<String> strValues = literalValues.get(attName);
				
				int i = 0;
				for (String str : strValues) {
					intValues.put(str, i);
					inverseIntValues.put(i, str);
					i++;
				}
				
				numericalValueMap.put(attName, intValues);
				inverseNumericalValueMap.put(attName, inverseIntValues);
			}
		}

	}
	
	
	public void createNumercalBody() {
		numericalBody = new Object[body.length][body[0].length];
		for (int i = 0; i < body.length; i++) 
			for (int j = 0; j < body[0].length; j++)
				numericalBody[i][j] = body[i][j];
	}
	
	/**
	 * 
	 *  It save the data in the oneHot format for literal type attributes and numerical format for time stamp in a txt file.
	 *  Here each literal value is mapped to an integer.
	 */
	public void intermadiateNumericalFile() {   ///////////////		
		
		if (instanceSet.size() == 0 || attributeType.size() == 0) {
				String[] options = {"OK"};
				String message = "Empty table!"; 
				JPanel panel = new JPanel();
				JLabel lbl = new JLabel(message);
				panel.add(lbl);
				int selectedOption = JOptionPane.showOptionDialog(null, panel, message, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

				if(selectedOption == 0)
					return;	
			}
		// creating the header of the table
		createHeaderNumericalLiterals();
		
		//creating the body of the table ; i.e. inserting a row for each 
		createBodyNumericalLiterals();
		
//		for (int i = 0; i < body.length; i++) {
//			for (int j = 0; j < body[0].length; j++)
//				System.out.print(" , " + body[i][j]);
//			System.out.println();
//		}
		
		
		// removing singular value columns
		Set<Integer> singularValuedColumnsIndices = findSingularValueColumns();
		if (singularValuedColumnsIndices.size() > 0)
			removeColumns(singularValuedColumnsIndices);
		
		System.out.println(" singular" + singularValuedColumnsIndices);
		
		// removing one the duplicated columns
		Set<Integer> duplicateColumns = findDuplicateColumns();
		if (duplicateColumns.size() > 0)
			removeColumns(duplicateColumns);
		
		System.out.println("duplicate"+ duplicateColumns);
		
		//create Numerical value table
		createNumercalBody();
		
		setMeanMedianVariance();
		
		//Writing data table to a text file in a Tetrad friendly format
		writeTableToFileNumericalLiterals();
		
		// for decision tree creation  ******************
//		writeTableToFileLiterals();
		//****************************
	} 
	
	/**
	 * This method writes table to a text file.
	 * In this case the literal values are mapped to integers.
	 */
	public void writeTableToFileNumericalLiterals() { ///////////
		// impute the missing values if needed
		if (removeNullValues)
			imputeMissingValues();
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("DataTableNumerical.txt", "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		writer.println(headerToString());

		boolean checkForMissingValues = true;
		for(int idx = 0 ; idx < body.length ; idx++) {
			if (checkForMissingValues)
				for (int i =0 ; i < body[idx].length; i++) {
					Object item = body[idx][i];
					if (item.equals("*" )) {
						dataExtraction.setDataHasMissingValues(true);
						checkForMissingValues = false;
					}
				}
			writer.println(rowToString(body[idx]));	
		}
		writer.close();
		
		// indicating in the dataExtraction object if the table has been created
	}
	/**
	public void writeTableToFileLiterals() {
		// impute the missing values if needed
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("DataTable.txt", "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		writer.println(headerToStringComma());

		boolean checkForMissingValues = true;
		for(int idx = 0 ; idx < body.length ; idx++) 
			writer.println(rowToStringLiteral(body[idx]));	
		
		writer.close();
	} */
	
	/**
	 * This function remove first rows an the columns with more missing values than the given threshold.
	 * Then it imputes the remaining missing values according to the selected method.
	 */
	public void imputeMissingValues() {
		
		// find the rows with more missing values than the given threshold
		LinkedList<Integer> rowsToRemove = new LinkedList<Integer>();
		for(int idx = 0 ; idx < body.length ; idx++) {
			int count = 0;
			for (int i =0 ; i < body[idx].length; i++) {
				Object item = body[idx][i];
				if (item.equals("*" ))
					count++;
			}
			if (count >  ((body[idx].length * this.nullValueThresholdInARow) / 100))
				rowsToRemove.add(idx);
		}
		
		// remove the rows that have been found
		if (!rowsToRemove.isEmpty())
			removeRowes(rowsToRemove);
		
		//find the columns with more missing values than the given threshold
		int[] count = new int[body[0].length];
		for (int i = 0; i < count.length; i++) {
			count[i] = 0;
		}
		
		for(int idx = 0 ; idx < body.length ; idx++) {
			for (int i =0 ; i < body[idx].length; i++) {
				Object item = body[idx][i];
				if (item.equals("*" ))
					count[i]++;
			}
		}
		
		Set<Integer> columnIndexesToRemove = new HashSet<Integer>();
		
		for (int i = 0; i < count.length; i++) {
			if (count[i] > ((body.length * this.nullValueThresholdInAColumn) / 100))
				columnIndexesToRemove.add(i);
		}
		
		// remove columns that have been found
		if (!columnIndexesToRemove.isEmpty())
			removeColumns(columnIndexesToRemove);
		
		//impute remaining missing values
		Object[] lastValues = new Object[body[0].length];
		
		for(int idx = 0 ; idx < body.length ; idx++) {
			for (int i =0 ; i < body[idx].length; i++) {
				Object item = body[idx][i];
				if (item.equals("*" )) {
					if (interpolationMethod.equals("Previous Value") && lastValues[i] != null) 
						body[idx][i] = lastValues[i];
					else if ((interpolationMethod.equals("Previous Value") && lastValues[i] == null))
						body[idx][i] = meanValues.get(headerAttributeNames.get(i));
					else if (interpolationMethod.equals("Mean"))
						body[idx][i] = meanValues.get(headerAttributeNames.get(i));
				} else
					lastValues[i] = item;
			} 
		}

		//indicate that there is no more missing value
		dataExtraction.setDataHasMissingValues(false);
	}
	
	public String headerToStringComma() {
		String header = new String();
		for (String name : headerAttributeNames) 
			header = header + name + ",";
		
		return header.substring(0, header.length()-1);
	}
	
	/**
	 * returns a string of the header of the table to be written on the txt file
	 */
	
	public String headerToString() {
		String header = new String();
		for (String name : headerAttributeNames) 
			header = header + name + "\t";
		
		return header.substring(0, header.length()-1);
	}
	
	/**
	 * returns a string of the given row of the table to be written on the txt file
	 */
	public String rowToString(Object[] elements) {
		String row = new String();
		for (int i = 0; i < elements.length; i++) {
			Object item = elements[i];
			row = row + item.toString() + "\t";
		}
		
		return row.substring(0, row.length()-1);
	}
	
	public String rowToStringLiteral(Object[] elements) {
		String row = new String();
		for (int i = 0; i < elements.length; i++) {
			String attName = headerAttributeNames.get(i);
			Map<Integer, String> m = inverseNumericalValueMap.get(attName);
			Object item = m.get(Double.parseDouble(elements[i].toString()));
			row = row + item.toString() + ",";
		}
		
		return row.substring(0, row.length()-1);
	}
	
	public void removeRowes(LinkedList<Integer> rowsToRemove) {
		Object[][] newBody = new Object[body.length - rowsToRemove.size()][body[0].length];
		int idx = 0;
		for (int i = 0; i < body.length; i++) {
			if (!rowsToRemove.contains(i)) {
				for (int j = 0; j < body[0].length; j++)
					newBody[idx][j] = body[i][j];
				idx++;
			}	
		}
		
		body = newBody;
	}
	
	/**
	 * This method creates the body of the table, maybe with duplicated columns or columns with singular values  (OneHot)
	 */
	public void createBody() {
		body = new Object[instanceSet.size()][headerAttributeNames.size()];
		int rowIdx = 0;
		for(Map<String, Object> instance : instanceSet) {
			for (String attName : headerAttributeNames) {
				String value = getIntegerValue(attName, instance);
				if (attName.equals(classAttName)) 
					addValueToBody(value, attName, rowIdx);
				else
					if (attributeType.get(attName).equals(Type.BOOLEAN))  {
						if (value.equals("true")) {
							addValueToBody(1, attName+"_true", rowIdx);
							addValueToBody(0, attName+"_false", rowIdx);
						}
					    else if (value.equals("false")) {
					    	addValueToBody(0, attName+"_true", rowIdx);
					    	addValueToBody(1, attName+"_false", rowIdx);
					    }
					    else {
					    	addValueToBody(0, attName+"_true", rowIdx);
					    	addValueToBody(0, attName+"_false", rowIdx);
					    }
					} else if (attributeType.get(attName).equals(Type.LITERAL))  {
						LinkedList<String> values = literalValues.get(attName);
						for (String val : values) {
							if (value == null) {
								addValueToBody(0, attName+"_"+val, rowIdx);
							} else {
								if (value.equals(val)) {
									addValueToBody(1, attName+"_"+val, rowIdx);
								}
								else {
									addValueToBody(0, attName+"_"+val, rowIdx);
								}
							}
						}
					} else {
						if (value != null) 
							addValueToBody(value, attName, rowIdx);
						else {
							if (interpolationMethod.equals("mean"))
								addValueToBody(meanValues.get(attName), attName, rowIdx);
							if (interpolationMethod.equals("Previouse Value")) 
								addValueToBody(meanValues.get(attName), attName, rowIdx);
						}
						
					}
			}
			
			rowIdx++;
		}
		
	}
	
	/**
	 * This method creates the body of the table, maybe with duplicated columns or columns with singular values.
	 * In this method literal values are mapped to Integers
	 */
	public void createBodyNumericalLiterals() {
		body = new Object[instanceSet.size()][headerAttributeNames.size()];
		int rowIdx = 0;
		for(Map<String, Object> instance : instanceSet) {
//			System.out.println("row " +rowIdx);
			int i = 0;
			for (String attName : headerAttributeNames) {
//				String value = getIntegerValue(attName, instance);
//				System.out.println("col " + i);
				i++;
				String value = null;
				if (instance.containsKey(attName)) {
//					System.out.println("attName  --> " + attName);
					if ( instance.get(attName) == null) {
						value = "0";
					} else 
						value = instance.get(attName).toString();
//					System.out.println("attValue  --> " + value);
				}
				else {
					value = "*";
//					System.out.println("null  --> " + attName);
				}
				if (attName.equals(classAttName)) 
					addValueToBody(getIntegerValue(attName, instance), attName, rowIdx);
				else
					if (attributeType.get(attName).equals(Type.BOOLEAN))  {
						if (value.equals("true")) 
							addValueToBody(1, attName, rowIdx);
					    else if (value.equals("false")) 
					    	addValueToBody(0, attName, rowIdx);
					    else 
					    	addValueToBody("*", attName, rowIdx);
					} else if (attributeType.get(attName).equals(Type.LITERAL))  {
						if (value == null) {
							addValueToBody("NOT SET", attName, rowIdx);
						} else {
					//		System.out.println(" value : " + numericalValueMap.get(attName).get(value));
							addValueToBody(numericalValueMap.get(attName).get(value), attName, rowIdx);
						}
					} else {
						if (value != null) 
							addValueToBody(value, attName, rowIdx);
						else 
							addValueToBody("*", attName, rowIdx);
					}
			
			}

			rowIdx++;
		}
		
		for (int i = 0; i < body[1].length; i++)
			for (int j = 0; j < body.length; j++)
				if (body[j][i] == null)
					body[j][i] = "*";
		
	}
	
	public void handelNullValue(String attName, int rowIdx, Map<String, Object> lastValues) {
		if (interpolationMethod.equals("mean"))
			addValueToBody(meanValues.get(attName), attName, rowIdx);
		if (interpolationMethod.equals("Previouse Value")) 		
		    if (attributeType.get(attName).equals(Type.BOOLEAN) || attributeType.get(attName).equals(Type.LITERAL)){
				if (lastValues.containsKey(attName))
					addValueToBody(lastValues.get(attName), attName, rowIdx);
				else {
					addValueToBody((int)(Math.random() * ( numericalValueMap.get(attName).size() + 1)), attName, rowIdx);
				}
			} else {
				if (lastValues.containsKey(attName))
					addValueToBody(lastValues.get(attName), attName, rowIdx);
				else 
					addValueToBody(meanValues.get(attName), attName, rowIdx);
			}
			
	}
	
	/**
	 * It creates the header of the table, maybe includes the name of duplicated columns or columns with singular values
	 */
	
	public void createHeader() {
		headerAttributeNames = new LinkedList<String>();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			if (attName.equals(classAttName)) 
				headerAttributeNames.add(attName);
			else 
				if (attributeType.get(attName).equals(Type.BOOLEAN)) {
					headerAttributeNames.add(attName + "_" + "true");
					headerAttributeNames.add(attName + "_" + "false");
				} 
				else if (attributeType.get(attName).equals(Type.LITERAL)) {
					for (String str : literalValues.get(attName)) 
						headerAttributeNames.add(attName + "_" + replaceNotAllowedStrings(str));
				}
				else 
					headerAttributeNames.add(attName);
		}
		createHeaderAttNameColumnNumberMap();
	}
	

	/**
	 * It creates the header of the table, maybe includes the name of duplicated columns or columns with singular values.
	 * In this method it is assumed that literal attributes are mapped to integers
	 */
	
	public void createHeaderNumericalLiterals() {
		headerAttributeNames = new LinkedList<String>();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) 
			headerAttributeNames.add(attName);
		createHeaderAttNameColumnNumberMap();
	}
	
	
	public void createHeaderAttNameColumnNumberMap() {
		headerAttNameColumnNumberMap = new HashMap<String, Integer>();
		Integer idx = 0;
		for (String attName : headerAttributeNames) {
			
			headerAttNameColumnNumberMap.put(attName, idx);
			idx++;
		}
	}
	/**
	 * 
	 *  It save the data in the numerical format in a txt file
	 */
	public void txtDataFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("DataTableOneHot.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + '\t';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		for(Map<String, Object> instance : instanceSet) {
			String row = new String();
			row = convertToTxtRow(instance);
			writer.println(row);
		}
		writer.close();
	}  
	
	/**
	 * This function creates a string of the text file of the data table
	 */
	public void stringDataTable() {
		stringDataTable = new String();
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + '\t';
		}
		header = header.substring(0, header.length() - 1);
		stringDataTable = stringDataTable + header + '\n';
		
		//creating the body of the table ; i.e. inserting a row for each 
		for(Map<String, Object> instance : instanceSet) {
			String row = new String();
			row = convertToTxtRow(instance);
			stringDataTable = stringDataTable + row + '\n';
		}
	}
	
	/**
	 * 
	 *  It save the data about Expert department in the numerical format in a txt file
	
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
			header = header + attName + '\t';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		int idx = 0;
		for(Map<String, Object> instance : instanceSet) {
			String row = new String();
			row = convertToRow(instance, idx);
			writer.println(row);
			idx++;
		}
		writer.close();
	}    */ 
	
	/**
	 * 
	 *  It save the data about other departments but Expert department in the numerical format in a txt file
	
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
			header = header + attName + '\t';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		int idx = 0;
		for(Map<String, Object> instance : instanceSet) {
			String row = new String();
			row = convertToRow(instance, idx);
			writer.println(row);
			idx++;
		}
		writer.close();
	}     */
	
	/**
	 * 
	 *  It save one instance of the data in the numerical format in a txt file
	 */
	public void writeInstanceInFile(Map<String, Object> instance) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("DataTableOneHot.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			header = header + attName + '\t';
		}
		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		String row = new String();
		row = convertToRow(instance, 0);
		writer.println(row);
		writer.close();
	}
	
	/**
	 * 
	 *  It creates a row of the temporal data file in the right format out of each instance 
	 *  in the instance set.
	 */
	public String convertToRow(Map<String, Object> instance, int rowIdx) {
		String row = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			String value = getIntegerValue(attName, instance);
			if (attName.equals(classAttName)) {
				row = row + value;
				addValueToBody(value, attName, rowIdx);
			}
			else
				if (attributeType.get(attName).equals(Type.BOOLEAN))  {
					if (value.equals("true")) {
						row = row + "1\t0\t";
						addValueToBody(1, attName+"_true", rowIdx);
						addValueToBody(0, attName+"_false", rowIdx);
					}
				    else if (value.equals("false")) {
				    	row = row + "0\t1\t";
				    	addValueToBody(0, attName+"_true", rowIdx);
				    	addValueToBody(1, attName+"_false", rowIdx);
				    }
				    else {
				    	row = row + "0\t0\t";
				    	addValueToBody(0, attName+"_true", rowIdx);
				    	addValueToBody(0, attName+"_false", rowIdx);
				    }
				} else if (attributeType.get(attName).equals(Type.LITERAL))  {
					LinkedList<String> values = literalValues.get(attName);
					for (String val : values) {
						if (value == null) {
							row = row + "0\t";
							addValueToBody(0, attName+"_"+val, rowIdx);
						} else {
							if (value.equals(val)) {
								row = row + "1\t";
								addValueToBody(1, attName+"_"+val, rowIdx);
							}
							else {
								row = row + "0\t";
								addValueToBody(0, attName+"_"+val, rowIdx);
							}
						}
					}
				} else {
					if (value != null) {
						row = row + value + "\t";
						addValueToBody(value, attName, rowIdx);
					}
					else {
						row = row + "*\t";
						addValueToBody("* ", attName, rowIdx);
					}
				}
		}
		
		row = row.substring(0, row.length() - 1);
		return row;
	}
	
	/**
	 * 
	 *  Add values to the body of the table 
	 *  
	 */
	public void addValueToBody(Object value, String columnName, int rowIdx) {
		
		if (headerAttNameColumnNumberMap.containsKey(columnName))
			body[rowIdx][headerAttNameColumnNumberMap.get(columnName)] = value;
		else
			body[rowIdx][headerAttNameColumnNumberMap.get(columnName)] = value;
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
//			if (time != null && time < 0)
//				System.out.println("Date : "+ obj.toString() + " Time : "+ time);
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
		
		// if rimestamp
		if (attType.equals(Type.TIMESTAMP)) {
			Long time = ((Date) obj).getTime();
//			if (time != null && time < 0)
//				System.out.println("Date : "+ obj.toString() + " Time : "+ time);
			return time.toString();
		}
		
		// if boolean
		if (attType.equals(Type.BOOLEAN)) {
			if (obj.toString().equals("true"))
				return "1";
			else if (obj.toString().equals("false"))
				return "0";
			else
				return "NOT SET";
		}
		
		// if literal
		if (attType.equals(Type.LITERAL)) {
//			System.out.println(numericalValueMap.containsKey(attName));
//			System.out.println(numericalValueMap.get(attName));
//			if (obj == null)
//				System.out.println(obj.toString());
//			System.out.println(numericalValueMap.get(attName).get(obj.toString()) != null);
//			System.out.println();
			if (obj == null)
				return "NOT SET";
			
			if (numericalValueMap.containsKey(attName) && numericalValueMap.get(attName).get(obj.toString()) != null)
				return numericalValueMap.get(attName).get(obj.toString()).toString();
			else
				return "*";
		}
		
		if (obj == null)
			return "*";
		
		return obj.toString();
	}
	
	/**
	 * 
	 *  It returns the string format of the data table that can be used
	 *  with the command line version of the tetrad.
	 */
	public String getStringDataTable() {
		return stringDataTable;
	}
	
	/**
	 * This function a set of all the names of the columns that have just one value.
	 */
	/*public Set<String> removeMonoValueCulumns() {
		Set<String> attsToRemove = new HashSet<String>();
		Set<String> attsToInvestigate = new HashSet<String>();
		for (String attName : attributeType.keySet()) {
			if (attributeType.get(attName).equals(Type.LITERAL))
				if (literalValues.get(attName).size() <= 1)
					attsToRemove.add(attName);
			else
				attsToInvestigate.add(attName);
	}*/

	public LinkedList<String> getAttributeNames() {
		return headerAttributeNames;
	}
	
	public String[] getHeader() {
		String[] attNames = new String[headerAttributeNames.size()];
		int i = 0;
		for (String attName : headerAttributeNames) {
			attNames[i] = attName;
			i++;
		}
		
		return attNames;
	}
	
	// removes the not allowed char for the consistency
   	public String replaceNotAllowedStrings(String str) {
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}
   	
   	public Set<Integer> findSingularValueColumns() {
   		// finding the columns with just one value
   		Set<Integer> singularValuedColumnsIndices = new HashSet<Integer>();
   		System.out.println("Singular value atts:");
   		
   		for (int i = 0; i < body[0].length ; i++) {
   			
   			boolean singularValued = true;
   			boolean justNull = true;
   			Object first = body[0][i];
   			for (int j = 1; j < body.length ; j++) {
   				if ( body[j][i] != null && first != null && !first.equals(body[j][i])) {
   					singularValued = false;
   				}
   				
   				if (body[j][i] != null && !body[j][i].equals("*")) 
					justNull = false;
   			}
   			
   			if (singularValued || justNull) {
   				singularValuedColumnsIndices.add(i);
   				System.out.println(headerAttributeNames.get(i));
   			}
   		}
   		
   		return singularValuedColumnsIndices;
   	}
   	
   	/**
   	 * 
   	 * It removes the columns whose index is in input set
   	 */
   	public void removeColumns(Set<Integer> indices)	{
   		//removing columns with singular value
   		if (indices.size() > 0) {
   			// clean body
   			Object[][] newBody = new Object[body.length][body[0].length - indices.size()];
   			for (int i = 0; i < body.length ; i++) {
   				int idx = 0;
   	   			for (int j = 0; j < body[0].length ; j++) 
   	   				if (!indices.contains(j)) {
   	   					newBody[i][idx] = body[i][j];
   	   					idx++;
   	   				}
   	   		}
   	   		body = newBody;
   	   		
   	   		//clean header
   	   		LinkedList<String> attNames = new LinkedList<String>();
   	   		int idx = 0;
   	   		for (String name : headerAttributeNames) {
   	   			if (!indices.contains(idx)) 
   	   			 attNames.add(name);
   	   			idx++;
   	   		}
  			headerAttributeNames = attNames;
   		}

   	}
   	
   	/**
   	 * This method finds the columns that are duplicated
   	 * @return
   	 */
   	public Set<Integer> findDuplicateColumns() {
   		// finding the columns with just one value
   		Set<Integer> duplivateColumnsIndices = new HashSet<Integer>();
   		System.out.println("Duplicate atts:");
   		System.out.println(" cols "+body[0].length+" rows " +  body.length + "  ");
   		for (int i = 0; i < body[0].length ; i++) {
   			for (int j = i+1; j < body[0].length ; j++) {
   				boolean isDuplicate = true;
				boolean isPrinted = false;
   				for (int k = 0; k < body.length ; k++)  {
  				/**	if (i==2 && j==22) {
  						System.out.println(" "+i +"  " + j + "  " + k);
   						Object[] o = body[k];
   						System.out.println(body[k][i]);
   						System.out.println(body[k][j]);
   						System.out.println(body[k][i].equals(body[k][j]));
   						System.out.println(body[k][i].equals("*"));
   						System.out.println(body[k][j] == null || body[k][j].equals("*"));
   					} */
   					if (!body[k][i].equals(body[k][j]) && !(body[k][i].equals("*") && (body[k][j].equals("*") || body[k][j] == null))) {
   						if (!isPrinted) {
  // 							System.out.println(j);
   							isPrinted = true;
   						}
   						isDuplicate = false;
   					}
   				}// else 
   					//	System.out.println(body[k][i].toString() + "    "+ body[k][j].toString());

   				if (isDuplicate) {
   	   				duplivateColumnsIndices.add(j);
   	   				System.out.println(headerAttributeNames.get(i)+ " -- " + headerAttributeNames.get(j));
   				}
   			}
   		}
   		
   		return duplivateColumnsIndices;
   	}
   	
   	public Object[][] getTableBody() {
		return body;
	}
   	
   	public Object[][] getNumericalBody() {
   		return numericalBody;
   	}
   	
   	public Map<String, Map<Integer, String>> getInverseNumericalValueMap() {
   		return inverseNumericalValueMap;
   	}
   	
   	public void setInterpolationMethod(String method) {
   		this.interpolationMethod = method;
   	}
	public void setNullValueThresholdInARow(int t) {
		nullValueThresholdInARow = t;
	}
	
	public void setNullValueThresholdInAColumn(int t) {
		nullValueThresholdInAColumn = t;
	}
	
	public void setRemoveNullValues(boolean b) {
		removeNullValues = b;
	}
	
	public void setMeanMedianVariance() {
		mean = new HashMap<String, Double>();
		median = new HashMap<String, Double>();
		variance = new HashMap<String, Double>();
		standardDeviation  = new HashMap<String, Double>();
		for (int column = 0; column < body[0].length; column++) {
			mean(column);
			variance(column);
			stdDev(column);
			median(column);
		}
	}

	
	public void mean(int column) {
        double sum = 0.0;
        for(int row = 0 ; row < body.length; row++)
        	if (!(body[row][column].toString()).equals("*") && !(body[row][column].toString()).equals("NOT SET")) 
        		sum += Double.valueOf(body[row][column].toString());
        			

        		
        mean.put(headerAttributeNames.get(column), sum/body.length);
    }

    public void  variance(int column) {
        double m = this.mean.get(headerAttributeNames.get(column));
        double temp = 0;
        for(int row = 0 ; row < body.length; row++)
        	if (!(body[row][column].toString()).equals("*") && !(body[row][column].toString()).equals("NOT SET"))
        		temp += (Double.valueOf(body[row][column].toString())-m)*(Double.valueOf(body[row][column].toString())-m);
        		
        variance.put(headerAttributeNames.get(column), temp/(body.length-1));
    }

    public void stdDev(int column) {
    		standardDeviation.put(headerAttributeNames.get(column), Math.sqrt(variance.get(headerAttributeNames.get(column))));
    }

    public void median(int column) {
    	LinkedList<Double> list = new LinkedList<Double>();
    	for(int row = 0 ; row < body.length; row++)
    		if (!(body[row][column].toString()).equals("*")  && !(body[row][column].toString()).equals("NOT SET"))
    			list.add(Double.valueOf(body[row][column].toString()));

    	double[] data = new double[list.size()];
    	for (int i = 0; i < list.size(); i++)
    		data[i] = list.get(i);
    		
        Arrays.sort(data);
        if (data.length % 2 == 0)
            median.put(headerAttributeNames.get(column), ((data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0));
        else
    	    median.put(headerAttributeNames.get(column), data[data.length / 2]);
    }
    
    public Map<String, Double> getMean() {
    	return mean;
    }
    
    public Map<String, Double> getMedian() {
    	return median;
    }
    
    public Map<String, Double> getVariance() {
    	return variance;
    }
    
    public Map<String, Double> getStdDev() {
    	return standardDeviation;
    }
    
    // ---------------------------- Limit Data Table -------------------------
    /**
     * remove all the columns from the data table except the one in the attNames
     * @param nodeNames : set of attribute names that we want to keep
     */
	public void limitDataTable(Set<String> attNames) {
		
		LimitDataTable ldt = new LimitDataTable(headerAttributeNames, body);
		ldt.limitDataTable(attNames);
	}
}
