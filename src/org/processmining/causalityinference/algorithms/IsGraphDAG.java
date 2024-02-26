package org.processmining.causalityinference.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

public class IsGraphDAG {

    private Map<Integer, List<Integer>> adjacencyList;
    private Set<String> nodes;
    private Set<String> edges;
    private Graph graph;
    
    public IsGraphDAG(Set<String> nodes, Set<String> edges) {
    	
    	this.nodes = nodes;
    	this.edges = edges;
    }
    
    public IsGraphDAG(Graph graph) {
    	
    	this.graph = graph;
    	List<Node> graphNodes = graph.getNodes();
    	nodes = new HashSet<String>();
    	for (Node n : graphNodes) {
    		nodes.add(n.getName());
    	}
    	edges = new HashSet<String>();
    	for (Edge e : graph.getEdges()) {
    		edges.add(e.toString());
    	}
    }
 
    public boolean checkIsGraphDAG() {
    	Map<String, Integer> nodesCode = new HashMap<String, Integer>();
    	
    	// assigning an integer number to each node
    	Integer count = 1;
    	for (String node : nodes) {
    		nodesCode.put(node, count);
    		count++;
    	}
    	
    	GraphLinkedList(nodes.size());
    	
    	//adding edges to the graph
    	for (String edge : edges) {
    		Object[] info =  extractEdgeEndpointsAndType(edge);
    		System.out.println("edge");
    		System.out.println(info[0]);
    		System.out.println(info[1]);
    		if ((int)info[2] == 0) 
    			setEdge(nodesCode.get(info[0]), nodesCode.get(info[1]));
    		else
    			return false;
    	}
    	
    	if (checkDAG())
    		return true;
    	
    	return false;
    } 
    
    public Object[] extractEdgeEndpointsAndType(String edge) {
		Object[] edgeInfo = new Object[3];
		
		String[] parts = new String[2];
		String eType = new String();
				
		if (edge.contains(" o-o ")) {
			parts = edge.split(" o-o ");
			edgeInfo[2] = 3;
		}
		
		if (edge.contains(" o-> ")) {
			parts = edge.split(" o-> ");
			edgeInfo[2] = 2;
		}
		
		if (edge.contains(" <-> ")) {
			parts = edge.split(" <-> ");
			edgeInfo[2] = 1;
		}
		
		if (edge.contains(" --> ")) {
			parts = edge.split(" --> ");
			edgeInfo[2] = 0;
		}
		
		edgeInfo[0] = parts[0];  // input node of the edge
		edgeInfo[1] = parts[1];  // output node of the edge
		
		
		
		return edgeInfo;
	}
    
    public String getAttName(String name) {
    	String[] array = new String[nodes.size()];
    	int i = 0;
    	for (String attName : nodes) {
    		array[i] = attName;
    		i++;
    	}
    	
    	Arrays.stream(array)
        .sorted(Comparator.comparingInt(String::length))
        .forEach(a -> System.out.print(a + " "));
    	
    	for (int j = array.length-1; j >= 0; j--) 
    		if (name.contains(array[j]))
    			return array[j];
    		

    	return name;
    		
    }
    
    public void GraphLinkedList(int v)
    {
        adjacencyList = new HashMap<Integer, List<Integer>>();
        for (int i = 1; i <= v; i++)
            adjacencyList.put(i, new LinkedList<Integer>());
    }
 
    public void setEdge(int from, int to)
    {
        List<Integer> dls = adjacencyList.get(from);
        dls.add(to);
    }
 
    public List<Integer> getEdge(int to)
    {
        if (to > adjacencyList.size())
        {
            System.out.println("The vertices does not exists");
            return null;
        }
        return adjacencyList.get(to);
    }
 
    public boolean checkDAG()
    {
        Integer count = 0;
        Iterator<Integer> iteratorI = this.adjacencyList.keySet().iterator();
        Integer size = this.adjacencyList.size() - 1;
        while (iteratorI.hasNext())
        {
            Integer i = iteratorI.next();
            List<Integer> adjList = this.adjacencyList.get(i);
            if (count == size)
            {
                return true;
            }
            if (adjList.size() == 0)
            {
                count++;
             //   System.out.println("Target Node - " + i);
                Iterator<Integer> iteratorJ = this.adjacencyList.keySet()
                        .iterator();
                while (iteratorJ.hasNext())
                {
                    Integer j = iteratorJ.next();
                    List<Integer> li = this.adjacencyList.get(j);
                    if (li.contains(i))
                    {
                        li.remove(i);
                        System.out.println("Deleting edge between target node "
                                + i + " - " + j + " ");
                    }
                }
                this.adjacencyList.remove(i);
                iteratorI = this.adjacencyList.keySet().iterator();
            }
        }
        return false;
    }

}
