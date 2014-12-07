/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import weka.filters.supervised.instance.SMOTE;

public class SVMWithBalancedData {

    public static void main(String[] args) {
        
        System.out.println("-------------------------------------------------");
        System.out.println("Running LIBSVM with balanced data");
        System.out.println("---------------------------------------------------");

        SVMWithBalancedData wekaTestDB = new SVMWithBalancedData();
        try {
         // wekaTestDB.saveTrainingDataToFileHybridSampling();
          wekaTestDB.testCrossValidataion();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testCrossValidataion() throws Exception {

        

//      LibSVM --> initialize the model and set SVM type and kernal type
        LibSVM svm = new LibSVM();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.00048828125"; //Final result: [3.0, -11.0]
        svm.setOptions(weka.core.Utils.splitOptions(svmOptions));
        System.out.println("SVM Type and Keranl Type= " + svm.getSVMType() + svm.getKernelType());//1,3 best result 81%
        //     svm.setNormalize(true);


//      load training data from .arff file
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\balancedTrainingDataHybrid.arff");
        System.out.println("\n\nLoaded data:\n\n" + source.getDataSet());
        Instances dataFiltered = source.getDataSet();
        dataFiltered.setClassIndex(0);

        //     gridSearch(svm, dataFiltered);
        Evaluation evaluation = new Evaluation(dataFiltered);
        evaluation.crossValidateModel(svm, dataFiltered, 10, new Random(1));
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
        System.out.println("accuracy for crime class= " + evaluation.truePositiveRate(0) + "%");
        System.out.println("accuracy for other class= " + evaluation.truePositiveRate(1) + "%");


    }

    private Instances createTestInstancesFromDB() throws Exception {

        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");
        query.setQuery("SELECT content, label FROM article_ceylon_today");
        Instances data = query.retrieveInstances();


        FastVector attributeList = new FastVector(2);
        Attribute a1 = new Attribute("text", (FastVector) null);
        FastVector classVal = new FastVector();
        classVal.addElement("crime");
        classVal.addElement("non-crime");
        Attribute c = new Attribute("@@class@@", classVal);
        //add class attribute and news text
        attributeList.addElement(a1);
        attributeList.addElement(c);
        Instances testData = new Instances("TestNews", attributeList, 0);
        if (testData.classIndex() == -1) {
            testData.setClassIndex(1);
        }

        for (int i = 0; i < data.numInstances(); i++) {
            Instance inst = new Instance(testData.numAttributes());
            inst.setValue(a1, data.instance(i).stringValue(0));
            inst.setDataset(testData);
            inst.setClassMissing();

            System.out.println(inst);
            testData.add(inst);
        }
        return testData;
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

    public void saveTrainingDataToFileDownSampling() throws Exception {
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
        filter.setLowerCaseTokens(true); //ok accepted
        filter.setOutputWordCounts(true); // ok accepted given in research papers
        filter.setTFTransform(true); // normalization as given in scikit
        filter.setIDFTransform(true); // normalization as given in scikit       
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR2.txt")); // stop word removal given in research paper
        filter.setTokenizer(tokenizer); // given in research papers
        filter.setStemmer(scnlpl); // given in research papers as stemming , here we go forward as use lemmatizer
        // feature selection has not performed as "how to" paper say that it wont improve accuracy in SVM and our dictionary
        // is small


        System.out.println("Stemmer Name- " + filter.getStemmer());


        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        int numberOfPapers = 5;

        Instances[] otherArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'other'");
        otherArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='other'");
        otherArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='other'");
        otherArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'other'");
        otherArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'other'");
        otherArticles[4] = query.retrieveInstances();

        Instances[] crimeArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'crime'");
        crimeArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='crime'");
        crimeArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='crime'");
        crimeArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'crime'");
        crimeArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'crime'");
        crimeArticles[4] = query.retrieveInstances();




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

        int crimeArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < crimeArticles[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, crimeArticles[i].instance(j).stringValue(0));
                inst.setValue(c, crimeArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                crimeArticlesCount++;
            }
        }
        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);

        int otherArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < otherArticles[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, otherArticles[i].instance(j).stringValue(0));
                inst.setValue(c, otherArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                otherArticlesCount++;

                if (otherArticlesCount == crimeArticlesCount) {
                    break;
                }
            }
            if (otherArticlesCount == crimeArticlesCount) {
                break;
            }
        }
        System.out.println("Total Number of Other Instances: " + otherArticlesCount);

        System.out.println("Total num of instances= " + trainingData.numInstances());


        // apply the StringToWordVector filter
        filter.setInputFormat(trainingData);
        Instances dataFiltered = Filter.useFilter(trainingData, filter);
        System.out.println("Number of Attributes after stop words removal- " + dataFiltered.numAttributes());
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);


        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataFiltered);
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\balancedTrainingDataDS.arff"));
        saver.writeBatch();
    }

    public void saveTrainingDataToFileUpSampling() throws Exception {
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
        filter.setLowerCaseTokens(true); //ok accepted
        filter.setOutputWordCounts(true); // ok accepted given in research papers
        filter.setTFTransform(true); // normalization as given in scikit
        filter.setIDFTransform(true); // normalization as given in scikit       
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR2.txt")); // stop word removal given in research paper
        filter.setTokenizer(tokenizer); // given in research papers
        filter.setStemmer(scnlpl); // given in research papers as stemming , here we go forward as use lemmatizer
        // feature selection has not performed as "how to" paper say that it wont improve accuracy in SVM and our dictionary
        // is small


        System.out.println("Stemmer Name- " + filter.getStemmer());


        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        int numberOfPapers = 5;

        Instances[] otherArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'other'");
        otherArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='other'");
        otherArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='other'");
        otherArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'other'");
        otherArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'other'");
        otherArticles[4] = query.retrieveInstances();

        Instances[] crimeArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'crime'");
        crimeArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='crime'");
        crimeArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='crime'");
        crimeArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'crime'");
        crimeArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'crime'");
        crimeArticles[4] = query.retrieveInstances();




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

        int crimeArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < crimeArticles[i].numInstances(); j++) {
                for (int k = 0; k < 5; k++) {
                    Instance inst = new Instance(trainingData.numAttributes());
                    inst.setValue(a1, crimeArticles[i].instance(j).stringValue(0));
                    inst.setValue(c, crimeArticles[i].instance(j).stringValue(1));
                    inst.setDataset(trainingData);


                    System.out.println(inst);
                    trainingData.add(inst);
                    crimeArticlesCount++;
                }
            }
        }
        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);

        int otherArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < otherArticles[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, otherArticles[i].instance(j).stringValue(0));
                inst.setValue(c, otherArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                otherArticlesCount++;

                if (otherArticlesCount == crimeArticlesCount) {
                    break;
                }
            }
            if (otherArticlesCount == crimeArticlesCount) {
                break;
            }
        }
        System.out.println("Total Number of Other Instances: " + otherArticlesCount);

        System.out.println("Total num of instances= " + trainingData.numInstances());


        // apply the StringToWordVector filter
        filter.setInputFormat(trainingData);
        Instances dataFiltered = Filter.useFilter(trainingData, filter);
        System.out.println("Number of Attributes after stop words removal- " + dataFiltered.numAttributes());
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);


        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataFiltered);
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\balancedTrainingDataUS.arff"));
        saver.writeBatch();
    }

    public void saveTrainingDataToFileUpSamplingWithSMOTE() throws Exception {
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
        filter.setLowerCaseTokens(true); //ok accepted
        filter.setOutputWordCounts(true); // ok accepted given in research papers
        filter.setTFTransform(true); // normalization as given in scikit
        filter.setIDFTransform(true); // normalization as given in scikit       
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR2.txt")); // stop word removal given in research paper
        filter.setTokenizer(tokenizer); // given in research papers
        filter.setStemmer(scnlpl); // given in research papers as stemming , here we go forward as use lemmatizer
        // feature selection has not performed as "how to" paper say that it wont improve accuracy in SVM and our dictionary
        // is small


        System.out.println("Stemmer Name- " + filter.getStemmer());


        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        int numberOfPapers = 5;

        Instances[] otherArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'other'");
        otherArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='other'");
        otherArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='other'");
        otherArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'other'");
        otherArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'other'");
        otherArticles[4] = query.retrieveInstances();

        Instances[] crimeArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'crime'");
        crimeArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='crime'");
        crimeArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='crime'");
        crimeArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'crime'");
        crimeArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'crime'");
        crimeArticles[4] = query.retrieveInstances();




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

        int crimeArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < crimeArticles[i].numInstances(); j++) {

                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, crimeArticles[i].instance(j).stringValue(0));
                inst.setValue(c, crimeArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                crimeArticlesCount++;
//                if (crimeArticlesCount == 50) {
//                    break;
//                }
            }
//            if (crimeArticlesCount == 50) {
//                break;
//            }
        }

        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);

        int otherArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < otherArticles[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, otherArticles[i].instance(j).stringValue(0));
                inst.setValue(c, otherArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                otherArticlesCount++;

//                if (otherArticlesCount == 100) {
//                    break;
//                }
            }
////            if (otherArticlesCount == 100) {
////                break;
////            }
        }
        System.out.println("Total Number of Other Instances: " + otherArticlesCount);

        System.out.println("Total num of instances= " + trainingData.numInstances());


        // apply the StringToWordVector filter
        filter.setInputFormat(trainingData);
        Instances dataFiltered = Filter.useFilter(trainingData, filter);
        System.out.println("Number of Attributes after stop words removal- " + dataFiltered.numAttributes());
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

        //Resamples a dataset by applying the Synthetic Minority Oversampling TEchnique (SMOTE)
        //http://www.cs.cmu.edu/afs/cs/project/jair/pub/volume16/chawla02a-html/node6.html
        //http://weka.sourceforge.net/doc.packages/SMOTE/weka/filters/supervised/instance/SMOTE.html
        SMOTE s = new SMOTE();
        s.setInputFormat(dataFiltered);
        // Specifies percentage of SMOTE instances to create.
        s.setPercentage(464.0);
        Instances dataBalanced = Filter.useFilter(dataFiltered, s);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataBalanced);
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\balancedTrainingDataUSSMOTE464.arff"));
        saver.writeBatch();
    }
    
    public void saveTrainingDataToFileHybridSampling() throws Exception {
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
        filter.setLowerCaseTokens(true); //ok accepted
        filter.setOutputWordCounts(true); // ok accepted given in research papers
        filter.setTFTransform(true); // normalization as given in scikit
        filter.setIDFTransform(true); // normalization as given in scikit       
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR2.txt")); // stop word removal given in research paper
        filter.setTokenizer(tokenizer); // given in research papers
        filter.setStemmer(scnlpl); // given in research papers as stemming , here we go forward as use lemmatizer
        // feature selection has not performed as "how to" paper say that it wont improve accuracy in SVM and our dictionary
        // is small


        System.out.println("Stemmer Name- " + filter.getStemmer());


        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        int numberOfPapers = 5;

        Instances[] otherArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'other'");
        otherArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='other'");
        otherArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='other'");
        otherArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'other'");
        otherArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'other'");
        otherArticles[4] = query.retrieveInstances();

        Instances[] crimeArticles = new Instances[numberOfPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` = 'crime'");
        crimeArticles[0] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` ='crime'");
        crimeArticles[1] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` ='crime'");
        crimeArticles[2] = query.retrieveInstances();

        //SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` = 'crime'");
        crimeArticles[3] = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` = 'crime'");
        crimeArticles[4] = query.retrieveInstances();




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

        //up sampling using SMOTE
        int crimeArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < crimeArticles[i].numInstances(); j++) {

                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, crimeArticles[i].instance(j).stringValue(0));
                inst.setValue(c, crimeArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                crimeArticlesCount++;
//                if (crimeArticlesCount == 50) {
//                    break;
//                }
            }
//            if (crimeArticlesCount == 50) {
//                break;
//            }
        }

        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);

        //downsampling using ?? 
        int otherArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < otherArticles[i].numInstances(); j++) {
                Instance inst = new Instance(trainingData.numAttributes());
                inst.setValue(a1, otherArticles[i].instance(j).stringValue(0));
                inst.setValue(c, otherArticles[i].instance(j).stringValue(1));
                inst.setDataset(trainingData);


                System.out.println(inst);
                trainingData.add(inst);
                otherArticlesCount++;

                if (otherArticlesCount == 4872) {
                    break;
                }
            }
            if (otherArticlesCount == 4872) {
                break;
            }
        }
        System.out.println("Total Number of Other Instances: " + otherArticlesCount);

        System.out.println("Total num of instances= " + trainingData.numInstances());


        // apply the StringToWordVector filter
        filter.setInputFormat(trainingData);
        Instances dataFiltered = Filter.useFilter(trainingData, filter);
        System.out.println("Number of Attributes after stop words removal- " + dataFiltered.numAttributes());
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

        //Resamples a dataset by applying the Synthetic Minority Oversampling TEchnique (SMOTE)
        //http://www.cs.cmu.edu/afs/cs/project/jair/pub/volume16/chawla02a-html/node6.html
        //http://weka.sourceforge.net/doc.packages/SMOTE/weka/filters/supervised/instance/SMOTE.html
        SMOTE s = new SMOTE();
        s.setInputFormat(dataFiltered);
        // Specifies percentage of SMOTE instances to create.
        s.setPercentage(300.0);//464
        Instances dataBalanced = Filter.useFilter(dataFiltered, s);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataBalanced);
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\balancedTrainingDataHybrid.arff"));
        saver.writeBatch();
    }
}
