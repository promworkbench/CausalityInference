package org.processmining.causalityinference.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.DataConvertUtils;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.tabular.ContinuousTabularDatasetFileReader;

public class SEM {
	
	Graph graph;
	DataSet dataSet = null;
	
	Map<String, GraphNode> nameNodeMap;
	Map<String, Double> edgeCoefficients;
	Map<String, Double> nodesErrVar;
	Map<String, Double> nodesMean;
	Map<String, Double> nodesStdDev;
	Set<String> requiredEdges;
	
	private String path = "DataTableLimited.txt";
	
	public SEM(Set<String> nodeNames, Set<String> edgeNames) {
			
		graph = new Dag();
		nameNodeMap = new HashMap<>();
		
		for (String nodeName : nodeNames) {
			addNode(nodeName);
		}
		
		for (String edgeName : edgeNames) {
			addEdge(edgeName);
		}
	}
	
	/**
	 * Add node to the graph and update nameNodeMap.
	 * @param nodeName
	 */
	public void addNode(String nodeName) {
		GraphNode node = new GraphNode(nodeName);
		graph.addNode(node);
		nameNodeMap.put(nodeName, node);
	}
	
	/**
	 * 
	 * @param nodeName
	 * @return true if the graph already has this node.
	 */
	public boolean containsNode(String nodeName) {
		if (graph == null)
			return false;
		
		for (String name : graph.getNodeNames())
			if (nodeName.equals(name))
				return true;
		
		return false;
	}
	
	public void addEdge(String edgeName) {
		
		GraphNode startNode = endPoint(edgeName, "start");
		GraphNode endNode = endPoint(edgeName, "end");
		
		if (!containsNode(startNode.getName()))
			return;
		
		if (!containsNode(endNode.getName()))
			return;
		
		if (!containsEdge(edgeName))
			graph.addDirectedEdge(endPoint(edgeName, "start"), endPoint(edgeName, "end"));
		
		removeEdge(edgeName);
		graph.addDirectedEdge(endPoint(edgeName, "start"), endPoint(edgeName, "end"));
	}
	
	/**
	 * 
	 * @param edgeName
	 * @return true if the graph already has an edge between these nodes.
	 */
	public boolean containsEdge(String edgeName) {
		if (graph == null)
			return false;
		
		if (graph.getNumEdges() == 0)
			return false;
		
		GraphNode startNode = endPoint(edgeName, "start");
		GraphNode endNode = endPoint(edgeName, "end");
		
		if (startNode == null || endNode == null)
			return false;
		
		if (graph.getEdge(startNode, endNode) == null)
			return false;

		return true;
	}
	
	public void removeEdge(String edgeName) {
		GraphNode startNode = endPoint(edgeName, "start");
		GraphNode endNode = endPoint(edgeName, "end");
		
		if (containsEdge(edgeName))
			graph.removeEdge(startNode, endNode);
	}
	
	
	/**
	 * @param edgeName
	 * @param side \in {start , end}
	 * @return if side = start then returns the node at the start of the edge. If side = end then returns the node at the end of the edge. 
	 */
	private GraphNode endPoint(String edgeName, String side) {
		String[] parts = edgeName.split(" --> ");
		if (side.equals("start"))
			return nameNodeMap.get(parts[0]);
		if (side.equals("end"))
			return nameNodeMap.get(parts[1]);
		
		return null;
	}

	public void estimate() {
		
		readDataset();
		
		SemPm pm = new SemPm(graph);
		ICovarianceMatrix covMatrix = null;
		SemEstimator estimator = null;
		covMatrix = new CovarianceMatrix(dataSet);
		try {
			estimator = new SemEstimator(covMatrix, pm);
		} catch (IllegalArgumentException e) {
			System.out.println("Something went wrong. "+ e);
			
		}
        estimator.estimate();
        SemIm im  = estimator.getEstimatedSem();
        setEdgeCoefficients(im);
        setNodeStatisticalProperties(im);
	}
	
	public void readDataset() {
		dataSet = null;
		char delimiter = '\t';
        Path dataFilePath = Paths.get(path);

        ContinuousTabularDatasetFileReader dataReader= new ContinuousTabularDatasetFileReader(dataFilePath, Delimiter.TAB);
        dataReader.setMissingDataMarker("*");
		try {
			dataSet = (DataSet) DataConvertUtils.toDataModel(dataReader.readInData());
		} catch (IOException e) {
			System.out.println("Failed to read in data.");
			e.printStackTrace();
		} 
	}
	
	public void setEdgeCoefficients(SemIm im) {
		edgeCoefficients = new HashMap<String, Double>();
		Set<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			BigDecimal bd = BigDecimal.valueOf(im.getEdgeCoef(edge));
			edgeCoefficients.put(edge.toString(), bd.setScale(2, RoundingMode.CEILING).doubleValue());
		}
	}
	
	public void setNodeStatisticalProperties(SemIm im) {
		nodesErrVar = new HashMap<String, Double>();
		nodesMean = new HashMap<String, Double>();
		nodesStdDev = new HashMap<String, Double>();
				
		List<Node> nodes = graph.getNodes();
		for (Node node : nodes) {
			nodesErrVar.put(node.getName(), im.getErrVar(node));
			nodesMean.put(node.getName(), im.getMean(node));
			nodesStdDev.put(node.getName(), im.getMeanStdDev(node));
			System.out.println(node.getName());
			System.out.println("err : " + im.getErrVar(node) + " mean : "+ im.getMean(node) + " SD : "+im.getMeanStdDev(node));
		}
	}
	
	public void factorTheFinalGraph() {
		
		Set<Edge> edgesToRemove = new HashSet<Edge>();
		Set<Edge> e = graph.getEdges();
		
		for (Edge edge : edgesToRemove)
			graph.removeEdge(edge);
		
		edgesToRemove = new HashSet<Edge>();
		Set<String> edgesToAdd = new HashSet<String>();
		
		String[] edgeTypes = {" --> ", " o-o ", " o-> ", " <-> "};
		
		if (requiredEdges != null && !requiredEdges.isEmpty())
			for (String rEdge : requiredEdges) {
				String[] rParts = new String[2];
				rEdge.split(" --> ");
				String from = rParts[0];
				String to = rParts[1];
				boolean isPresent = false;
				String eType = new String();
				String[] parts = new String[2];
				for (Edge edge : graph.getEdges()) {
					String s = edge.toString();
					for (String et : edgeTypes) {
						if (s.contains(et));
						eType = et;
						parts = s.split(et);
					}

					if (parts[0].equals(from) && parts[1].equals(to) && eType.equals("-->"))
						isPresent = true;
					if ((parts[0].equals(from) && parts[1].equals(to) && (eType.equals("<->") || eType.equals("o-o") ||
							eType.equals("<-o") || eType.equals("o->"))) ||parts[0].equals(to) && parts[2].equals(from) && (eType.equals("<->") || eType.equals("o-o") ||
									eType.equals("<-o") || eType.equals("o->") || eType.equals("-->"))) {
						edgesToRemove.add(edge);
						edgesToAdd.add(rEdge);
					}
				}
				if (!isPresent) 
					edgesToAdd.add(rEdge);
			}
		
		for (Edge edge : edgesToRemove) 
			graph.removeEdge(edge);
		
		if (!edgesToAdd.isEmpty())
			for (String edge : edgesToAdd) {
				String[] parts = edge.split(" --> ");
				String from = parts[0];
				String to = parts[1];
				Node n1 = graph.getNode(from);
				Node n2 = graph.getNode(to);
				if (graph.getEdges(n1, n2) != null)
					graph.removeEdge(n1, n2);
				if (graph.getEdges(n2, n1) != null)
					graph.removeEdge(n2, n1);
				graph.addDirectedEdge(n1, n2);
			}
		
		requiredEdges = null;
	}
	
	public Map<String, Double> edgeCoefficients() {
		return edgeCoefficients;
	}
	
	public void setRequiredEdges(Set<String> edges) {
		requiredEdges = edges;
	}
	
	//-------------------- Test code ----------------------
	
	public void setPath(String p) {
		path = p;
	}
	
	public static void main(String[] args) {
		//nodes
		Set<String> nodes = new HashSet<>();
		nodes.add("A");
		nodes.add("B");
		nodes.add("C");
		
		//edges
		Set<String> edges = new HashSet<>();
		edges.add("A --> B");
		edges.add("A --> C");
		
		//test data
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("testDataSEM.txt", "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		writer.println("A" + "\t" + "B" + "\t" +"C");
		
		writer.println("1\t10\t5");
		writer.println("2\t22\t7");
		writer.println("3\t27\t9");
		writer.println("4\t40\t24");
		writer.println("5\t51\t25");
		writer.println("6\t68\t28");
		writer.println("7\t70\t35");
		writer.println("8\t88\t45");
		
		writer.close();
		
		SEM sem = new SEM(nodes, edges);
		sem.setPath("testDataSEM.txt");
		sem.estimate();
		
		System.out.println("one of the coefficients should be about 10 and the other should be about 5");
		
		for (String name : sem.edgeCoefficients.keySet())
			System.out.println(name + sem.edgeCoefficients.get(name));
		
		for (String node : sem.nameNodeMap.keySet())
		System.out.println("err : " + sem.nodesErrVar.get(node) + " mean : "+ sem.nodesMean.get(node) + " SD : "+ sem.nodesStdDev.get(node));
	}
}
