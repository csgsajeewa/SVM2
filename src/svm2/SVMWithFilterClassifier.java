package svm2;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.unsupervised.attribute.StringToWordVector;
import java.io.File;
import java.util.Random;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.experiment.InstanceQuery;

/**
 * use filtered classifier
 *
 * @author chamath
 */
public class SVMWithFilterClassifier {

    public static void main(String[] args) {

        SVMWithFilterClassifier svm = new SVMWithFilterClassifier();
        try {
            System.out.println("---------------------------");
            System.out.println("SVM with Filterd Classifier");
            System.out.println("---------------------------");
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
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR4.txt"));
        filter.setTokenizer(tokenizer);
        filter.setStemmer(stemmer);
        System.out.println("Stemmer Name- " + filter.getStemmer());

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

        System.out.println("-------------------------------------------------");
        System.out.println("Running LIBSVM with FC");
        System.out.println("---------------------------------------------------");
//        LibSVM --> initialize the model and set SVM type and kernal type
        LibSVM svm = new LibSVM();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.001953125 -W 10 1"; //-C 3 -G 0.00048828125"
        svm.setOptions(weka.core.Utils.splitOptions(svmOptions));
        svm.setNormalize(true);
        System.out.println("SVM Type and Keranl Type= " + svm.getSVMType() + svm.getKernelType());//1,3 best result 81%

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(svm);
        fc.setFilter(filter);
        evaluate(trainingData, fc);

    }

    /**
     * evaluate classifier
     *
     * @param trainingData
     * @param fc
     * @throws Exception
     */
    private void evaluate(Instances trainingData, FilteredClassifier fc) throws Exception {
        Evaluation evaluation = new Evaluation(trainingData);
        evaluation.crossValidateModel(fc, trainingData, 10, new Random(1));
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