package org.processmining.causalityinference.limitGraphDeapth;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.causalityinference.algorithms.GraphVisualizer;
import org.processmining.dataTable.AggregatedDataExtraction;

import com.mxgraph.view.mxGraph;

public class LeftPanel extends JPanel {
	
	Set<String> nodeNames;
	Set<String> edgeNames;
	JScrollPane scrollPane;
	GraphVisualizer graphVisualizer;
	RightPanel rightPanel;
	String classAttName;
	AggregatedDataExtraction tabularDataCreator;

	// ---------- causal related values -------------
	Map<String, Double> coefficients;
	
	public LeftPanel(Set<String> nodeNames, Set<String> edgeNames, String classAttName) {

		this.nodeNames = nodeNames;
		this.edgeNames = edgeNames;
		this.classAttName = classAttName;
	}
	
	public void setAware(RightPanel rightPanel) {
		this.rightPanel = rightPanel;
	}
	
	public void setAwareTable(AggregatedDataExtraction tabularDataCreator) {
		this.tabularDataCreator = tabularDataCreator;
	}
	 
	public LeftPanel(Set<String> nodeNames, Set<String> edgeNames) {
		this.nodeNames = nodeNames;
		this.edgeNames = edgeNames;
		drawGraph();
	}
	
	public void drawGraph() {	
		graphVisualizer = new GraphVisualizer(tabularDataCreator);
		graphVisualizer.setNodes(nodeNames);
		graphVisualizer.setEdges(edgeNames);
		graphVisualizer.beginGraph();
		graphVisualizer.setClassAttName(classAttName);
		graphVisualizer.drawSearchGraph();
		graphVisualizer.addGraphToView(true, this, rightPanel, classAttName);
		removeAll();
		add(graphVisualizer.getScrollPane());
		updateUI();
	}
	
	public void reDraw() {
		removeAll();
		add(graphVisualizer.getScrollPane());
		updateUI();
	}
	
	public mxGraph getGraph() {
		return graphVisualizer.getGraph();
	} 
	
	public void setedgeCoefficients(Map<String, Double> edgeCoefficients) {
		graphVisualizer = new GraphVisualizer(tabularDataCreator);
		graphVisualizer.setNodes(nodeNames);
		graphVisualizer.setEdges(edgeNames);
		graphVisualizer.setClassAttName(classAttName);
		graphVisualizer.setEdgeCoefficients(edgeCoefficients);
		graphVisualizer.beginGraph();
		graphVisualizer.drawSearchGraph();
		graphVisualizer.addGraphToView(true, this, rightPanel, classAttName);
		removeAll();
		add(graphVisualizer.getScrollPane());
		updateUI();
	}
		
	/**
	 * computes the location of every node in the left panel.
	 * @param n : number of vertices in the graph
	 * @return an n by 2 array including the location of each node in a regular n-gon
	 */
	public int[][] nodesLocation(int n) {
		
		int[][] locations = new int[n][2];
		int height = this.getSize().height;
		int width = this.getSize().width;
	
		int raddi = 200;
		double theta = 2 * Math.PI / n;
		for (int i = 0; i < n; ++i) {
			locations[i][0] = (int) (Math.round(raddi * Math.cos(theta * i)) + 200);
			locations[i][1] = (int) (Math.round(raddi * Math.sin(theta * i)) + 200);
		}
		
		return locations;
	}
	
	public void addNode(String nodeName) {
		if (nodeNames == null)
			nodeNames = new HashSet<>();
		
		nodeNames.add(nodeName);
		
		drawGraph();
	}
	
	public void removeNode(String nodeName) {
		if (nodeNames.contains(nodeName))
			nodeNames.remove(nodeName);
		
		drawGraph();
	}
	
	public void addEdge(String edgeName) {
		if (edgeNames == null)
			edgeNames = new HashSet<>();
		
		String parts1[] = getEndPoints(edgeName);
		for (String edge : edgeNames) {
			String parts2[] = getEndPoints(edge);
			if (parts1[0].equals(parts2[0]) && parts1[1].equals(parts2[1]))
				removeEdge(edge);
			if (parts1[1].equals(parts2[0]) && parts1[0].equals(parts2[1]))
				removeEdge(edge);
		}
		edgeNames.add(edgeName);
		
		drawGraph();
	}
	
	public String[] getEndPoints(String edgeName) {
		
		if (edgeName.contains(" --> "))
			return edgeName.split(" --> ");
		if (edgeName.contains(" <-> "))
			return edgeName.split(" <-> ");
		if (edgeName.contains(" o-> "))
			return edgeName.split(" o-> ");
		if (edgeName.contains(" o-o "))
			return edgeName.split(" o-o ");
		
		return null;
	}
	
	public void removeEdge(String edgeName) {
		if (edgeNames.contains(edgeName))
			edgeNames.remove(edgeName);
		
		drawGraph();
	}
	
	public void setNodeNames(Set<String> names) {
		nodeNames = names;
	}
	
	public void setEdgeNames(Set<String> names) {
		edgeNames = names;
	}
	//------------------------ Test code -----------------------
	public LeftPanel() {
		
	}
	
	public static void main(String[] arg) {
		LeftPanel p = new LeftPanel();
		Set<String> nodes = new HashSet<>();
		nodes.add("A");
		nodes.add("B");
		nodes.add("C");
		nodes.add("D");
		p.setNodeNames(nodes);
		Set<String> edges = new HashSet<>();
		edges.add("A --> B");
		edges.add("A --> C");
		edges.add("C --> D");
		p.setEdgeNames(edges);
		
		p.drawGraph();
		
		//1. Create the frame.
		JFrame frame = new JFrame("FrameDemo");

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//3. Create components and put them in the frame.
		//...create emptyLabel...
		frame.getContentPane().add(p);

		//4. Size the frame.
		frame.setSize(600, 600);
		
		frame.add(p);

		//5. Show it.
		frame.setVisible(true);
		
/**		LinkedList<String> nodesList = new LinkedList<>();
		nodesList.add("A");
		nodesList.add("B");
		nodesList.add("C");
		nodesList.add("D");
		
		
		JFrame frame = new JFrame("FrameDemo");

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//3. Create components and put them in the frame.
		//...create emptyLabel...
		frame.getContentPane().add(gv.getScrollPane());

		//4. Size the frame.
		frame.setSize(600, 600);
		
	//	frame.add(p.scrollPane);

		//5. Show it.
		frame.setVisible(true); */
		
	}		
}