package org.processmining.causalityinference.algorithms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.causalityinference.limitGraphDeapth.GraphMouseListenerLimitedGraph;
import org.processmining.causalityinference.limitGraphDeapth.LeftPanel;
import org.processmining.causalityinference.limitGraphDeapth.LimitGraph;
import org.processmining.causalityinference.limitGraphDeapth.RightPanel;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.dataTable.AggregatedDataExtraction;
import org.processmining.framework.plugin.PluginContext;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

import edu.cmu.tetrad.graph.Node;

public class GraphVisualizer extends JPanel {
	
	/**
	 * GraphVisualizer is used for drawing bayesian networks or modifying it and creating a knowledge graph.
	 * 
	 */
	PluginContext context;
	boolean isKnowledgeGraph = false;
	boolean isEmpty = false;
	mxGraph graph;
	mxGraphComponent graphComponent;
	Map<String, Object> nodesInGraph;
	Map<Object, String> reverseNodNameMap;
	Map<String, Object> edgesInGraph;
	Map<Object, String> reverseEdgeNameMap;
	Set<String> currentNodesInGraph;
	Set<String> currentEdgesInGraph;
	LinkedList<String> initialNodeNames;
	Set<String> addedNodesSet;
	Set<String> removedNodesSet;
	Set<String> initialEdgeNames = null;
	Set<String> forbidenEdges;
	Set<String> mustEdgesSet;
	Set<String> removedNodes;
	Set<String> removedEdges;
	Set<String> forbiddenEdges;
	Set<String> mustEdges;
	Object[] confounderEdges;     // o-o
	Object[] directEdges;         // -->
	Object[] halfDirectEdges;     // o->
	Object[] biDirectionalEdges;  // <->
	Map<String, String> nodeLabels;
	mxCell dependentNode;
	Map<String, Double> edgeCoefficients = null;
	int[][] locations = null;   // when the location == null it means that we are going to do the graph from the scratch
	Map<Node, Object[][]> tables;
	Map<Node, Object[]> headers;
	Map<String, Double> coefficients;
	Map<String, Map<Integer, String>> inverseMap;
	String classAttName = "Process Workload";
	Map<String, Double> nodesStdDev;
	Map<String, Double> nodesMean;
	Map<String, Double> nodesErrVar;
	
	Map<String, String> semText;
	
	JScrollPane scrollPane;
	LeftPanelMain leftPanel;
	AggregatedDataExtraction tabularDataCreator;
	
	GraphMouseListener graphMouseListener;
	
	boolean limitDepth;
	int depth = 1;
	
	/**
	 * To display the graph in the left panel.
	 * @param context
	 * @param leftPanel
	 */
	public GraphVisualizer(PluginContext context, LeftPanelMain leftPanel) {
		this.context = context;
		this.leftPanel = leftPanel;
		
		this.nodesInGraph = new HashMap<String, Object>();
		this.reverseNodNameMap = new HashMap<Object, String>();
		this.edgesInGraph = new HashMap<String, Object>();
		this.reverseEdgeNameMap = new HashMap<Object, String>();
		
		this.setLayout(new GridLayout());
	}
	
	public void setAware(AggregatedDataExtraction tabularDataCreator) {
		this.tabularDataCreator = tabularDataCreator;
	}
	
	/**
	 * To display the graph in a new window.
	 */
	public GraphVisualizer(AggregatedDataExtraction tabularDataCreator) {   
		this.nodesInGraph = new HashMap<String, Object>();
		this.reverseNodNameMap = new HashMap<Object, String>();
		this.edgesInGraph = new HashMap<String, Object>();
		this.reverseEdgeNameMap = new HashMap<Object, String>();
		this.tabularDataCreator = tabularDataCreator;
		
		this.setLayout(new GridLayout());
	}
	
	/**
	 * To display the graph in a new window.
	 */
	public GraphVisualizer() {   
		this.nodesInGraph = new HashMap<String, Object>();
		this.reverseNodNameMap = new HashMap<Object, String>();
		this.edgesInGraph = new HashMap<String, Object>();
		this.reverseEdgeNameMap = new HashMap<Object, String>();
		
		this.setLayout(new GridLayout());
	}
	
	public void doRepresentationWork() {
		beginGraph();
		drawGraph();
	//	doLayouting();
	}
	
	public void setHeaders(Map<Node, Object[]> headers) {
		this.headers = headers;
	}
	
	public void setNodeLabels(Map<String, String> map) {
		this.nodeLabels = map;
	}
	 
	public void setTables(Map<Node, Object[][]> tables) {
		this.tables = tables;
	}
	
	public void setCoefficients(Map<String, Double> coefficients) {
		this.coefficients = coefficients;
	}
	
	public void setNodesStdDev(Map<String, Double> nodesStdDev) {
		this.nodesStdDev = nodesStdDev;
	}
	
	public void setNodesMean(Map<String, Double> nodesMean) {
		this.nodesMean = nodesMean;
	}
	
	public void setNodesErrVar(Map<String, Double> nodesErrVar) {
		this.nodesErrVar = nodesErrVar;
	}
	
	public Map<String, Double> getEdgeCoefficients() {
		return coefficients;
	}
	
	public Map<String, Object> getNodesInGraph() {
		return nodesInGraph;
	}
	
	public Map<String, Object> getEdgesInGraph() {
		return edgesInGraph;
	}
	
	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}
	
	public void setClassAttName(String attName) {
		this.classAttName = attName;
	}
	
	public void beginGraph() {
		if (this.graphComponent != null) {
			this.scrollPane.remove(this.graphComponent);
			//this.scrollPane.updateUI();
			this.remove(this.scrollPane);
			//this.updateUI();
		}
		graph = new mxGraph();
		graph.getModel().beginUpdate();
	}
	
	public void drawGraph() {
		if (isKnowledgeGraph) 
			if (initialEdgeNames == null)
				drawEmptyGraph();
			else
				drawKnowledgeGraph();	
		else
			drawSearchGraph();
	}
	
	/**
	 * this function draw a graph that is the result of the search algorithm.
	 */
	public void drawSearchGraph() {
		
		graph = new mxGraph() {
			    
			   // Implements a tooltip that shows the actual 
			   // source and target of an edge 
			   public String getToolTipForCell(Object cell) 
			   { 
				//   System.out.println("AAAAAAAAAAAA");
				   
				   if (model.isEdge(cell)) 
				   { 
					   return null;
				   } 

				   return getToolTipText(cell);
			   } 
			    
			   // Removes the folding icon and disables any folding 
			   public boolean isCellFoldable(Object cell, boolean collapse) 
			   { 
			    return false; 
			   } 
			  }; 
		Object parent = graph.getDefaultParent();
		
		if (initialNodeNames == null)
			return;
//		if (locations == null)
			locations = nodesLocation(initialNodeNames.size());
		
		graph.getModel().beginUpdate();
		
		try
		{
			//add nodes to the graph
			int i = 0;
			String color = "yellow";
//			classAttName = "Average service time Trace"; //TODO
			for (String node : initialNodeNames) {
				if (classAttName != null && node.equals(classAttName)) {
					color = "violet";
					System.out.println("class node name " + node);
				}
				else 
					color = "yellow";
				Object nodeObject = graph.insertVertex(parent, node, node, locations[i][0]+10, locations[i][1]+10, 200, 60, "fillColor="+color+";fontSize=12");
				this.nodesInGraph.put(node, nodeObject);
				this.reverseNodNameMap.put(nodeObject, node);
				System.out.println("node name " + i + " " + node);
				i++;
			}	
			
//			System.out.println(initialNodeNames.size());
			//add edges to the bayesian network to the graph
			Set<Object> confounderEdges = new HashSet<Object>();
			Set<Object> directedEdges = new HashSet<Object>();
			Set<Object> halfDirectedEdges = new HashSet<Object>();
			Set<Object> noDirectionEdges = new HashSet<Object>();
			
			if (initialEdgeNames != null) {
				for (String edge : initialEdgeNames) {
					String label = "";
					if (coefficients != null && coefficients.keySet().contains(edge.toString()))
						label = coefficients.get(edge.toString()).toString();
					Object[] edgeInfo = extractEdgeEndpointsAndType(edge);
					Object arc = graph.insertEdge(parent, null, label, nodesInGraph.get(edgeInfo[0]), nodesInGraph.get(edgeInfo[1]), "strokeColor=blue;strokeWidth=2;fontSize=12");
					
					this.edgesInGraph.put(edge, arc);
					this.reverseEdgeNameMap.put(arc, edge);
					
					if ((int)edgeInfo[2] == 0)
						directedEdges.add(arc);
					else if ((int)edgeInfo[2] == 1)
						confounderEdges.add(arc);
					else if ((int)edgeInfo[2] == 2)
						halfDirectedEdges.add(arc);
					else if ((int)edgeInfo[2] == 3)
						noDirectionEdges.add(arc);
				}
			}
			
			graphComponent = new mxGraphComponent(this.graph);
			graphComponent.setToolTips(true);
			graphComponent.setToolTipText("ciao");

			
			if (directedEdges.size() > 0) {
				Object[] edges = turnToArray(directedEdges);
				setStyleDirected(edges);
			}
			
			if (confounderEdges.size() > 0) {
				Object[] edges = turnToArray(confounderEdges);
				setStyleConfounder(edges);
			}
			
			if (halfDirectedEdges.size() > 0) {
				Object[] edges = turnToArray(halfDirectedEdges);
				setStyleHalfDirected(edges);
			}
			
			if (noDirectionEdges.size() > 0) {
				Object[] edges = turnToArray(noDirectionEdges);
				setStyleNoKnownDirection(edges);
			}
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	private void setSemText() {
		semText = new HashMap<>();
		Map<String, LinkedList<String[]>> eqs = new HashMap<>();
		for (String name : initialNodeNames) {
			LinkedList<String[]> list = new LinkedList<>();
			eqs.put(name, list);
		}
		
		for (String edge : initialEdgeNames) {
			Object[] edgeInfo = extractEdgeEndpointsAndType(edge);
			String[] array = new String[2];
			array[0] = edgeInfo[0].toString();
			array[1] = coefficients.get(edge.toString()).toString();
			eqs.get(edgeInfo[1]).add(array);
		}
		
		for (String name : eqs.keySet()) {
			String eq = name + " = ";
			if (eqs.get(name) != null && !eqs.get(name).isEmpty())
				for (String[] arr : eqs.get(name)) {
					eq = eq + arr[0] + " * " + arr[1] + " + ";
				}
			eq = eq.substring(0, eq.length()-2);
			semText.put(name,  eq + "+ noise");
		}
		
		Map<String,String> newSemText = new HashMap<>();
		
		if (!semText.isEmpty()) {
			for (String attName : semText.keySet())
				if (semText.get(attName).length() == (attName.length() + 8))
					newSemText.put(attName, attName + " = noise");
				else
					newSemText.put(attName, semText.get(attName));
			
			semText = newSemText;
		}
	}
	
	public void writeSem() {
		setSemText();
		try{
	        Writer output = null;
	        File file = new File("SEM.txt");
	        output = new BufferedWriter(new FileWriter(file));
	        for (String name : semText.keySet()) {
	        	output.write(semText.get(name));
	        	output.write(System.getProperty( "line.separator" ));
	        }

	        output.close();
	        System.out.println("File has been written");

	    }catch(Exception e){
	        System.out.println("Could not create file");
	    }
	}
	
	
	public String getToolTipText(Object cell) {
		
		String name = reverseNodNameMap.get(cell);
/**		String text = new String("hello");
		
		Parameters params = leftPanel.params;
		if (leftPanel.params.getSituationType().equals(SituationType.PL)) 
			this.tabularDataCreator = leftPanel.rightPanelCausalityGraph.tabularDataCreator;
		else 
			this.tabularDataCreator = leftPanel.rightPanelCreatTable.tabularDataCreator;

		
		if (tabularDataCreator == null)
			System.out.println(" --2 ");
		
		if (name.equals(tabularDataCreator.classAttributeName())) {
		   
		   if (params.getSituationType().equals(SituationType.TS)) // trace situation
			   text = "Situation type : trace  **  Independent Attribute : " + tabularDataCreator.classAttributeName();
		   else if (params.getSituationType().equals(SituationType.CS))
			   text = "Situation type : choice  **  Independent Attribute : " + tabularDataCreator.classAttributeName();
		   else  if (params.getSituationType().equals(SituationType.ES)){
			   String str = new String();
			   if (params.getGrouperAttName().equals(ActivityGrouperAttName.D)) {
				   str = "Duration : [ " + params.getMinThreshold() + ", " + params.getMaxThreshold() + " ]";
			   } else {
				   if (params.getGrouperAttName().equals(ActivityGrouperAttName.AN)) {
					   str = "Activity name(s) : {"; 
				   } else if (params.getGrouperAttName().equals(ActivityGrouperAttName.R))
					   str = "Activity with resource(s) : {"; 
				   else if (params.getGrouperAttName().equals(ActivityGrouperAttName.TS))
					   str = "Timestamp in [" + params.getMinThreshold() + ", " + params.getMaxThreshold() + " ] at {";
				   
				   for (String s : params.getGrouperAttValues())
					   str = str + s + ",";
				   
				   str = str.substring(0, str.length()-1) + "} ";
			   }
				  
			   text = "Situation type : " + params.getGrouperAttName() + "  **  Values : " + str + "  **  Independent Attribute : " + tabularDataCreator.classAttributeName();
		   }
		} else {
		   Collection<String> actNames = params.getActivitiesToConsider();
		   if (actNames != null)
			   for (String actName : actNames) {
				   if (name.contains(actName)) {
					   text = "Activity name : " + actName + "  **  " + "Attribute : " + name.substring(actName.length() + 1, name.length());
					   return text;
				   }
			   }
		   if (name.length()>6 && name.substring(0, 7).equals("Choice_"))
			   text = "Choice attribute  **  Choice place : " + name.substring(7, name.length());
		   
		   text = name;
		}  
		
		return text; */
		
		return name;
	}

	/**
	 * this function draw an empty graph that is the result of the search algorithm.
	 */
	public void drawEmptyGraph() {
		
		graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		if (initialNodeNames == null)
			return;
		
		int[][] locations = nodesLocation(initialNodeNames.size());
		
		graph.getModel().beginUpdate();
		try
		{
			//add nodes to the graph
			int i = 0;
			for (String node : initialNodeNames) {
				Object nodeObject = graph.insertVertex(parent, node, node, locations[i][0], locations[i][1], 200, 60, "fillColor=yellow"+";fontSize=12");
				this.nodesInGraph.put(node, nodeObject);
				this.reverseNodNameMap.put(nodeObject, node);
				i++;
			}					
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	/**
	 * This function draws a modified graph by the user including the forbidden and must edges.
	 */
	public void drawKnowledgeGraph() {
		
		graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		graph.getModel().beginUpdate();
		if (initialNodeNames == null)
			return;
		
		try
		{
			//add nodes to the graph
			int[][] locations = new int [5][2];
			if (initialNodeNames != null)
				locations = nodesLocation(initialNodeNames.size());
			
			int i = 0;
			for (String node : initialNodeNames) {
				Object nodeObject = graph.insertVertex(parent, node, node, locations[i][0], locations[i][1], 200, 60, "fillColor=yellow"+";fontSize=12");
				this.nodesInGraph.put(node, nodeObject);
				this.reverseNodNameMap.put(nodeObject, node);
				i++;
			}	
			
			/**	
			//adding edges to the bayesian network to the graph
			Set<Object> confounderEdges = new HashSet<Object>();
			Set<Object> directedEdges = new HashSet<Object>();
			Set<Object> halfDirectedEdges = new HashSet<Object>();
			Set<Object> noDirectionEdges = new HashSet<Object>();
			
		
			for (String edge : getCurrentEdges()) {
				Object[] edgeInfo = extractEdgeEndpointsAndType(edge);
				Object arc = graph.insertEdge(parent, null, "", nodesInGraph.get(edgeInfo[0]), nodesInGraph.get(edgeInfo[1]), "strokeColor=blue;strokeWidth=2;fontSize=12");
				
				if ((int)edgeInfo[2] == 0)
					directedEdges.add(arc);
				else if ((int)edgeInfo[2] == 1)
					confounderEdges.add(arc);
				else if ((int)edgeInfo[2] == 2)
					halfDirectedEdges.add(arc);
				else if ((int)edgeInfo[2] == 3)
					noDirectionEdges.add(arc);
			}
			
			if (directedEdges.size() > 0) {
				Object[] edges = turnToArray(directedEdges);
				setStyleDirected(edges);
			}
			
			if (confounderEdges.size() > 0) {
				Object[] edges = turnToArray(confounderEdges);
				setStyleConfounder(edges);
			}
			
			if (halfDirectedEdges.size() > 0) {
				Object[] edges = turnToArray(halfDirectedEdges);
				setStyleHalfDirected(edges);
			}
			
			if (noDirectionEdges.size() > 0) {
				Object[] edges = turnToArray(noDirectionEdges);
				setStyleNoKnownDirection(edges);
			}  */
			
			//adding forbidden edges to the graph
			if (forbiddenEdges != null)
				for (String edge : forbiddenEdges) {
					Object[] edgeInfo = extractEdgeEndpointsAndType(edge);
					graph.insertEdge(parent, null, "", nodesInGraph.get(edgeInfo[0]), nodesInGraph.get(edgeInfo[1]), 
						"strokeColor=red;strokeWidth=2;fontSize=16");
				}
			
			//adding must edges to the graph
			if (mustEdges != null)
				for (String edge : mustEdges) {
					Object[] edgeInfo = extractEdgeEndpointsAndType(edge);
					graph.insertEdge(parent, null, "", nodesInGraph.get(edgeInfo[0]), nodesInGraph.get(edgeInfo[1]), 
						"strokeColor=green;strokeWidth=2;fontSize=16");
				}
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	}
	
	/**
	 * It returns the set of edges in the graph in the current moment
	 * @param 
	 * @return
	 */
	public Set<String> getCurrentEdges() {
		
		Set<String> tempEdges = new HashSet<String>();
		for (String edge : initialEdgeNames) 
			tempEdges.add(edge);
		
		for (String edge : initialEdgeNames) {
			// removing the forbidden edges from the edges of the graph
			Object[] info = extractEdgeEndpointsAndType(edge);
			for (String forbiddenEdge : forbiddenEdges) {
				Object[] fInfo = extractEdgeEndpointsAndType(forbiddenEdge);
				if ((info[0].equals(fInfo[0]) && info[1].equals(fInfo[1])) || (info[0].equals(fInfo[1]) && info[1].equals(fInfo[0])))
					tempEdges.remove(edge);
			}
			//remove must edges
			for (String mustEdge : mustEdges) {
				Object[] mInfo = extractEdgeEndpointsAndType(mustEdge);
				if ((info[0].equals(mInfo[0]) && info[1].equals(mInfo[1])) || (info[0].equals(mInfo[1]) && info[1].equals(mInfo[0])))
					tempEdges.remove(edge);
			}
		}
		return tempEdges;
	}
	
	/**
	 * convert a set of objects to an array of abjects
	 * @param obgs
	 * @return
	 */
	public Object[] turnToArray(Set<Object> obgs) {
		
		if (obgs.size() == 0) 
			return null;
		
		Object[] array = new Object[obgs.size()];
		int j = 0;
		for (Object o : obgs) {
			array[j] = o;
			j++;
		}
		
		return array;
	}
	
	/**
	 * add a node to the graph
	 */
	public void addNode(String nodeName) {
		removedNodes.remove(nodeName);
		currentNodesInGraph.add(nodeName);
	}
	
	/**
	 * remove a node to the graph
	 */
	public void removeNode(String nodeName) {
		removedNodes.add(nodeName);
		currentNodesInGraph.remove(nodeName);
	}
	
	/**
	 * add an edge to the graph
	 */
	public void addEdge(String edgeName) {
		if (removedEdges.contains(edgeName))
			removedEdges.remove(edgeName);
		currentEdgesInGraph.add(edgeName);
		//TODO    current edge is extra
	}
	
	/**
	 * remove an edge to the graph
	 */
	public void removeEdge(String edgeName) {
		removedEdges.add(edgeName);
		//TODO cuurent edge and node sets are extra
		currentEdgesInGraph.remove(edgeName);
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
		
//		int dim;
		
//		if (height < width) 
//			dim = height;
//		else
//			dim = width;
		
//		int radi = (dim - 100)/2;
//		int centerX = width/2 - 100;
//		int centerY = height/2;
		
//		for (int i = 0; i < n; i++) {
//			locations[i][0] = (int) (centerX + radi * Math.round( Math.cos(2 * Math.PI * i / n)));
//			locations[i][1] = (int) (centerY + radi * Math.round( Math.sin(2 * Math.PI * i / n)));
//		}
		
		int raddi = 200;
		double theta = 2 * Math.PI / n;
		for (int i = 0; i < n; ++i) {
			locations[i][0] = (int) (Math.round(raddi * Math.cos(theta * i)) + 200);
			locations[i][1] = (int) (Math.round(raddi * Math.sin(theta * i)) + 200);
		}
		
		return locations;
	}
	
	/**
	 * 
	 * Each input edge is a string in one of these forms:
	 * v1 --> v2    (edge type 0)
	 * v1 <-> v2    (edge type 1)
	 * v1 o-> v2    (edge type 2)
	 * v1 o-o v2    (edge type 3)
	 * 
	 * This function returns an Object[3] that contains v1, v2, and the edge type
	 * of the input edge.
	 */
	public Object[] extractEdgeEndpointsAndType(String edge) {
		Object[] edgeInfo = new Object[3];
		
		String[] parts = breakEdge(edge);//edge.split(" ");
		edgeInfo[0] = parts[0].subSequence(0, parts[0].length() - 1); // input node of the edge
		edgeInfo[1] = parts[1].subSequence(1, parts[1].length()); // output node of the edge
		if (edge.contains("-->")) {
			int type = 0;
			edgeInfo[2] = type;
		}
		if (edge.contains("<->")) {
			int type = 1;
			edgeInfo[2] = type;
		}
		if (edge.contains("o->")) {
			int type = 2;
			edgeInfo[2] = type;
		}
		if (edge.contains("o-o")) {
			int type = 3;
			edgeInfo[2] = type;
		}
		
		return edgeInfo;
	}
	
	public Map<Object, String> getReverseEdgeNameMap() {
		return reverseEdgeNameMap;
	}

	
	private String[] breakEdge(String edge) {
		if (edge.contains("o-o"))
			return edge.split("o-o");
		if (edge.contains("-->"))
			return edge.split("-->");
		if (edge.contains("<->"))
			return edge.split("<->");
		if (edge.contains("o->"))
			return edge.split("o->");
		return null;
	}

	public void addGraphToView() {
		this.graphComponent = new mxGraphComponent(this.graph);
		this.graphComponent.setToolTips(true); 
		graphComponent.setToolTipText("ciao");
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		this.scrollPane.setPreferredSize(new Dimension(800, 800));
		
		this.scrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		this.scrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
		
//		if (leftPanel.isLimited == true) {
//			leftPanel.removeAll();
//			leftPanel.add(this);
//			leftPanel.updateUI();
//		} else {
			leftPanel.bayesianNetworkVisualizer.removeAll();
			leftPanel.bayesianNetworkVisualizer.add(this);
			leftPanel.bayesianNetworkVisualizer.updateUI();
//		}
		
//--		boolean knowledge = false;
//--		if (isEmpty || isKnowledgeGraph)
//--			knowledge = true;
		graphMouseListener = new GraphMouseListener(this.context, this.leftPanel, this);
		graphMouseListener.setInverseNodNameMap(reverseNodNameMap);
		this.graphComponent.getGraphControl().addMouseListener(graphMouseListener);
	}
	
	public void addGraphToView(boolean b, LeftPanel leftPanel, RightPanel rightPanel, String classAttName) {
		this.graphComponent = new mxGraphComponent(this.graph);
		this.graphComponent.setToolTips(true); 
		graphComponent.setToolTipText("ciao");
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		this.scrollPane.setPreferredSize(new Dimension(800, 800));
		
		this.scrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		this.scrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
		
		GraphMouseListenerLimitedGraph graphMouseListener = new GraphMouseListenerLimitedGraph(leftPanel, rightPanel, classAttName);
//		graphMouseListener.setInverseNodNameMap(reverseNodNameMap);
		this.graphComponent.getGraphControl().addMouseListener(graphMouseListener);
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
		
	public void addGraphToView(mxGraph graph) {
		this.graphComponent = new mxGraphComponent(graph);
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		this.scrollPane.setPreferredSize(new Dimension(800, 800));
		
		this.scrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		this.scrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
	}
	
	/**
	 * defining the style for the confounder edges.
	 */
	public void setStyleConfounder(Object[] edges) {
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_STARTARROW, mxConstants.ARROW_CLASSIC );
	}
	
	/**
	 * defining the style for those edges that indicate a possible causal relationship.
	 */
	public void setStyleDirected(Object[] edges) {
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
	}
	
	/**
	 * defining the style for those edges (Xo->Y) that mention there is a connection
	 * Y is not a cause of X, but we don't know if X-->Y is true or X<->Y.
	 *  o-o
	 * @param edges
	 */
	public void setStyleHalfDirected(Object[] edges) {
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
	}
	
	/**
	 * defining the  for those edges that mention there is a connection
	 * between the two nodes in its end points but the direction is not known.
	 *  o-o
	 * @param edges
	 */
	public void setStyleNoKnownDirection(Object[] edges) {
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
		mxStyleUtils.setCellStyles(graphComponent.getGraph().getModel(), 
	    	    edges, mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
	}
	
	public void setNodes(LinkedList<String> nodes) {
		this.initialNodeNames = nodes;
	}
	
	public void setNodes(Set<String> nodes) {
		LinkedList<String> list = new LinkedList<>();
		for (String name : nodes)
			list.add(name);
		
		this.initialNodeNames = list;
	}
	
	public void setEdges(Set<String> edges) {
		this.initialEdgeNames = edges;
	}
	
	/**
	 * this function removes all the solitary nodes from the graph,
	 * nodes adjacent to no edge.
	 */
	public void removeStandAloneNodesFromTheGraph() {
		for (String node : nodesInGraph.keySet()) {
			boolean isAlone = true;
			for (String edge : edgesInGraph.keySet()) {
				Object[] info = extractEdgeEndpointsAndType(edge);
				if (info[0].equals(node) || info[1].equals(node)) {
					isAlone = false;
					break;
				}
			}
			if (isAlone) {
				Object n = nodesInGraph.get(node);
				nodesInGraph.remove(node);
				reverseNodNameMap.remove(n);
				removedNodes.add(node);
			}
		}
	}
	
	
	public void removeNode(Object cell) {
		if (!nodesInGraph.containsValue(cell))
			return;
		String nodeName = reverseNodNameMap.get(cell);
		
	//	for (String node : nodesInGraph.keySet())
	//		if (nodesInGraph.get(node).equals(cell))
	//		{
	//			nodeName = node;
	//			break;
	//		}
		
		// removing adjacent edges
		Set<String> edges = new HashSet<String>();
		for (String edge : edgesInGraph.keySet()) {
			Object[] info = extractEdgeEndpointsAndType(edge);
			if (info[0].equals(nodeName) || info[1].equals(nodeName)) {
				edges.add(edge);
			}
		}
		
		if (edges.size() > 0) 
			for (String edge : edges) {
				Object o = edgesInGraph.get(edge);
				edgesInGraph.remove(edge);
				reverseEdgeNameMap.remove(o);
				removedEdges.add(edge);
			}
		
		// remove the node itself
		Object n = nodesInGraph.get(nodeName);
		nodesInGraph.remove(nodeName);
		reverseNodNameMap.remove(n);
		removedNodes.add(nodeName);
	}
	
	public String nodeName(Object cell) {
		return reverseNodNameMap.get(cell);
	}
	
	public void setIsKnowledgeGraph(boolean b) {
		this.isKnowledgeGraph = b;
	}
	
	public mxGraph getGraph() {
		return graph;
	}
	
	public void setIsEmpty(boolean b) {
		this.isEmpty = b;
	}  
	
	public static void main(String args[]) {
		
	}
	
	public void addForbiddenEdge(String firstNode, String secondNode) {
		if (mustEdges != null && mustEdges.contains(firstNode + " --> " + secondNode))
			mustEdges.remove(firstNode + " --> " + secondNode);
		
		Object arc = graph.insertEdge(graph.getDefaultParent(), null, "", nodesInGraph.get(firstNode), nodesInGraph.get(secondNode), "strokeColor=red;strokeWidth=2;fontSize=12");
		if  (forbiddenEdges == null)
			forbiddenEdges = new HashSet<String>();
		forbiddenEdges.add(firstNode + " --> " + secondNode);
	}
	
	public void setForbiddenEdges(Set<String> f) {
		forbiddenEdges = f;
	}
	
	public void setRequiredEdges(Set<String> m) {
		mustEdges = m;
	}
	
	public Set<String> getForbiddenEdges() {
		return forbiddenEdges;
	}
	
	public Set<String> getRequiredEdges() {
		return mustEdges;
	}
	
	/**
	 * This methid empty the knowledge forbidden and required edges.
	 */
	public void clearKnowledge() {
		forbiddenEdges = new HashSet<String>();
		mustEdges = new HashSet<String>();
	}
	
	public void addRequiredEdge(String firstNode, String secondNode) {
		if (forbiddenEdges != null)
			if (forbiddenEdges.contains(firstNode + " --> " + secondNode))
				forbiddenEdges.remove(firstNode + " --> " + secondNode);
		
		
		if (mustEdges == null) {
			mustEdges = new HashSet<String>();
		}
		
		String edgeName = firstNode + " --> " + secondNode;
		if (!mustEdges.contains(edgeName)) {
			mustEdges.add(edgeName);
			Object arc = graph.insertEdge(graph.getDefaultParent(), null, "", nodesInGraph.get(firstNode), nodesInGraph.get(secondNode), "strokeColor=green;strokeWidth=2;fontSize=12");
		}
	//	reverseEdgeNameMap.put(arc, edgeName);
	}
	
	public void addForbiddenEdgesByLog(Map<String, Set<String>> forbiddenDirections) {
		if (forbiddenDirections.isEmpty())
			return;
		
		for (String first : forbiddenDirections.keySet())
			if (!forbiddenDirections.get(first).isEmpty())
				for (String second : forbiddenDirections.get(first))
					addForidenDirectionByLog(first, second);	
	}
	
	public void addForidenDirectionByLog(String first, String second) {
		for (String from : nodesInGraph.keySet())
			if (from.length() >= first.length() && from.subSequence(0, first.length()).equals(first.replace(" ", "_")))
				for (String to : nodesInGraph.keySet())
					if (to.length() >= second.length() && to.subSequence(0, second.length()).equals(second.replace(" ", "_")))
						addForbiddenEdge(from, to);
	}
	
	public void setInverseAttValueMap(Map<String, Map<Integer, String>> inverseMap) {
		this.inverseMap = inverseMap;
	}
	
	/**
	 * this class is writing the knowlendge given by the user in the tetradCMD knowledge format, in a txt file.
	 */
/**	public void intermadiateFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("bin\\DataTableOneHot.txt", "ASCII");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// creating the header of the table
		String header = new String();
		Set<String> attNames = attributeType.keySet();
		for (String attName : attNames) {
			if (attName.equals(classAttName)) {
				header = header + attName;
				attributeNames.add(attName);
			}
			else 
				if (attributeType.get(attName).equals(Type.BOOLEAN)) {
					header = header + attName + "_" + "true" + '\t' + attName + "_" + "false";
					attributeNames.add(attName + "_" + "true");
					attributeNames.add(attName + "_" + "false");
				} 
				else if (attributeType.get(attName).equals(Type.LITERAL)) {
					for (String str : literalValues.get(attName)) {
						
						header = header + attName + "_" + str + "\t";
						attributeNames.add(attName + "_" +str);
					}
				}
				else
					header = header + attName + '\t';
		}
//		header = header.substring(0, header.length() - 1);
		writer.println(header);
		
		//creating the body of the table ; i.e. inserting a row for each 
		for(Map<String, Object> instance : instanceSet) {
			String row = new String();
			row = convertToRow(instance);
			writer.println(row);
		}
		writer.close();
		
		if (!instanceSet.isEmpty()) 
			data.setTableIsCreated(true);
		else
			data.setTableIsCreated(false);
	}    
	*/
	
	public void setEdgeCoefficients(Map<String, Double> c) {
		this.coefficients = c;
	}
}

class GraphMouseListener implements MouseListener {
	PluginContext context;
	CausalityGraph model;
	ModelVisualizerStarStar causalGraphVisualizer;
	ModelVisualizerStarStar visualizer;
//	RightPanelTable rightPanelCreatTable;
	RightPanelCausalityGraph rightPanelCausalityGraph;
	String firstNode = null;
	LeftPanelMain leftPanel;
	GraphVisualizer graphVisualizer = null;
	Map<Object, String> reverseNodNameMap;
	Map<String, Map<Integer, String>> inverseValueMap;
	AggregatedDataExtraction tabularDataCreator;
	Parameters params;
	 
	public GraphMouseListener(PluginContext context, LeftPanelMain leftPanel, GraphVisualizer graphVisualizer) {
		this.context = context;
		this.model = leftPanel.model;
		this.causalGraphVisualizer = leftPanel.bayesianNetworkVisualizer;
		this.params = leftPanel.params;
		this.leftPanel = leftPanel;
		this.graphVisualizer = graphVisualizer;
		this.visualizer = causalGraphVisualizer;
		this.tabularDataCreator = params.getDataTableCreator();
		this.rightPanelCausalityGraph = leftPanel.rightPanelCausalityGraph;

		if (params.getDataTableCreator().getInverseAttValueMap() != null)
			this.inverseValueMap = params.getDataTableCreator().getInverseAttValueMap();
		
	}
	 
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		
		Object cell = this.graphVisualizer.graphComponent.getCellAt(e.getX(), e.getY());
		
		if (this.graphVisualizer.nodesInGraph.containsValue(cell)) {
			firstNode = this.visualizer.graphVisualizer.nodeName(cell);
//			System.out.println(cell.toString());
		} else
			firstNode = null;
	}

	public void mouseReleased(MouseEvent e) {
		Object cell = this.graphVisualizer.graphComponent.getCellAt(e.getX(), e.getY());
		Map<String, Object> temp = this.visualizer.graphVisualizer.edgesInGraph;
		
		if (this.graphVisualizer.edgesInGraph.containsValue(cell)) {
			if (rightPanelCausalityGraph.delete) {
				this.graphVisualizer.edgesInGraph.remove(cell);
				this.visualizer.doRepresentationWork();
				this.visualizer.addGraphToView();
			}
		}
		else if (this.graphVisualizer.nodesInGraph.containsValue(cell)) {
			
			String secondNode = this.visualizer.graphVisualizer.nodeName(cell);
			if (firstNode.equals(secondNode) && graphVisualizer.limitDepth) {
				limitGraphDeapth(firstNode, leftPanel);
				return; 
			}
//			System.out.println(rightPanelCausalityGraph.delete);
			System.out.println(rightPanelCausalityGraph.forbiddenEdge);
			System.out.println(rightPanelCausalityGraph.requiredEdge);
			if (rightPanelCausalityGraph.delete) {
				this.graphVisualizer.removeNode(cell);	
				this.graphVisualizer.doRepresentationWork();
				this.graphVisualizer.addGraphToView();
			} else if (rightPanelCausalityGraph.forbiddenEdge && !firstNode.equals(secondNode)) {
				if (firstNode != null) {
					this.graphVisualizer.addForbiddenEdge(firstNode, secondNode);
					rightPanelCausalityGraph.knowledge.setForbidden(firstNode, secondNode);
				}
			} else if (rightPanelCausalityGraph.requiredEdge && !firstNode.equals(secondNode)) {
				if (firstNode != null)  {
					this.graphVisualizer.addRequiredEdge(firstNode, secondNode);
					System.out.println(rightPanelCausalityGraph.knowledge);
					rightPanelCausalityGraph.knowledge.setRequired(firstNode, secondNode);
				}
			} else if (firstNode.equals(secondNode) && model.getHeaders() != null && model.getTables() != null && leftPanel.rightPanelCausalityGraph.dataTypeComboBox.getSelectedItem().toString().equals("Discrete")) {
				if (! model.getEstimationIsValid())
					return;
				if (leftPanel.rightPanelCausalityGraph.dataTypeComboBox.getSelectedItem().toString().equals("Discrete")) {
				//	System.out.println(cell.toString());
					if (model.getInterventionEffectsOnClass().get(reverseNodNameMap.get(cell)) == null) {
						showPupup(null, null, null);
					} else {
						String[] header = createHeader(reverseNodNameMap.get(cell), model.getClassCategories());
						Object[][] body = createBody(model.getInterventionEffectsOnClass().get(reverseNodNameMap.get(cell)));
						replaceIntCodesWithStringValues(header, body, inverseValueMap);
						SituationFeatureTableVisualizer table = new SituationFeatureTableVisualizer(header, body, null, params.getDataTableCreator().classAttributeName(), params.getDataTableCreator().getAttTypes());
						showPupup(table, reverseNodNameMap.get(cell), graphVisualizer.inverseMap);
					}
				}  else
					System.out.println(" 867 GV ??? ");
			} else if (!leftPanel.rightPanelCausalityGraph.dataTypeComboBox.getSelectedItem().toString().equals("Discrete")){
				if (! model.getEstimationIsValid())
				return;
				
				showPupup(secondNode);
				System.out.println(" ??? ");
			}
		}	
	}
	
	private void limitGraphDeapth(String nodeName,  LeftPanelMain leftPanel) {
		LimitGraph lg = new LimitGraph(graphVisualizer.nodesInGraph.keySet(), graphVisualizer.edgesInGraph.keySet(), nodeName, tabularDataCreator, leftPanel.params);	
		//TODO .rightPanelCausalityGraph.tabularDataCreator
	}

	public void replaceIntCodesWithStringValues(String[] header, Object[][] body, Map<String, Map<Integer, String>> inverseValueMap) {
		String classAttName = model.getClassAttName();
		for (int i = 0; i < header.length; i++) {
			Map<Integer, String> classCategoryInverseMap = inverseValueMap.get(classAttName);
			if (!inverseValueMap.keySet().contains(header[i])) {
				System.out.println(header[i]);
				System.out.println(classCategoryInverseMap.get(Integer.parseInt(header[i])));
				header[i] = (classCategoryInverseMap.get(Integer.parseInt(header[i])));
			}
			else
				for (int j = 0; j < body.length; j++)
					body[j][i] = (inverseValueMap.get(header[i]).get(Integer.parseInt(body[j][i].toString())));
		}
		
	}
	
	public Object[][] createBody(Map<String, double[]> probs) {
		int num = 0;
		for (String s : probs.keySet()) {
			num = probs.get(s).length;
			break;
		}
		Object[][] body = new Object[probs.size()][num + 1];
		int idx = 0;
		for (String s : probs.keySet()) {
			double[] values = probs.get(s);
			body[idx][0] = s;
			int idx2 = 0;
			for (int i = 0; i < num; i++)
				body[idx][i+1] = values[i];
			idx++;
		}
		return body;
	}
	
	public String[] createHeader(String Xname, String[] categoriesOfY) {
		String[] header = new String[categoriesOfY.length + 1];
		header[0] =Xname;
		for (int i = 1 ; i < header.length; i++ )
			header[i] = categoriesOfY[i-1];
		
		return header;
	}
	
	public String[] stringArray(Object[] arr) {
		String[] newArray = new String[arr.length];
		for (int i = 0; i < arr.length; i++) {
			newArray[i] = arr[i].toString();
		}
		return newArray;
	}
	
	public void setInverseNodNameMap(Map<Object, String> reverseNodNameMap) {
		 this.reverseNodNameMap = reverseNodNameMap;
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
		
/**		JPanel p = new JPanel();
		p.setLayout(null);
		p.setBorder(BorderFactory.createTitledBorder(
		        BorderFactory.createEtchedBorder(), name, TitledBorder.CENTER,
		        TitledBorder.TOP));
		
		JScrollPane sPane = new JScrollPane();
	    sPane.setPreferredSize(new Dimension(500, 550));
	    sPane.setMinimumSize(new Dimension(500, 550));
	    JTable t = table.getTableForPupup();
	    t.setPreferredSize(new Dimension(400, 450));
	    t.setMinimumSize(new Dimension(400, 450));
	    sPane.getViewport().add(t);
		p.add(sPane);
		sPane.setVisible(true);
		JOptionPane.showMessageDialog(null, p);
//    	JOptionPane.showConfirmDialog(null, 
//    			p,name,JOptionPane.OK_OPTION); */
	}
	
	public void showPupup(String nodeName) {
		if (model.getNodeCoefficients().get(nodeName) == null)
			return;
		
		JFrame frame = new JFrame(nodeName);
		frame.setLayout(new GridLayout(10,1));
		frame.setSize(new Dimension(300, 500));
		if (!nodeName.equals(model.getClassAttName())) {
			JLabel equationY = new JLabel("  Y = " + model.getClassAttName());
			frame.getContentPane().add(equationY);
			JLabel equationX = new JLabel("  X = " + nodeName);
			frame.getContentPane().add(equationX);
			JLabel parents = new JLabel("  PA(Y) = those parents of Y that are not on any path from X to Y ");
			frame.getContentPane().add(parents);
			JLabel equation = new JLabel("  Y = " + model.getNodeCoefficients().get(nodeName) + " * X + error + f (PA(Y))" );
			frame.getContentPane().add(equation);
		}
		
		Map<String, Double> mean = rightPanelCausalityGraph.tabularDataCreator.getMean();
		Map<String, Double> median = rightPanelCausalityGraph.tabularDataCreator.getMedian();
		Map<String, Double> variance = rightPanelCausalityGraph.tabularDataCreator.getVariance();
		Map<String, Double> stdDev = rightPanelCausalityGraph.tabularDataCreator.getStdDev();
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
		}
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
		frame.show();  
	}
}
