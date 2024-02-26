package org.processmining.causalityinference.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class TestTetradCmd {
//	private Process p;
	public static void main(String args[]) 
    { 
		
        String s = null;
        Set<String> nodes = new HashSet<String>();
        boolean flagNodes = false;
        Set<String> edges = new HashSet<String>();
        boolean flagEdges = false;

        try {

        	Process p = Runtime.getRuntime().exec("java -jar bin\\tetrad\\tetradcmd.jar -data "
      //  			+ " C:\\Users\\qafari\\Desktop\\dataSetsTetrad\\brainIQ.txt -datatype continuous -algorithm fci -depth 3 -significance 0.05");
        			+ "C:\\Users\\qafari\\Documents\\Projects\\reciept01.txt -datatype continuous -algorithm fci -depth 3 -significance 0.05");
            
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
            	if  (flagNodes) {
            		String[] parts = s.split(" ");
            		for (String node : parts) {
            			nodes.add(node);
            		}
            		
            		flagNodes = false;
            	}
            	
            	if (flagEdges) {
            		if (s.length() > 0)
            			edges.add(s.substring(3, s.length()));
            	}
            	
            	if (s.equals("Graph Nodes:")) {
            		flagNodes = true;
            	}
            	
            	if (s.equals("Graph Edges: ")) {
            		flagEdges = true;
            	}
            	
                System.out.println(s);
            }
            
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            
            System.out.println("**********\n");
            System.out.println("Nodes\n");
            int i = 0;
            for (String str : nodes) {
            	System.out.println(i++ +" - "+ str);
            }
            
            System.out.println("\nEdges\n");
            i = 0;
            for (String str : edges) {
            	System.out.println(i++ +" - "+ str);
            }
            
            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    } 
}
