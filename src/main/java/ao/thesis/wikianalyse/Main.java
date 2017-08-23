package ao.thesis.wikianalyse;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ao.thesis.wikianalyse.analysis.Analysis;
import ao.thesis.wikianalyse.analysis.ratingsystems.NamedEntityAnalysis;
import ao.thesis.wikianalyse.analysis.ratingsystems.WikiTrustAnalysis;
import ao.thesis.wikianalyse.io.input.InputReader;
import ao.thesis.wikianalyse.io.input.StopWordReader;
import ao.thesis.wikianalyse.io.output.NEDataOutputWriter;
import ao.thesis.wikianalyse.io.output.OutputWriter;
import ao.thesis.wikianalyse.io.output.WikiTrustDataOutputWriter;
import edu.stanford.nlp.ie.crf.CRFClassifier;

public class Main {
	
	public static final String USER_DIR = System.getProperty("user.dir");
	
	public static final String DEF_INPUT_DIR 	= USER_DIR+"/input/";
	public static final String DEF_OUTPUT_DIR 	= USER_DIR+"/output/";
	public static final String TOKEN_DIR 	= USER_DIR+"/output/tokens/";
	
	private static final Logger LOGGER = Logger.getLogger(Main.class);
	
	//StopWordDirectories
	/*
	 * http://www.ranks.nl/stopwords/italian
	 */
	private static final String IT_STOPWORDS = USER_DIR+"/resources/it_stop_words.txt";
	private static final String ENG_STOPWORDS = USER_DIR+"/resources/engStopWords";
	
	//Classifier
//	private static String classifier3ClassPath = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
//	private static String classifier7ClassPath = System.getProperty("user.dir")+"/resources/classifiers/english.muc.7class.distsim.crf.ser.gz";
//	private static String classifierNCCPath = System.getProperty("user.dir")+"/resources/classifiers/example.serialized.ncc.ncc.ser.gz";
	private static String classifier4ClassPath = System.getProperty("user.dir")+"/resources/classifiers/english.conll.4class.distsim.crf.ser.gz";
	
	private static Analysis analysis;
	private static OutputWriter writer;
	private static DateTime readlimitDate;
	private static int limitRevisions;
	private static int limitArticles;
	
	private Main(){}
	
	
	public static void main(String[] args) throws Exception{
		
//		setupItReproduction();
//		setupNE();
		setupEng();
		
		InputReader reader = null;
		long timeStart;
		try{
			LOGGER.info("Start Analysis");
			
			timeStart = System.currentTimeMillis();
			reader = new InputReader(DEF_INPUT_DIR, analysis, writer, readlimitDate, limitRevisions, limitArticles);
			reader.read();
			
			LOGGER.info("Analysis Time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
			
		} catch(Exception e){
			LOGGER.info(e);
			e.printStackTrace();
//		} finally {
//			if(Objects.nonNull(reader)){
//				
//				LOGGER.info("Start Reputation Assignment");
//				
//				TimelineReader timelineReader = new TimelineReader(REP_DB);
//				timeStart = System.currentTimeMillis();
//				timelineReader.readTimelineAndUpdateReputation();
//				
//				LOGGER.info("Reputation Assignment Time: "+ (System.currentTimeMillis() - timeStart) + " ms.");
//				
//				DBOutputWriter writer = new DBOutputWriter();
//				writer.write(REP_DB);
//			}
		}
	}
	
	
	private static List<String> setupStopWords(String dir){
		StopWordReader stopWordReader = new StopWordReader();
		return stopWordReader.readStopWords(dir, LOGGER);
	}
	

	private static void setupItReproduction(){
		List<String> stopWords = setupStopWords(IT_STOPWORDS);
		analysis = new WikiTrustAnalysis(stopWords);
		writer = new WikiTrustDataOutputWriter("Timeline");
		readlimitDate = new DateTime(2005, 12, 11, 23, 59);
		limitRevisions = 1000000000;
		limitArticles = 1000000000;
	}
	

	private static void setupEng(){
		List<String> stopWords = setupStopWords(ENG_STOPWORDS);
		analysis = new WikiTrustAnalysis(stopWords);
		writer = new WikiTrustDataOutputWriter("Timeline");
		readlimitDate = new DateTime(2017, 12, 31, 23, 59);
		limitRevisions = 1000000000;
		limitArticles = 1000;
	}
	
	
	private static void setupNE(){
		
		try {
			analysis = new NamedEntityAnalysis(CRFClassifier.getClassifier(classifier4ClassPath));
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			LOGGER.error("No classifier found.");
		}
		
		writer = new NEDataOutputWriter("NETimeline");
		readlimitDate = new DateTime(2017, 12, 31, 23, 59);
		limitRevisions = 1000000000;
		limitArticles = 1000;
	}
}
