package org.processmining.causalityinference.limitGraphDeapth;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.causalityinference.algorithms.GraphVisualizer;
import org.processmining.causalityinference.algorithms.IsGraphDAG;
import org.processmining.causalityinference.algorithms.SEM;
import org.processmining.dataTable.AggregatedDataExtraction;

public class RightPanel extends JPanel {
	LeftPanel leftPanel;
//	Parameters params;
	JButton EstimateButton;
	JButton reseatButton;
	JButton clearKnowledgeButton;
	JButton viewKnowledgeButton;
	JButton finalGraphButton;
	JCheckBox isKnowledgeNeeded;
	JCheckBox emptyInitGraph;
	GraphVisualizer graphVisualizer;
	
//	CausalityGraph model;
	AggregatedDataExtraction tabularDataCreator;
	
	JRadioButton requiredDirection;
	JRadioButton deleteEdge;
	
	boolean requiredEdge;
	boolean delete;
	
	public RightPanel(LeftPanel leftPanel) {	
		this.leftPanel = leftPanel;
		setLayout(new GridLayout(10,1));

		
		requiredDirection = new JRadioButton("Add required direction");
		requiredDirection.setActionCommand("required");
		requiredDirection.setSelected(true);
		requiredEdge = true;
		
		deleteEdge = new JRadioButton("Delete edge");
		deleteEdge.setActionCommand("delete");
		
		ButtonGroup edgeEditGroup = new ButtonGroup();

		edgeEditGroup.add(requiredDirection);
		edgeEditGroup.add(deleteEdge);

	    
	    requiredDirection.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e){  
	        	requiredEdge = true;
	        	delete = false;
	    }  
	    });  
	    
	    deleteEdge.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e){  
	        	requiredEdge = false;
	        	delete = true;
	    }  
	    }); 
		
		add(new JLabel("  "));
		
		add(this.requiredDirection);
		add(this.deleteEdge);
						
		EstimationHandler h = new EstimationHandler();
		this.EstimateButton = new JButton("Estimate");
		this.EstimateButton.addActionListener(h);
		this.add(EstimateButton);
	}
	
	public void setAwareTable(AggregatedDataExtraction tabularDataCreator) {
		this.tabularDataCreator = tabularDataCreator;
	}
    
	public void showPupup(String message) {
		
		String[] options = {"OK"};
		JPanel panel = new JPanel();
		JLabel lbl = new JLabel(message);
		panel.add(lbl);
		int selectedOption = JOptionPane.showOptionDialog(null, panel, message, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

		if(selectedOption == 0)
			return;
	}
	
/**	private class ViewKnowledgeGraphHandler implements ActionListener {
		LeftPanel leftPanel;
		GraphVisualizer knowledgeVisualizer;
		GraphVisualizer graphVisualizer;
		
		public void actionPerformed(ActionEvent e) {
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
	//		knowledge = model.getKnowledge();
			knowledgeVisualizer = new GraphVisualizer();
			knowledgeVisualizer.beginGraph();
			if (leftPanel.getGraph() != null)
				knowledgeVisualizer.setNodes(model.getNodes());
			
			if (graphVisualizer.getForbiddenEdges() != null || graphVisualizer.getRequiredEdges() != null) { 
				knowledgeVisualizer.setForbiddenEdges(graphVisualizer.getForbiddenEdges());
				knowledgeVisualizer.setRequiredEdges(graphVisualizer.getRequiredEdges());
				knowledgeVisualizer.drawKnowledgeGraph();
		
				
				panel.add(knowledgeVisualizer.getScrollPane(), BorderLayout.CENTER);
			} else {
				JLabel label = new JLabel("No knowledge is available!");
				panel.add(label);
			}
			
//			panel.add(knowledgeVisualizer);
			String[] options = {"OK"};
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Knowledge graph" , JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
				return;
		}
		
//		public void setCausalityGraph(CausalityGraph cg) {
//			this.model = cg;
//		}
		
		public void setGraphVisualizer(GraphVisualizer gv) {
			graphVisualizer = gv;
		}
	} */

	private class EstimationHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("Estimate")) {
				IsGraphDAG isDag = new IsGraphDAG(leftPanel.nodeNames, leftPanel.edgeNames);
				if (!isDag. checkIsGraphDAG()) {
					showPupup(" The graph is not a DAG!");
					return;
				}
				
				tabularDataCreator.LimitDataTable(leftPanel.nodeNames);
				SEM sem = new SEM(leftPanel.nodeNames, leftPanel.edgeNames);
				sem.estimate();
				
				leftPanel.setedgeCoefficients(sem.edgeCoefficients());
			}
			
		}
		
	} 
}