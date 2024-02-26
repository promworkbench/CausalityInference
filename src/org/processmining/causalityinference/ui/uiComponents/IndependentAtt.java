package org.processmining.causalityinference.ui.uiComponents;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.processmining.causalityinference.algorithms.SelectionUtil;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class IndependentAtt extends JPanel{
	private Parameters params;
	private SituationType situationType;
	
	JList aggIndepTraceAtts;
	JList normalIndepTraceAtts;
	JList activtiesToConsider;
    JList aggIndepEventAtts;
    JList normalIndepEventAtts;
    JCheckBox submodelSelection;
    JCheckBox choiceSelection;
    
    // ------ Process level ---------
    JList resourceList;
    JList resourceAttList;
    JList activityList;
    JList eventAttList;
    JList traceAttList;
    
	public IndependentAtt(Parameters params) {
		this.params = params;
		this.situationType = params.getSituationType();
	}
	
	public void setIndepAttPanel() {

		setLayout(new GridLayout(12,1));
		//setBorder(BorderFactory.createEtchedBorder());
		
		add(new JLabel("----------- Independent attributes -----------------"));
		
		JPanel p = new JPanel();
		choiceSelection = new JCheckBox("Choice attributes");
		choiceSelection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (choiceSelection.isSelected())
					handelChoiceAtts();	
				else {
					params.setIndepChoice(false);
					params.setSelectedORplaces(null);
				}
			}		
		});
		p.add(choiceSelection);
		
		submodelSelection = new JCheckBox("Submodel attribute");
		submodelSelection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (submodelSelection.isSelected())
					handelSubModel();
				else
					params.setIndepSubmodel(false);
			}		
		});
		p.add(submodelSelection);
		
		add(p);
		
		
       // trace aggregated atts
       JLabel aggTraceAttLabel = new JLabel("Select relevant aggregated trace attributes:");
       add(aggTraceAttLabel);
       aggIndepTraceAtts = new JList(turn2Array(params.getTraceAggAttNames()));
       aggIndepTraceAtts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       aggIndepTraceAtts.setVisibleRowCount(3);
       JScrollPane listScroller0 = new JScrollPane(aggIndepTraceAtts);
       add(listScroller0);
       
       // trace normal atts
       if (params.getAllTraceAttNames() != null) {
       	JLabel traceAttLabel = new JLabel("Select relevant trace or choice attributes:");
       	add(traceAttLabel);
       	normalIndepTraceAtts = new JList(getTraceNormalAttNames());
       	normalIndepTraceAtts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       	normalIndepTraceAtts.setVisibleRowCount(3);
        JScrollPane listScroller1 = new JScrollPane(normalIndepTraceAtts);
   		add(listScroller1);
       }
       
       // independent event atts  -->
       add(new JLabel("Select relevant activities:"));
       activtiesToConsider = new JList(turn2Array(params.getAllActivityNames()));
       activtiesToConsider.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       activtiesToConsider.setVisibleRowCount(3);
       JScrollPane listScroller2 = new JScrollPane(activtiesToConsider);
       add(listScroller2);
       
       // event aggregated atts
       add(new JLabel("Select relevant aggregated event attributes:"));
       aggIndepEventAtts = new JList(turn2Array(params.getAggAttNames()));
       aggIndepEventAtts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       aggIndepEventAtts.setVisibleRowCount(3);
       JScrollPane listScroller3 = new JScrollPane(aggIndepEventAtts);
       add(listScroller3);
   	
       // event normal atts
       add(new JLabel("Select relevant event attributes:"));
       normalIndepEventAtts = new JList(turn2Array(params.getAllEventAttNames()));
       normalIndepEventAtts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       normalIndepEventAtts.setVisibleRowCount(3);
       JScrollPane listScroller4 = new JScrollPane(normalIndepEventAtts);
       add(listScroller4);
	}
	
	protected void handelChoiceAtts() {
		SelectionUtil selUtil;
		Set<DirectedGraphNode> places = null;
		try {
			selUtil = new SelectionUtil(params.getContext(), params.getPetrinet());
			places = selUtil.getChoice("Select the set of independent choice places", true);
			if (places != null) {
				Set<Place> set = new HashSet<>();
				for (DirectedGraphNode place : places)
					set.add((Place) place);
				params.setSelectedORplaces(set);
				params.setIndepChoice(true);
			}
		} catch (Exception e) {
			System.out.println("target choice place selection problem!");
			e.printStackTrace();
		}
		
	}

	private String[] getTraceNormalAttNames() {
		
		if (params.getClassAttName() == null)
			return turn2Array(params.getAllTraceAttNames());
		
		if (!params.getClassAttName().equals("Trace Delay"))
			return turn2Array(params.getAllTraceAttNames());

		String[] items = new String[params.getAllTraceAttNames().size()-1];
		int i = 0;
		for (String item : params.getAllTraceAttNames()) {
			if (!item.equals("Trace Delay")) {
				items[i] = item;
				i++;
			}
		}
		
		return items;
	}
	
	public String[] turn2Array(ArrayList<String> set) {
		String[] array = new String[set.size()];
		int i = 0;
		for (String actname : set) {
			array[i] = actname;
			i++;
		}
		
		return array;
	}
	
	public void setIndepAttsInParam() {
		if (situationType.equals(SituationType.PL)) {  
		    // trace agg atts
			if (!traceAttList.getSelectedValuesList().isEmpty()) 
				params.setTraceAttsPL(traceAttList.getSelectedValuesList());
			else 
				params.setTraceAttsPL(null);
			
			// activity names
			if (!activityList.getSelectedValuesList().isEmpty()) 
				params.setActivitiesToConsciderPL(activityList.getSelectedValuesList());
			else 
				params.setActivitiesToConsciderPL(null);
			
			// event agg atts
			if (!eventAttList.getSelectedValuesList().isEmpty()) 
				params.setEventAttsPL(eventAttList.getSelectedValuesList());
			else 
				params.setEventAttsPL(null);
			
			// resource names
			if (!resourceList.getSelectedValuesList().isEmpty()) 
				params.setReourcesToConsiderPL(resourceList.getSelectedValuesList());
			else 
				params.setReourcesToConsiderPL(null);
			
			// resource agg atts
			if (!resourceAttList.getSelectedValuesList().isEmpty()) 
				params.setResourceAttsPL(resourceAttList.getSelectedValuesList());
			else 
				params.setResourceAttsPL(null);
			
		} else {
			if (!aggIndepTraceAtts.getSelectedValuesList().isEmpty()) 
				params.setSelectedAggTraceAttNames(aggIndepTraceAtts.getSelectedValuesList());
			else 
				params.setSelectedAggTraceAttNames(null);

			// ---- normal trace
			if (!normalIndepTraceAtts.getSelectedValuesList().isEmpty()) {
				params.setSelectedNormalTraceAttNames(normalIndepTraceAtts.getSelectedValuesList());
				handelTraceAttributes();
			} else {
				params.setSelectedNormalTraceAttNames(null);
			}
				
			// ---- activities to consider
			if (!activtiesToConsider.getSelectedValuesList().isEmpty())
				params.setActivitiesToConsider(activtiesToConsider.getSelectedValuesList());
			else 
				params.setActivitiesToConsider(null);

			// ---- aggregated event
			if (!aggIndepEventAtts.getSelectedValuesList().isEmpty()) 
				params.setSelectedAggEventAttNames(aggIndepEventAtts.getSelectedValuesList());
			else 
				params.setSelectedAggEventAttNames(null);
				
			// ---- normal event
			if (!normalIndepEventAtts.getSelectedValuesList().isEmpty()) 
				params.setSelectedNormalEventAttNames(normalIndepEventAtts.getSelectedValuesList());
			else 
				params.setSelectedNormalEventAttNames(null);
		}
	}

	private void handelTraceAttributes() {
		if (doesInclude(normalIndepTraceAtts.getSelectedValuesList(), "Sub Model Attribute"))
			handelSubModel();
		if (doesInclude(normalIndepTraceAtts.getSelectedValuesList(), "Trace Delay"))
			handelTraceDelay();
	}
	
	private boolean doesInclude(Collection<String> collection, String value) {
		for (Object element : collection) {
		    if (element.equals(value)) {
		        return true;
		    }
		}
		
		return false;
	}
	
	private void handelSubModel() {
		try {
			SelectionUtil selUtil = new SelectionUtil(params.getContext(), params.getPetrinet());
			Set<DirectedGraphNode> selection = selUtil.getChoice("Select a Set of Transitions", false);
			Set<Transition> set = new HashSet<Transition>();
			for (DirectedGraphNode t : selection)
				set.add((Transition) t);
			
			params.setSelectetSub_model(set);
			params.setIndepSubmodel(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	//---------------------Process level------------------
	
	public void setIndepAttPanelprecesslevel() {
		setLayout(new GridLayout(12,1));
		
		JLabel l1 = new JLabel("Relevant trace attributes:");
		add(l1);
		String[] attTrace = {"Average service time Trace", "Average waiting time Trace",
					"Process workLoad", "Process number waiting"};
		traceAttList = new JList(attTrace);
		traceAttList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		traceAttList.setVisibleRowCount(3);
        JScrollPane ls1 = new JScrollPane(traceAttList);
		add(ls1);
		
		// Event attribute list
		JLabel l2 = new JLabel("Relevant event attributes:");
		add(l2);
		String[] attEvent = {"Average service time Event", "Number of active Event", "Number of waiting Event"};
		eventAttList = new JList(attEvent);
		eventAttList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		eventAttList.setVisibleRowCount(3);
	    JScrollPane ls2 = new JScrollPane(eventAttList);
		add(ls2);	

		// Activity name list
		JLabel l3 = new JLabel("Activities to consider:");
		add(l3);
		activityList = new JList(toArray(params.getAllActivityNames()));
		activityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		activityList.setVisibleRowCount(3);
	    JScrollPane ls3 = new JScrollPane(activityList);
		add(ls3);
		
		String resourceAttName = null;
		if (params.getAllLiteralValues().containsKey("org:resource")) 
			resourceAttName = "org:resource";
		if (params.getAllLiteralValues().containsKey("resource")) 
			resourceAttName = "resource";
		
		if (resourceAttName != null) {
			// Resource attribute list
			JLabel l4 = new JLabel("Relevant resource attributes:");
			add(l4);
			String[] attResource = {"Average service time Resource", "WorkLoad Resource"};
			resourceAttList = new JList(attResource);
			resourceAttList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			resourceAttList.setVisibleRowCount(3);
		    JScrollPane ls4 = new JScrollPane(resourceAttList);
			add(ls4);	
			
			// Resource name list
			JLabel l5 = new JLabel("Resources to consider:");
			add(l5);
			resourceList = new JList(toArray(params.getAllLiteralValues().get(resourceAttName)));
			resourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			resourceList.setVisibleRowCount(3);
		    JScrollPane ls5 = new JScrollPane(resourceList);
			add(ls5);
		}
	}
	
	private String[] toArray(ArrayList<String> list) {
		String[] array = new String[list.size()];
		int i = 0;
		for (String name : list) {
			array[i] = name;
			i++;
		}
		
		return array;
	}
	
	private String[] toArray(Set<String> set) {
		String[] array = new String[set.size()];
		int i = 0;
		for (String name : set) {
			array[i] = name;
			i++;
		}
		
		return array;
	}
}
