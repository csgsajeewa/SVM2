package svm2;

import gate.DocumentKeyWordFinder;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.tokenizers.NGramTokenizer;
import weka.experiment.InstanceQuery;

/**
 * SVM with Gate Gazetter
 *
 * @author chamath
 */
public class SVMWithGateGazetter {

    private DocumentKeyWordFinder documentKeyWordFinder;
    private NGramTokenizer tokenizer;
    private StanfordCoreNLPLemmatizer scnlpl;

    public SVMWithGateGazetter() {
        tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(1);
        tokenizer.setDelimiters("\\W");
        documentKeyWordFinder = new DocumentKeyWordFinder();
        scnlpl = new StanfordCoreNLPLemmatizer();
    }

    /**
     *
     * @throws Exception
     */
    private void run() throws Exception {

        InstanceQuery query = new InstanceQuery();
        String words;
        Boolean exist;
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
        int numCrimeCorrect = 0;
        int numCrimeWrong = 0;
        int numOtherCorrect = 0;
        int numOtherWrong = 0;

        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < crimeArticles[i].numInstances(); j++) {
                words = "";
                String content = crimeArticles[i].instance(j).stringValue(0);
                tokenizer.tokenize(content);
                while (tokenizer.hasMoreElements()) {
                    String element = (String) tokenizer.nextElement();
                    words = words.concat(scnlpl.stem(element));
                    words = words.concat(" ");
                }

                exist = documentKeyWordFinder.isKeyWordExist(words);
                if (exist) {
                    System.out.println("crime- Correct");
                    System.out.println("Sentence= " + words);
                    numCrimeCorrect++;
                } else {
                    System.out.println("crime- Incorrect");
                    System.out.println("Sentence= " + words);
                    numCrimeWrong++;
                }

                crimeArticlesCount++;

            }

        }

        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);

        int otherArticlesCount = 0;
        for (int i = 0; i < numberOfPapers; i++) {
            for (int j = 0; j < otherArticles[i].numInstances(); j++) {
                words = "";
                String content = otherArticles[i].instance(j).stringValue(0);
                tokenizer.tokenize(content);
                while (tokenizer.hasMoreElements()) {
                    String element = (String) tokenizer.nextElement();
                    words = words.concat(scnlpl.stem(element));
                    words = words.concat(" ");
                }
                exist = documentKeyWordFinder.isKeyWordExist(words);
                if (exist) {
                    System.out.println("Sentence= " + words);
                    System.out.println("Other-Incorrect");
                    numOtherWrong++;
                } else {
                    System.out.println("Other-Correct");
                    System.out.println("Sentence= " + words);
                    numOtherCorrect++;
                }
                otherArticlesCount++;

            }
        }
        System.out.println("Total Number of Crime Instances: " + crimeArticlesCount);
        System.out.println("Total Number of Other Instances: " + otherArticlesCount);

        System.out.println("Total num of instances= " + trainingData.numInstances());
        System.out.println("Total num of correct crime= " + numCrimeCorrect);
        System.out.println("Total num of incorrect crime= " + numCrimeWrong);
        System.out.println("Total num of correct other= " + numOtherCorrect);
        System.out.println("Total num of incorrect other= " + numOtherWrong);



    }

    public static void main(String[] args) throws Exception {
        System.out.println("-------------------------------------------------");
        System.out.println("Running LIBSVM With Gate Gazetter");
        System.out.println("-------------------------------------------------");
        SVMWithGateGazetter sVMWithGateGazetter = new SVMWithGateGazetter();
        sVMWithGateGazetter.run();
    }
}
