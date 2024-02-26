package org.processmining.causalityinference.evaluation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.causalityinference.algorithms.CausalityGraph;

import edu.cmu.tetrad.graph.Graph;

public class FindBestCausalStructure {
	
	/**
	 * This class finds the best causal structure (DAG) which is 
	 * compatible with the discovered PAG. Best causal structure in
	 * the sense that it is most compatible with the data.
	 * 
	 * Steps:
	 *    1- Generate all compatible DAGs
	 *    2- Evaluate each DAG with the test data
	 *    3- return the best DAG
	 */
	private CausalityGraph sem;
	
	/**
	 * the best DAG
	 */
	private Graph bestDAG = null; 
	
	/**
	 * the coefficients of the best DAG
	 */
	Map<String, Double> coeffBestDAG = null; 
	
	/**
	 * the accuracy of the best DAG
	 */
	Double accuracyBestDAG = 0.0; 
	
	public FindBestCausalStructure(CausalityGraph sem) {
		this.sem = sem;
		if (sem == null) {
			String[] options = {"OK"};
			JPanel panel = new JPanel();
			JLabel lbl = new JLabel("Do the search first");;
			panel.add(lbl);
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Invalid action", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);
		}
	}
	
	public void findBestDAG() {
		// Generating all the compatible DAGs
		AllCompatibleDAGs allDAGs = new AllCompatibleDAGs(sem);
		allDAGs.generateAllDAGs();
		LinkedList<Graph> dags = allDAGs.getAllDAGs();
		
		// Find the best DAG
		Map<Graph, Double> scores = new HashMap<>();
		Evaluation eval = new Evaluation(sem.getParameters().getTrainData(), sem.getParameters().getTestData());
		
		for (Graph dag : dags) {
			eval.setGraphAndCoeff(dag, allDAGs.getEdgeCoefficients(dag));
			eval.initiate();
			if (accuracyBestDAG <= eval.accuracy()) {
				bestDAG = dag;
				coeffBestDAG = allDAGs.getEdgeCoefficients(dag);
				accuracyBestDAG = eval.accuracy();
			}
		}
		
		System.out.println("end of find Best DAG");
	}
	
	public Graph getTheBestDAG() {
		return bestDAG;
	}
	
	public Map<String, Double> getTheCoefficientsBestDAG() {
		return coeffBestDAG;
	}
	
	public Double getAccuracyBestDAG() {
		return accuracyBestDAG;
	}
	
	public void setGraph() {
		sem.setGraph(bestDAG, coeffBestDAG);
		
	}

}
