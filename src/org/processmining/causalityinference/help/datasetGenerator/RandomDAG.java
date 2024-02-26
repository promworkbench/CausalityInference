package org.processmining.causalityinference.help.datasetGenerator;

import java.util.Random;

public class RandomDAG {
 
 // The maximum number of vertex for the random graph
private int maxVertex = 20;
private int numEdges;

private int MIN_PER_RANK = 0; /* Nodes/Rank: How 'fat' the DAG should be.  */
private int MAX_PER_RANK = 3;
private int MIN_RANKS = 3;    /* Ranks: How 'tall' the DAG should be.  */
private int MAX_RANKS = 5;
private int PERCENT = 30;     /* Chance of having an Edge.  */

/**
 * represent the graph as a set of edges.
 */
private int[][] edges;
 
 public RandomDAG(int n, int e) {
	maxVertex = n;
	numEdges = e;
}
 
 
public void generate() {
	int i, j, k,nodes = 0;
	Random random = new Random();
	int rand = random.nextInt();
	int ranks = MIN_RANKS
	              + (rand % (MAX_RANKS - MIN_RANKS + 1));

	System.out.println("digraph {\n");
	  for (i = 0; i < ranks; i++)
	    {
		  rand = random.nextInt();
	      /* New nodes of 'higher' rank than all nodes generated till now.  */
	      int new_nodes = MIN_PER_RANK
	                      + (rand % (MAX_PER_RANK - MIN_PER_RANK + 1));

	      /* Edges from old nodes ('nodes') to new ones ('new_nodes').  */
	      for (j = 0; j < nodes; j++)
	        for (k = 0; k < new_nodes; k++)
	          if ( (random.nextInt() % 100) < PERCENT)
	            System.out.println(j + "  " + j + "  " + k + "  " + nodes); /* An Edge.  */

	      nodes += new_nodes; /* Accumulate into old node set.  */
	    }
	  System.out.println("}\n");
}

// Function to check for cycle, upon addition of a new
 // edge in the graph
 public boolean checkAcyclic(int[][] edge, int ed,
                                    boolean[] check, int v)
 {
     int i;
     boolean value;
     
     // If the current vertex is visited already, then
     // the graph contains cycle
     
     if (check[v] == true)
         
         return false;
     
     else {
         
         check[v] = true;
         
         // For each vertex, go for all the vertex
         // connected to it
         for (i = ed; i >= 0; i--) {
             
             if (edge[i][0] == v)
                 
         return checkAcyclic(edge, ed, check, edge[i][1]);
             
         }
     }
     
     // In case, if the path ends then reassign the
     // vertexes visited in that path to false again
     check[v] = false;
     
     if (i == 0)
         return true;
     return true;
 }
 
 // Function to generate random graph
 public void generateRandomGraphs(int e)
 {
     
     int i = 0, j = 0, count = 0;
     boolean[] check = new boolean[maxVertex + 1];
     Random rand = new Random();
     edges = new int[numEdges][2];
     
     // Build a connection between two random vertex
     while (i < e) {
         
         edges[i][0] = rand.nextInt(maxVertex) + 1;
         edges[i][1] = rand.nextInt(maxVertex) + 1;
         
         for (j = 1; j <= maxVertex; j++)
             check[j] = false;
         
         if (checkAcyclic(edges, i, check, edges[i][0]) == true)
             
             i++;
         
         // Check for cycle and if found discard this
         // edge and generate random vertex pair again
     }

 }
 
 public void printDAG() {
	 System.out.println("The Generated Random Graph is :");
     
     // Print the Graph
     for (int i = 0; i < maxVertex; i++) {
         
         int count = 0;
         System.out.print((i + 1) + " -> { ");
         
         for (int j = 0; j < numEdges; j++) {
             
             if (edges[j][0] == i + 1) {
                 System.out.print(edges[j][1] + " ");
                 count++;
             }
             
             else if (edges[j][1] == i + 1) {
                 count++;
             }
             
             else if (j == numEdges - 1 && count == 0)
                 System.out.print("Isolated Vertex!");
         }
         
         System.out.print(" }\n");
     }
 }
 
 public static void main(String args[]) throws Exception
 {
     int e = 4;
     int n = 6;
     System.out.println("Enter the number of Nodes :"+ n);
     System.out.println("Enter the number of Edges :"+ e);
     
     RandomDAG g = new RandomDAG(n, e);
     g.generateRandomGraphs(e);
     g.printDAG();
 }
}