package org.processmining.causalityinference.help;
//JAVA program to print all  
//paths from a source to 
//destination. 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set; 

//A directed graph using 
//adjacency list representation 
public class TestAllPathFinder { 

 // No. of vertices in graph 
 private int v;  
   
 // adjacency list  
 private ArrayList<Integer>[] adjList;  
 
 private Set<List<Integer>> allPaths = new HashSet<List<Integer>>();
   
 //Constructor 
 public TestAllPathFinder(int vertices){ 
       
     //initialise vertex count 
     this.v = vertices; 
       
     // initialise adjacency list 
     initAdjList();  
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
 public void printAllPaths(int s, int d)  
 { 
     boolean[] isVisited = new boolean[v]; 
     ArrayList<Integer> pathList = new ArrayList<>(); 
       
     //add source to path[] 
     pathList.add(s); 
       
     //Call recursive utility 
     printAllPathsUtil(s, d, isVisited, pathList); 
     System.out.println("all path");
     for (List<Integer> list : allPaths)
    	 System.out.println(list); 
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
         System.out.println(localPathList); 
    	 
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
	 TestAllPathFinder g = new TestAllPathFinder(4); 
     g.addEdge(0,1); 
     g.addEdge(0,2); 
     g.addEdge(0,3); 
     g.addEdge(2,0); 
     g.addEdge(2,1); 
     g.addEdge(1,3); 
   
     // arbitrary source 
     int s = 0; 
   
     // arbitrary destination 
     int d = 3; 
   
     System.out.println("Following are all different paths from "+s+" to "+d); 
     g.printAllPaths(s, d); 

 } 
} 

//This code is contributed by Himanshu Shekhar. 