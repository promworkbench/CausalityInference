package org.processmining.causalityinference.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;

/**
 * In this class whenever the index of a node is used it is the index of the node in BayesIM!
 * @author qafari
 *
 */
public class D_separationTest {

	private BayesIm im;
	private BayesPm pm;
	private LinkedList<Integer> adj[]; //Adjacency Lists 
	private Map<Integer, Set<Integer>> graph;
	private Map<Integer, Set<Integer>> parents;
	
	
	public D_separationTest() {
		this.im = null;
		this.pm = null;
		graph = new HashMap<Integer, Set<Integer>> ();
		parents = new HashMap<Integer, Set<Integer>> ();
	}
	
	public D_separationTest(BayesIm im, BayesPm pm) {
		this.im = im;
		this.pm = pm;
		setGraph();
	}
	
	/**
	 * Using the im and pm creates the adjacency list of the initial graph
	 * and the parents of each node.
	 */
	public void setGraph() {
		graph = new HashMap<Integer, Set<Integer>> ();
		parents = new HashMap<Integer, Set<Integer>> ();
		
		for (int i = 0; i < im.getNumNodes(); i++) {
			if (im.getNumParents(i) > 0)
				parents.put(i, turnArrayToSet(im.getParents(i)));
			addNodeToGraph(i);
		}
	}
	
	/**
	 * This function creates the initial graph using the parent information in the BayesIM im.
	 * @param nodeIdx
	 */
	public void addNodeToGraph(int nodeIdx) {
		if (im.getNumParents(nodeIdx) > 0) 
			for (int p : turnArrayToSet(im.getParents(nodeIdx)))
				if (graph.containsKey(p))
					graph.get(p).add(nodeIdx);
				else {
					Set<Integer> set = new HashSet<Integer>();
					set.add(nodeIdx);
					graph.put(p, set);
				}
		else if (!graph.containsKey(nodeIdx)){
			Set<Integer> set = new HashSet<Integer>();
			graph.put(nodeIdx, set);
		}			
	}
	
	/**
	 * 
	 * @param X --> the possible cause
	 * @param Y  --> the possible effect
	 * @return  returns true if X and Y are d-separated (independent) and false o.w.
	 */
	public boolean isD_separate(Node X, Node Y) {
		int yIdx = getNodeIndex(Y);    // if Y has no parent then Y is a stand alone node and no other node has any effect on it
		if (im.getNumParents(yIdx) == 0)
			return true;
		
		if (X.equals(Y)) // if X and Y are both the same node, then there is nothing to compute.
			return true;
		
		int xIdx =  getNodeIndex(X);   
		Set<Integer> ansestralGraphNodes = new HashSet<Integer>();
		
		addTheAnsestors(ansestralGraphNodes, xIdx);
		addTheAnsestors(ansestralGraphNodes, yIdx);
		marryParents(ansestralGraphNodes);
		
		PathFinder pf = new PathFinder();
		pf.setGraph(graph);
		pf.turnToUndirected();

		return !pf.isConnected(yIdx, xIdx);
		
	}
	
	/**
	 * 
	 * @param xIdx --> the possible cause
	 * @param yIdx  --> the possible effect
	 * @return  returns true if X and Y are d-separated (independent) and false o.w.
	 */
	public boolean isD_separate(int xIdx, int yIdx) { 
		Set<Integer> ansestralGraphNodes = new HashSet<Integer>();
		ansestralGraphNodes.add(xIdx);
		ansestralGraphNodes.add(yIdx);
		
		addTheAnsestors(ansestralGraphNodes, xIdx);
		addTheAnsestors(ansestralGraphNodes, yIdx);
		marryParents(ansestralGraphNodes);
		
		PathFinder pf = new PathFinder();
		pf.setGraph(graph);
		pf.turnToUndirected();

		return !pf.isConnected(xIdx, yIdx);
	}
	
	/**
	 * 
	 * @param xIdx --> the possible cause
	 * @param yIdx  --> the possible effect
	 * @return  returns true if There are at least one directed path
	 * from X to Y and false o.w.
	 */
	public boolean isThereAnyDirectedPath(Node X, Node Y) { 
		int yIdx = getNodeIndex(Y);    // if Y has no parent then Y is a stand alone node and no other node has any effect on it
		if (X.equals(Y)) // if X and Y are both the same node, then there is nothing to compute.
			return true;
		
		int xIdx = getNodeIndex(X);   
		PathFinder pf = new PathFinder();
		pf.setGraph(graph);

		return pf.isConnected(xIdx, yIdx);
	}
	
	
	/**
	 * 
	 * it adds an undirected link between every pair of nodes that are both 
	 * parents of the same node and there is no link between them.
	 */
	public void marryParents(Set<Integer> nodes) {
		
		induceInitialGraph(nodes);
		
		//find the parents that are not married
		Set<Set<Integer>> parentsToMarry = findUnmarriedParents();
		if (parentsToMarry == null)
			return;
		
		// marry parents
		for (Set<Integer> setOfNodes : parentsToMarry) {
			LinkedList<Integer> order = new LinkedList<Integer>();
			// fix an order
			for (Integer node : setOfNodes)
				order.add(node);
			for (int i = 0; i < order.size() - 1; i++)
				for (int j = i+1; j < order.size(); j++)
					if (graph.keySet().contains(order.get(i)))
						graph.get(order.get(i)).add(order.get(j));
					else if (graph.keySet().contains(order.get(j)))
						graph.get(order.get(j)).add(order.get(i)); 
					else {
						Set<Integer> set = new HashSet<Integer>();
						set.add(order.get(i));
						graph.put(order.get(j), set);
						}
		}
		
//		System.out.println("graph with married parents ");
//		System.out.println(graph);
	}
	
	/**
	 * This function turns the graph to an induced graph according to the given set of nodes.
	 * @param nodes
	 */
	public void induceInitialGraph(Set<Integer> nodes) {
		Set<Integer> nodeToRemove = new HashSet<Integer>();
		
		// finding the nodes that are not in the set of nodes for induced graph
		for (Integer node : graph.keySet())
			if (!nodes.contains(node))
				nodeToRemove.add(node);
		
		// removing the nodes that have to be removed from the graph nodes
		if (!nodeToRemove.isEmpty())
			for (Integer node: nodeToRemove)
				graph.remove(node);
		
		// removing any unwanted node in the adjacency list of a node
		Map<Integer, Set<Integer>> newGraph = new HashMap<Integer, Set<Integer>>();
		for (Integer node : graph.keySet()) 
			if (!graph.get(node).isEmpty()) {
				Set<Integer> kids = graph.get(node);
				for (Integer kid : kids)
					if (nodes.contains(kid))
						if (newGraph.containsKey(node))
							newGraph.get(node).add(kid);
						else {
							Set<Integer> newKid = new HashSet<Integer>();
							newKid.add(kid);
							newGraph.put(node, newKid);
						}
				
			}	
		graph = newGraph;
//		System.out.println("cleaned graph :");
//		System.out.println(graph);
	}
	
	/**
	 * 
	 * @param arr  array of integers
	 * @return  a link list of integers with the same order
	 */
	public LinkedList<Integer> turnArrayToLinkedList(int[] arr) {
		LinkedList<Integer> list = new LinkedList<Integer>();
		for (int i : arr)
			list.add(i);
		
		return list;
	}
	
	public Set<Integer> turnArrayToSet(int[] arr) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i : arr)
			set.add(i);
		
		return set;
	}
	
	public Set<Set<Integer>> findUnmarriedParents() {
		Set<Set<Integer>> parentsToMarry = new HashSet<Set<Integer>>();
		
		// if the graph contain just one node then trivially there is no pair of parents to marry
		if (graph.keySet().size() <= 1)
			return null;
		
		Set<Integer> allTheNodesInTheGraph = new HashSet<Integer>();
		for (int node : graph.keySet()) {
			allTheNodesInTheGraph.add(node);
			if (!graph.get(node).isEmpty())
				for (Integer v : graph.get(node))
					allTheNodesInTheGraph.add(v);
		}
		
		for (int node : allTheNodesInTheGraph) {
			Set<Integer> nodeParents = getParents(node);
			if (nodeParents.size() > 1)
				parentsToMarry.add(nodeParents);
		}
				
		return parentsToMarry;
	}
	
	/**
	 * 
	 * @param node
	 * @return the set of the parents of the given node in the graph.
	 */
	public Set<Integer> getParents(Integer node) {
		Set<Integer> parents = new HashSet<Integer>();
		for (Integer v : graph.keySet()) {
			Set<Integer> kids = graph.get(v);
			if (kids.contains(node))
				parents.add(v);
		}
		
		return parents;
	}
	
	/**
	 *  This function add all the ancestors of the given node to the graph
	 * @param g  --> the adjacency list presentation of the graph
	 * @param xIdx  --> the node index in BayesIm
	 */
	public void addTheAnsestors(Set<Integer> g, int xIdx) {
		Set<Integer> nodeParents = this.parents.get(xIdx);
		
		if (parents.get(xIdx) == null)
			return;
		
		if (nodeParents.size() == 0) 
			return;
		g.add(xIdx);
		g.addAll(nodeParents);
		for (int i : nodeParents) 
			addTheAnsestors(g, i);	
	}
	
	
	/**
	 * 
	 * @param sourceIdx --> the index of the source Node in BayesIm
	 * @param destIdx --> the index of the destination Node in BayesIm
	 * @return   It returns true if the edge s-->d exist o.w. false.
	 */
	public boolean directedEdgeExists(int sourceIdx, int destIdx) {
		
		int[] parents = im.getParents(destIdx);
		if (parents.length == 0)
			return false;
		for (int idx : parents)
			if (idx == sourceIdx)
				return true;
		
		return false;
	}
	
	public int getNodeIndex(Node node) {
		int XIdx = 0;
		for (int i = 0; i < im.getNumNodes(); i++)
			if (im.getNode(i).getName().equals(node.getName()))
				XIdx = i;
		return XIdx;
	}
		
	//--------------------------------------------
	//--------------- Test -----------------------
	
	
	public void addEdge(int u , int v) {
		if (graph.keySet().contains(u))
			graph.get(u).add(v);
		else {
			Set<Integer> kids = new HashSet<Integer>();
			kids.add(v);
			graph.put(u, kids);
		}	
	}
	
	public void addSingleNode(int u) {
		Set<Integer> kids = new HashSet<Integer>();
		graph.put(u, kids);	
	}
	
	public void setParents() {
		for (Integer node : graph.keySet())
			if (graph.get(node) != null)
				for (Integer kid : graph.get(node))
					if (parents.containsKey(kid))
						parents.get(kid).add(node);
					else {
						Set<Integer> set = new HashSet<Integer>();
						set.add(node);
						parents.put(kid, set);
					}
	}
	/**
	 *  1 <-- 2 <-- 3 <-- 4 <-- 5
	 *  a graph with just a path.
	 *  test d_separation for 1 and 5
	 */
	public void setGraph1() {
		graph = new HashMap<Integer, Set<Integer>> ();
		parents = new HashMap<Integer, Set<Integer>> ();
		addEdge(2, 1);
		addEdge(3, 2);
		addEdge(4, 3);
		addEdge(5, 4);
		
		setParents();
//		System.out.println("Graph 1 is set ");
	}
	
	/**
	 *  1 <-- 3 <-- 4 --> 6 <-- 5 --> 2
	 *  a graph with a path and a collider.
	 *  test d_separation for 1 and 2
	 */
	public void setGraph2() {
		graph = new HashMap<Integer, Set<Integer>> ();
		parents = new HashMap<Integer, Set<Integer>> ();
		addEdge(3, 1);
		addEdge(4, 3);
		addEdge(4, 6);
		addEdge(5, 6);
		addEdge(5, 2);
		
		setParents();
	}
	
	
	/**
	 *  a graph for checking the marriage of parents.
	 *  test d_separation for 1 and 13
	 */
	public void setGraph3() {
		graph = new HashMap<Integer, Set<Integer>> ();
		parents = new HashMap<Integer, Set<Integer>> ();
		addEdge(2, 1);
		addEdge(2, 5);
		addEdge(5, 1);
		addEdge(6, 1);
		addEdge(5, 7);
		addEdge(4, 2);
		addEdge(4, 7);
		addEdge(3, 2);
		addEdge(2, 8);
		addEdge(9, 8);
		addEdge(9, 13);
		addEdge(10, 13);
		addEdge(10, 11);
		addSingleNode(12);
		
		setParents();
	}
	
	public static void main(String[] args) {
		D_separationTest dst1 = new D_separationTest();
		dst1.setGraph1();
		System.out.println(dst1.isD_separate(1, 5));
		
		D_separationTest dst2 = new D_separationTest();
		dst2.setGraph2();
		System.out.println(dst2.isD_separate(1, 2));
		
		D_separationTest dst3 = new D_separationTest();
		dst3.setGraph3();
		System.out.println(dst3.isD_separate(1, 13));
	}
}
