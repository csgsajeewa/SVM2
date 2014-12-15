/*
 * load the annie plugin, using the processing resources create new corpus pipe line
 */
package gate;

import gate.*;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hp
 */
public class CorpusPipeLine {

    private SerialAnalyserController serialAnalyserController;
    public void configure() throws MalformedURLException, GateException {
        try {
            //load the plugin ANNIE first to use resources under that
            Gate.getCreoleRegister ().registerDirectories(new File(Gate.getPluginsHome(),"ANNIE").toURI ().toURL ());
            Gate.getCreoleRegister ().registerDirectories(new File(Gate.getPluginsHome(),"Tagger_DateNormalizer").toURI ().toURL ());// folder name in plugins folder
            //create a application using the contorller
           serialAnalyserController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",
                    Factory.newFeatureMap(),
                    Factory.newFeatureMap(), "TestOne");
                    // load each PR
            
                // use default parameters
                FeatureMap params = Factory.newFeatureMap();
                //create each processing resource and add to application
                ProcessingResource annotationDeletePR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR",params);
                ProcessingResource defaultTokeniser = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser",params);
                ProcessingResource defaultGazetteer = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",params);
                ProcessingResource sentenceSplitter = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter",params);
                ProcessingResource posTagger = (ProcessingResource) Factory.createResource("gate.creole.POSTagger",params);
                ProcessingResource ANNIETransducer = (ProcessingResource) Factory.createResource("gate.creole.ANNIETransducer",params);
                
                
                
                
                
                serialAnalyserController.add(annotationDeletePR);
                serialAnalyserController.add(defaultTokeniser);
                serialAnalyserController.add(defaultGazetteer);
                serialAnalyserController.add(sentenceSplitter);
                serialAnalyserController.add(posTagger);
                serialAnalyserController.add(ANNIETransducer);
               
           
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(CorpusPipeLine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setCorpus(Corpus corpus){
        serialAnalyserController.setCorpus(corpus);
    }
    
    public void execute() throws ExecutionException{
        serialAnalyserController.execute();
    }
}
