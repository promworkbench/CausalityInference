package org.processmining.causalityinference.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;

public class EstimateTheModel {
	Graph graph;
	DataSet data;
	Boolean isDiscrete;
	Map<String, Type> attributeType;
	Map<String, Set<String>> literalValues;
	
	
	public EstimateTheModel(Graph graph, DataSet data, String dataType) {
		this.data = data;
		this.graph = graph;
		
	}
	
	public void doEstimation() {
		if (isDag(graph)) {
			if (isDiscrete)
				estimateBayesianNetwork();
//TODO			else
//TODO				estimateSEM();
		} else
			if (isDiscrete)
				System.out.println("The graph must be a DAG!");
		//TODO else for discerete part the graph check of being a PAG
	}
	
	public void estimateBayesianNetwork() {
		BayesPm bayesPm = new BayesPm(graph);
		setVariables(bayesPm);
	}
	
	public void setVariables(BayesPm bayesPm) {
		for (int i = 0 ; i <bayesPm.getNumNodes() ; i++) {
        	if (attributeType.get(bayesPm.getNode(i).getName()).equals(Type.BOOLEAN)) {
        		bayesPm.setNumCategories(bayesPm.getNode(i), 2);
        		List<String> categories = new ArrayList<String>();
        		categories.add("true");
        		categories.add("false");
        		bayesPm.setCategories(bayesPm.getNode(i), categories);
        	}
        	if (attributeType.get(bayesPm.getNode(i).getName()).equals(Type.LITERAL)) {
        		bayesPm.setNumCategories(bayesPm.getNode(i), literalValues.get(bayesPm.getNode(i).getName()).size());
        		List<String> categories = getArrayList(literalValues.get(bayesPm.getNode(i).getName()));
        		bayesPm.setCategories(bayesPm.getNode(i), categories);
        	}
        }
	}
	
	public List<String> getArrayList(Set<String> categoriesSet) {
		List<String> categories = new ArrayList<String>();
		for (String s : categoriesSet)
			categories.add(s);
		return categories;
	}
	
	public boolean isDag(Graph graph) {
		IsGraphDAG g = new IsGraphDAG(graph);
		return g.checkIsGraphDAG();
	}
}
