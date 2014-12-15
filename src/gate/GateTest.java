///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
package gate;

import gate.annotation.AnnotationImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

public class GateTest {

    public static void main(String[] args)
            throws Exception {

        Gate.init(); //prepare the library
        List annotationsRequired = new ArrayList<>();
        ArrayList<String> annotationType = new ArrayList<>();
        annotationType.add("Token");

        ListIterator iter = annotationType.listIterator();
        while (iter.hasNext()) {
            String annotation = (String) iter.next();
            annotationsRequired.add(annotation);
        }



        Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus"); // create corpus 

        File newsDirectory = new File("C:\\Users\\hp\\Desktop\\DuplicateDetetcionImplementation\\Articles");
        BufferedReader newsArticleReader;
        int i = 0;
        CorpusPipeLine cp = new CorpusPipeLine();
        cp.configure();
        for (File fileEntry : newsDirectory.listFiles()) {
            System.out.println("Article : " + i + " -----------------------Begins Here-------------------");
            System.out.println(fileEntry.getPath());
            newsArticleReader = new BufferedReader(new FileReader(fileEntry.getPath()));
            String news = "";
            String newsLine = "";
            while ((newsLine = newsArticleReader.readLine()) != null) {
                news = news.concat(newsLine);
                news = news.concat(" ");
            }

            Document doc = Factory.newDocument(news); // create new gate document
            System.out.println(corpus.add(doc));

            cp.setCorpus(corpus);
            cp.execute();
            corpus.clear();

            String docXMLString = null;
            Set annotationsToWrite = new HashSet();
            // if we want to just write out specific annotation types, we must
            // extract the annotations into a Set
            if (annotationsRequired != null) {
                // Create a temporary Set to hold the annotations we wish to write out

                // we only extract annotations from the default (unnamed) AnnotationSet
                // in this example
                AnnotationSet defaultAnnots = doc.getAnnotations();
                Iterator annotTypesIt = annotationsRequired.iterator();
                while (annotTypesIt.hasNext()) {
                    // extract all the annotations of each requested type and add them to
                    // the temporary set
                    AnnotationSet annotsOfThisType =
                            defaultAnnots.get((String) annotTypesIt.next());
                    if (annotsOfThisType != null) {
                        annotationsToWrite.addAll(annotsOfThisType);
                    }
                }
            }

            // Release the document, as it is no longer needed
            Factory.deleteResource(doc);

            Iterator annotIt = annotationsToWrite.iterator();
            while (annotIt.hasNext()) {
                // extract all the annotations of each requested type and add them to
                // the temporary set
                AnnotationImpl CurrentAnnot = (AnnotationImpl) annotIt.next();
                //System.out.println("Current Annotation="+CurrentAnnot);

                String antText = gate.Utils.stringFor(doc, CurrentAnnot);
               
               if (CurrentAnnot.getType().equalsIgnoreCase("Token")  && (CurrentAnnot.getFeatures().get("category") == "NN" || CurrentAnnot.getFeatures().get("category") == "NNS" )) 
                {   System.out.println("Noun: " +CurrentAnnot.getFeatures().get("category"));
                    System.out.println("Noun - string: " +CurrentAnnot.getFeatures().get("string").toString());
                }
                else if (CurrentAnnot.getType().equalsIgnoreCase("Token")  && (CurrentAnnot.getFeatures().get("category") == "JJ" || CurrentAnnot.getFeatures().get("category") == "JJR" || CurrentAnnot.getFeatures().get("category") == "JJS" )){
                    System.out.println("Adjective: " +CurrentAnnot.getFeatures().get("category"));
                    System.out.println("Adjective - string: " +CurrentAnnot.getFeatures().get("string"));
                }else if (CurrentAnnot.getType().equalsIgnoreCase("Token")  && (CurrentAnnot.getFeatures().get("category") == "RB" || CurrentAnnot.getFeatures().get("category") == "RBR" || CurrentAnnot.getFeatures().get("category") == "RBS" )){
                    System.out.println("Adverb: " +CurrentAnnot.getFeatures().get("category"));
                    System.out.println("Adverb - string: " +CurrentAnnot.getFeatures().get("string"));
                }else  if (CurrentAnnot.getType().equalsIgnoreCase("Token") && (CurrentAnnot.getFeatures().get("category").equals("VBD") || CurrentAnnot.getFeatures().get("category") == "VBG" || CurrentAnnot.getFeatures().get("category") == "VBN" || CurrentAnnot.getFeatures().get("category") == "VBP"|| CurrentAnnot.getFeatures().get("category") == "VB" || CurrentAnnot.getFeatures().get("category") == "VBZ" )) 
                {   System.out.println("Verb: " +CurrentAnnot.getFeatures().get("category"));
                    System.out.println("Verb - string: " +CurrentAnnot.getFeatures().get("string"));
                }
            }
            System.out.println("Article : " + i + " -----------------------------------Ends Here--------------------");
            i++;



        }
    } // main
}