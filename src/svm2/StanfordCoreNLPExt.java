package svm2;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author chamath
 */
public class StanfordCoreNLPExt extends StanfordCoreNLP implements Serializable{

    public StanfordCoreNLPExt(Properties props) {
        super(props);
    }
    
}
