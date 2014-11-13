/**
 * 
 */
package src.simhash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import weka.core.stemmers.SnowballStemmer;


public class Simhash {

	private IWordSeg wordSeg;

	public Simhash(IWordSeg wordSeg) {
		this.wordSeg = wordSeg;
	}

	public int hammingDistance(int hash1, int hash2) {
		int i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		i = (i + (i >>> 4)) & 0x0f0f0f0f;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;
	}

	public int hammingDistance(long hash1, long hash2) {
		long i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		return (int) i & 0x7f;
	}

        /**
         * this takes each string which represents the document, then divide string into tokens, for
         * each token then we calculate 64bit sim hash value as explained in google paper
         * @param doc
         * @return 
         */
	public long simhash64(String doc) throws FileNotFoundException, IOException {
		int bitLen = 64;
		int[] bits = new int[bitLen];
                //here it get tokens from its tokenisation implementation, instead we can take requried tokens from the
                //weka after applying stop words, stemming, case folding, pharse detection 
		ArrayList<String>stopWords=buildStopWordList();
                List<String> tokens = wordSeg.tokenizer(doc,stopWords); // get tokens after removing stop words
                //for each token in string 
		for (String t : tokens) {
                        //calculate 64 bit hash value for each token
                        t=t.toLowerCase(); //case folding
                        SnowballStemmer stemmer = new SnowballStemmer();
                        stemmer.setStemmer("english");
                        t=stemmer.stem(t); // stemming 
                        System.out.println(t);
			long v = MurmurHash.hash64(t);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
             //   System.out.println(Long.toBinaryString(hash));
		return hash;
	}

	public long simhash32(String doc) {
		int bitLen = 32;
		int[] bits = new int[bitLen];
		List<String> tokens = wordSeg.tokens(doc);
		for (String t : tokens) {
			int v = MurmurHash.hash32(t);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		int hash = 0x00000000;
		int one = 0x00000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
        
      private ArrayList<String> buildStopWordList() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\hp\\Desktop\\SVM implementation\\stopWords.txt")));
        String line = "";
        ArrayList<String> list = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            list.add(line);
        }
        return list;
    }
}
