package svm2;

import gate.DocumentContentFilter;
import java.io.File;
import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.GridSearch;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.experiment.InstanceQuery;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * SVM with gate extracted keywords
 *
 * @author chamath
 */
public class SVMWithGate {

    DocumentContentFilter documentContentFilter;

    public SVMWithGate() {
        System.out.println("---------------------------");
        System.out.println("SVM with Gate Extracted Keywords");
        System.out.println("---------------------------");
        documentContentFilter = new DocumentContentFilter();
    }

    /**
     * for testing purposes
     */
    public void test() {
        System.out.println(documentContentFilter.getFilterdContent("Brothers killed in Gandara. Two brothers have been killed after being assaulted with an axe in the Nawadunna area in \n"
                + "Gandara. Police said an unidentified group has carried out the alleged murder at around 8.30 on Friday night.\n"
                + "The individuals succumbed to their injuries after being admitted to the Matara Hospital.The deceased are aged \n"
                + "36 and 38 years The suspects are said to have fled the area. Police investigations have been launched to arrest the \n"
                + "suspects."));
        System.out.println(documentContentFilter.getFilterdContent("Kotadeniyawa woman raped and killed. A woman was allegedly raped and killed while she was bathing in the Ma-Oya \n"
                + "in the Aluthwaththa area in Wellawagedara, Kotadeniyawa.Police Media Spokesperson SSP Ajith Rohana, speaking to \n"
                + "News 1st said that her body was discovered during a search on Saturday night when she had not returned home.He said \n"
                + "that the victim was a sixty-year-old resident of Aluthwatta."));
    }

    public static void main(String[] args) throws Exception {
        SVMWithGate svm = new SVMWithGate();
        // sVMWithGate.test();
        svm.saveTrainingDataToFile();
        svm.run();
    }

    /**
     *
     * @throws Exception
     */
    public void run() throws Exception {

//      LibSVM --> initialize the model and set SVM type and kernal type        
        LibSVM svm = new LibSVM();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.001953125 -W 10 1"; //-C 3 -G 0.00048828125"
        svm.setOptions(weka.core.Utils.splitOptions(svmOptions));
        System.out.println("SVM Type and Keranl Type= " + svm.getSVMType() + svm.getKernelType());//1,3 best result 81%
        svm.setNormalize(true);
//      load training data from .arff file
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\trainingDataWithG.arff");
        System.out.println("\n\nLoaded data:\n\n" + source.getDataSet());
        Instances dataFiltered = source.getDataSet();
        dataFiltered.setClassIndex(0);
        evaluate(dataFiltered, svm);

    }

    /**
     * perform grid search to find best cost and gamma values
     *
     * @param svm
     * @param dataFiltered
     * @throws Exception
     */
    public void gridSearch(LibSVM svm, Instances dataFiltered) throws Exception {
        GridSearch gs = new GridSearch();
        gs.setClassifier(svm);
        gs.setFilter(new AllFilter());

        //setting cost search values
        gs.setXProperty("classifier.cost");
        gs.setXMin(-5);
        gs.setXMax(15);
        gs.setXStep(2);
        gs.setXBase(2);
        gs.setXExpression("pow(BASE,I)");

        //setting gamma search values
        gs.setYProperty("classifier.gamma");
        gs.setYMin(-15);
        gs.setYMax(3);
        gs.setYStep(2);
        gs.setYBase(2);
        gs.setYExpression("pow(BASE,I)");
        //-y-property classifier.kernel.gamma -y-min -5.0 -y-max 2.0 -y-step 1.0 -y-base 10.0 -y-expression pow(BASE,I) -filter weka.filters.AllFilter -x-property classifier.nu -x-min 0.01 -x-max 1.0 -x-step 10.0 -x-base 10.0 -x-expression I -sample-size 100.0 -traversal COLUMN-WISE -log-file "C:\Program Files\Weka-3-6" -S 1 -W weka.classifiers.functions.LibSVM -- -S 2 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.0010 -P 0.1

        int evaluationIndex = 6;
        SelectedTag st;
        st = new SelectedTag(evaluationIndex, weka.classifiers.meta.GridSearch.TAGS_EVALUATION);
        gs.setEvaluation(st);
        gs.setGridIsExtendable(true);
        gs.setDebug(true);
        gs.buildClassifier(dataFiltered);

        System.out.println("Criteria " + gs.getEvaluation().getSelectedTag().getID());
        System.out.println("Evaluation Summary " + gs.getValues());
    }

    /**
     * save training data to file
     * @throws Exception 
     */
    public void saveTrainingDataToFile() throws Exception {
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
        System.out.println("Stemmer Name- " + filter.getStemmer());
        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");
        int numOfNewsPapers = 5;
        Instances[] instances = new Instances[numOfNewsPapers];
        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` IS NOT NULL");//IS NOT NULL
        instances[0] = query.retrieveInstances();
        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` IS NOT NULL ");
        instances[1] = query.retrieveInstances();
        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` IS NOT NULL");
        instances[2] = query.retrieveInstances();
        query.setQuery("SELECT content, label FROM article_the_island_2012 where `label` IS NOT NULL ");
        instances[3] = query.retrieveInstances();
        query.setQuery("SELECT content, label FROM article_the_island_2013 where `label` IS NOT NULL ");
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
                inst.setValue(a1, documentContentFilter.getFilterdContent(instances[i].instance(j).stringValue(0)));
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
        saver.setFile(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\arffData\\trainingDataWithG.arff"));
        saver.writeBatch();
    }

    /**
     * 
     * @param dataFiltered
     * @param svm
     * @throws Exception 
     */
    private void evaluate(Instances dataFiltered, LibSVM svm) throws Exception {
        //       gridSearch(svm, dataFiltered);
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
    }
}
