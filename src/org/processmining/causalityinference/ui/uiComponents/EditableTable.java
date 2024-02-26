package org.processmining.causalityinference.ui.uiComponents;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class EditableTable {

  public static void main(String[] a) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    String[] columnTitles = { "First Name", "Last Name", "Weight (lb)", "Blood Group", "Age>20yrs" };
    Object[][] dataEntries = { { "Saravan", "Pantham", new Integer(50), "B", new Boolean(false) },
        { "Eric", "", new Integer(180), "O", new Boolean(true) },
        { "John", "", new Integer(120), "AB", new Boolean(false) },
        { "Mathew", "", new Integer(140), "A", new Boolean(true) }, };
    TableModel model = new EditableTableModel(columnTitles, dataEntries);
    JTable table = new JTable(model);
    table.createDefaultColumnsFromModel();

    String[] bloodGroups = { "A", "B", "AB", "O" };
    JComboBox comboBox = new JComboBox(bloodGroups);
    table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));

    frame.add(new JScrollPane(table));

    frame.setSize(300, 200);
    frame.setVisible(true);
  }

}

class EditableTableModel extends AbstractTableModel {
  String[] columnTitles;

  Object[][] dataEntries;

  int rowCount;

  public EditableTableModel(String[] columnTitles, Object[][] dataEntries) {
    this.columnTitles = columnTitles;
    this.dataEntries = dataEntries;
  }

  public int getRowCount() {
    return dataEntries.length;
  }

  public int getColumnCount() {
    return columnTitles.length;
  }

  public Object getValueAt(int row, int column) {
    return dataEntries[row][column];
  }

  public String getColumnName(int column) {
    return columnTitles[column];
  }

  public Class getColumnClass(int column) {
    return getValueAt(0, column).getClass();
  }

  public boolean isCellEditable(int row, int column) {
    return true;
  }

  public void setValueAt(Object value, int row, int column) {
    dataEntries[row][column] = value;
  }
}