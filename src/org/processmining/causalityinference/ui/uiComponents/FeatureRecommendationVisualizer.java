package org.processmining.causalityinference.ui.uiComponents;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.causalityinference.algorithms.LeftPanelMain;
import org.processmining.framework.plugin.PluginContext;

public class FeatureRecommendationVisualizer extends JPanel{
	PluginContext context;
	
	JScrollPane scrollPane;
	LeftPanelMain leftPanel;
	RightPanelFeatureRecommendation rightPanelFR;
	
	public FeatureRecommendationVisualizer(PluginContext context, LeftPanelMain leftPanel) {
		this.context = context;
		this.leftPanel =leftPanel;
		
		this.setLayout(new GridLayout());
	}
	
	public void addTablePanelToView(JScrollPane scrollPane) {
//		leftPanel.knowledgeVisualizer.removeAll();

		leftPanel.featureRecommendationVisualizer.removeAll();
		leftPanel.featureRecommendationVisualizer.add(scrollPane);
		leftPanel.featureRecommendationVisualizer.updateUI();
		this.updateUI();
	}
	
	public void addTablePanelToView(JPanel panel) {
//		leftPanel.knowledgeVisualizer.removeAll();

		leftPanel.featureRecommendationVisualizer.removeAll();
		leftPanel.featureRecommendationVisualizer.add(panel);
		leftPanel.featureRecommendationVisualizer.updateUI();
		this.updateUI();
	}
	
	public void setAware(RightPanelFeatureRecommendation rightPanel) {
		this.rightPanelFR = rightPanel;
	}
}
