package org.processmining.causalityinference.ui.uiComponents;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.causalityinference.algorithms.SelectionUtil;
import org.processmining.causalityinference.parameters.ActivityGrouperAttName;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.causalityinference.parameters.TimeIntervalAttributeLevel;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import csplugins.id.mapping.ui.CheckComboBox;

public class DependentAtt {
	
	SituationType situationType = SituationType.TS; 
	
	Parameters params;
	
	JComboBox<ActivityGrouperAttName> actGrouperCB;
	String minThreshold;
	String maxThreshold;
	Collection<String> selectedValues;
	JComboBox<String> attCB;
	JComboBox<String> attCB2;
	JLabel depAttLabel = new JLabel("Select the dependent attribute name:");
	JComboBox<TimeIntervalAttributeLevel> levelCB;
	
	public DependentAtt(Parameters params) {
		this.params = params;
		situationType = params.getSituationType();
	}
	
	public JPanel setDepAttPanel() {
		JPanel panel = new JPanel();
		
		if (situationType.equals(SituationType.TS)) 
			setTraceDepAttPanel(panel);
		else if (situationType.equals(SituationType.ES)) 
			setEventDepAttPanel(panel);
		else if (situationType.equals(SituationType.CS)) 
			setChoiceDepAttPanel(panel);
		else
			setTimeIntervalDepAttPanel(panel);
		
		return panel;
	}

	private void setTimeIntervalDepAttPanel(JPanel panel) {
		panel.setLayout(new GridLayout(4,1));
		
		//  dependent attribute label
		panel.add(depAttLabel);
		
		// attribute level 
		levelCB = new JComboBox<>();
		TimeIntervalAttributeLevel[] levels = {TimeIntervalAttributeLevel.PL, TimeIntervalAttributeLevel.TL, TimeIntervalAttributeLevel.EL, TimeIntervalAttributeLevel.RL};
		levelCB.setModel( new DefaultComboBoxModel<TimeIntervalAttributeLevel>(levels));
		panel.add(levelCB);
		
		// attribute name
		attCB = new JComboBox<>();
        attCB.setPreferredSize(new Dimension(150, 30));
        attCB.setEnabled(false);
        panel.add(attCB);
        
        // which activity or resource
        attCB2 = new JComboBox<>();
        attCB2.setPreferredSize(new Dimension(150, 30));
        attCB2.setEnabled(false);
        panel.add(attCB2);
        
        //add action listener to levelCB 
        ActionListenerTimeInterval handler = new ActionListenerTimeInterval(this, params);
        levelCB.addActionListener(handler);
        
        ActionListenerSetDepAttName handlerDepAttName = new ActionListenerSetDepAttName(this, params);
        attCB.addActionListener(handlerDepAttName);
        attCB2.addActionListener(handlerDepAttName);
        
	}

	private void setChoiceDepAttPanel(JPanel panel) {
		JButton button = new JButton("Select choice place");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handelChoice();
			}
			
		});
//		handelChoice();
		panel.add(button);
	}
	
	public TimeIntervalAttributeLevel getLevel() {
		return (TimeIntervalAttributeLevel) levelCB.getSelectedItem();
	}
	
	public JComboBox getAttCB() {
		return attCB;
	}
	
	public JComboBox getAttCB2() {
		return attCB2;
	}
	
	/**
	 * Get the target choice place from user and set it classAttName in params
	 */
	private void handelChoice() {
		SelectionUtil selUtil;
		Set<DirectedGraphNode> targetPlace = new HashSet<>();
		try {
			selUtil = new SelectionUtil(params.getContext(), params.getPetrinet());
			while (targetPlace.size() != 1) 
				targetPlace = selUtil.getChoice("Select the dependent choice place (one choice place!)", true);
			Place p = null;
			for (DirectedGraphNode node : targetPlace) {
				p = (Place)node;
			}
			params.setClassAttName("Choice_" + p.getLabel());
		} catch (Exception e) {
			System.out.println("target choice place selection problem!");
			e.printStackTrace();
		}
	}
	
	/**
	 * set the dependent attribute selection panel for TRACE situation
	 * @param dependent attribute panel
	 */
	private void setTraceDepAttPanel(JPanel panel) {
		panel.setLayout(new GridLayout(2,1));
		
		panel.add(depAttLabel);
		
		setDepAttCB(panel);
	}
	
	/**
	 * set the dependent attribute selection panel for EVENT situation
	 * @param dependent attribute panel
	 */
	private void setEventDepAttPanel(JPanel panel) {
		panel.setLayout(new GridLayout(4,1));
		
		// Attribute
		panel.add(depAttLabel);
		
		setDepAttCB(panel);
        
        // activity grouper
        panel.add(new JLabel("Select the activity grouper attribute name:"));
        
        setactGrouperCB(panel);
	
	}
	
	/**
	 * set the comboBox for selecting dependent attribute EVENT and TRACE situation
	 * @param dependent attribute panel
	 */
	private void setDepAttCB(JPanel panel) {
		attCB = new JComboBox<>();
        attCB.setPreferredSize(new Dimension(150, 30));
        attCB.setEnabled(true);
        
        if (situationType.equals(SituationType.TS))
        	for (String attribute : params.getAllAggAndNormalTraceAttNames()) {
        		if (!attribute.equals("Choice Attribute") && !attribute.equals("Sub Model Attribute"))
        			attCB.addItem(attribute); 
        		}
        else if (situationType.equals(SituationType.ES))
        	for (String attribute : params.getAllAggAndNormalEventAttNames()) 
        		attCB.addItem(attribute);
        
		attCB.setSelectedIndex(0);
		attCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				params.setClassAttName((String)attCB.getSelectedItem());
				if (attCB.getSelectedItem().equals("Trace Delay"))
					handelTraceDelay();
			}
			
		});
		
		panel.add(attCB);
	}
	
	private void handelTraceDelay() {
		JPanel p=new JPanel(new BorderLayout());
		SlickerFactory instance = SlickerFactory.instance();	
		NiceDoubleSlider traceDelayThresholdSlider = instance.createNiceDoubleSlider("Set the threshold for trace delay", 0, 1, 0.5, Orientation.HORIZONTAL);
    	p.add(traceDelayThresholdSlider,BorderLayout.CENTER);
    	p.add(new JLabel("Trace delay thereshold"),BorderLayout.NORTH);
    	int yn=JOptionPane.showConfirmDialog(null, 
    			p,"TRACE DELAY THRESHOLD",JOptionPane.YES_NO_OPTION);
    	if (yn==JOptionPane.NO_OPTION) {
    		params.setTraceDelayThreshold(0);
    		return;
    	}
    
    	params.setTraceDelayThreshold(traceDelayThresholdSlider.getValue());
	}
	
	/**
	 * set the comboBox for selecting dependent activity grouper (event situation)
	 * or dependent activity level (time interval situation)
	 * @param dependent attribute panel
	 */
	public void setactGrouperCB(JPanel panel) {
		actGrouperCB = new JComboBox<>();
		actGrouperCB.setPreferredSize(new Dimension(150, 30));
		
	    ActivityGrouperAttName[] groupingAtts = {ActivityGrouperAttName.AN, ActivityGrouperAttName.D, ActivityGrouperAttName.R, ActivityGrouperAttName.TS};
		Arrays.sort(groupingAtts);
		actGrouperCB.setModel( new DefaultComboBoxModel<ActivityGrouperAttName>(groupingAtts));
		actGrouperCBHandler dscbh = new actGrouperCBHandler();
		actGrouperCB.addActionListener(dscbh);
		
		
		actGrouperCB.setEnabled(true);
		
		panel.add(actGrouperCB);
	}
	
	/**
	 * Handles all the activity problem issues after selecting one ;)
	 * 
	 * When we use event situation, we need to use activity grouper 
	 * to select the set of events which have to be considered as 
	 * target events. After selecting the activity grouper attribute
	 * name (activity name, resource, timestamp, duration), we need 
	 * to select the attribute values of attribute selector attribute
	 * name. This class handle the value selection.
	 * @author qafari
	 *
	 */
	private class actGrouperCBHandler implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			showWhichActsPupup();	
		}

		private void showWhichActsPupup() {
						
			JPanel p = new JPanel( new GridLayout(5,1));
			String message = new String();
			ActivityGrouperAttName grouper = (ActivityGrouperAttName) actGrouperCB.getSelectedItem();
			params.setGrouperAttName(grouper);
			if (grouper.equals(ActivityGrouperAttName.R)) 
				message = "Please select the resource values.";
			else if (grouper.equals(ActivityGrouperAttName.D)) 
				message = "Please select the durstion values in ms.";
			else if (grouper.equals(ActivityGrouperAttName.TS)) 
				message = "Please select period of time.";
			else if (grouper.equals(ActivityGrouperAttName.AN)) 
				message = "Please select the activity names.";
			JLabel label = new JLabel(message);
			p.add(label);
			
			JTextField min = null;
			JTextField max = null;
			CheckComboBox cbb = null;
//			JLabel l = null;
			if (grouper.equals(ActivityGrouperAttName.R)) {
				if (params.getAllLiteralValues() != null && params.getAllLiteralValues().containsKey("org:resource")) {
					cbb = new CheckComboBox(params.getAllLiteralValues().get("org:resource"));
			    	Dimension dim=cbb.getPreferredSize();
			    	dim.width*=2;
			    	cbb.setPreferredSize(dim);
			    	p.add(cbb);
				}
			} else if (grouper.equals(ActivityGrouperAttName.TS)) {
				Set<String> days = new HashSet<String>();
				days.add("Sunday");
				days.add("Monday");
				days.add("Tuesday");
				days.add("Wednesday");
				days.add("Thursday");
				days.add("Friday");
				days.add("Saturday");
				
				cbb = new CheckComboBox(days);
				p.add(cbb);
				min = new JTextField("00:00");
				max = new JTextField("00:00");
				p.add(min);
				p.add(max);

			} else if (grouper.equals(ActivityGrouperAttName.D)) {
//				l = new JLabel("Min : " + params.getMinAllActDuration() + "Max : " + params.getMaxAllActDuration());
				min = new JTextField(params.getMinAllActDuration() + " ");
				max = new JTextField(params.getMaxAllActDuration() + " ");
				p.add(min);
				p.add(max);
			} else if (grouper.equals(ActivityGrouperAttName.AN)) {
				if (params.getAllActivityNames() != null ) {
					cbb = new CheckComboBox(params.getAllActivityNames());
			    	Dimension dim=cbb.getPreferredSize();
			    	dim.width*=2;
			    	cbb.setPreferredSize(dim);
			    	p.add(cbb);
				}
			}
			
	    	int yn=JOptionPane.showConfirmDialog(null, 
					p,message,JOptionPane.YES_NO_OPTION);
			if (yn==JOptionPane.NO_OPTION)
				return;
			
			if (params.getAllLiteralValues() != null && grouper.equals(ActivityGrouperAttName.R) && params.getAllLiteralValues().containsKey("org:resource")) {
				selectedValues  = cbb.getSelectedItems();
				params.setGrouperAttValues(selectedValues);
				}
			
			if (grouper.equals(ActivityGrouperAttName.AN)) {
				selectedValues  = cbb.getSelectedItems();
				params.setGrouperAttValues(selectedValues);
			}
			
			if (grouper.equals(ActivityGrouperAttName.TS)) {
				selectedValues  = cbb.getSelectedItems();
				params.setGrouperAttValues(selectedValues);
				params.setMinThreshold(min.getText());
				params.setMaxThreshold(max.getText());
				minThreshold = min.getText();
				maxThreshold = max.getText();
			}
			
			if (grouper.equals(ActivityGrouperAttName.D)) {
				params.setMinThreshold(min.getText());
				params.setMaxThreshold(max.getText());
				minThreshold = min.getText();
				maxThreshold = max.getText();
			}
		}
	}
}
