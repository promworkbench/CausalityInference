package org.processmining.causalityinference.limitGraphDeapth;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.causalityinference.algorithms.GraphVisualizer;
import org.processmining.causalityinference.algorithms.SituationFeatureTableVisualizer;
import org.processmining.framework.plugin.PluginContext;

import com.mxgraph.swing.mxGraphComponent;

public class GraphMouseListenerLimitedGraph implements MouseListener {
	PluginContext context;
//	CausalityGraph model;
	mxGraphComponent graphComponent;

	String firstNode = null;
	LeftPanel leftPanel;
	RightPanel rightPanel;
	GraphVisualizer graphVisualizer = null;
	Map<Object, String> reverseNodNameMap;
	Map<String, Map<Integer, String>> inverseValueMap;
	String classAttName;
//	AggregatedDataExtraction tabularDataCreator;
	
	
	public GraphMouseListenerLimitedGraph(LeftPanel leftPanel, RightPanel rightPanel, String classAttName) {
		this.rightPanel = rightPanel;
		this.leftPanel = leftPanel;
		this.graphVisualizer = leftPanel.graphVisualizer;
		this.classAttName = classAttName;
		graphComponent = leftPanel.graphVisualizer.getGraphComponent();
		
	}
	 
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		
		Object cell = graphComponent.getCellAt(e.getX(), e.getY());
		
		if (graphVisualizer.getNodesInGraph().containsValue(cell)) {
			firstNode = graphVisualizer.nodeName(cell);
		} else
			firstNode = null;
	}

	public void mouseReleased(MouseEvent e) {
		Object cell = graphComponent.getCellAt(e.getX(), e.getY());
		Map<String, Object> temp = graphVisualizer.getEdgesInGraph();
		
		//delete an edge
		if (this.graphVisualizer.getEdgesInGraph().containsValue(cell)) {
			if (rightPanel.delete) {
				String edgeName = graphVisualizer.getReverseEdgeNameMap().get(cell);
				leftPanel.removeEdge(edgeName);
			}
		}
		else if (this.graphVisualizer.getNodesInGraph().containsValue(cell)) {
			
			String secondNode = graphVisualizer.nodeName(cell);
			
			//delete a node
			if (rightPanel.delete && firstNode.equals(secondNode)) {
				//TODO
			// add required edge	
			} else if (rightPanel.requiredEdge && !firstNode.equals(secondNode)) { 
				if (firstNode != null)  {
					leftPanel.addEdge(firstNode + " --> " + secondNode);
				}
			}
		}	
	}


	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
//		graphVisualizer.getGraphComponent().setToolTipText("Jello");
//		System.out.println("ciao");
	}
	
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
//		System.out.println("ciao2");
	}
	
	public void showPupup(SituationFeatureTableVisualizer table, String name, Map<String, Map<Integer, String>> inverseMap) {
		JFrame frame = new JFrame(name);
		frame.setSize(new Dimension(500, 500));
		if (table == null) {
			JLabel label = new JLabel("This variable has no causal effect on the class variable!");
			frame.getContentPane().add(label);
		} else {
			JTable t = table.getTableForPupup(inverseMap); // TODO , numAtt);
			JScrollPane sPane = new JScrollPane(t, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sPane.setPreferredSize(new Dimension(400, 450));
			sPane.setMinimumSize(new Dimension(400, 450));
			frame.getContentPane().add(sPane, BorderLayout.CENTER);
		}
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		frame.show();  
	}
	
	public void showPupup(String nodeName) {
//		if (model.getNodeCoefficients().get(nodeName) == null)
//			return;
		
		JFrame frame = new JFrame(nodeName);
		frame.setLayout(new GridLayout(10,1));
		frame.setSize(new Dimension(300, 500));
/**		if (!nodeName.equals(model.getClassAttName())) {
			JLabel equationY = new JLabel("  Y = " + model.getClassAttName());
			frame.getContentPane().add(equationY);
			JLabel equationX = new JLabel("  X = " + nodeName);
			frame.getContentPane().add(equationX);
			JLabel parents = new JLabel("  PA(Y) = those parents of Y that are not on any path from X to Y ");
			frame.getContentPane().add(parents);
			JLabel equation = new JLabel("  Y = " + model.getNodeCoefficients().get(nodeName) + " * X + error + f (PA(Y))" );
			frame.getContentPane().add(equation);
		}
		
		Map<String, Double> mean = tabularDataCreator.getMean();
		Map<String, Double> median = tabularDataCreator.getMedian();
		Map<String, Double> variance = tabularDataCreator.getVariance();
		Map<String, Double> stdDev = tabularDataCreator.getStdDev();
		if (mean.containsKey(nodeName)) {
			JLabel label = new JLabel("  Mean : " + mean.get(nodeName));
			frame.getContentPane().add(label);
		}
		if (median.containsKey(nodeName)) {
			JLabel label = new JLabel("  Median : " + median.get(nodeName));
			frame.getContentPane().add(label);
		}
		if (variance.containsKey(nodeName)) {
			JLabel label = new JLabel("  Variance : " +variance.get(nodeName));
			frame.getContentPane().add(label);
		}
		if (stdDev.containsKey(nodeName)) {
			JLabel label = new JLabel("  Standard deviation : " + stdDev.get(nodeName));
			frame.getContentPane().add(label);
		} */
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		frame.show();  
	}
}
