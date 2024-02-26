package org.processmining.causalityinference.ui.uiComponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.processmining.causalityinference.dialogs.RelativeLayout;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.TimeUnit;

public class TimeSettingPanel {
	
    private JFrame frame;
    private JTable CompTable = null;
    private CompTableModel CompModel = null;
    private JButton addButton = null;
    private Parameters params;
    Map<Integer, Comp> timeLines = new HashMap<>();
    int num = 1;
    JPanel panel;
    
    public TimeSettingPanel(Parameters params) {
    	this.params = params;
    }
    
    public TimeSettingPanel() {
    	
    }

    public static void main(String args[]) {
    	TimeSettingPanel tsp = new TimeSettingPanel();
        tsp.makeUI();
    }

    public void makeUI() {
    	SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new TimeSettingPanel().generatePanel();
            }
        });
    }
    
    public JPanel generatePanel() {
    	
  /**  	String[] options = {"Done!"};
    	panel = new JPanel();
    	panel.add(new JLabel("Time unit:"));
    	TimeUnitDuration tu = new TimeUnitDuration(params);
		panel.add(tu.getTimeUnitPanel(), BorderLayout.NORTH);
    	panel.add(new JLabel("Time unit    |    Granularity |    Offset"),BorderLayout.NORTH);
    	
    	CompTable = CreateCompTable();
        JScrollPane CompTableScrollpane = new JScrollPane(CompTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(CompTableScrollpane, BorderLayout.CENTER);
        panel.add(CreateBottom(), BorderLayout.SOUTH);
        panel.repaint();
        
        int selectedOption = JOptionPane.showOptionDialog(null, panel, "Time line setting:", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

        if(selectedOption == 0)
        {
        	getTimeLineSetting();
        } */
        return panel; 
    } 
    
    public JPanel getTimeLinePanel() {
    	panel = new JPanel();
    	panel.setBorder(BorderFactory.createEtchedBorder());
    	panel.setLayout(new RelativeLayout(RelativeLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());
    	
    	TimeUnitDuration tu = new TimeUnitDuration(params);
		panel.add(tu.getTimeUnitPanel(), new Float(25));
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
    	p.add(new JLabel("  Time unit        |    Granularity     |    Offset"),BorderLayout.NORTH);
    	
    	CompTable = CreateCompTable();
        JScrollPane CompTableScrollpane = new JScrollPane(CompTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        p.add(CompTableScrollpane, BorderLayout.CENTER);
        p.add(CreateBottom(), BorderLayout.SOUTH);
        p.repaint();
        panel.add(p, new Float(75));
        return panel;
    }
    
    
    
    public LinkedList<Object[]> getTimeLineSetting() {
    	LinkedList<Object[]> timeLineParams = new LinkedList<>();
    	System.out.println(CompTable.getRowCount());
    	for (int i = 0; i < CompTable.getRowCount(); i++) {
    		Comp c = (Comp) CompTable.getValueAt(i, 0);
    		Object[] oneTimeLine = new Object[3];
    		oneTimeLine[0] = c.getTimeUnit();
    		oneTimeLine[1] = c.getGranularity();
    		oneTimeLine[2] = c.getOffset();
    		timeLineParams.add(oneTimeLine);
    	}
    	
/**    	//TODO test code
    	timeLineParams = new LinkedList<>();
    	
    	Object[] oneTimeLine1 = new Object[3];
    	oneTimeLine1[0] = TimeUnit.W;
		oneTimeLine1[1] = "1";
		oneTimeLine1[2] = "2";
		timeLineParams.add(oneTimeLine1);
		
		Object[] oneTimeLine2 = new Object[3];
    	oneTimeLine2[0] = TimeUnit.D;
		oneTimeLine2[1] = "2";
		oneTimeLine2[2] = "7";
		timeLineParams.add(oneTimeLine2);
		//TODO end TestCode */
    	
    	return timeLineParams;
    }
    

    public JTable CreateCompTable() {
        CompModel = new CompTableModel();
        CompModel.addRow();
        JTable table = new JTable(CompModel);
        table.setRowHeight(new CompCellPanel().getPreferredSize().height);
        table.setTableHeader(null);
        CompCellEditorRenderer compCellEditorRenderer = new CompCellEditorRenderer();
        table.setDefaultRenderer(Object.class, compCellEditorRenderer);
        table.setDefaultEditor(Object.class, compCellEditorRenderer);
        return table;
    }

    public JButton CreateBottom() {
        addButton = new JButton("Add time line");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Object source = ae.getSource();

                if (source == addButton) {
                    CompModel.addRow();
//                    CompModel.fireTableDataChanged(); // moved to TableModel
                    System.out.println("if line has been added, num rows : " + CompModel.getRowCount());
                }
            }
        });
//        JPanel panel = new JPanel(new GridBagLayout());
 //       panel.add(addButton);
 //       return panel;
        return addButton;
    } 


class TimeUnitDuration {
	private Parameters params;
    
    JRadioButton millisec;
	JRadioButton sec;
	JRadioButton min;
	JRadioButton hour;
	JRadioButton day;
	JRadioButton week;

	public TimeUnitDuration(Parameters params) {
		this.params = params;
	}
	
	public JPanel getTimeUnitPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2,1));
		mainPanel.add(new JLabel("Duration time unit"));
		
		JPanel panel = new JPanel();
		
		TimeUnitHandler tuh = new TimeUnitHandler();
		millisec = new JRadioButton("Millisecond");
		millisec.addActionListener(tuh);
		panel.add(millisec);
		sec = new JRadioButton("Seccond");
		sec.addActionListener(tuh);
		panel.add(sec);
		min = new JRadioButton("Minute");
		min.addActionListener(tuh);
		panel.add(min);
		hour = new JRadioButton("Hour");
		hour.addActionListener(tuh);
		panel.add(hour);
		day = new JRadioButton("Day");
		day.addActionListener(tuh);
		panel.add(day);
		week = new JRadioButton("Week");
		week.addActionListener(tuh);
		panel.add(week);
		
		day.setSelected(true);
		
		ButtonGroup group = new ButtonGroup();
		group.add(millisec);
		group.add(sec);
		group.add(min);
		group.add(hour);
		group.add(day);
		group.add(week);
		
		mainPanel.add(panel);
		return mainPanel;
	}
    
    private class TimeUnitHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(millisec))
				params.setTimeUnit(TimeUnit.MS);
			else if (e.getSource().equals(sec))
				params.setTimeUnit(TimeUnit.Sec);
			else if (e.getSource().equals(min))
				params.setTimeUnit(TimeUnit.Min);
			else if (e.getSource().equals(hour))
				params.setTimeUnit(TimeUnit.H);
			else if (e.getSource().equals(week))
				params.setTimeUnit(TimeUnit.W);
			else 
				params.setTimeUnit(TimeUnit.D);
			
		}	
	}
	  
}

class CompCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

    private static final long serialVersionUID = 1L;
    private CompCellPanel renderer = new CompCellPanel();
    private CompCellPanel editor = new CompCellPanel();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        renderer.setComp((Comp) value);
        return renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setComp((Comp) value);
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getComp();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }
}

class CompTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    @Override
    public int getColumnCount() {
        return 1;
    }

    public void addRow() {
    	Comp c = new Comp(4, "1", "0");
        super.addRow(new Object[]{c});
        //super.fireTableDataChanged();
    }
}

class Comp {
    int timeUnit;
    String lower;
    String upper;

    public Comp(int timeUnit, String lower, String upper) {
        this.timeUnit = timeUnit;
        this.lower = lower;
        this.upper = upper;
        
    }
    
    public TimeUnit getTimeUnit() {
    	System.out.println("time unit : " + timeUnit);
    	if (timeUnit == 0)
    		return TimeUnit.MS;
    	if (timeUnit == 1)
    		return TimeUnit.Sec;
    	if (timeUnit == 2)
    		return TimeUnit.Min;
    	if (timeUnit == 3)
    		return TimeUnit.H;
    	if (timeUnit == 5)
    		return TimeUnit.W;
    	return TimeUnit.D;
    }
    
    public String getGranularity() {
    	System.out.println("window : " + lower);
    	return lower;
    }
    
    public String getOffset() {
    	System.out.println("offset : " + upper);
    	return upper;
    }
}

class CompCellPanel extends JPanel {
	
    private static final long serialVersionUID = 1L;
    private JComboBox timeUnitCombo = new JComboBox(new Object[]{TimeUnit.MS, TimeUnit.Sec, TimeUnit.Min, TimeUnit.H, TimeUnit.D, TimeUnit.W});
    private JTextField granularityField = new JTextField();
    private JTextField offsetField = new JTextField();
    private JButton removeButton = new JButton("remove");
    private Parameters params;

    CompCellPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, (Component) e.getSource());
                int row = table.getEditingRow();
                System.out.println("row : " + row);
                table.getCellEditor().stopCellEditing();
                ((DefaultTableModel) table.getModel()).removeRow(row);
            }
        });
        add(timeUnitCombo);
        add(granularityField);
        add(offsetField);
        add(Box.createHorizontalStrut(100));
        add(removeButton);
    }


    public void setComp(Comp Comp) {
        timeUnitCombo.setSelectedIndex(Comp.timeUnit);
        granularityField.setText(Comp.lower);
        offsetField.setText(Comp.upper);
    }


    public Comp getComp() {
        return new Comp(timeUnitCombo.getSelectedIndex(), granularityField.getText(), offsetField.getText());
    }
}
}