package org.processmining.causalityinference.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

//JAVA program to print all  
//paths from a source to 
//destination. 


//A directed graph using 
//adjacency list representation 
public class AllPathFinder {

 // No. of vertices in graph 
 private int v;  
 private Graph graph;
   
 // adjacency list  
 private ArrayList<Integer>[] adjList;  
 private Map<Integer, String> idxNodeName;
 private Map<String, Integer> nodeNameIdx;
 private Set<List<Integer>> allPaths = new HashSet<List<Integer>>();
 //Constructor 
 public AllPathFinder(Graph graph){ 
	 this.graph = graph;
	 v = graph.getNumNodes();
	 idxNodeName = new HashMap<Integer, String>();
	 nodeNameIdx = new HashMap<String, Integer>();
	 
	 List<Node> graphNodes = graph.getNodes();
	 int i = 0;
	 for (Node n : graphNodes) {
		 idxNodeName.put(i, n.getName());
		 nodeNameIdx.put(n.getName(), i);
		 i++;
	 }
	 
     // initialise adjacency list 
     initAdjList(); 
     
     // Add edges to the adjacency matrix
 	 for (Edge e : graph.getEdges()) {
 	 	 String edge = e.toString();
 	 	 Object[] edgeInfo = new Object[3];
		
		String[] parts = edge.split(" --> ");
//		System.out.println(edge);
//		System.out.println(nodeNameIdx.get(parts[0]));
//		System.out.println(nodeNameIdx.get(parts[2]));
		if (nodeNameIdx.get(parts[0]) != null && nodeNameIdx.get(parts[1]) != null)
			addEdge(nodeNameIdx.get(parts[0]), nodeNameIdx.get(parts[1]));
 	 }
 } 
   
 // utility method to initialise 
 // adjacency list 
 @SuppressWarnings("unchecked") 
 private void initAdjList() 
 { 
     adjList = new ArrayList[v]; 
       
     for(int i = 0; i < v; i++) 
     { 
         adjList[i] = new ArrayList<>(); 
     } 
 } 
   
 // add edge from u to v 
 public void addEdge(int u, int v) 
 { 
     // Add v to u's list. 
     adjList[u].add(v);  
 } 
   
 // Prints all paths from 
 // 's' to 'd' 
 public void findAllPaths(int s, int d)  
 { 
     boolean[] isVisited = new boolean[v]; 
     ArrayList<Integer> pathList = new ArrayList<>(); 
       
     //add source to path[] 
     pathList.add(s); 
     
     //Call recursive utility 
     printAllPathsUtil(s, d, isVisited, pathList); 
 } 
 
 public Set<ArrayList<String>> getAllpaths(Node s, Node d) {
	 String n1 = s.getName();
	 String n2 = d.getName();
	 
	 findAllPaths(nodeNameIdx.get(n1), nodeNameIdx.get(n2));
	 
	 Set<ArrayList<String>> pathLists = new HashSet<ArrayList<String>>();
	 
	 for (List<Integer> list : allPaths) {
		 ArrayList<String> onePath = new ArrayList<String>();
		 for (Integer idx : list)
			 onePath.add(idxNodeName.get(idx));
		 pathLists.add(onePath);
	 }
		 
	 return pathLists;
 }
 
 public Set<List<String>> getAllpathsString() {
	 Set<List<String>> stringPaths = new HashSet<List<String>>();
	 for (List<Integer> list : allPaths) {
		 ArrayList<String> strList = new ArrayList<>();
		 for (Integer i : list)
			 strList.add(idxNodeName.get(i));
		 stringPaths.add(strList);
	 }
	 return stringPaths;
 }

 // A recursive function to print 
 // all paths from 'u' to 'd'. 
 // isVisited[] keeps track of 
 // vertices in current path. 
 // localPathList<> stores actual 
 // vertices in the current path 
 private void printAllPathsUtil(Integer u, Integer d, 
                                 boolean[] isVisited, 
                         List<Integer> localPathList) { 
       
     // Mark the current node 
     isVisited[u] = true; 
       
     if (u.equals(d))  
     { 
    	 ArrayList<Integer> pathList = new ArrayList<>();
    	 for (Integer i : localPathList)
    		 pathList.add(i);
    	 allPaths.add(pathList);
         // if match found then no need to traverse more till depth 
         isVisited[u]= false; 
         return ; 
     } 
       
     // Recur for all the vertices 
     // adjacent to current vertex 
     for (Integer i : adjList[u])  
     { 
         if (!isVisited[i]) 
         { 
             // store current node  
             // in path[] 
             localPathList.add(i); 
             printAllPathsUtil(i, d, isVisited, localPathList); 
               
             // remove current node 
             // in path[] 
             localPathList.remove(i); 
         } 
     } 
       
     // Mark the current node 
     isVisited[u] = false; 
 } 

 // Driver program 
 public static void main(String[] args)  
 { 
     // Create a sample graph 
   
     // arbitrary source 
     int s = 2; 
   
     // arbitrary destination 
     int d = 3; 
   
     System.out.println("Following are all different paths from "+s+" to "+d); 

 } 
} 
