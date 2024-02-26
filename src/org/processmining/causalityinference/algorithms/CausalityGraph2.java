package org.processmining.causalityinference.algorithms;

import static org.junit.Assert.assertEquals;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.BDeuScore;
import edu.cmu.tetrad.search.Fci;
import edu.cmu.tetrad.search.GFci;
import edu.cmu.tetrad.search.GraphSearch;
import edu.cmu.tetrad.search.IndTestChiSquare;
import edu.cmu.tetrad.search.IndTestFisherZ;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.util.DataConvertUtils;
import edu.cmu.tetrad.util.DelimiterUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.pitt.dbmi.data.reader.ContinuousData;
import edu.pitt.dbmi.data.reader.Data;
import edu.pitt.dbmi.data.reader.DataColumn;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataReader;
import edu.pitt.dbmi.data.reader.tabular.VerticalDiscreteTabularDatasetFileReader;

public class CausalityGraph2 {
	
	private Set<String> nodes;
	private Set<String> edges;
	
	private Graph dag;
	private Map<Node, DiscreteVariable> nodesToVariables;
	
//	private boolean isContinuous = false;
	private String dataType = "Discrete";
	private double significance = 0.05;
	private int depth = -1;
	double alpha = 0.01;
	private DataSet data;
	private final Delimiter delimiter = Delimiter.TAB;
	private final char quoteCharacter = '"';
	private final String missingValueMarker = "*";
	private final String commentMarker = "//";
	private final boolean hasHeader = true;
	private Data data2;
//    double penaltyDiscount = 4.0;
    
	
	public CausalityGraph2() { //XLog log, Petrinet model, PNRepResult res) {

		try {
			this.data = getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
////		IndependenceTest test = new IndTestFisherZ(data, alpha);
		
////		Cfci search = new Cfci(test);

        // Run search
 ////       search.search();
		
//		Dag dgraph = new Dag();
//		GraphNode n1 = new GraphNode("n1");
//		GraphNode n2 = new GraphNode("n1");
//		dgraph.addNode(n1);
//		dgraph.addNode(n2);
//		dgraph.addDirectedEdge(n1, n2);
//		
//		final DagToPag dagToPag = new DagToPag(dgraph);
//		dagToPag.setCompleteRuleSetUsed(false);
//		Graph PAG_True = dagToPag.convert();
//		//PAG_True = GraphUtils.replaceNodes(PAG_True, data.getVariables());
//		
//		//Graph rfciPag = runPagCs(data, alpha);
//

	}
	
	public DataSet getData() throws IOException {
		
		double alpha = 0.05;
        char delimiter = '\t';
        double penaltyDiscount = 2;
        Path dataFile = Paths.get("DataTableNumerical2.txt");
 //       Path dataFile = Paths.get("bin\\sim_discrete_data_20vars_100cases.txt");
        VerticalDiscreteTabularDatasetFileReader dataReader = new VerticalDiscreteTabularDatasetFileReader(dataFile, DelimiterUtils.toDelimiter(delimiter));  // Discrete
//        ContinuousTabularDatasetFileReader dataReader= new ContinuousTabularDatasetFileReader(dataFile, Delimiter.TAB);  //  Continuous
        DataSet dataSet = (DataSet) DataConvertUtils.toDataModel(dataReader.readInData());
        

        
        IndependenceTest indTest = new IndTestChiSquare(dataSet, alpha);  // Discrete
 //       IndependenceTest indTest = new IndTestChiSquare(dataSet, alpha);   //  Continuous

        BDeuScore score = new BDeuScore(dataSet);  // Discrete
        score.setStructurePrior(1.0);  // Discrete
        score.setSamplePrior(1.0);  // Discrete
        
//        ICovarianceMatrix cov = new CovarianceMatrix(dataSet);   //  Continuous
//
 //       IndTestFisherZ indTest = new IndTestFisherZ(cov, alpha);   //  Continuous
 //       SemBicScore score = new SemBicScore(cov);   //  Continuous
//        score.setPenaltyDiscount(penaltyDiscount);   //  Continuous

        indTest.setAlpha(alpha);
        
        //    Fci fci = new Fci(indTest);   // other search algs
        //      Graph graph = fci.search();    // other search algs
        
        GFci gFci = new GFci(indTest, score);
        gFci.setFaithfulnessAssumed(true);
        gFci.setMaxDegree(-1);
        gFci.setMaxPathLength(-1);
        gFci.setCompleteRuleSetUsed(false);
        gFci.setVerbose(true);

        Graph graphGFci = gFci.search();
        
//        System.out.println("--Edges: ");
        Set<Edge> edges = graphGFci.getEdges();
        for (Edge edge : edges) {
        	String name = edge.toString();
 //       	System.out.println(name);
        	String[] chanks = name.split(" ");
        	if (chanks[1].equals("o-o"))
        	{
        		Node n1 = edge.getNode1();
        		Node n2 = edge.getNode2();
        		graphGFci.removeEdge(edge);
        		graphGFci.addDirectedEdge(n1, n2);
        	}
        	//	graphGFci.removeEdge(edge);
        	if (chanks[1].equals("o->"))
        	{
        		Node n1 = edge.getNode1();
        		Node n2 = edge.getNode2();
        		graphGFci.removeEdge(edge);
        		graphGFci.addDirectedEdge(n1, n2);
        	}
        }
        
//        System.out.println("----Edges: ");
 //       edges = graphGFci.getEdges();
 //       for (Edge edge : edges) {
//        	String name = edge.toString();
 //       	System.out.println(name);
//        }
//        System.out.println("2");
        
        List<Node> nodes = new ArrayList<>();

//      SemPm pm = new SemPm(graphGFci);     // Continuous
//      SemIm im = new SemIm(pm);   // Continuous
//      StandardizedSemIm sem = new StandardizedSemIm(im);   // Continuous
//      SemPm semPm = new SemPm(graphGFci);      // Continuous
//      SemEstimator estimator = new SemEstimator(new CovarianceMatrix(dataSet), semPm);  // Continuous
//      estimator.estimate();    // Continuous
//        
//      System.out.println(estimator.getEstimatedSem());
     
        
        
//        System.out.println("3");
        
        return dataSet;

    }


	private Graph getPagFCI() { // double penaltyDiscount, DataSet data) {
        IndTestFisherZ test = new IndTestFisherZ(data, alpha);
 //       IndTestChiSquare test = new IndTestChiSquare(data, alpha);
//        SemBicScore score = new SemBicScore(new CovarianceMatrix(data));
//        score.setPenaltyDiscount(penaltyDiscount);

        GraphSearch search = new Fci(test);
//        GraphSearch search = new GFci(score);
//        GraphSearch search = new Pc(test);

        return search.search();
    }

    // Without the ar names.
    public void test1b() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("src/test/resources/cheese2.txt");
        char[] chars = fileToCharArray(file);

        DataReader reader = new DataReader();
        reader.setDelimiter(DelimiterType.WHITESPACE);
        reader.setVariablesSupplied(false);
        reader.setIdsSupplied(true);
        reader.setIdLabel(null);

        DataSet data = reader.parseTabular(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        assertEquals(12.3, data.getDouble(0, 0), 0.1);
    }

    private char[] fileToCharArray(File file) {
        try {
            FileReader reader = new FileReader(file);
            CharArrayWriter writer = new CharArrayWriter();
            int c;

            while ((c = reader.read()) != -1) {
                writer.write(c);
            }

            return writer.toCharArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
	
    public void dataReader() throws IOException {
    	int[] columnsToExclude = {8, 2, 4, 11, 9};
    	Path dataFile = Paths.get(getClass().getResource("DataTableOneHot.txt").getFile());
    	TabularColumnReader columnReader = new TabularColumnFileReader(dataFile, delimiter);
        columnReader.setCommentMarker(commentMarker);
        columnReader.setQuoteCharacter(quoteCharacter);

        boolean isDiscrete = false;
        DataColumn[] dataColumns = columnReader.readInDataColumns(columnsToExclude, isDiscrete);

        long expected = 6;
        long actual = dataColumns.length;
//        Assert.assertEquals(expected, actual);

        TabularDataReader dataReader = new TabularDataFileReader(dataFile, delimiter);
        dataReader.setCommentMarker(commentMarker);
        dataReader.setQuoteCharacter(quoteCharacter);
        dataReader.setMissingDataMarker(missingValueMarker);

        Data data = dataReader.read(dataColumns, hasHeader);
 //       Assert.assertTrue(data instanceof ContinuousData);
        this.data2 = data;
        
        ContinuousData continuousData = (ContinuousData) data;
        double[][] contData = continuousData.getData();   
    }
        
        
	public void setDataType(String type) {
		this.dataType = type;
	}
	
	public String getDataType() {
		return this.dataType;
	}
	
	public Set<String> getNodes() {
		return nodes;
	}
	
	public Set<String> getEdges() {
		return edges;
	}
	
	public void setSignificance(double d) {
		this.significance = d;
	}
	
	public double getSignificance() {
		return this.significance;
	}
	
	public void setDepth( int d) {
		this.depth = d;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public static void main(String[] args) {
		CausalityGraph2 cg = new CausalityGraph2();
		Graph pag = cg.getPagFCI();
		System.out.println("here");
		System.out.println(pag.toString());
	}
}
