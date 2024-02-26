package org.processmining.dataTable.Augmentation;

import java.nio.file.Path;
import java.nio.file.Paths;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.AlgorithmFactory;
import edu.cmu.tetrad.algcomparison.algorithm.oracle.pattern.Fges;
import edu.cmu.tetrad.algcomparison.independence.ConditionalGaussianLRT;
import edu.cmu.tetrad.algcomparison.score.ConditionalGaussianBicScore;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.DataConvertUtils;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.cmu.tetrad.util.Parameters;
import edu.pitt.dbmi.data.reader.Data;
import edu.pitt.dbmi.data.reader.DataColumn;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularColumnReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataFileReader;
import edu.pitt.dbmi.data.reader.tabular.TabularDataReader;

/**
 * Author : Jeremy Espino MD
 * Created  10/15/19 6:47 PM
 */
public class FgsApiExample {

    public static void main(String[] args) throws Exception {

        // set path to data
        Path dataFile = Paths.get("/IdeaProjects/tetrad/tetrad-lib/src/test/resources/","iq_brain_size.tetrad.txt");

        Delimiter delimiter = Delimiter.WHITESPACE;
        int numberOfCategories = 2;
        boolean hasHeader = true;
        boolean isDiscrete = false;


        // tabular data is our input (can also use covariance)
        TabularColumnReader columnReader = new TabularColumnFileReader(dataFile, delimiter);

        // define the column variables
        DataColumn[] dataColumns;

        // read in the data column types

        // optionally skip columns you don't want (e.g., the row id)
        // dataColumns = columnReader.readInDataColumns(new int[]{0},isDiscrete);

        // read in all columns
        dataColumns = columnReader.readInDataColumns(isDiscrete);


        // setup data reader
        TabularDataReader dataReader = new TabularDataFileReader(dataFile, delimiter);

        // if this is a mixed dataset determine which columns are discrete i.e, updates dataColumns values from all continuous
        dataReader.determineDiscreteDataColumns(dataColumns, numberOfCategories, hasHeader);


        // actually read in the data
        Data data = dataReader.read(dataColumns, hasHeader, null)  ;
        DataModel dataModel = DataConvertUtils.toDataModel(data);


        // instantiate instance of the FGES using our algorithm factory which allows you to easily swap out ind. tests and scores

        // for continuous data can use sem bic score
        //Algorithm fges = AlgorithmFactory.create(Fges.class, null, SemBicScore.class);

        // for mixed data can use Conditional Gaussian
        Algorithm fges = AlgorithmFactory.create(Fges.class, ConditionalGaussianLRT.class, ConditionalGaussianBicScore.class);


        // we've standardized parameters using the manual i.e., the id names in the manual are the names to use, also in Params class
        Parameters parameters = new Parameters();
        ParamDescriptions paramDescriptions = ParamDescriptions.getInstance();

        // fges parameters
        parameters.set("depth", 100);
        parameters.set("penaltyDiscount", 1.0);
        parameters.set("faithfulnessAssumed", true);
        parameters.set("verbose", true);
        parameters.set("symmetricFirstStep", false);

        // conditional gaussian parameters if using that test and score
        parameters.set("discretize", false);

        // perform the search
        Graph graph = fges.search(dataModel, parameters);

        // output the graph
        System.out.println();
        System.out.println(graph.toString().trim());
        System.out.flush();

    }

}

