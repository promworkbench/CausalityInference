package org.processmining.causalityinference.algorithms;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.ui.RightPanelProcessLevel;
import org.processmining.causalityinference.ui.uiComponents.FeatureRecommendationVisualizer;
import org.processmining.causalityinference.ui.uiComponents.RightPanelFeatureRecommendation;
import org.processmining.framework.plugin.PluginContext;

public class LeftPanelMain extends JTabbedPane {
	PluginContext context;
	CausalityGraph model;
	Parameters params;

	RightPanelCausalityGraph rightPanelCausalityGraph;
	public RightPanelFeatureRecommendation rightPanelFeatureRecommendation;
	RightPanelProcessLevel rightPanelProcessLevel;
	
	ModelVisualizerStarStar bayesianNetworkVisualizer;
	public FeatureRecommendationVisualizer featureRecommendationVisualizer;
	TableVisualizer tableVisualizer;
	
	Map<String, JPanel> addedJPanels;
	boolean limitedGraph = true;
	
	public LeftPanelMain(PluginContext context, CausalityGraph model, Parameters params) {
		this.context = context;
		this.model = model;
		this.params = params;
		
		/*RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);*/
		
		this.addedJPanels = new HashMap<String, JPanel>();
		
		this.bayesianNetworkVisualizer = new ModelVisualizerStarStar(context, model, this);
		this.featureRecommendationVisualizer = new FeatureRecommendationVisualizer(context, this);
		this.tableVisualizer =  new TableVisualizer(context, this);
		
//		this.addJPanel("** Data", this.visualizer);
		this.addJPanel("** Data Table", this.tableVisualizer);
		this.addJPanel("** Feature Recommendation", this.featureRecommendationVisualizer);
		this.addJPanel("** causality Graph", this.bayesianNetworkVisualizer);
		
		
		//this.add(this.visualizer, new Float(100));
	}

	public void setAwareProcessLevel(RightPanelProcessLevel rightPanelProcessLevel) {
		this.rightPanelProcessLevel = rightPanelProcessLevel;
	}
	
	public void setAwareCausalityGraph(RightPanelCausalityGraph rightPanelCausalityGraph) {
		this.rightPanelCausalityGraph =rightPanelCausalityGraph;
	}
	
	public void setAwareFeatureRecommendation(RightPanelFeatureRecommendation rightPanelFeatureRecommendation) {
		this.rightPanelFeatureRecommendation = rightPanelFeatureRecommendation;
		
	}
	
	public Map<String, JPanel> getAddedJPanels() {
		return this.addedJPanels;
	}
	
	public void addJPanel(String label, JPanel panel) {
		this.addedJPanels.put(label, panel);
		this.add(label, panel);
	}
	
	public void destroyDataTableTab() {
		this.remove(this.tableVisualizer);
		this.tableVisualizer = null;
		this.updateUI();
	}
	
	
	public TableVisualizer getTableVisualizer() {
		return tableVisualizer;
	}
//	public void destroyKnowledgeTab() {
//		this.remove(this.knowledgeVisualizer);
//		this.knowledgeVisualizer = null;
//		this.updateUI();
//	}

	public ModelVisualizerStarStar getBayesianNetworkVisualizer() {
		return bayesianNetworkVisualizer;
	}
}
