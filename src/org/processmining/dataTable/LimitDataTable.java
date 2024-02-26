package org.processmining.dataTable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Set;

/**
 * Given a set of attribute names and the header and the body of a table,
 * removes all the columns in the table except those that their names are
 * in the given attribute name set. The resulting table id saved in :
 *      "DataTableLimited.txt"
 * 
 * @author Mahnaz
 *
 */
public class LimitDataTable {
	LinkedList<String> headerAttributeNames;
	Object[][] body;
	String path = "DataTableLimited.txt";

	public LimitDataTable(LinkedList<String> headerAttributeNames, Object[][] body) {
		this.headerAttributeNames = headerAttributeNames;
		this.body = body;
		
	}

	public void limitDataTable(Set<String> attNames) {
		int[] idxToKeep = limitHeader(attNames);
		limitBody(idxToKeep);
		writeLimitedTableToFile();
	}
	
	/**
     * remove all the columns from the header of the data table except the one in the attNames
     * @param nodeNames : set of attribute names that we want to keep
     */
	private int[] limitHeader(Set<String> attNames) {
		
		LinkedList<String> newHeader = new LinkedList<>();
		int[] idxToKeep = new int[attNames.size()];
		
		int i = 0;
		int j = 0;
		for (String attName : headerAttributeNames) {
			if (attNames.contains(attName)) {
				newHeader.add(attName);
				idxToKeep[i] = j;
				i++;
			}
			j++;
		}
		
		headerAttributeNames = newHeader;
		
		return idxToKeep;
	}
	
	public void limitBody(int[] idxs) {
		
		Object[][] newBody = new Object[body.length][idxs.length];
		
		for (int i = 0; i < body.length; i++) {
			for (int k = 0; k < idxs.length; k++) {
				newBody[i][k] = body[i][idxs[k]];
			}
		}
		
		body = newBody;
	}
	
	/**
	 * This method writes the limited table to a text file.
	 * All the attribute values are numerical and there is no missing value inthis data.
	 */
	public void writeLimitedTableToFile() { 
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path, "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		writer.println(headerToString());

		for(int idx = 0 ; idx < body.length ; idx++) 
			writer.println(rowToString(body[idx]));

		writer.close();
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

}
