package org.processmining.causalityinference.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.espertech.esper.collection.Pair;



//This class represents a directed graph using adjacency list 
//representation 
class PathFinder { 
	
	private int V;   // No. of vertices 
	private LinkedList<Integer> adj[]; //Adjacency Lists 
	private Map<Integer, Set<Integer>> graph;

 // Constructor 
	PathFinder()  { 
     graph = new HashMap<Integer, Set<Integer>>(); 
 	} 
	
	/**
	 * turn the directed graph to an undirected one
	 */
	public void turnToUndirected() {
		Set<Pair<Integer, Integer>> edgesToBeAdded = new HashSet<Pair<Integer, Integer>>();
		for (Integer node : graph.keySet()) {
//			System.out.println("node : " + node);
			for (Integer kid : graph.get(node)) {
				edgesToBeAdded.add(new Pair<Integer, Integer>(kid, node));
//				System.out.println("kid : " + kid);
			} 
		}
		
		if (!edgesToBeAdded.isEmpty())
			for (Pair<Integer, Integer> pair : edgesToBeAdded)
				addEdge(pair.getFirst(), pair.getSecond());	
	}
	
	void setGraph(Map<Integer, Set<Integer>> g) {
		graph = g;
	}
	
	Map<Integer, Set<Integer>> getGraph() {
		return graph;
	}
 // Function to add an edge into the graph 
 void addEdge(int v,int w) 
 { 	
	 if (graph.containsKey(v))
		 graph.get(v).add(w);
	 else {
		 Set<Integer> edges = new HashSet<Integer>();
		 edges.add(w);
		 graph.put(v, edges);
	 }
	 
	 if (graph.containsKey(w))
		 graph.get(w).add(v);
	 else {
		 Set<Integer> edges = new HashSet<Integer>();
		 edges.add(v);
		 graph.put(w, edges);
	 }
		 
 } 
 void addDirectedEdge(int u, int v) {
	 if (graph.containsKey(u))
		 graph.get(u).add(v);
	 else {
		 Set<Integer> edges = new HashSet<Integer>();
		 edges.add(v);
		 graph.put(u, edges);
	 }
 }
 	boolean isConnected(int u, int v) {
 		if (!graph.containsKey(u))
 			return false;
 		Set<Integer> cd = BFS(u);
 		if (cd == null)
 			return false;
 		
 		if (cd.contains(v))
 			return true;
 		else 
 			return false;
 	}
 // prints BFS traversal from a given source s 
	Set<Integer> BFS(int s) 
	{ 
     // Mark all the vertices as not visited(By default 
     // set as false) 
     Map<Integer, Boolean> visited = new HashMap<Integer, Boolean>(); 

     // Create a queue for BFS 
     LinkedList<Integer> queue = new LinkedList<Integer>(); 

     // Mark the current node as visited and enqueue it 
     visited.put(s, true); 
     queue.add(s); 
     
     Set<Integer> result = new HashSet<Integer>();
     while (queue.size() != 0) 
     { 
         // Dequeue a vertex from queue and print it 
         s = queue.poll(); 
         result.add(s);
//         System.out.print(s+" "); 

         // Get all adjacent vertices of the dequeued vertex s 
         // If a adjacent has not been visited, then mark it 
         // visited and enqueue it 
         if (!graph.keySet().contains(s))
        	 break;
         
         Iterator value = graph.get(s).iterator();  
         while (value.hasNext()) 
         { 
             int n = (int) value.next(); 
             if (!visited.containsKey(n)) 
             { 
                 visited.put(n, true); 
                 queue.add(n); 
                 result.add(n);
             } 
         } 
     } 
     return result;
 } 

 // Driver method to 
 public static void main(String args[]) 
 { 
	 PathFinder g = new PathFinder(); 
	 
     g.addDirectedEdge(1, 0); 
     g.addDirectedEdge(2, 1); 
     g.addDirectedEdge(2, 4); 
     g.addDirectedEdge(3, 2); 
 //    g.addDirectedEdge(2, 3);
 //    g.addEdge(0, 5);
 //    g.addDirectedEdge(0, 4);
 //    g.addDirectedEdge(6, 5);

     System.out.println("Following is Breadth First Traversal "+ 
                        "(starting from vertex 2)"); 

     Set<Integer> result = g.BFS(2); 
     System.out.println(" 0========= 0" );
     System.out.println(result);
     
     for (Integer i : g.getGraph().keySet())
    	 for (Integer j : g.getGraph().keySet())
    		 System.out.println("Node "+ i + "and node "+j+" are connected ---> "+ g.isConnected(i, j) );
     
 } 
} 
//This code is contributed by Aakash Hasija 