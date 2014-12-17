package simhash;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface IWordSeg {

	public List<String> tokens(String doc);
        
        public List<String> fullTokens(String doc,ArrayList<String> stopWords);
	
	public List<String> tokenizer(String doc,ArrayList<String> stopWords);
        
        public List<String> tokens(String doc, Set<String> stopWords);
        
        
}
