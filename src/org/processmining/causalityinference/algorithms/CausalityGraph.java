package org.processmining.causalityinference.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.causalityinference.limitGraphDeapth.LimitGraph;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.EmBayesEstimator;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge2;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.BDeuScore;
import edu.cmu.tetrad.search.DagToPag;
import edu.cmu.tetrad.search.GFci;
import edu.cmu.tetrad.search.IndTestChiSquare;
import edu.cmu.tetrad.search.IndTestFisherZ;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.SemBicScore;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.DataConvertUtils;
import edu.cmu.tetrad.util.DelimiterUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.tabular.ContinuousTabularDatasetFileReader;
import edu.pitt.dbmi.data.reader.tabular.VerticalDiscreteTabularDatasetFileReader;
import net.sf.saxon.exslt.Math;

public class CausalityGraph {
	
	private LinkedList<String> nodes = null;
	private Set<String> edges = null;
	
//	private boolean isContinuous = false;
	private String dataType = "Discrete";
	private double significance = 0.05;
	private int depth = -1;
	
	private int numberOfNodes;
	private String path = "DataTableNumerical.txt";
	
	private XLog log;
	private Petrinet model;
	private PNRepResult res;
	
	DataSet dataSet = null;
	private Graph graph;
	private Knowledge2 knowledge = null;
	Set<String> forbiddenEdges = null;
	Set<String> requiredEdges = null;
	
	private Map<String, Set<String>> literalValues = null;
	private Map<String, Type> types = null;
	Map<Integer, String> attIdxNameMap;
	
	private Map<Node, Object[][]> tables;
	private Map<Node, Object[]> headers;
	private Map<String, Double> edgeCoefficients;
	private Map<String, Double> nodeCoefficients;
	
	private Map<String, Double> nodesErrVar;
	private Map<String, Double> nodesMean;
	private Map<String, Double> nodesStdDev;
	
	private String classAttName;
	private Parameters params;
	Map<String, Map<String, double[]>> effectOfIntervineOnAttributeOnClass;
	String[] numClassCategories;
	boolean estimationIsValid = false;
	boolean limitGraph = false;
	
	char delimiter = '\t';
	
	public CausalityGraph(XLog log) { 
		this.log = log;
	}
	
	public CausalityGraph(XLog log, Petrinet model, PNRepResult res) {
		this.log = log;
		this.model = model;
		this.res = res;
	} 
		
	public void inferCausalityGraph() {
		if (dataType.equals("Discrete")) 
			inferCausalityGraphDiscreteData();
	    else 
			inferCausalityGraphContiniousData();
		
		writeGraphToFile();
	}

	/**
	 * This method is used when we start with an empty graph.
	 */
	public void initGraph(LinkedList<String> nodeNames) {

		inferCausalityGraph();
		Set<Edge> set = graph.getEdges();
		for (Edge edge : set)
			graph.removeEdge(edge);
		
		setNodesAndEdges(graph);
	}
	
	/**
	 * 
	 * Estimate the effect of an attribute on the class attribute
	 */
	public boolean estimate() {
		if (!isDag())
			return false;
		
		if (dataType.equals("Discrete"))
			estimateCausalityGraphDiscreteData();
		else		
			estimateCausalityGraphContiniousData();
		
		estimationIsValid = true;
		
		return true;			
	}
	
	public void setEstimationIsValide(boolean b) {
		estimationIsValid = b;
	}
	
	public boolean getEstimationIsValid() {
		return estimationIsValid;
	}
	
	// ---------------- read the data -------------------
	
	public void readData() {
		Path dataFilePath = Paths.get(this.path);
	    char delimiter = '\t';

	    VerticalDiscreteTabularDatasetFileReader dataReader = new VerticalDiscreteTabularDatasetFileReader(dataFilePath, DelimiterUtils.toDelimiter(delimiter));
  		dataReader.setMissingDataMarker("*");
  		try {
  			dataSet = (DataSet) DataConvertUtils.toDataModel(dataReader.readInData());
  		} catch (IOException e) {
  			System.out.println("Failed to read in data.");
  			e.printStackTrace();
  		} 
  		
  		params.setNumCol(dataSet.getNumColumns());
  		params.setNumRow(dataSet.getNumRows());
  		
  		dataSet.getNumColumns();
	}
	
	public void setLimitGraph (boolean b) {
		limitGraph = b;
	}
	
	// ------ Discrete Data -------------
	public void inferCausalityGraphDiscreteData() {
		
		readData();
       

		double alpha = significance;
       
		 IndependenceTest indTest = new IndTestChiSquare(dataSet, significance);
		 System.out.println(indTest.getCovMatrices());
		 BDeuScore score = new BDeuScore(dataSet);
	     score.setStructurePrior(1.0);
	     score.setSamplePrior(1.0);
	     
	     GFci gFci = new GFci(indTest, score);
	     System.out.println(indTest.getScore());
	     
	     generateKnowledge();
	     if (knowledge != null) {
	    	 gFci.setKnowledge(knowledge);
	     }
	     gFci.setFaithfulnessAssumed(true);
	     gFci.setMaxDegree(-1);
	     gFci.setMaxPathLength(-1);
	     gFci.setCompleteRuleSetUsed(false);
	     gFci.setVerbose(true);

	     
	     graph = gFci.search();
	     
	     setNodesAndEdges(graph);
	     clearKnowledge();
	     System.out.println("sampel size: "+ indTest.getScore());
	     System.out.println("Graph: ");
//	     for (String s : edges)
//	    	 System.out.println("edge: "+ s);
	}
	
	public void clearKnowledge() {
		this.knowledge = null;
	}
	
	public boolean isDag() {
		for (Edge edge : graph.getEdges()) {
			String s = edge.toString();
			String[] parts = s.split(" ");
			if (parts[1] == "-->")
				return false;
		}
		
		IsGraphDAG testDag = new IsGraphDAG(graph);
		
		return testDag.checkIsGraphDAG();
	}
	
	public Node getClassAttNode() {
		for (Node node : graph.getNodes())
			if (classAttName.equals(node.getName()))
				return node;
		return null;
	}
	
	private void generateKnowledge() {
		if ((forbiddenEdges == null || forbiddenEdges.isEmpty())
				&& (requiredEdges == null || requiredEdges.isEmpty())) {
			knowledge = null;
			return;
		}
		knowledge = new Knowledge2(params.getAttributeNames());
		if (forbiddenEdges != null)
			for (String edge : forbiddenEdges) {
				String[] parts = edge.split(" ");
				knowledge.setForbidden(parts[0], parts[2]);
			}
		if (requiredEdges != null)
			for (String edge : requiredEdges) {
				String[] parts = edge.split(" ");
				knowledge.setRequired(parts[0], parts[2]);
			}
	}


	public void factorTheFinalGraph() {
//		inferCausalityGraph();
		Set<Edge> edgesToRemove = new HashSet<Edge>();
		Set<Edge> e = graph.getEdges();
		
		if (forbiddenEdges != null && !forbiddenEdges.isEmpty())
			for (String fEdge : forbiddenEdges) {
				String[] fParts = fEdge.split(" --> ");
				String from = fParts[0];
				String to = fParts[1];
				for (Edge edge : graph.getEdges()) {
					String s = edge.toString();
					String[] parts = null;
					if (s.contains("-->")) {
						parts = s.split("-->");
						if (parts[0].contains(from) && parts[1].contains(to))
							edgesToRemove.add(edge);
					}
				}
			}
		
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
		
//		System.out.println(graph);
		
		forbiddenEdges = null;
		requiredEdges = null;
	}
	
	public void estimateCausalityGraphDiscreteData() {
		BayesPm bayesPM = new BayesPm(graph);
		for (int i = 0; i < bayesPM.getNumNodes(); i++) {
			Node node = bayesPM.getNode(i);
			String nodeName = node.getName();
			if (types.keySet().contains(nodeName)) {
				if (types.get(nodeName).equals(Type.LITERAL) || types.get(nodeName).equals(Type.BOOLEAN) || types.get(nodeName).equals(Type.DISCRETE)){						
					bayesPM.setNumCategories(node, literalValues.get(nodeName).size());
					bayesPM.setCategories(node, categories(nodeName));
				} 
			} else 
				System.out.println(nodeName + " is missing!");
		}
		
		EmBayesEstimator estimator = new EmBayesEstimator(bayesPM, dataSet);
		BayesIm bayesIM = estimator.getEstimatedIm();
		creatProbTables(bayesIM, bayesPM);
		
		numClassCategories = getClassCategories(bayesPM);
		effectOfIntervineOnAttributeOnClass = estimateEffectOnClassAtt(bayesIM, bayesPM);
//		System.out.println("Non");
//TODO		bayesIM.
	}
	
	private List<String> categoriesBoolean() {
		List<String> categories = new ArrayList<String>();
		categories.add("0");
		categories.add("1");
		return categories;
	}
	
	public String[] getClassCategories(BayesPm pm) {
		String[] categories = new String[pm.getNumCategories(getClassAttNode())];
		for (int i = 0; i < pm.getNumCategories(getClassAttNode()); i++)
			categories[i] = pm.getCategory(getClassAttNode(), i);
		return categories;
	}

	private List<String> categories(String nodeName) {
		List<String> categories = new ArrayList<String>();
		if (this.params.getAttTypes().get(nodeName).equals(Type.DISCRETE)) {
			for (String item : literalValues.get(nodeName))
				categories.add(item);
			return categories;
		}

		for (Integer i = 0; i < literalValues.get(nodeName).size(); i++)
			categories.add(i.toString());
		return categories;
	}
	
	/**
	 * 
	 * @param im
	 * @param pm
	 * @return the headers and the body of the tables of conditional
	 * probabilities of each node condition on its parents.
	 */
	public Object[] creatProbTables(BayesIm im, BayesPm pm) {
		attIdxNameMap = new HashMap<Integer, String>();
		attIdxNameMap = getAttIdxNameMap(pm);
		
		NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
		tables = new HashMap<Node, Object[][]>();
		headers = new HashMap<Node, Object[]>();
		
		int classIdx;
		for (int i = 0; i < pm.getNumNodes(); i++) 
			if (pm.getNode(i).getName().equals(classAttName))
				classIdx = i;
		
        for (int i = 0; i < im.getNumNodes(); i++) {
//        	System.out.println("idx : " + i);
//        	System.out.println("node : " + im.getNode(i));
//        	System.out.println("row : " + im.getNumRows(i));
//        	System.out.println("cal : " + im.getNumColumns(i));
            Node node = im.getNode(i);
            Object[][] table = null;
            Object[] header = null;
            if (im.getNumParents(i) == 0) {
                header = new Object[pm.getNumCategories(node)];
                for (int j = 0; j < pm.getNumCategories(node); j++)
                	header[j] = pm.getCategory(node, j);
                headers.put(node, header);
            } else {
                header = new Object[im.getNumColumns(i) + im.getNumParents(i)];
                int[] parents = im.getParents(i);
                for (int k = 0; k < parents.length; k++) {
                	System.out.println("parent : " + im.getParent(i, k));
                    header[k] = im.getParent(i, k);
                }
                
                int c = 0;
                for (int k = im.getNumParents(i); k < im.getNumParents(i)+pm.getNumCategories(node); k++) {
                    header[k] = pm.getCategory(node, c);
                    c++;
                }
                headers.put(node, header);
            }
            
            table = new Object[im.getNumRows(i)][header.length];
            for (int j = 0; j < im.getNumRows(i); j++) {
            	int cIdx = 0;
                for (int k = 0; k < im.getNumParents(i); k++) {
                    table[j][cIdx] = im.getParentValue(i, j, k);
                    cIdx++;
                }

                for (int k = 0; k < im.getNumColumns(i); k++) {
                	double p = im.getProbability(i, j, k);
                	Object kk = nf.format(im.getProbability(i, j, k));
                	table[j][cIdx] = (nf.format(im.getProbability(i, j, k)));
                	cIdx++;
                }
            }
            tables.put(node, table);
        }
        
        Object[] headerAndTable = new Object[2];
        headerAndTable[0] = headers;
        headerAndTable[1] = tables;
       return headerAndTable;
	}
	
	public Map<Integer, String> getAttIdxNameMap(BayesPm pm) {
		Map<Integer, String> attIdxNameMap = new HashMap<Integer, String>();
		for (int i = 0; i < pm.getNumNodes();i++) {
			attIdxNameMap.put(i, pm.getNode(i).getName());
		}
		return attIdxNameMap;
	}
	
	public Map<Integer, String> getAttIdxNameMap() {
		return attIdxNameMap;
	}
	
	public Map<Node, Object[][]> getTables() {
		return tables;
	}
	
	public Map<Node, Object[]> getHeaders() {
		return headers;
	}
	
	public Map<String, Double> getNodeCoefficients() {
		return nodeCoefficients;
	}
	
	public Integer getClassNodeIdx(BayesPm pm) {
		Node classNode = getClassAttNode();
		for (int i = 0; i < pm.getNumNodes(); i++)
			if (classNode.equals(pm.getNode(i)))
				return i;
		return null;
	}
	
	public Integer getNodeIdx(Node node, BayesPm pm) {
		for (int i = 0; i < pm.getNumNodes(); i++)
			if (node.equals(pm.getNode(i)))
				return i;
		return null;
	}
	
	/**
	 * 
	 * @param im
	 * @param pm
	 * @return  A Map of nodes and the table body including the conditional 
	 * probability of choosing each one of its value on the class attribute value.
	 */
	public  Map<String, Map<String, double[]>> estimateEffectOnClassAtt(BayesIm im, BayesPm pm) {
		ComputeProbabilities cp = new ComputeProbabilities(params.getDataTableCreator().getTableHeader(),
				params.getDataTableCreator().getNumericalTableBody(), params.getDataTableCreator().getInverseAttValueMap(), pm, im);
		Map<String, Map<String, double[]>> probs = cp.getProbability(getClassAttNode());
		return probs;
	}
	
	/**
	public Map<Integer,Double[]> effectOnClassNode(Node node, BayesPm pm) {
		Map<Integer,Double[]> probs = new  HashMap<Integer,Double[]>();
		
		AllPathFinder pathFinder = new AllPathFinder(pm, graph);
		pathFinder.findAllPaths(getNodeIdx(node, pm), getClassNodeIdx(pm));
		Set<ArrayList<Integer>> paths = pathFinder.getAllpathsIdx();
		
		for (int i = 0; i < pm.getNumCategories(node) ; i++) {
			String category = pm.getCategory(node, i);
			double[] probOneCategoryOnePath = new double[pm.getNumCategories(node)];
			for(ArrayList<Integer> path : paths)
				probOneCategoryOnePath = getProbOnPath(category, path, pm);
		}
	//	for (ArrayList<Integer> path : paths) 
		return null;
	}
	
	public double[] getProbOnPath(String category, ArrayList<Integer> path, BayesPm pm) {
		// private Map<Node, Object[][]> tables;
		// private Map<Node, Object[]> headers;
	//	Object[][] table  = tables.get(pm.getNode(path));
		return null;
	}  */
	
	public Map<String, Object[][]> getBodysWithStringKeys() {
		Map<String, Object[][]> b = new HashMap<String, Object[][]>();
		for (Node node: headers.keySet())
			b.put(node.getName(), tables.get(node));
		return b;
	}
	
	public Map<String, Object[]> getHeadersWithStringKeys() {
		Map<String, Object[]> h = new HashMap<String, Object[]>();
		for (Node node: headers.keySet())
			h.put(node.getName(), headers.get(node));
		return h;
	} 
	
	public String[] getClassCategories() {
		return numClassCategories;
	}
	
	public void setGraph(Graph g, Map<String, Double> coefMap) {
		graph = g;
        nodeCoefficients = coefMap;
        setNodesAndEdges(g);
	}
	
	// ------ Continuous Data -------------
	public void inferCausalityGraphContiniousData() {
		double alpha = significance;
		readContiniousData();
		
//*		Path dataFile = Paths.get(path);

//*	    Delimiter delimiter2 = Delimiter.TAB;
//*	    boolean hasHeader = true;
//*	    boolean isDiscrete = false;
//*        DataModel dataModel = null;

	    // tabular data is our input (can also use covariance)
//*	    TabularColumnReader columnReader = new TabularColumnFileReader(dataFile, delimiter2);

	    // define the column variables
//*	    DataColumn[] dataColumns = null;

	    // read in the data column types

	    // optionally skip columns you don't want (e.g., the row id)
	    // dataColumns = columnReader.readInDataColumns(new int[]{0},isDiscrete);

	    // read in all columns
//*	    try {
//*			dataColumns = columnReader.readInDataColumns(isDiscrete);
//*	    } catch (IOException e) {
//*			System.out.println("There is a prolem in column reader. CausalityGraph 438");
//*			e.printStackTrace();
//*		}

	    
	    // setup data reader
//*	    TabularDataReader dataReader2 = new TabularDataFileReader(dataFile, delimiter2);
	    
	    // if this is a mixed dataset determine which columns are discrete i.e, updates dataColumns values from all continuous
	    //    dataReader2.determineDiscreteDataColumns(dataColumns, numberOfCategories, hasHeader);
	    
//*	    dataReader2.setMissingDataMarker("*");
	    // actually read in the data
//*	    Data data = null;
//*		try {
//*			data = dataReader2.read(dataColumns, hasHeader, null);
//*		} catch (IOException e) {
//*			System.out.println("There is a prolem in data reader. CausalityGraph 455");
//*			e.printStackTrace();
//*		}
	    
//*		dataModel = DataConvertUtils.toDataModel(data);
//*		Algorithm fges = null;
//*		try {
//* 		fges = AlgorithmFactory.create(Fges.class, ConditionalGaussianLRT.class, ConditionalGaussianBicScore.class);
//* 	} catch (IllegalAccessException | InstantiationException e) {
//* 		System.out.println("There is a prolem in initiating the search algorithm. CausalityGraph 465");
//* 		e.printStackTrace();
//* 	}

//&      // we've standardized parameters using the manual i.e., the id names in the manual are the names to use, also in Params class
//&      Parameters parameters = new Parameters();
//&      ParamDescriptions paramDescriptions = ParamDescriptions.getInstance();
//&
//&      // fges parameters
//&      parameters.set("depth", 100);
//&      parameters.set("penaltyDiscount", 1.0);
//&      parameters.set("faithfulnessAssumed", true);
//&      parameters.set("verbose", true);
//&      parameters.set("symmetricFirstStep", false);
//&
//&      // conditional gaussian parameters if using that test and score
//&      parameters.set("discretize", false);
//&      
//&      ScoreWrapper score = new edu.cmu.tetrad.algcomparison.score.SemBicScore();
//&      IndependenceWrapper test = new FisherZ();
//&
//&      Algorithm fges = new edu.cmu.tetrad.algcomparison.algorithm.oracle.pattern.Fges(score, false);
//&
//&		if (knowledge != null) {
//&      	((Fges) fges).setKnowledge((IKnowledge)knowledge);
//&     }
//&      
//&		// perform the search
//&      graph = fges.search(dataSet, parameters);
        
        ICovarianceMatrix cov = new CovarianceMatrix(dataSet);

        IndTestFisherZ independenceTest = new IndTestFisherZ(cov, alpha);
        SemBicScore score1 = new SemBicScore(cov);
        double penaltyDiscount = 2;
        score1.setPenaltyDiscount(penaltyDiscount);

        independenceTest.setAlpha(0.05);

        GFci gFci = new GFci(independenceTest, score1);
        gFci.setVerbose(false);
        gFci.setMaxDegree(4);
        gFci.setMaxPathLength(-1);
        gFci.setVerbose(true);
        gFci.setCompleteRuleSetUsed(false);
        gFci.setFaithfulnessAssumed(true);
        generateKnowledge();
        if (knowledge != null) {
          	((GFci) gFci).setKnowledge((IKnowledge)knowledge);
         }
        graph = gFci.search();
        setNodesAndEdges(graph);
        
        if (limitGraph == true && params.getSituationType().equals(SituationType.PL)) {
    			LimitGraph lg = new LimitGraph(nodes, edges, classAttName, params.getDataTableCreator(), params);
    			//TODO needs to be removed before commit.
    		}

	}
	
	public void readContiniousData() {
		dataSet = null;
		delimiter = '\t';
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
	
	public void estimateCausalityGraphContiniousData() {
		SemPm pm = new SemPm(graph);
		ICovarianceMatrix covMatrix = null;
		SemEstimator estimator2 = null;
		if (dataSet == null)
			readContiniousData();
		covMatrix = new CovarianceMatrix(dataSet);
		
		try {
			estimator2 = new SemEstimator(covMatrix, pm);
		} catch (IllegalArgumentException e) {
			System.out.println("Something went wrong. "+ e);
			
		}
        estimator2.estimate();
        SemIm im  = estimator2.getEstimatedSem();
        setEdgeCoefficients(im);
        setNodeCoefficients();
        setMean(im);
        writeSEM(im);  //TODO needs to be removed before commit.
	}
	
	public void writeSEM(SemIm im) {  //TODO needs to be removed before commit.
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("sem.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("fail to create the file");
			e.printStackTrace();
		}
		
		for (Node v : im.getVariableNodes()) {
			String eq = v.getName() + " = ";
			for (Node n : im.getVariableNodes()) { // parents
				if (im.existsEdgeCoef(n, v)) {
					BigDecimal bd = BigDecimal.valueOf(im.getEdgeCoef(n, v));
					double c = bd.setScale(4, RoundingMode.CEILING).doubleValue();
					if (c > 0)
						eq =  eq + "+" + c + " * " + n.getName();
					else if (im.getEdgeCoef(n, v) < 0)
						eq =  eq + "-" + Math.abs(c) + " * " + n.getName();
					}
			}
			
			if (eq.charAt(eq.length()-2) == '=')
				writer.println(eq + " noise");
			else
				writer.println(eq + " + noise");
		}
		
		writer.close();
	}
	 
/**	public void setEdgeCoefficients(SemIm im) {
		edgeCoefficients = new HashMap<Pair<Node, Node>, Double>();
		Set<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			Pair<Node, Node> pair = new Pair<Node, Node>(edge.getNode1(), edge.getNode2());
			edgeCoefficients.put(pair, im.getEdgeCoef(edge));
		}
	}  */
	
	public void setMean(SemIm im) {
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
//		double[] means = im.getMeans();
//		double[] vmeans = im.getVariableMeans();
	}
	
	public void setNodeCoefficients() {
		AllPathFinder apf = new AllPathFinder(graph); 
		Node classNode = getClassAttNode();
		nodeCoefficients = new HashMap<String, Double>();
		
		for (Node n : graph.getNodes()) 
			if (!n.equals(classNode))
				nodeCoefficients.put(n.getName(), computeNodeCoefficient(n));
	}
	
	/**
	 * if we write the effect of node n on class node s as value(s) = a value(n) + error
	 * this function compute a by summing the multiplication of all edge coefficient 
	 * on all paths from n to s.
	 * @param node
	 * @return
	 */
	public double computeNodeCoefficient(Node node) {
		AllPathFinder apf = new AllPathFinder(graph); 
		Node classNode = getClassAttNode();
		Set<ArrayList<String>> allPaths = apf.getAllpaths(node, classNode);
		if (allPaths.isEmpty())
			return 0;
		double coef = 0;
		for (ArrayList<String> path : allPaths) {
			double coefOnePath = 1;
			String first = path.get(0);
			for (int n = 1; n < path.size(); n++) {
				String second = new String();
				second = path.get(n);
				double d = getEdgeCoefficient(first, second);
				first = path.get(n);
				coefOnePath = coefOnePath * d;
			}
			coef = coef + coefOnePath;
		}
		return coef;
	}
	
	public double getEdgeCoefficient(String first, String second) {
		for (String edge : edgeCoefficients.keySet()) {
			String[] parts = edge.split(" ");
			if (first.equals(parts[0]) && second.equals(parts[2]))
				return edgeCoefficients.get(edge);
		}
		return 0;
	}
	
	public void setEdgeCoefficients(SemIm im) {
		edgeCoefficients = new HashMap<String, Double>();
		Set<Edge> edges = graph.getEdges();
		for (Edge edge : edges) {
			BigDecimal bd = BigDecimal.valueOf(im.getEdgeCoef(edge));
			edgeCoefficients.put(edge.toString(), bd.setScale(2, RoundingMode.CEILING).doubleValue());
		}
	}
	
	public Map<String, Double> getNodesErrVar() {
		return nodesErrVar;
	}
	
	public Map<String, Double> getNodesMean() {
		return nodesMean;
	}
	
	public Map<String, Double> getNodesStdDev() {
		return nodesStdDev;
	}
	
	//--------------------------- Generate Best DAG ----------------------
	
	public Map<String, Double> setCoefficientsOneDAG(Graph dag) {
		SemPm pm = new SemPm(dag);
		ICovarianceMatrix covMatrix = null;
		SemEstimator estimator2 = null;
		if (dataSet == null)
			readContiniousData();
		covMatrix = new CovarianceMatrix(dataSet);
		
		try {
			estimator2 = new SemEstimator(covMatrix, pm);
		} catch (IllegalArgumentException e) {
			System.out.println("Something went wrong. "+ e);
			
		}
        estimator2.estimate();
        SemIm im  = estimator2.getEstimatedSem();
        
        // set Edge Coefficients

        Map<String, Double> edgeCoefficients = new HashMap<String, Double>();
		Set<Edge> edges = dag.getEdges();
		for (Edge edge : edges) {
			BigDecimal bd = BigDecimal.valueOf(im.getEdgeCoef(edge));
			edgeCoefficients.put(edge.toString(), bd.setScale(2, RoundingMode.CEILING).doubleValue());
		}
		
		return edgeCoefficients;
	}
	
	//----------------------------General----------------------------------
	
	public void setNodesAndEdges(Graph graph) {  ///TODO
		
		// adding node names to the nodes field
	     List<String> nodeNames = graph.getNodeNames();
	     nodes = new LinkedList<String>();
	     for (String name : nodeNames)
	    	 nodes.add(name);
	     
	  // adding edge names to the edges field
	     edges = new HashSet<String>();
	     Set<Edge> ee = graph.getEdges();
	     if (!ee.isEmpty())
	    	 for (Edge e : ee)
	    		 edges.add(e.toString()); 
	}
	
	public void addEdge(String edge) {
		int startIdx = edge.indexOf('.')+2;
		edge = edge.substring(startIdx, edge.length());
		edge.replace(" dd nl","");
		edge.replace(" pd pl","");
		edges.add(edge);
	}
	
	public void clearEstimation() {
		effectOfIntervineOnAttributeOnClass = new HashMap<String, Map<String, double[]>>();
		edgeCoefficients = new HashMap<String, Double>();
		nodeCoefficients = new HashMap<String, Double>();
		estimationIsValid = false;
	}
	
	public void setNumberOfNodes(int num) {
		this.numberOfNodes = num;
	}
	
	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}
	
//	public void setIsContinuous(boolean b) {
//		this.isContinuous = b;
///	}
	
//	public boolean getIsContinuous() {
//		return isContinuous;
//	}
	
	public void setDataType(String type) {
		this.dataType = type;
	}
	
	public String getDataType() {
		return this.dataType;
	}
	
	public LinkedList<String> getNodes() {
		return nodes;
	}
	
	public Set<String> getEdges() {
		return edges;
	}
	
	public XLog getLog() {
		return log;
	}
	
	public Petrinet getPetrinet() {
		return model;
	}
	
	public PNRepResult getReplayResult() {
		return res;
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
	
	public Map<String, Double> getEdgeCoefficients() {
		return edgeCoefficients;
	}
	
	public void setKnowledge(Knowledge2 k) {
		this.knowledge = k;
	}
	
	public void setVriableCategories(Map<String, Set<String>> literalValues) {
		this.literalValues = literalValues;
	}
	
	public void setVriableTypes(Map<String, Type> types) {
		this.types = types;
	}	
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public Graph getGraph() {
		return graph; 
	}
	
	public void setAwareParams(Parameters params) {
		this.params = params;
	}
	
	public Parameters getParameters() {
		return params;
	}
	// --------------------------test-------------------------
	
	public void setAttTypes() {
		Map<String, Type> t = new HashMap<String, Type>();
		t.put("T02_Check_confirmation_of_receipt_Resource", Type.LITERAL);
		t.put("Confirmation_of_receipt_Resource", Type.LITERAL);
		t.put("responsible", Type.LITERAL);
		t.put("department", Type.LITERAL);
		t.put("Trace_Delay", Type.LITERAL);
		t.put("group", Type.LITERAL);
		t.put("department", Type.LITERAL);
	}
	
	public void setAttLiteralValues() {
		
	}
	
	public void setAttTypes2() {
		Map<String, Type> t = new HashMap<String, Type>();
		t.put("T", Type.LITERAL);
		t.put("A", Type.LITERAL);
		t.put("S", Type.LITERAL);
		t.put("I", Type.LITERAL);
		t.put("K", Type.LITERAL);
		this.types = t;
	}
	
	public void setAttLiteralValues2() {
		Map<String, Set<String>> allCategories = new HashMap<String, Set<String>>();
		Set<String> categories = new HashSet<String>();
		
		categories.add("0");
		categories.add("1");
		allCategories.put("A", categories);
		
		Set<String> categoriesT = new HashSet<String>();
		categoriesT.add("1");
		categoriesT.add("2");
		allCategories.put("T", categoriesT);
		
		Set<String> categoriesS = new HashSet<String>();
		categoriesS.add("2");
		categoriesS.add("3");
		categoriesS.add("4");
		categoriesS.add("6");
		categoriesS.add("7");
		allCategories.put("S", categoriesS);
		
		Set<String> categoriesK = new HashSet<String>();
		categoriesK.add("1");
		categoriesK.add("2");
		categoriesK.add("3");
		categoriesK.add("4");
		allCategories.put("K", categoriesK);
		
		Set<String> categoriesI = new HashSet<String>();
		categoriesI.add("2");
		categoriesI.add("3");
		categoriesI.add("4");
		allCategories.put("I", categoriesI);
		
		this.literalValues = allCategories;
	}
	
	public void setKnowledge2() {
		LinkedList<String> nodes = new LinkedList<String>();
		nodes.add("A");
		nodes.add("I");
		nodes.add("K");
		nodes.add("T");
		nodes.add("S");
		Knowledge2 knowledge = new Knowledge2(nodes);
		knowledge.setRequired("I", "A");
		knowledge.setRequired("T", "S");
		knowledge.setRequired("T", "I");
		knowledge.setRequired("K", "I");
		this.knowledge = knowledge;
	}
	
	private void setCategories2(BayesPm pm) {
		for(int i = 0; i < pm.getNumNodes(); i++) {
			Node node = pm.getNode(i);
//			System.out.println("i : "+ i + "   node : "+ node.getName());
		}
		System.out.println("-----------------");
		for(int i = 0; i < pm.getNumNodes(); i++) {
			List<String> categories = new ArrayList<String>();
			Node node = pm.getNode(i);
//			System.out.println("i : "+ i + "node : "+ node.getName());
			for (String s : literalValues.get(node.getName()))
				categories.add(s);
			pm.setNumCategories(node, literalValues.get(node.getName()).size());
			pm.setCategories(node, categories);
		}
	}
	
	public Graph inferGraph2() {
		double alpha = significance;
        char delimiter = '\t';
  //      Path dataFilePath = Paths.get("bin\\DataTableOneHot.txt");
        Path dataFilePath = Paths.get(this.path);
        
		VerticalDiscreteTabularDatasetFileReader dataReader = new VerticalDiscreteTabularDatasetFileReader(dataFilePath, DelimiterUtils.toDelimiter(delimiter));
		try {
			dataSet = (DataSet) DataConvertUtils.toDataModel(dataReader.readInData());
		} catch (IOException e) {
			System.out.println("Failed to read in data.");
			e.printStackTrace();
		}
		
		 IndependenceTest indTest = new IndTestChiSquare(dataSet, alpha);
		 BDeuScore score = new BDeuScore(dataSet);
	     score.setStructurePrior(1.0);
	     score.setSamplePrior(1.0);

	     GFci gFci = new GFci(indTest, score);
	     generateKnowledge();
	     if (knowledge != null) {
	    	 gFci.setKnowledge(knowledge);
	     }
	     gFci.setFaithfulnessAssumed(true);
	     gFci.setMaxDegree(-1);
	     gFci.setMaxPathLength(-1);
	     gFci.setCompleteRuleSetUsed(false);
	     gFci.setVerbose(true);

	     
	     Graph graph = gFci.search();
	     
	     setNodesAndEdges(graph);
	    
//	     System.out.println("Graph: ");
//	     for (String s : edges)
//	    	 System.out.println("edge: "+ s);
	     return graph;
	}
	
	public boolean isEqual(int[] first, int[] second) {
		if (first.length != second.length)
			return false;
		for ( int i = 0; i < first.length; i++) 
			if (first[i] != second[i])
				return false;
			
		return true;
	}
	
	public int[] nextOrder(int[] order, int[] dims) {   // why static????
		boolean flag = false;
		int n = dims.length - 1;
		while (!flag && n >= 0) {
			int item = order[n] + 1;
			if (item == dims[n]) {
				order[n] = 0;
				n = n-1;
			} else {
				order[n] = item; 
				flag = true;
			}
		}
		return order;
	}
	
	public Map<String, Map<String, double[]>> getInterventionEffectsOnClass() {
		return effectOfIntervineOnAttributeOnClass;
	}
	
	public void setClassAttName(String actName, String attName) {
		if (actName == null) {
			classAttName = attName;
			return;
		}
	}
	
	public String getClassAttName() {
		return classAttName;
	}
	
	/**
	public void showRemoveNullValuesPupup() {
		JPanel p=new JPanel( new GridLayout(5,1));
		String message = "The null values needs to be removed for estimation.";
		String message1 = "Choose the least percentage of null values in a row to be removed:";
		JLabel label1 = new JLabel(message1);
		p.add(label1);
		NiceIntegerSlider nullThreshold = SlickerFactory.instance().createNiceIntegerSlider("Significance", 0, 100, 50, Orientation.HORIZONTAL);
		p.add(nullThreshold);
		String messag2 = "How to interpolate missing values?";
		JLabel label = new JLabel(message1);
		p.add(label);
		
		JComboBox interpolationMethod = new JComboBox<String>();
		interpolationMethod.setPreferredSize(new Dimension(30, 30));
		interpolationMethod.addItem("Mean");
		interpolationMethod.addItem("Previouse Value");
		interpolationMethod.setSelectedItem("Mean");
		p.add(interpolationMethod);
		
    	int yn=JOptionPane.showConfirmDialog(null, 
				p,message,JOptionPane.YES_NO_OPTION);
		if (yn==JOptionPane.NO_OPTION)
			return;
		Object method = interpolationMethod.getSelectedItem();
		rightPanelCreatTable.dataManipulation.setRemoveNullValues(true);
		rightPanelCreatTable.dataManipulation.setInterpolationMethod((String) method);
		rightPanelCreatTable.dataManipulation.setNullValueThresholdInARow(nullThreshold.getValue());
		rightPanelCreatTable.dataManipulation.rewriteTheFile();
		readContiniousData();
		estimateCausalityGraphContiniousData();
	} */
	
	public CausalityGraph() {
		
		Dag dgraph = new Dag();
		GraphNode n1 = new GraphNode("n1");
		GraphNode n2 = new GraphNode("n1");
		dgraph.addNode(n1);
		dgraph.addNode(n2);
		dgraph.addDirectedEdge(n1, n2);
		
		final DagToPag dagToPag = new DagToPag(dgraph);
		dagToPag.setCompleteRuleSetUsed(false);
		Graph PAG_True = dagToPag.convert();
		//PAG_True = GraphUtils.replaceNodes(PAG_True, data.getVariables());
		
		//Graph rfciPag = runPagCs(data, alpha);

	} 


	public static void main(String[] args) throws InstantiationException, IOException, Exception {
		CausalityGraph cg = new CausalityGraph();
		cg.setDepth(5);
		cg.setSignificance(0.05);
		cg.inferCausalityGraphDiscreteData();
/**		cg.setPath("bin\\DataTableNumerical2.txt");
		cg.setPath("bin\\cc.txt");
		cg.setAttTypes2();
		cg.setAttLiteralValues2();
		cg.setKnowledge2();
		cg.setDepth(5);
		cg.setSignificance(0.05);
		Graph g =cg.inferGraph2();
        Node aa = g.getNode("A");
        Node ii = g.getNode("I");
        Node ss = g.getNode("S");
        Node tt = g.getNode("T");
        Node kk = g.getNode("K");
		g.addDirectedEdge(ii, aa);
		g.addDirectedEdge(tt, ii);
		g.addDirectedEdge(kk, ii);
		g.addDirectedEdge(tt, ss);
		BayesPm bayesPM = new BayesPm(g);
		System.out.println("DAG1 : " + cg.inferGraph2().toString());
		System.out.println(cg.inferGraph2());
		cg.setCategories2(bayesPM);
		EmBayesEstimator estimator = new EmBayesEstimator(bayesPM, cg.dataSet);
		System.out.println("Estimator **************");
//		System.out.println(estimator.);
		BayesIm im = estimator.getEstimatedIm();
		System.out.println("IM **************");
		System.out.println(im);
//		Map<Node, Object[][]> tables = cg.creatProbTables(im, bayesPM);
		System.out.println("DAG : " + im.getDag().toString());
		
		for (int i = 0; i<im.getNumNodes(); i++) {
			Node node = im.getNode(i);
			int idx = im.getNodeIndex(node);
			System.out.println("#parents : " + im.getNumParents(idx) );
			if (im.getNumParents(idx) > 0) {
				int[] v = new int[1];
				v[0] = 1;
				System.out.println("Prob **************");
				LinkedList<int[]> orders = new LinkedList<int[]>();
				for ( int d = 0 ; d < bayesPM.getNumCategories(node); d++) {
					int[] parentIdxs = im.getParents(idx);
					int[] parentDims = im.getParentDims(idx);
					int[] oneOrder = new int[parentIdxs.length];
					for (int oi = 0 ; oi < oneOrder.length; oi++)
						oneOrder[oi] = 0;
					int[] o = new int[parentIdxs.length];
					for (int oi = 0 ; oi < oneOrder.length; oi++)
						o[oi] = 0;
					orders.add(o);
					int[] lastOrder = new int[parentIdxs.length];
					for (int oi = 0 ; oi < lastOrder.length; oi++)
						lastOrder[oi] = parentDims[oi] - 1;
					boolean flag = true;
					while (flag) {
						orders.add(o);
						o = cg.nextOrder(oneOrder, parentDims);
						oneOrder = o;

						if (cg.isEqual(o, lastOrder))
							flag = false;
					}
					
					for (int[] or : orders)
						for (int n = 0 ; n < bayesPM.getNumCategories(node); n++ )
							System.out.println("prob : "+ im.getProbability(idx, im.getRowIndex(idx, or), n));	
					
				}
			}
			
			System.out.println("i : "+ i + "   node : "+ node.getName()+ " #row : " + im.getNumRows(idx)+ " #col : " + im.getNumColumns(idx));
			for (int k = 0; k < im.getNumRows(idx); k++) {
				String s = new String();
				for (int j=0; j < im.getNumColumns(idx); j++)
					s = s + " ;  " + im.getProbability(idx, k, j);
				System.out.println(s);
			}
			System.out.println("----------------------");
		}
			
		System.out.println("here");
		
		cg.setDataType("Continious");
		cg.inferCausalityGraph();
		System.out.println("here");  */
	}
	
	public void setForbiddenEdges(Set<String> edges) {
		forbiddenEdges = edges;
	}
	
	public Set<String> getForbiddenEdges() {
		return forbiddenEdges;
	}
	
	public void setRequiredEdges(Set<String> edges) {
		requiredEdges = edges;
	}
	
	public Set<String> getRequiredEdges() {
		return requiredEdges;
	}

	public void resetEdgeCoefficients() {
		edgeCoefficients = new HashMap<>();
		
	}
	
	public void setDelimiter(char c) {
		delimiter = c;
	}

	public Node getNode(String attName) {
		return graph.getNode(attName);
	}
	
	/**
	 * Write the graph to a text file.
	 * the information recorded includes
	 * edges
	 * ancestors of each node
	 */
	private void writeGraphToFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("graphStructure.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writer.println("Edges:");
		for (Edge e : graph.getEdges()) 
			writer.println(e);
		
		writer.println("Adjacency:");
		for (Node n : graph.getNodes()) {
			writer.println(n.getName() );
			writer.println(adjacency(n));
		}
		writer.close();
		
	}
	/**
	 * returns the set of nodes that are adjacent to node n. 
	 * Direction does not matter
	 * @param n
	 * @return
	 */
	private String adjacency(Node n) {
		String s = new String();
		for (Edge edge : graph.getEdges()) {
			String e = edge.toString();
			if (e.contains(n.getName())) {
				s = s + " /// " + e.replace(n.getName(), " ");
			}
		}
		return s;
	}

}


