package ao.thesis.wikianalyse.utils.textanalyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Reader for stop word lists.
 * 
 * @author anna
 *
 */
public class StopWordReader {
	
	private static List<String> stopWords = new ArrayList<String>();
	
	public List<String> getStopWords(){
		return stopWords;
	}
	
	public List<String> readStopWords(String dir, Logger logger){
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(dir)));
			
			String stopword;
			while((stopword = br.readLine())!=null){
				stopWords.add(stopword);
			}
			
		} catch (IOException e) {
			logger.error("Stopwords could not be read.",e);
		}
		return stopWords;
	}

}
