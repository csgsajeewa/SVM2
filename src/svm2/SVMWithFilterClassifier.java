/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svm2;

import java.awt.BorderLayout;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.util.Random;
import javax.swing.JFrame;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.GridSearch;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.experiment.InstanceQuery;
import weka.filters.AllFilter;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

public class SVMWithFilterClassifier {

//    public static void main(String[] args) {
//
//        SVMWithFilterClassifier wekaTestDB = new SVMWithFilterClassifier();
//        try {
//            wekaTestDB.testCrossValidataion();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void testCrossValidataion() throws Exception {

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
        filter.setStopwords(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\StopWordsR2.txt"));
        filter.setTokenizer(tokenizer);
        filter.setStemmer(scnlpl);
        System.out.println("Stemmer Name- " + filter.getStemmer());

        InstanceQuery query = new InstanceQuery();
        query.setUsername("root");
        query.setPassword("");

        query.setQuery("SELECT content, label FROM article_ceylon_today_2013 where `label` IS NOT NULL");
        Instances other1 = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2012 where `label` IS NOT NULL");
        Instances other2 = query.retrieveInstances();

        query.setQuery("SELECT content, label FROM article_daily_mirror_2013 where `label` IS NOT NULL");
        Instances other3 = query.retrieveInstances();

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
        for (int i = 0; i < other1.numInstances(); i++) {
            Instance inst = new Instance(trainingData.numAttributes());
            inst.setValue(a1, other1.instance(i).stringValue(0));
            inst.setValue(c, other1.instance(i).stringValue(1));
            inst.setDataset(trainingData);


            System.out.println(inst);
            trainingData.add(inst);
            count++;
        }

        for (int i = 0; i < other2.numInstances(); i++) {
            Instance inst = new Instance(trainingData.numAttributes());
            inst.setValue(a1, other2.instance(i).stringValue(0));
            inst.setValue(c, other2.instance(i).stringValue(1));
            inst.setDataset(trainingData);


            System.out.println(inst);
            trainingData.add(inst);
            count++;
        }

        for (int i = 0; i < other3.numInstances(); i++) {
            Instance inst = new Instance(trainingData.numAttributes());
            inst.setValue(a1, other3.instance(i).stringValue(0));
            inst.setValue(c, other3.instance(i).stringValue(1));
            inst.setDataset(trainingData);


            System.out.println(inst);
            trainingData.add(inst);
            count++;
        }

        System.out.println("Other1= " + other1.numInstances());
        System.out.println("Other2= " + other2.numInstances());
        System.out.println("Other3= " + other3.numInstances());
        System.out.println("Total num of instances= " + count);

//        LibSVM --> initialize the model and set SVM type and kernal type
        LibSVM svm = new LibSVM();
        String svmOptions = "-S 0 -K 2 -C 8 -G 0.001953125"; //-C 3 -G 0.00048828125"
        svm.setOptions(weka.core.Utils.splitOptions(svmOptions));
        System.out.println("SVM Type and Keranl Type= " + svm.getSVMType() + svm.getKernelType());//1,3 best result 81%

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(svm);
        fc.setFilter(filter);


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



    }

    

}