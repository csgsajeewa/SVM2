package simhash;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import weka.core.tokenizers.NGramTokenizer;

/**
 * tokenize the given doc string
 *
 * @author chamath
 */
public class BinaryWordSeg implements IWordSeg {
    
    /**
     * return all the tokens
     * @param doc
     * @return 
     */
    @Override
    public List<String> tokens(String doc) {
        List<String> binaryWords = new LinkedList<>();
        for (int i = 0; i < doc.length() - 1; i += 1) {
            StringBuilder bui = new StringBuilder();
            bui.append(doc.charAt(i)).append(doc.charAt(i + 1));
            binaryWords.add(bui.toString());
        }
        return binaryWords;
    }
    
    /**
     * 
     * @param doc
     * @param stopWords
     * @return 
     */
    @Override
    public List<String> tokens(String doc, Set<String> stopWords) {
        return null;
    }
    
    /**
     * 
     * @param doc
     * @param stopWords
     * @return 
     */
    @Override
    public List<String> fullTokens(String doc, ArrayList<String> stopWords) {
        List<String> words = new ArrayList<>();
        String[] w = doc.split(" ");
      
        for (int i = 0; i < w.length; i++) {
            String string = w[i].toLowerCase();
            if(!stopWords.contains(string)){
               words.add(w[i]);
            }
        }
        return words;
    }

    /**
     * tokenise the given string and remove stop words
     * @param doc
     * @param stopWords
     * @return 
     */
    @Override
    public List<String> tokenizer(String doc, ArrayList<String> stopWords) {
        List<String> words = new ArrayList<>();
        NGramTokenizer nGramTokenizer=new NGramTokenizer();
        nGramTokenizer.setNGramMaxSize(1);
        nGramTokenizer.setNGramMinSize(1);
        nGramTokenizer.tokenize(doc);
        String token;
        while(nGramTokenizer.hasMoreElements()){
            token= (String)nGramTokenizer.nextElement();
            token=token.toLowerCase();
            if(!stopWords.contains(token)){
                words.add(token);
            }
            
        }
        return words;
    }
    
    
}
