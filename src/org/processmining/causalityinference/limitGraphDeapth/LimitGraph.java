package org.processmining.causalityinference.limitGraphDeapth;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.causalityinference.dialogs.RelativeLayout;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.dataTable.AggregatedDataExtraction;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class LimitGraph {
	mxGraph graph;
	mxGraphComponent graphComponent;
	AggregatedDataExtraction tabularDataCreator;
	Parameters params;
	Set<String> nodeNames;
	Set<String> edgeNames;
	String classNodeName;
	int deapth = 2;
	
	public LimitGraph(Set<String> nodes, Set<String> edges, String className, AggregatedDataExtraction tabularDataCreator, Parameters params) {
		nodeNames = nodes;
		edgeNames = edges;
		classNodeName = className;
		this.params = params;
		this.tabularDataCreator = tabularDataCreator;
		tabularDataCreator.setTargetAttName(classNodeName);
		retriveTheSubGraph();
	}
	
	public LimitGraph(LinkedList<String> nodes, Set<String> edges, String className, AggregatedDataExtraction tabularDataCreator, Parameters params) {
		
		Set<String> set = new HashSet<>();
		for (String str : nodes)
			set.add(str);
		
		nodeNames = set;
		edgeNames = edges;
	    deapth = params.getDepth();   
		classNodeName = className;
		this.params = params;
		this.tabularDataCreator = tabularDataCreator;
		tabularDataCreator.setTargetAttName(classNodeName);
		retriveTheSubGraph();
	}
	
	public void retriveTheSubGraph() {
		Set<String> nodes = new HashSet<>();
		Set<String> edges = new HashSet<>();
		nodes.add(classNodeName);
		Set<String> newNodes = new HashSet<>();
		
		for (int i = 1; i <= deapth; i++) {
			newNodes = new HashSet<>();
			for (String edgeName : edgeNames) {
				for (String nodeName : nodes)
					if (contains(edgeName, nodeName)) {
						newNodes.add(otherNodeName(edgeName, nodeName));
						edges.add(edgeName);
					}
			}
			newNodes.addAll(nodes);
			nodes = newNodes;
		}
		
		if (nodes.size() > 1) {
			nodeNames = nodes;
			edgeNames = edges;
			showTheSubGraph();
		}
	}
	
	public String[] getNodesOfEdge(String edgeName) {
		String[] parts = null;
		if (edgeName.contains(" --> ")) 
			return parts = edgeName.split(" --> ");
		else if (edgeName.contains(" o-> "))
			return parts = edgeName.split(" o-> ");
		else if (edgeName.contains(" o-o "))
			return parts = edgeName.split(" o-o ");
		else if (edgeName.contains(" <-> "))
			return parts = edgeName.split(" <-> ");
		
		return null;
	}
	
	private boolean contains(String edgeName, String nodeName) {
		String[] parts = getNodesOfEdge(edgeName);
		
		if (parts[0].equals(nodeName)) {
			System.out.println("nodee name : " + nodeName);
			System.out.println("edge name : " + edgeName);
			return true;
		}
		
		if (parts[1].equals(nodeName)){
			System.out.println("nodee name : " + nodeName);
			System.out.println("edge name : " + edgeName);
			return true;
		}
			
		return false;
	}

	private void showTheSubGraph() {
		LeftPanel leftPanel = new LeftPanel(nodeNames, edgeNames, classNodeName);
		RightPanel rightPanel = new RightPanel(leftPanel);
		rightPanel.setAwareTable(tabularDataCreator);
		leftPanel.setAware(rightPanel);
		leftPanel.setAwareTable(tabularDataCreator);
		leftPanel.drawGraph();
		MainView main = new MainView(leftPanel, rightPanel);
		
	}

	private String otherNodeName(String edgeName, String nodeName) {
		String[] parts = getNodesOfEdge(edgeName);
		
		if (parts[0].equals(nodeName))
			return parts[1];
		else
			return parts[0];
	}

	public void setEdgeNames(Set<String> set) {
		edgeNames = set;
	}
	
	public Set<String> getEdgeNames() {
		return edgeNames;
	}
	
	public Set<String> getLimitedNodeNames() {
		return nodeNames;
	}
	
	class MainView extends JPanel {
		Set<String> nodes;
		Set<String> edges;
		
		public MainView(LeftPanel leftPanel, RightPanel rightPanel) {

			
			RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
			rl.setFill( true );
			this.setLayout(rl);
			
			this.add(leftPanel, new Float(70));
			this.add(rightPanel, new Float(30));
			
			String[] options = {"OK"};
			JPanel panel = new JPanel();

			panel.add(this);
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Limited Graph", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
			{
			    System.out.println(" Limited View ");
			}
//			JFrame frame = new JFrame("Limited Graph");
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//			frame.getContentPane().add(this);
//			frame.pack();
//			frame.setVisible(true); 
		}
	}
	
	
}
