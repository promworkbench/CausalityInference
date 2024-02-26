package org.processmining.causalityinference.dataGenerator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DAGgenerator {
	/**
	 * We are going to generate a DAG with n vertices v1,...vn.
	 * inputs:
	 * 		n : the number of vertices.
	 * 		p : probability of each edge.
	 * 
	 * The adjacency matrix in generated in the form on a n by n array.
	 */
	
	int numVertices = 10;
	double prob = 0.5;
	public DAGgenerator(int n, double p) {
		
	}
	
	public void setNumVertices(int n) {
		this.numVertices = n;
	}
	
	public void setProbability(double d) {
		if (d >= 0 && d <= 1)
			prob = d;
		else {
			prob = 0.5;
			JPanel panel = new JPanel();
			String[] options = {"ok"};
			panel.add(new JLabel("prob has been set to 0.5!"));
			JOptionPane.showMessageDialog(null,panel,"Information",JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
