/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svm2;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.Serializable;
import java.util.Properties;
/**
 *
 * @author hp
 */
public class StanfordCoreNLPExt extends StanfordCoreNLP implements Serializable{

    public StanfordCoreNLPExt(Properties props) {
        super(props);
    }
    
}
