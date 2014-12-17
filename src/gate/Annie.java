package gate;

import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.IOException;

/**
 * load ANNIE plugin from the saved state and then set corpus and run it.
 * @author chamath
 */
public class Annie  {

  /** The Corpus Pipeline application to contain ANNIE */
  private CorpusController annieController;

  /**
   * Initialize the ANNIE system. This creates a "corpus pipeline"
   * application that can be used to run sets of documents through
   * the extraction system.
   * 
   * @throws GateException
   * @throws IOException 
   */
  public void initAnnie() throws GateException, IOException {
    // load the ANNIE application from the saved state in plugins/ANNIE
    File pluginsHome = Gate.getPluginsHome();
    File anniePlugin = new File(pluginsHome, "ANNIE");
    File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
    annieController =(CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);   
  } 

  /**
   * Tell ANNIE's controller about the corpus you want to run on
   * @param corpus 
   */
  public void setCorpus(Corpus corpus) {
    annieController.setCorpus(corpus);
  } 

 /**
  * Run ANNIE
  * @throws GateException 
  */
  public void execute() throws GateException {  
    annieController.execute();    
  } 
}
