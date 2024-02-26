package org.processmining.causalityinference.algorithms;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;

public class ModelVisualizerStarStar extends JPanel {
	PluginContext context;
	CausalityGraph model;
	mxGraph graph;
	mxGraphComponent graphComponent;
	boolean isKnowledgeGraph = false;
	Map<String, Object> nodesInTheGraph;
	Map<String, Object> edgesInTheGraph;
	
	JScrollPane scrollPane;
	GraphVisualizer graphVisualizer;
	LeftPanelMain leftPanel;
//	RightPanelKnowledge rightPanelKnowledge;
//	GraphMouseListener graphMouseListener;
	
	public ModelVisualizerStarStar(PluginContext context, CausalityGraph model, LeftPanelMain leftPanel) {
		this.context = context;
		this.model = model;
		this.graphVisualizer = new GraphVisualizer(context , leftPanel);
		this.leftPanel =leftPanel;
		
		this.nodesInTheGraph = new HashMap<String, Object>();
		this.edgesInTheGraph = new HashMap<String, Object>();
		
		this.setLayout(new GridLayout());
	}
	
	public void setIsKnowledgeGraph(boolean b) {
		this.isKnowledgeGraph = b;
		graphVisualizer.setIsKnowledgeGraph(b);
	}
	
	
//	public void setAwareKnowledge(RightPanelKnowledge rightPanelKnowledge) {
//		this.rightPanelKnowledge = rightPanelKnowledge;
		
//		graphVisualizer.doRepresentationWork();
//		graphVisualizer.addGraphToView();
//	}
	
	public void doRepresentationWork() {
		graphVisualizer.beginGraph();
		graphVisualizer.drawGraph();
	//	doLayouting();
	}
	
	public void setGraph(Graph g) {
		LinkedList<String> nodes = new LinkedList<String>();
		for (String nodeName: g.getNodeNames())
			nodes.add(nodeName);
		setNodes(nodes);
		
		Set<String> edges = new HashSet<String>();
		for (Edge edge : g.getEdges()) 
			edges.add(edge.toString());
		setEdges(edges);
	}
	
	public void addGraphToView() {
		graphVisualizer.addGraphToView();
	}
	
	public void setNodes(LinkedList<String> nodes) {
		graphVisualizer.setNodes(nodes);
	}
	
	public void setEdges(Set<String>edges) {
		graphVisualizer.setEdges(edges);
	}
	
	public void setGraph(boolean isDiscrete) {
		graphVisualizer.setNodes(model.getNodes());
		graphVisualizer.setEdges(model.getEdges());
		graphVisualizer.setClassAttName(model.getClassAttName());
		if (isDiscrete) {
			graphVisualizer.setTables(model.getTables());
			graphVisualizer.setHeaders(model.getHeaders());
		} else 
			graphVisualizer.setCoefficients(model.getEdgeCoefficients());
		
	}
	
	public void setEdgeCoefficients(Map<String, Double> edgeCoefficients) {
		// TODO Auto-generated method stub
		
	}

	public GraphVisualizer getGraphVisualizer() {
		return graphVisualizer;
	}
}


