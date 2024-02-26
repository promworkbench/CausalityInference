package org.processmining.causalityinference.algorithms;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.processmining.causalityinference.dialogs.RelativeLayout;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.causalityinference.ui.RightPanelProcessLevel;
import org.processmining.causalityinference.ui.uiComponents.RightPanelFeatureRecommendation;
import org.processmining.causalityinference.ui.uiComponents.RightPanelTableSetting;
import org.processmining.framework.plugin.PluginContext;

public class MainView extends JPanel {
	PluginContext context;
	CausalityGraph model;
	Parameters params;
	
	LeftPanelMain leftPanel;
	
//	RightPanelTable rightPanelCreatTable;
	RightPanelProcessLevel rightPanelProcessLevel;
	RightPanelFeatureRecommendation rightPanelFeatureRecommendation;
	RightPanelCausalityGraph rightPanelCausalityGraph;
	
	RightPanelTableSetting rightPanelTraceSituation;
	RightPanelTableSetting rightPanelEventSituation;
	RightPanelTableSetting rightPanelChoiceSituation;
	RightPanelTableSetting rightPanelTimeIntervalSituation;
	
	JTabbedPane rightPanelTabbedPane;
	
//	GraphMouseListener graphMouseListener;
	
	public MainView(PluginContext context, CausalityGraph model) {
		this.context = context;
		this.model = model;
		
		if (model.getReplayResult() != null) {
			params = new Parameters(model.getLog(), model.getPetrinet(), model.getReplayResult());
			params.setContext(context);
			params.init();
		} else {
			params = new Parameters(model.getLog());
			params.init();
		}
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		this.leftPanel = new LeftPanelMain(context, model, params);		
		
//		this.rightPanelCreatTable = new RightPanelTable(context, model, params);
//		this.rightPanelProcessLevel = new RightPanelProcessLevel(context, model, params , this);
		this.rightPanelCausalityGraph = new RightPanelCausalityGraph(context, model, this.leftPanel);
		this.rightPanelFeatureRecommendation = new RightPanelFeatureRecommendation(context, this.leftPanel, params);
		this.rightPanelTraceSituation = new RightPanelTableSetting(context, params, SituationType.TS, this);
		this.rightPanelEventSituation = new RightPanelTableSetting(context, params, SituationType.ES, this);
		this.rightPanelChoiceSituation = new RightPanelTableSetting(context, params, SituationType.CS, this);
		this.rightPanelTimeIntervalSituation = new RightPanelTableSetting(context, params, SituationType.PL, this);

		this.rightPanelTabbedPane = new JTabbedPane();
//		this.rightPanelTabbedPane.add("Table setting", this.rightPanelCreatTable);
		this.rightPanelTabbedPane.add("Trace situation", this.rightPanelTraceSituation);
		this.rightPanelTabbedPane.add("Event situation", this.rightPanelEventSituation);
		this.rightPanelTabbedPane.add("Choice situation", this.rightPanelChoiceSituation);
		this.rightPanelTabbedPane.add("Time interval situaiton", this.rightPanelTimeIntervalSituation);
		this.rightPanelTabbedPane.add("Feature recommendation", this.rightPanelFeatureRecommendation);
		this.rightPanelTabbedPane.add("Causality graph setting", this.rightPanelCausalityGraph);
//		rightPanelTabbedPane.setEnabledAt(1, false);
		
		this.model.setAwareParams(params);
		
		this.add(this.leftPanel, new Float(70));
		this.add(this.rightPanelTabbedPane, new Float(30));
		
		this.leftPanel.setAwareCausalityGraph(this.rightPanelCausalityGraph);
		this.leftPanel.setAwareFeatureRecommendation(this.rightPanelFeatureRecommendation);
//-		this.leftPanel.setAwareProcessLevel(this.rightPanelProcessLevel);
	}
	
	public LeftPanelMain getLeftPanel() {
		return leftPanel;
	}
	
	public RightPanelFeatureRecommendation getRightPanelFeatureRecommendation() {
		return rightPanelFeatureRecommendation;
	}
	
	public  RightPanelCausalityGraph getRightPanelCausalityGraph() {
		return rightPanelCausalityGraph;
	}
	
	public RightPanelTableSetting getRightPanelTraceSituation() {
		return rightPanelTraceSituation;
	}
}



