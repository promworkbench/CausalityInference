package org.processmining.causalityinference.algorithms;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ChoiceSubModleHandler implements ListSelectionListener {
	String[] items;
	Set<String> selectedItems;

	NiceDoubleSlider traceDelayThresholdSlider;
	boolean isChoiceHandeled = false;
	boolean isSubModelHandeled = false;
	boolean isTraceDelayHandeled = false;
	
	Parameters params;

	public ChoiceSubModleHandler(Parameters params, String[] items) {
		this.items = items;
		this.params = params;
	}

	public void valueChanged(ListSelectionEvent e) {
		JList list = (JList)e.getSource();
	    int selected = list.getSelectedIndex();
//	    int previous = selected == e.getFirstIndex() ? e.getLastIndex() : e.getFirstIndex();

	    Collection<String> newSelectedItems = list.getSelectedValuesList();
	    String attName = null;
	    attName = getNewSelectedAttName(newSelectedItems);
	    
	    selectedItems = new HashSet<String>();
	    for (String item : newSelectedItems)
	    	selectedItems.add(item);
	    
	    if (attName != null) {
	    	if (attName.equals("Choice Attribute") && !isChoiceHandeled) 
	    		handelChoice();
	    	else if (attName.equals("Sub Model Attribute") && !isSubModelHandeled)
	    		handelSubModel();
	    	else if (attName.equals("Trace Delay") && !isTraceDelayHandeled)
	    		handelTraceDelay();
	    } 
	    handleFlags();
	}
	
	/**
	 * Handle flags that show the validity of the setting 
	 * for the choice, sub Model, and trace delay attributes.
	 */
	private void handleFlags() {
		if (!selectedItems.contains("Choice Attribute")) {
	    		isChoiceHandeled = false;
	    		params.setSelectedORplaces(null);
	    }
		if (!selectedItems.contains("Sub Model Attribute")) {
    		isSubModelHandeled = false;
    		params.setSelectetSub_model(null);;
    	}
		if (!selectedItems.contains("Trace Delay")) {
    		isTraceDelayHandeled = false;
    		params.setTraceDelayThreshold(0);;
    	}		
	}

	/**
	 * @param newSelectedItems
	 * @return the new selected item if exists o.w. null
	 */
	private String getNewSelectedAttName(Collection<String> newSelectedItems) {
		if (selectedItems == null)
	    	return getNewItem(newSelectedItems);
	    else if (selectedItems.size() < newSelectedItems.size())
	    	return getNewItem(newSelectedItems, selectedItems);
		
		return null;
	}

	private void handelChoice() {
		try {
			SelectionUtil selUtil = new SelectionUtil(params.getContext(), params.getModel());
			Set<DirectedGraphNode> selection = selUtil.getChoice("Select a Set of Choice Places", true);
			Set<Place> set = new HashSet<Place>();
			for (DirectedGraphNode node : selection) {
				set.add((Place)node);
			}
			params.setSelectedORplaces(set);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.isChoiceHandeled = true;
	}
	
	private void handelSubModel() {
		try {
			SelectionUtil selUtil = new SelectionUtil(params.getContext(), params.getModel());
			Set<DirectedGraphNode> selection = selUtil.getChoice("Select a Set of Transitions", false);
			Set<Transition> set = new HashSet<Transition>();
			for (DirectedGraphNode transition : selection) {
				set.add((Transition)transition);
			}
			params.setSelectetSub_model(set);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.isSubModelHandeled = true;
	}
	
	private void handelTraceDelay() {
		JPanel p=new JPanel(new BorderLayout());
		SlickerFactory instance = SlickerFactory.instance();	
		traceDelayThresholdSlider = instance.createNiceDoubleSlider("Set the threshold for trace delay", 0, 1, 0.5, Orientation.HORIZONTAL);
    	p.add(traceDelayThresholdSlider,BorderLayout.CENTER);
    	p.add(new JLabel("Trace delay threshold"),BorderLayout.NORTH);
    	int yn=JOptionPane.showConfirmDialog(null, 
    			p,"TRACE DELAY THRESHOLD",JOptionPane.YES_NO_OPTION);
    	if (yn==JOptionPane.NO_OPTION) {
    		this.isTraceDelayHandeled = false;
    		return;
    	}
    
    	params.setTraceDelayThreshold(traceDelayThresholdSlider.getValue());
    	this.isTraceDelayHandeled = true;
	}
	
	/**
	 * retuurn and print the only item that exist in itemNew but not in itemOld
	 * @param items
	 */
	private String getNewItem(Collection<String> itemsNew, Collection<String> itemsOld) {
		for (String item : itemsNew)
			if (!itemsOld.contains(item)) {
				System.out.println("new selected trace normal att " + item);
				return item;
			}
		return null;
	}

	/**
	 * return and print the only item selected in the JList
	 * @param items
	 */
	private String getNewItem(Collection<String> items) {
		for (String item : items) {
			 System.out.println("new selected trace normal att " + item);
			 return item;
		}
		return null;
	}
}
