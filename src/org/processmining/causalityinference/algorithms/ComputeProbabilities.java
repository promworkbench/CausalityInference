package org.processmining.causalityinference.algorithms;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;

public class ComputeProbabilities {
	String[] header;
	Object[][] body;
	Map<String, Map<Integer, String>> inverseMapCategories;
	BayesPm pm;
	BayesIm im;
	
	public ComputeProbabilities(String[] header, Object[][] body, Map<String, Map<Integer, String>> inverseNumericalValueMap, BayesPm pm, BayesIm im) {
		this.header = header;
		this.body = body;
		this.pm = pm;
		this.im = im;
		if (inverseNumericalValueMap != null)
			this.inverseMapCategories = inverseNumericalValueMap;
		else
			this.inverseMapCategories = null;
	}
	/** 
	 * 
	 * @param X
	 * @param Y
	 * @return It returns a map in which each entry is related to a nod
	 * except Y node ) and its value is a map including of all values x of X and 
	 * the Prob(do X = x)(Y) which is an array including all
	 * Prob(do X = x)(y) for all x \in values(X) and y \in values(Y). 
	 * Prob(do X = x)(y) is computed as \sum_z P(y|x,z)p(z) for all z \in values(Z) where Z = Z_1,..., Z_n is the list 
	 *  of parents of X.
	 *  the output is of the format < X , <category x in Values(X), [Prob(do X = x)(Y=y)]>>.
	 */
	public Map<String, Map<String, double[]>> getProbability(Node Y) {
		Map<String, Map<String, double[]>> result = new HashMap<String, Map<String, double[]>>();
		
		for (int j = 0; j < im.getNumNodes(); j++)
			if (!im.getNode(j).equals(Y)) {
				Node X = im.getNode(j);
				D_separationTest dst = new D_separationTest(im, pm);
				boolean test2 = dst.isThereAnyDirectedPath(im.getNode(j), Y);
				System.out.println("Is connected : " + test2);
				boolean test = dst.isD_separate(im.getNode(j), Y);
				System.out.println("if D-separated : " + test);
				if (!test && test2) {
					Map<String, double[]> probXY = new HashMap<String, double[]>();
					Map<Node, String[]> parentNodeCategories = getCategoriesOfParents(X);
	//				System.out.println("num categories : " + pm.getNumCategories(X));
					for (int i = 0; i < pm.getNumCategories(X); i++) {
						String x = pm.getCategory(X, i);
						double[] probforOneCategoryOfX = prob(X, x, Y ,  parentNodeCategories);
						probXY.put(x, probforOneCategoryOfX);
					}
					result.put(X.getName(), probXY);
//					System.out.println("none");
				}
			}

		
		
		return result;
	}
	
	
	/** 
	 * It returns all the Prob(do X = x)(Y) which is an array including all
	 * Prob(do X = x)(y) for all x \in values(X) and y \in values(Y). 
	 * Prob(do X = x)(y) is computed as \sum_z P(y|x,z)p(z) for all z \in values(Z) where Z = Z_1,..., Z_n is the list 
	 *  of parents of X.
	 *  the output is of the format <category x in Values(X), [Prob(do X = x)(Y=y)].
	 * @param X
	 * @param Y
	 * @return
	 */
	public Map<String, double[]> getProbability(Node X, Node Y) {
		
		Map<String, double[]> probXY = new HashMap<String, double[]>();
		Map<Node, String[]> parentNodeCategories = getCategoriesOfParents(X);
		
		int numValues = pm.getNumCategories(Y);
		for (Node node : parentNodeCategories.keySet()) 
			numValues = numValues * parentNodeCategories.get(node).length;
		for (int i = 0; i < pm.getNumCategories(X); i++) {
			String x = pm.getCategory(X, i);
			double[] probforOneCategoryOfX = prob(X, x, Y ,  parentNodeCategories);
			probXY.put(x, probforOneCategoryOfX);
		}
		
		return probXY;
	}
	
	/**
     *
	 * @param X
	 * @return It returns a Map in which for each parent Z of X, there is an entry
	 * <Z, categories of Z> in the Map.
	 */
	public Map<Node, String[]> getCategoriesOfParents(Node X) {
		int XIdx = getNodeIndex(X);
		
		int[] parentIdxs = im.getParents(XIdx);
		Node[] parentNodes = new Node[im.getNumParents(XIdx)];
		for (int i = 0; i < im.getNumParents(XIdx); i++)
			parentNodes[i] = im.getNode(parentIdxs[i]);
		Map<Node, String[]> parentNodeCategories = new HashMap<Node, String[]>();
		for (Node node : parentNodes) {
			String[] categories = new String[pm.getNumCategories(node)];
			for (int j = 0 ; j < pm.getNumCategories(node); j++)
				categories[j] = pm.getCategory(node, j);
			parentNodeCategories.put(node, categories);
		}
		
		return parentNodeCategories;
	}
	
	public int getNodeIndex(Node node) {
		int XIdx = 0;
		for (int i = 0; i < im.getNumNodes(); i++)
			if (im.getNode(i).getName().equals(node.getName()))
				XIdx = i;
		return XIdx;
	}
	
	/**
	 * 
	 * @param X
	 * @param x
	 * @param Y
	 * @param y
	 * @param parentNodeCategories
	 * @return This function returns an array of doubles which is Prob(do X=x)(y) for all 
	 * y \in Y and this probability is computed
	 * as \sum_z P(y|x,z)p(z) for all z \in values(Z) where Z = Z_1,..., Z_n
	 *  is the list of parents of X.
	 */
	public double[] prob(Node X, String x, Node Y ,Map<Node, String[]>  parentNodeCategories) {
		// Here at the begining we change the indices of variables to those in the table.
		int xIdx = getIdxInTable(X);
		int yIdx = getIdxInTable(Y);
		int[] ZIdxs = new int[parentNodeCategories.size()];
		double[] sumProb = new double[pm.getNumCategories(Y)];
		int idx = 0;
		if (ZIdxs.length == 0) {
			sumProb = ConditionalProb(x, X, Y);
		} else {
			for (Node node :  parentNodeCategories.keySet()) {
				ZIdxs[idx] =  getIdxInTable(node);
				idx++;
			}
			
			//----------------------------
			String[][] allCategoryCombinationsOfZ = getAllCategoryCombinationsOfZ(parentNodeCategories);
			int nZ = numZinTable(X, Y, ZIdxs); // count the number of rows in the table with any combination of values
			// for Z where X and Y are not null;
			
			for (int i = 0; i < allCategoryCombinationsOfZ.length; i++) {

				String[] oneCombination = allCategoryCombinationsOfZ[i];
				
				//num[0] := #(XZy_0 combination)
				//num[1] := #(XZy_1 combination)
				//....
				//num[n] := #(XZy_n combination)
				//num[n+1] := #(XZ combination)
				//num[n+2] := #(Z combination)
				int[] num = numberOfXYZcombiationInTable(X, x, Y, oneCombination, ZIdxs);  
				double[] probOneCombiantion = probOneCombiationZ(num, nZ);
				for (int j = 0; j < sumProb.length; j++) 
					sumProb[j] = sumProb[j] + probOneCombiantion[j];
//				System.out.println("prob");
			}
			
			double sum = 0;
			for (int j = 0; j < sumProb.length; j++) 
				sum = sum + sumProb[j];
			for (int j = 0; j < sumProb.length; j++) 
				if (sum == 0)
					sumProb[j] = 0;
				else
					sumProb[j] = sumProb[j]/sum;
		}
		return sumProb;
	}
	
	/**
	 * 
	 * @param x  -->  x \in X (a category of X)
	 * @param X
	 * @param Y
	 * @return
	 */
	public double[] ConditionalProb(String x, Node X, Node Y) {
		int xIdx = getIdxInTable(X);
		int yIdx = getIdxInTable(Y);
		int[][] num = new int[pm.getNumCategories(Y)][2];
		
		for (int rowIdx = 0; rowIdx < body.length; rowIdx++) {
			for (int i = 0; i < pm.getNumCategories(Y); i++) {
				if (hasCombination(rowIdx, xIdx, x, yIdx, pm.getCategory(Y, i))) {
					num[i][0]++;
				} 
				
				if (hasCombination(rowIdx, xIdx, x, -1, pm.getCategory(Y, i))) {
					num[i][1]++;
				}
			}
		}
		
		double[] probs = new double[pm.getNumCategories(Y)];
		for(int i = 0; i < pm.getNumCategories(Y); i++) {
			if (num[i][1] ==0)
				probs[i] = 0;
			else
				probs[i] = (Double.valueOf(num[i][0])) / Double.valueOf(num[i][1]);
		}
		
		return probs;
	}
		
	public boolean hasCombination(int rowIdx, int xIdx, String x, int yIdx, String y) {
		// If x has happened in that row
		if (yIdx == -1) 
			if (body[rowIdx][xIdx].toString().equals(x.toString()))
				return true;
			else
				return false;
		
//		System.out.println(body[rowIdx][xIdx]);
//		System.out.println(x);
//		System.out.println(body[rowIdx][yIdx]);
//		System.out.println(y);
		
		//If both x and y has append in that row
		if (body[rowIdx][xIdx].toString().equals(x.toString()) && body[rowIdx][yIdx].toString().equals(y.toString()))
			return true;
		return false;
	}
	
	public double[] probOneCombiationZ(int[] num, int nZ) {
		int n = num.length - 2;
		double d = 1d;
		double[] probs = new double[n];
		for (int i = 0; i < probs.length; i++) 
			probs[i] = 0;
		if (num[n] != 0 && nZ != 0)
			for (int i = 0; i < n; i++) {
//				System.out.println(Double.valueOf(num[i]));
//				System.out.println(Double.valueOf(num[n]));
//				System.out.println(Double.valueOf(num[n+1]));
//				System.out.println(Double.valueOf(nZ));
				probs[i] = ((Double.valueOf(num[i]))/Double.valueOf(num[n]))*(Double.valueOf(num[n+1])/Double.valueOf(nZ));
			}
		return probs;
	}
	
	public int numZinTable(Node X, Node Y, int[] ZIdxs) {
		int xIdx = getIdxInTable(X);
		int yIdx = getIdxInTable(Y);
		int num = 0;
		for (int rowIdx = 0; rowIdx < body.length; rowIdx++) 
			if (hasNoMissingValue(rowIdx, xIdx, yIdx, ZIdxs)) 
				num++;
		return num;
	}
	
	/**
	 * 
	 * @param X
	 * @param x
	 * @param Y
	 * @param oneCombination
	 * @param ZIdxs
	 * @return  an array of length number of categories of Y + 2 for which
	 *   i-th elementh is the number of times that the combination xy_iz has happend
	 *   in the table.
	 *    n-th element --> #(xz)
	 *    (n+1)-th element --> #(z)
	 */
	public int[] numberOfXYZcombiationInTable(Node X, String x, Node Y, String[] oneCombination, int[] ZIdxs) {
		int xIdx = getIdxInTable(X);
		int yIdx = getIdxInTable(Y);
		int[] num = new int[pm.getNumCategories(Y) + 2];
//		System.out.println("X : " + X.getName() + " Y : "+ Y.getName());
		for (int rowIdx = 0; rowIdx < body.length; rowIdx++) {
			if (hasNoMissingValue(rowIdx, xIdx, yIdx, ZIdxs)) 
				if (hasZCombination(rowIdx, ZIdxs, oneCombination)) {
				//	System.out.println((String) body[rowIdx][yIdx]);
					num[num.length - 1]++;
					if (body[rowIdx][xIdx].toString().equals(x.toString())) {
						num[num.length - 2]++;
		//				System.out.println("row : "+ rowIdx + " yIdx : "+ yIdx +" value : "+(String) body[rowIdx][yIdx]);
						num[pm.getCategoryIndex(Y, body[rowIdx][yIdx].toString())]++;
		//				for (int i = 0; i < pm.getNumCategories(Y); i++)
		//					
		//					if (body[rowIdx][yIdx].equals(pm.getCategory(Y, i))) {
		//						System.out.println(pm.getCategory(Y, i));
		//						num[i]++;
		//					}
					}
				}
		}
		
		return num;
	}
		
	public boolean hasZCombination(int rowIdx, int[] ZIdxs, String[] oneCombination) {
		for (int i = 0; i < ZIdxs.length; i++)
			if (!body[rowIdx][ZIdxs[i]].equals(Integer.valueOf(oneCombination[i])))
				return false;
		return true;
	}
	
	public boolean hasNoMissingValue(int rowIdx, int xIdx, int yIdx, int[] ZIdxs) {
		if (body[rowIdx][xIdx].equals("*") || body[rowIdx][yIdx].equals("*"))
			return false;
		for (int i = 0; i < ZIdxs.length; i++) 
			if (body[rowIdx][ZIdxs[i]].equals("*"))
				return false;
		
		return true;
	}
	
	public String[][] getAllCategoriesCombinationsOfXYZ(Node X, Node Y, Map<Node, String[]>  parentNodeCategories) {
		// computing the number of categories
		int num = 1; 
		Node[] parentOrder = new Node[parentNodeCategories.size()+2];
		int i = 0;
		for (Node node : parentNodeCategories.keySet()) {
			num = num * parentNodeCategories.get(node).length;
			parentOrder[i] = node;
			i++;
		}
		
		// computing the number of repeats
		// the index of X is 0
		// the index of Y is 1
		int r = 1;
		int[] numRepeat = new int[parentNodeCategories.size()+2];
		for (int j = parentNodeCategories.size()+1; j > 1; j--) {
			numRepeat[j] = r;
			r = r * parentNodeCategories.get(parentOrder[j]).length;
		}
		r = r * pm.getNumCategories(Y);
		numRepeat[1] = r;
		r = r * pm.getNumCategories(X);
		numRepeat[0] = r;
		
		String[][] combinations = new String[parentNodeCategories.size()][num];
		
		for (int colIdx = 0; colIdx < parentNodeCategories.size()+2; colIdx++) {
			int rowIdx = 0;
			int categoryIdx = 0;
			while (rowIdx < num) {
				String[] categories = null;
				if (colIdx == 0)
					categories = getCategories(X);
				else if (colIdx == 1)
					categories = getCategories(Y);
				else
					categories = parentNodeCategories.get(parentOrder[colIdx-2]);
					
				for (int numR = 0; numR < numRepeat[colIdx]; numR++) {
					combinations[rowIdx][colIdx] = categories[categoryIdx];
					rowIdx++;
				}
				categoryIdx++;
				if(categoryIdx == categories.length)
					categoryIdx = 0;
			}
		}
		
		return combinations;
	}
	
	public String[][] getAllCategoryCombinationsOfZ( Map<Node, String[]>  parentNodeCategories) {
		// computing the number of categories
		int num = 1; 
		Node[] parentOrder = new Node[parentNodeCategories.size()];
		int i = 0;
		for (Node node : parentNodeCategories.keySet()) {
			num = num * parentNodeCategories.get(node).length;
			parentOrder[i] = node;
			i++;
		}
		
		// computing the number of repeats
		// the index of X is 0
		// the index of Y is 1
		int r = 1;
		int[] numRepeat = new int[parentNodeCategories.size()];
		for (int j = 0; j < parentNodeCategories.size(); j++) {
			numRepeat[j] = r;
			r = r * parentNodeCategories.get(parentOrder[j]).length;
		}
		
		String[][] combinations = new String[num][parentNodeCategories.size()];
		
		for (int colIdx = 0; colIdx < parentNodeCategories.size(); colIdx++) {
			int rowIdx = 0;
			int categoryIdx = 0;
			while (rowIdx < num) {
				String[] categories = null;
				categories = parentNodeCategories.get(parentOrder[colIdx]);
					
				for (int numR = 0; numR < numRepeat[colIdx]; numR++) {
			//		System.out.println(" row: "+ rowIdx + " col: "+ colIdx + " category "+ categories[categoryIdx]);
					combinations[rowIdx][colIdx] = categories[categoryIdx];
					rowIdx++;
				}
				categoryIdx++;
				if(categoryIdx == categories.length)
					categoryIdx = 0;
			}
		}
		
		return combinations;
	}
	
	
	public String[] getCategories(Node node) {
		String[] categories = new String[pm.getNumCategories(node)];
		for (int i = 0; i < pm.getNumCategories(node); i++)
			categories[i] = pm.getCategory(node, i);
		return categories;
	}
	
	public int getIdxInTable(Node X) {
		for (int i = 0; i <header.length; i++) 
			if (header[i].equals(X.getName()))
				return i;
			
		return -1;
	}
}
