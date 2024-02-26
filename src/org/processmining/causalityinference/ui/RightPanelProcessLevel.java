package org.processmining.causalityinference.ui;

import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.processmining.causalityinference.algorithms.CausalityGraph;
import org.processmining.causalityinference.algorithms.MainView;
import org.processmining.causalityinference.dialogs.RelativeLayout;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.causalityinference.ui.uiComponents.CreateAndSaveButton;
import org.processmining.causalityinference.ui.uiComponents.IndependentAtt;
import org.processmining.causalityinference.ui.uiComponents.TimeSettingPanel;
import org.processmining.dataTable.AggregatedDataExtraction;
import org.processmining.framework.plugin.PluginContext;

public class RightPanelProcessLevel extends JPanel {
	private Parameters params;
	
	private AggregatedDataExtraction tabularDataCreator;
	
	PluginContext context;
	
	MainView mainView;
	
	JTextField windowNow;
	
	JTextField windowPast;
	
	JTextField timeSpan;
	
	JRadioButton traceSituation;
	JRadioButton eventSituation;
	JRadioButton resourceSituation;
	
	JList<String> traceAttList;
	Set<String> traceAtts;
	
	JList<String> eventAttList;
	Set<String> eventAtts;
	
	JList<String> resourceAttList;
	Set<String> resourceAtts;
	
	JList<String> activityList;
	Set<String> activitiesToConscider;
	
	JList<String> resourceList;
	Set<String> reourcesToConsider;
	
	public RightPanelProcessLevel(PluginContext context, CausalityGraph model, Parameters params,
			MainView mainView) {
		this.context = context;
		this.params = params;
		this.mainView = mainView;
		params.setSituationType(SituationType.PL);
		
		createPanel();
	}
	
	public void createPanel() {
		setLayout(new RelativeLayout(RelativeLayout.Y_AXIS));
		IndependentAtt indepAttPanel = new IndependentAtt(params);
		indepAttPanel.setIndepAttPanelprecesslevel();
		add(indepAttPanel, new Float(60));
		
		// Trace attribute list
		JPanel p = new JPanel();
		p.setLayout(new RelativeLayout(RelativeLayout.Y_AXIS));
		
		
		TimeSettingPanel timePanel = new TimeSettingPanel(params);
		p.add(timePanel.getTimeLinePanel(), new Float(60));
		
		CreateAndSaveButton save = new CreateAndSaveButton(params, SituationType.PL, mainView);
		save.setAware(indepAttPanel, timePanel);
		p.add(save, new Float(40));
		
		add(p, new Float(40));
	}
}
