package org.processmining.causalityinference.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.causalityinference.algorithms.CausalityGraph;
import org.processmining.causalityinference.algorithms.IsGraphDAG;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraphSingleConnections;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;


/**
 * Generate all the compatible DAGs with a given PAG.
 * @author qafari
 *
 */
public class AllCompatibleDAGs { 

	Graph pag;
	Map<Edge, LinkedList<String>> possibleEdges;
	
	/**
	 * A fixed order for the edges that is set at the beginning and 
	 * have to be respected through this class.
	 */
	LinkedList<Edge> edgeOrder;
	Set<List<String>> dags;
	LinkedList<String> binaryStrings;
	LinkedList<Graph> allDAGs;
	CausalityGraph sem;
	
	public AllCompatibleDAGs(CausalityGraph sem) {
		this.sem = sem;
		this.pag = sem.getGraph();
		
		// set a fixed order for the edges
		edgeOrder = new LinkedList<>();
		
		for (Edge edge : pag.getEdges()) 
			edgeOrder.add(edge);
	}
	
	public void generateAllDAGs() {
		possibleEdges = new HashMap<>();
		Set<Edge> edges = pag.getEdges();
		
		for (Edge edge : edges) 
			possibleEdges.put(edge, getPossibleEdges(edge));
		
		generateDAGs();
	}

	/**
	 * for the given edge produce a set of all the possible directed edges in the final DAG
	 * @param edge 
	 * @return a set of all the possible directed edges in the final DAG
	 */
	public LinkedList<String> getPossibleEdges(Edge edge) {
		String node1 = edge.getNode1().getName();
		String node2 = edge.getNode2().getName();
		LinkedList<String> edges = new LinkedList<>();
		
		if (edge.toString().contains("o-o") || edge.toString().contains("<->")) {
			edges.add(node1 + "-->" + node2);
			edges.add(node2 + "-->" + node1);
		} else if (edge.toString().contains("-->") || edge.toString().contains("o->")) {
			edges.add(node1 + "-->" + node2);
		}
		
	/**	System.out.println(" edge ");
		System.out.println(edge.toString());
		System.out.println(edges.size()); */
		
		return edges; 
	}

	private void generateDAGs() {
		LinkedList<LinkedList<String>> graphs = new LinkedList<>();
		int num = 1;
		int[] indices = new int[possibleEdges.size()];
		int numBinary = 0;
		int i = 0;
		for(Edge edge : edgeOrder) {
			indices[i] = possibleEdges.get(edge).size();
			if (possibleEdges.get(edge).size() == 2)
				numBinary++;
			i++;
		}
		
//		for (int j = 0; j < indices.length; j++)
//			System.out.println(indices[j]);
		
		binaryStrings = new LinkedList<>();
		String str = new String();
		generateAllBinaryStrings(numBinary,str);
		
		System.out.println(" === binaryStrings 1 ===");
		for (int j = 0; j < binaryStrings.size(); j++)
			System.out.println(binaryStrings.get(j));
		
		generateFinalBinaryCode(indices);
		
		System.out.println(" === binaryStrings 2 ===");
		for (int j = 0; j < binaryStrings.size(); j++)
			System.out.println(binaryStrings.get(j));
		
		allDAGs = new LinkedList<>();
		for (String edgeSet : binaryStrings) {
			Graph graph = turnBinaryStringToGraph(edgeSet);
			IsGraphDAG isDAG = new IsGraphDAG(graph);
			if (isDAG.checkIsGraphDAG())
				allDAGs.add(graph);
		}
			
	}
	
	private Graph turnBinaryStringToGraph(String edgeCode) {
		
		Graph graph = new EdgeListGraphSingleConnections(pag.getNodes());
		for (int i = 0; i < edgeCode.length(); i++) {
			if (edgeCode.charAt(i) == '0') 
				graph.addDirectedEdge(edgeOrder.get(i).getNode1(), edgeOrder.get(i).getNode2());
			else 
				graph.addDirectedEdge(edgeOrder.get(i).getNode2(), edgeOrder.get(i).getNode1());
		}		
		
		return graph;
	}

	/**
	 * Function to generate all the set of links corresponding to the edges of a 
	 * directed graph compatible with the given PAG.
	 * For each string of binary values (which is corresponding to a combination of
	 * edges if type o-o in the PAG) we just need to add 0 in the location of edges 
	 * with type o-> or -->.
	 * 
	 * @param indices --> indices of the edges of type o-> or -->
	 */
	private void generateFinalBinaryCode(int[] indices) {
		LinkedList<String> newBinaryStrings = new LinkedList<>();
		
		for (String str : binaryStrings) {
			String s = str;
			for (int i = 0; i < indices.length; i++)
				if (indices[i] == 1)
					s = new StringBuilder(s).insert(i, "0").toString();
			newBinaryStrings.add(s);
		}
			
		binaryStrings = newBinaryStrings;
	}

	/**
	 *  Function to generate all binary strings of length n
	 *  Each binary string is corresponding to one of the combination of edges 
	 *  of type o-o in the PAG
	 * @param n
	 * @param str
	 * @param i
	 */
	public void generateAllBinaryStrings(int n, String str)
	{
	    if (str.length() == n)
	    {
	        binaryStrings.add(str);
	        return;
	    }
	 
	    // First assign "0" at ith position
	    // and try for all other permutations
	    // for remaining positions
	    String str1 = str + "0";
	    generateAllBinaryStrings(n, str1);
	 
	    // And then assign "1" at ith position
	    // and try for all other permutations
	    // for remaining positions
	    String str2 = str + "1";
	    generateAllBinaryStrings(n, str2);
	}
	
	
	public LinkedList<Graph> getAllDAGs() {
		return allDAGs;
	}
	
	public Map<String, Double> getEdgeCoefficients(Graph dag) {
		return sem.setCoefficientsOneDAG(dag);
	}
	
	//********************* TEST CODE *************************
	public AllCompatibleDAGs(Graph g) {
		this.pag = g;
		
		// set a fixed order for the edges
		edgeOrder = new LinkedList<>();
		
		for (Edge edge : g.getEdges()) 
			edgeOrder.add(edge);
	}
	
	public void initiateBinaryStrings() {
		binaryStrings = new LinkedList<>();
	}
	
	public LinkedList<String> getBinaryStrings() {
		return binaryStrings;
	}
	
	/**
	 * Function to generate the test graph:
	 *      A --> B o-> C o-o D o-o E
	 */
	public static Graph testGraph() {
		LinkedList<String> nodeNames = new LinkedList<>();
		nodeNames.add("A");
		nodeNames.add("B");
		nodeNames.add("C");
		nodeNames.add("D");
		nodeNames.add("E");
		
		List nodes = new ArrayList<GraphNode>();
		Map<String, GraphNode> map = new HashMap<>();
		
		for (int i = 0; i < nodeNames.size(); i++) {
			String nodeName = nodeNames.get(i).toString();
			GraphNode node = new GraphNode(nodeName);
			nodes.add(node);
			map.put(nodeName, node);
		}
		
		Graph graph = new EdgeListGraphSingleConnections(nodes);
		
		graph.addDirectedEdge(map.get("A"), map.get("B"));
		graph.addPartiallyOrientedEdge(map.get("B"), map.get("C"));
		graph.addNondirectedEdge(map.get("C"), map.get("D"));
		graph.addNondirectedEdge(map.get("D"), map.get("E"));	
		
		return graph;
	}
	
	/**
	 * test generate all binary strings
	 */
	public void checkBinaryList() {
		
		System.out.println("All binary strings n = 5");
		initiateBinaryStrings();
		String s = new String();
		generateAllBinaryStrings(5, s);
		for (String str : getBinaryStrings())
			System.out.println(str);
	}
	
	public static void main(String[] args) {
		AllCompatibleDAGs allDAGs = new AllCompatibleDAGs(testGraph());
//		allDAGs.checkBinaryList();
		
		allDAGs.generateAllDAGs();
		LinkedList<Graph> dags = allDAGs.getAllDAGs();
		
		for (Graph g : dags) {
			System.out.println(" === new graph ===");
			for (Edge e : g.getEdges())
				System.out.println(e.toString());
		}
	}

}
