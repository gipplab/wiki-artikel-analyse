package ao.thesis.wikianalyse.io.input;

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
 * @author Anna Opaska
 *
 */
public class StopWordReader {
	
	public List<String> readStopWords(String dir, Logger logger){
		
		List<String> stopWords = new ArrayList<>();
		try ( 
			FileReader reader = new FileReader(new File(dir));
			BufferedReader br = new BufferedReader(reader);
		){
			String stopword;
			while((stopword = br.readLine())!=null){
				stopWords.add(stopword);
			}
			br.close();
			reader.close();
			
		} catch (IOException e) {
			logger.error("Stopwords could not be read.",e);
		}
		return stopWords;
	}

}
