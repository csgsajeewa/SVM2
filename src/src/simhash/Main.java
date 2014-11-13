/**
 * 
 */
package src.simhash;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.PrintWriter;
import net.sf.classifier4J.summariser.SimpleSummariser;
import weka.core.Summarizable;

/**
 * 1. calculate sim hash for each document ( 64 bit sim hash)
 * 2. build the smart index ( key is 12 bit vlaue, values are doc IDs)
 * 3. then calculate hamming distance between simhash values. ( difference threshold is 3)
 * @author hp
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
            SimpleSummariser simpleSummariser=new SimpleSummariser();
            

//		if (args.length != 2) {
//			System.err.println("Usage: inputfile outputfile");
//			return;
//		}
		long start = System.nanoTime();
		// Creates SimHash object.
		Simhash simHash = new Simhash(new BinaryWordSeg());

		// DocHashes is a list that will contain all of the calculated hashes.
		ArrayList<Long> docHashes = Lists.newArrayList();

		// Maps 12-bit key with the documents matching the partial hash
		Map<BitSet, HashSet<Integer>> hashIndex = Maps.newHashMap();

		// Read the documents. (Each line represents a document).
		List<String> docs = readDocs(args);

		int idx = 0;

		System.out.println("Start to build index...");
		for (String doc : docs) {
			// Calculate the document sim hash.
			//long docHash = simHash.simhash64(simpleSummariser.summarise(doc,3));
                        long docHash = simHash.simhash64(doc);
			System.out.println("Document=[" + doc + "] Hash=[" + docHash+ " , "+Long.toBinaryString(docHash) + "]" + "Bit Length of Hash:"+ Long.toBinaryString(docHash).length()+"bits");

			// Store the document hash in a list.
			docHashes.add(docHash);
			
		}
		
		File output = new File("C:\\Users\\hp\\Desktop\\DuplicateDetetcionImplementation\\Implementations\\sim hash\\simhash-java-master\\simhash-java-master\\src\\test_out");
		//clear the file before writing
                PrintWriter writer = new PrintWriter(output);
                writer.print("");
                writer.close();
                //
                idx = 0;
		

		for (String doc : docs) {
			// For each document.
                        System.out.println(doc);
			

			// Calculates document hash.
		//	long docHash = simHash.simhash64(simpleSummariser.summarise(doc,3));
			long docHash = simHash.simhash64(doc);
                        
			List<Integer> similarDocs = Lists.newLinkedList();
			Map<Integer, Integer> docDistances = Maps.newHashMap();
			
                        
                        for (int i=0;i<docHashes.size();i++) {
				int dist = simHash.hammingDistance(docHash, docHashes.get(i));
                               // System.out.println(dist);
				if (dist <= 12) {
					similarDocs.add(i);
					docDistances.put(i, dist);
				}
			}
			if (!similarDocs.isEmpty()) {
				Files.append("Documents similar as [" + doc + "]:\n", output, Charsets.UTF_8);
				for (int i : similarDocs) {
					if (i == idx)
						continue;
					Files.append("[" + i + "]\tDistance=[" + docDistances.get(i) + "]\n", output, Charsets.UTF_8);
				}
				Files.append("End\n", output, Charsets.UTF_8);
			}
			
			++idx;
		}

		System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
	}

        /**
         * currently this method takes strings(documents) from file, should be changed to get from data base
         * @param args
         * @return
         * @throws IOException 
         */
	private static List<String> readDocs(String[] args) throws IOException {
		return Files.readLines(new File("C:\\Users\\hp\\Desktop\\DuplicateDetetcionImplementation\\Implementations\\sim hash\\simhash-java-master\\simhash-java-master\\src\\test_in"), Charsets.UTF_8);
	}
}