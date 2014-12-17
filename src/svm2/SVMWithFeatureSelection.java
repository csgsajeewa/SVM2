package svm2;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import java.io.File;
import java.util.Random;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.GridSearch;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.experiment.InstanceQuery;
import weka.filters.AllFilter;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * SVM with features selected through information gain
 *
 * @author chamath
 */
public class SVMWithFeatureSelection {

    public static void main(String[] args) {
        System.out.println("---------------------------");
        System.out.println("SVM with features selected through information gain");
        System.out.println("---------------------------");
        SVMWithFeatureSelection svm = new SVMWithFeatureSelection();
        try {
            svm.saveTrainingDataToFile();
            svm.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @throws Exception
     */
    public void run() throws Exception {

//      LibSVM --> initialize the model and set SVM type and kernal type
        System.out.println("-------------------------------------------------");
        System.out.println("Running LIBSVM WIth Atrribute Selection");
        System.out.println("---------------------------------------------------");

        LibSVM svm = new LibSVM();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.001953125 -W 10 1"; //-C 3 -G 0.00048828125"
        svm.setOptions(weka.core.Utils.splitOptions(svmOptions));
        System.out.println("SVM Type and Keranl Type= " + svm.getSVMType() + svm.getKernelType());//1,3 best result 81%
        svm.setNormalize(true);

//      load training data from .arff file
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\trainingData8088InstancesLemmatizer.arff");
        System.out.println("\n\nLoaded data:\n\n" + source.getDataSet());
        Instances dataFiltered = source.getDataSet();
        dataFiltered.setClassIndex(0);
        System.out.println("number of attributes after loading from file: " + dataFiltered.numAttributes());

        //meta classifier for attribute selection - not much difference in results 
        AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();

        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);

        classifier.setClassifier(svm);
        classifier.setEvaluator(eval);
        classifier.setSearch(search);

//      attribute selection filter- specify a threshold value to cut off certain number of features///////////////
        AttributeSelection filter = new AttributeSelection();
        InfoGainAttributeEval eval1 = new InfoGainAttributeEval();
        eval1.setOptions(weka.core.Utils.splitOptions("-T 0.05"));
        Ranker r1 = new Ranker();
        filter.setEvaluator(eval1);
        filter.setSearch(r1);
        filter.setInputFormat(dataFiltered);
        // filter data
        Instances newData = Filter.useFilter(dataFiltered, filter);
        System.out.println("number of attributes after attribute selection: " + newData.numAttributes());
        evaluate(newData, svm);


    }

    /**
     *
     * @param svm
     * @param dataFiltered
     * @throws Exception
     */
    public void gridSearch(LibSVM svm, Instances dataFiltered) throws Exception {
        GridSearch gs = new GridSearch();
        gs.setClassifier(svm);
        gs.setFilter(new AllFilter());



        gs.setXProperty("classifier.cost");
        gs.setXMin(-5);
        gs.setXMax(15);
        gs.setXStep(2);
        gs.setXBase(2);
        gs.setXExpression("pow(BASE,I)");

        gs.setYProperty("classifier.gamma");
        gs.setYMin(-15);
        gs.setYMax(3);
        gs.setYStep(2);
        gs.setYBase(2);
        gs.setYExpression("pow(BASE,I)");
        //-y-property classifier.kernel.gamma -y-min -5.0 -y-max 2.0 -y-step 1.0 -y-base 10.0 -y-expression pow(BASE,I) -filter weka.filters.AllFilter -x-property classifier.nu -x-min 0.01 -x-max 1.0 -x-step 10.0 -x-base 10.0 -x-expression I -sample-size 100.0 -traversal COLUMN-WISE -log-file "C:\Program Files\Weka-3-6" -S 1 -W weka.classifiers.functions.LibSVM -- -S 2 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.0010 -P 0.1

        int EVALUATION_CC = 0;
        int EVALUATION_RMSE = 1;
        int EVALUATION_RRSE = 2;
        int EVALUATION_MAE = 3;
        int EVALUATION_RAE = 4;
        int EVALUATION_COMBINED = 5;
        int EVALUATION_ACC = 6;
        int EVALUATION_KAPPA = 7;
        Tag[] TAGS_EVALUATION = {
            new Tag(EVALUATION_CC, "CC", "Correlation coefficient"),
            new Tag(EVALUATION_RMSE, "RMSE", "Root mean squared error"),
            new Tag(EVALUATION_RRSE, "RRSE", "Root relative squared error"),
            new Tag(EVALUATION_MAE, "MAE", "Mean absolute error"),
            new Tag(EVALUATION_RAE, "RAE", "Root absolute error"),
            new Tag(EVALUATION_COMBINED, "COMB", "Combined = (1-abs(CC)) + RRSE + RAE"),
            new Tag(EVALUATION_ACC, "ACC", "Accuracy"),
            new Tag(EVALUATION_KAPPA, "KAP", "Kappa")
        };
        SelectedTag st = new SelectedTag(EVALUATION_ACC, TAGS_EVALUATION);
        System.out.println(st.getTags());
        gs.setEvaluation(st);
        //newly added
        gs.setGridIsExtendable(true);
        //
        gs.setDebug(true);

        gs.buildClassifier(dataFiltered);
        System.out.println("Criteria " + gs.getEvaluation().getSelectedTag().getID());
        System.out.println("&&&&&&&&&&&&" + gs.getValues());
    }

    /**
     * save training data to file
     *
     * @throws Exception
     */
    public void saveTrainingDataToFile() throws Exception {
        System.out.println("--------------------------------------------");
        System.out.println("Save Training Data To File");
        System.out.println("---------------------------------------------");
        //set tokenizer - we can specify n-grams for classification
        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(1);
        tokenizer.setDelimiters("\\W");

        //set stemmer - set english stemmer
        SnowballStemmer stemmer = new SnowballStemmer();
        stemmer.setStemmer("english");

        //set lemmatizer
        StanfordCoreNLPLemmatizer scnlpl = new StanfordCoreNLPLemmatizer();

        //create new filter for vector transformation
        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setOutputWordCounts(true);
        //Tf–idf can be successfully used for stop-words filtering in various subject fields including text summarization and classification.
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR4.txt"));
        filter.setTokenizer(tokenizer);
        filter.setStemmer(scnlpl);

        /**
         * feature reduction- similar as stop word removal , we can easily
         * remove person names,towns etc. If you only want to have the 100 most
         * frequent terms, you stringToWordVector.setWordsToKeep(100). Note that
         * this will try to keep 100 words of every class. If you do not want to
         * keep 100 words per class,
         * stringToWordVector.setDoNotOperateOnPerClassBasis(true). You will get
         * slightly above 100 if there are several words with the same
         * frequency, so the 100 is just a kind of target value.
         */
        filter.setWordsToKeep(200);
        filter.setDoNotOperateOnPerClassBasis(false);

        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        int numOfNewsPapers = 5;
        Instances[] instances = new Instances[5];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` IS NOT NULL");
        instances[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` IS NOT NULL");
        instances[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` IS NOT NULL");
        instances[2] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL");
        instances[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` IS NOT NULL");
        instances[4] = query.retrieveInstances();

        FastVector attributeList = new FastVector(2);
        Attribute a1 = new Attribute("text", (FastVector) null);
        FastVector classVal = new FastVector();
        classVal.addElement("crime");
        classVal.addElement("other");
        Attribute c = new Attribute("@@class@@", classVal);
        //add class attribute and news text
        attributeList.addElement(a1);
        attributeList.addElement(c);
        Instances trainingData = new Instances("TrainingNews", attributeList, 0);
        trainingData.setClassIndex(1);

        int count = 0;
        for (int i = 0; i < numOfNewsPapers; i++) {
            for (int j = 0; j < instances[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, instances[i].instance(j).stringValue(0));
                inst.setValue(c, instances[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                count++;
            }
        }


        for (int k = 0; k < numOfNewsPapers; k++) {
            System.out.println("Num of articles in " + k + " " + instances[k].numInstances());
        }
        System.out.println("Total Num of Insatances= " + count);


        // apply the StringToWordVector filter
        filter.setInputFormat(trainingData);
        Instances dataFiltered = Filter.useFilter(trainingData, filter);
        System.out.println("Number of Attributes after stop words removal- " + dataFiltered.numAttributes());
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);


        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataFiltered);
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\trainingData.arff"));
        saver.writeBatch();
    }

    /**
     * evaluate svm
     *
     * @param newData
     * @param svm
     * @throws Exception
     */
    private void evaluate(Instances newData, LibSVM svm) throws Exception {

        Evaluation evaluation = new Evaluation(newData);
        evaluation.crossValidateModel(svm, newData, 10, new Random(1));
        System.out.println(evaluation.toSummaryString());
        System.out.println(evaluation.weightedAreaUnderROC());
        double[][] confusionMatrix = evaluation.confusionMatrix();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(confusionMatrix[i][j] + "  ");

            }
            System.out.println();
        }
        System.out.println("accuracy for crime class= " + (confusionMatrix[0][0] / (confusionMatrix[0][1] + confusionMatrix[0][0])) * 100 + "%");
        System.out.println("accuracy for other class= " + (confusionMatrix[1][1] / (confusionMatrix[1][1] + confusionMatrix[1][0])) * 100 + "%");

    }
}
