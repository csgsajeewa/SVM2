package gate;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * load the annie plugin, using the processing resources create new corpus pipe
 * line
 *
 * @author chamath
 */
public class CorpusPipeLine {

    private SerialAnalyserController serialAnalyserController;

    public void configure() throws MalformedURLException, GateException {
        try {
            //load the plugin ANNIE first to use resources under that
            Gate.getCreoleRegister().registerDirectories(new File(Gate.getPluginsHome(), "ANNIE").toURI().toURL());
            //load DataNormalizerPlugin
            Gate.getCreoleRegister().registerDirectories(new File(Gate.getPluginsHome(), "Tagger_DateNormalizer").toURI().toURL());// folder name in plugins folder
            //create a application using the contorller
            serialAnalyserController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",
                    Factory.newFeatureMap(),
                    Factory.newFeatureMap(), "TestOne");
            // load each processing resource               
            FeatureMap params = Factory.newFeatureMap();
            //create each processing resource and add to application
            ProcessingResource annotationDeletePR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", params);
            ProcessingResource defaultTokeniser = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", params);
            ProcessingResource defaultGazetteer = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);
            ProcessingResource sentenceSplitter = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", params);
            ProcessingResource posTagger = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", params);
            ProcessingResource ANNIETransducer = (ProcessingResource) Factory.createResource("gate.creole.ANNIETransducer", params);

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

    /**
     * set corpus for processing
     *
     * @param corpus
     */
    public void setCorpus(Corpus corpus) {
        serialAnalyserController.setCorpus(corpus);
    }

    /**
     * execute pipeline
     *
     * @throws ExecutionException
     */
    public void execute() throws ExecutionException {
        serialAnalyserController.execute();
    }
}
