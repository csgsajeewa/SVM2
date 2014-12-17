package svm2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import weka.core.OptionHandler;
import weka.core.stemmers.Stemmer;

/**
 * Lemmatizer use as a improvement for stemmer Wrapper class for weka
 *
 * @author chamath
 */
public class StanfordCoreNLPLemmatizer implements Stemmer, OptionHandler {

    protected StanfordCoreNLP pipeline;

    public StanfordCoreNLPLemmatizer() {
        System.out.println("------------Initialize Lemmatizer-------------------------");
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);

    }

    /**
     * Convert given word into its base form
     *
     * @param word
     * @return
     */
    @Override
    public String stem(String word) {
        List<String> lemmas = new LinkedList<String>();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(word);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmas.get(0);

    }

    @Override
    public String getRevision() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration listOptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getOptions() {
        ArrayList result;
        result = new ArrayList();
        result.add("-S");
        result.add("Stanford Core NLP");

        return (String[]) result.toArray(new String[result.size()]);

    }

    public static void main(String[] args) {
        StanfordCoreNLPLemmatizer scnlpl = new StanfordCoreNLPLemmatizer();
        System.out.println(scnlpl.stem("aggrieved"));

    }
}
