package org.processmining.causalityinference.algorithms;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.datadiscovery.estimators.Type;

public class SituationFeatureTableVisualizer extends JPanel {
	String[] columnNames;
	Object[][] data;
	JScrollPane tableScrollPane;
	LeftPanelMain leftPanel;
	Map<String, Map<Integer, String>> inverseMap = null;
	String classAttName;
	Map<String, Type> types;
	
	public SituationFeatureTableVisualizer(String[] header, Object[][] body, Map<String, Map<Integer, String>> inverseNumericalValueMap, String classAttName, Map<String, Type> types) {
		this.columnNames = header;
		this.classAttName = classAttName;
		this.types = types;
		if (inverseNumericalValueMap != null) {
			this.inverseMap = inverseNumericalValueMap;
			this.data = inverse(body);
		} else {
			this.inverseMap = null;
			this.data = body;
		}
			
		this.data = stringCellValues(data);
	}
	
	/**
	 * This method replace the values of literal values (which are already replaced with integer identifiers)
	 * with their actual values.
	 * @param body
	 * @return
	 */
	public Object[][] inverse(Object[][] body) {
		Object[][] d = new Object[body.length][body[0].length];
		for (int i = 0; i < body.length; i++) 
			for (int j = 0; j <body[0].length; j++) {
				if (body[i][j] == null || body[i][j].equals("*"))
					d[i][j] = "*";
				else {
					if (inverseMap.containsKey(columnNames[j])) {
						Map<Integer, String> temp = inverseMap.get(columnNames[j]);
						if (!body[i][j].toString().equals("NOT SET"))
							d[i][j] = temp.get(Integer.valueOf(body[i][j].toString()));
						else
							d[i][j] = "NOT SET";
					} else 
						d[i][j] = body[i][j];
				}	
			}	
		return d;	
	}
	
	/**
	 * For the sack of consistency, it turn all the values in the cells of a table to String format.
	 * @param body
	 * @return
	 */
	public Object[][] stringCellValues(Object[][] body) {
		Object[][] d = new Object[body.length][body[0].length];
		for (int i = 0; i < body.length; i++) 
			for (int j = 0; j <body[0].length; j++) 
				d[i][j] = body[i][j].toString();
		
		return d;
	}
	
	public void setAwareLeftPanel(LeftPanelMain leftPanel) {
		this.leftPanel = leftPanel;
	}
	
	public JTable getTableForPupup(Map<String, Map<Integer, String>> inverseMap) {
		JTable table = new JTable(new TableModel(columnNames, data));
		table.setGridColor(Color.YELLOW);
		table.setBackground(Color.lightGray);
		return table;
	}
	
	public void addTableToView() {	
		JTable table = new JTable(new TableModel(columnNames, data));
		table.setGridColor(Color.YELLOW);
		table.setBackground(Color.lightGray);
		
		tableScrollPane = new JScrollPane(table);
		tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setPreferredSize(new Dimension(800, 800));
		
		tableScrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		tableScrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.tableScrollPane.updateUI();
		this.add(this.tableScrollPane);
		this.updateUI();
		
		leftPanel.tableVisualizer.removeAll();
		leftPanel.tableVisualizer.add(this);
		leftPanel.tableVisualizer.updateUI();
	}
	
	public JScrollPane getTableJScrollPane()
	{
		JTable table = new JTable(new TableModel(columnNames, replaceTimeStamps()));
		table.setGridColor(Color.YELLOW);
		table.setBackground(Color.lightGray);
		
		DefaultTableCellRenderer rendar1 = new DefaultTableCellRenderer();
	    rendar1.setBackground(Color.pink);
	    
	    if (getClassColIndex() != -1)
	    	table.getColumnModel().getColumn(getClassColIndex()).setCellRenderer(rendar1);
		
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		return tableScrollPane;
	}
	
	/**
	 * 
	 * @param data with possible timestamps in milliseconds
	 * @return data with string and human friendly time stamps 
	 */
	private Object[][] replaceTimeStamps() {
		Object[][] newData = new Object[data.length][data[0].length];
		for (int i = 0; i < columnNames.length; i++) {
			if (types.containsKey(columnNames[i]) && columnNames[i].contains("Timestamp")) {
				for (int j = 0; j < data.length; j++)
					if (!data[j][i].toString().equals("*")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS",Locale.US);

						GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
					
						calendar.setTimeInMillis(Long.parseLong(data[j][i].toString()));

						newData[j][i] = sdf.format(calendar.getTime());
					} else
						newData[j][i] = data[j][i];
				
			} else {
				for (int j = 0; j < data.length; j++)
					newData[j][i] = data[j][i];
			}
		}
				
		return newData;
	}
	
	/**
	 * 
	 * @return the index of the class attribute column in the final table.
	 */
	private int getClassColIndex() {
		int idx = 0;
		
		for (int i = 0; i < columnNames.length; i++)
			if (columnNames[i].equals(classAttName))
				return i;
		
		return -1;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

		//implement a table model by extending a class to use
		//the AbstractTableModel
	class TableModel extends AbstractTableModel{
		String[] columnNamesTable;
		Object[][] dataTable;
		
		public TableModel() {
			 //TODO
		}
		
		public TableModel(String[] header, Object[][] body) {
			this.columnNamesTable = header;
			this.dataTable = body;
		}
		
		@Override
		public int getRowCount()
		{
			return this.dataTable.length;
		}
		
		@Override
		public int getColumnCount()
		{
			return this.columnNamesTable.length;
		}
		
		@Override
		public Object getValueAt(int row, int column)
		{
			return this.dataTable[row][column];
		}
		
		//Used by the JTable object to set the column names
		@Override
		public String getColumnName(int column) {
			return this.columnNamesTable[column];
		}
		
		//Used by the JTable object to render different
		//functionality based on the data type
		@Override
		public Class getColumnClass(int c) {
	//		System.out.println(getValueAt(0, c).toString());
			return getValueAt(0, c).getClass();
		}
		
		@Override
		public boolean isCellEditable(int row, int column)
		{
			if (column == 0 || column == 1)
				return false;
			else
				return true;
		}
	}
	
	public void writeTableToFile(){

	    try{
	        Writer output = null;
	        File file = new File("csvData.txt");
	        output = new BufferedWriter(new FileWriter(file));
	        
	        String h = headerString();
	        output.write(h);
	        output.write(System.getProperty( "line.separator" ));
	        
	        for(int rowNum = 0; rowNum < data.length; rowNum++){
	           String r = row(rowNum);
	           output.write(r);
	          output.write(System.getProperty( "line.separator" ));
	        }

	        output.close();
	        System.out.println("File has been written");

	    }catch(Exception e){
	        System.out.println("Could not create file");
	    }
	}
	
	private String row(int rowNum) {
		String r = new String();
		for (int col = 0; col < columnNames.length; col++)
			r = r + data[rowNum][col].toString() + ",";
		
		return r.substring(0, r.length()-1);
	}

	private String headerString() {
		String h = new String();
		for (int i = 0; i < columnNames.length; i++)
			h = h + columnNames[i] + ",";
		
		return h.substring(0, h.length()-1);
	}
}