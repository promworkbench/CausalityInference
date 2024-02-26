package org.processmining.causalityinference.algorithms;

import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;

public class TableVisualizer extends JPanel {
	PluginContext context;
	Map<String, Object> nodesInTheGraph;
	Map<String, Object> edgesInTheGraph;
	
	JScrollPane scrollPane;
	LeftPanelMain leftPanel;
//	RightPanelKnowledge rightPanelKnowledge;
//	GraphMouseListener graphMouseListener;
	
	public TableVisualizer(PluginContext context, LeftPanelMain leftPanel) {
		this.context = context;
		this.leftPanel =leftPanel;
		
//		this.nodesInTheGraph = new HashMap<String, Object>();
//		this.edgesInTheGraph = new HashMap<String, Object>();
		
		this.setLayout(new GridLayout());
	}
	
	public void addTablePanelToView(JScrollPane scrollPane) {
//		leftPanel.knowledgeVisualizer.removeAll();

		leftPanel.tableVisualizer.removeAll();
		leftPanel.tableVisualizer.add(scrollPane);
		leftPanel.tableVisualizer.updateUI();
		this.updateUI();
	}
}