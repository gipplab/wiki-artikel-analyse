package ao.thesis.wikianalyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sweble.wikitext.dumpreader.export_0_10.ContributorType;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import ao.thesis.wikianalyse.io.InputReader;
import ao.thesis.wikianalyse.io.OutputWriter;
import ao.thesis.wikianalyse.matcher.Matcher;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class Main {
	
	public static final String INPUT_DIR = System.getProperty("user.dir")+"/input";
	public static final String OUTPUT_DIR = System.getProperty("user.dir")+"/output";
	public static final String STOPWORDS_DIR = System.getProperty("user.dir")+"/resources/engStopWords";
	private static final String EN_CLASSIFIER = System.getProperty("user.dir")
			+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
	
	private static CRFClassifier<CoreLabel> classifier;
	private static Logger logger = Logger.getLogger(Main.class);
	private static WikiConfig config = DefaultConfigEnWp.generate();
	private static WtEngineImpl engine = new WtEngineImpl(config);
	
	//---------------------------------------
	
	static InputReader reader = InputReader.getInputReader();
	static OutputWriter writer;
	static Matcher matcher = new Matcher(10,3); //(max comparisations, min match length)
	
	//---------------------------------------
	
	static List<Object> pages = new ArrayList<Object>();
	static List<String> stopWordList = new ArrayList<String>();

	
	public static void main(String[] args) {
		try {
			pages = reader.read(INPUT_DIR);
		} catch (Exception e) {
			logger.error("Input could not be read.", e);
			return;
		}
		
		if(!pages.isEmpty()){
			setStopWords(STOPWORDS_DIR);
			
			try {
				classifier = CRFClassifier.getClassifier(EN_CLASSIFIER);
			} catch (Exception e) {
				logger.error("Classifier could not be set.", e);
				return;
			}
			for(Object item : pages){
				try {
					if(item instanceof PageType){
						analysePage(((PageType) item));
					}
				} catch (LinkTargetException e) {
					logger.error("Could not set a pageTitle to analyse the page.", e);
				}
			}
		} else {
			logger.error("No input files found.");
		}
	}
	
	private static void analysePage(PageType page) throws LinkTargetException {
		/* TODO Does not filter editors yet.
		 */
		Set<ContributorType> editors = new HashSet<ContributorType>();
			
		for(Object revision : page.getRevisionOrUpload()){
			ContributorType editor = ((RevisionType) revision).getContributor();
			editors.add(editor);
		}
		
		PageTitle pageTitle = PageTitle.make(config, page.getTitle());
		PageId pageId = new PageId(pageTitle, -1);
		
		EditAnalysis editAnalysis = new EditAnalysis(pageId, engine);
		TextAnalysis textAnalysis = new TextAnalysis(matcher, classifier, stopWordList);

		PageOrga orga = new PageOrga(page, engine, textAnalysis, editAnalysis);
		
		orga.analyse(pageId);
		
		setOutput(page, editors, orga, textAnalysis);
	}
	
	private static void setOutput(PageType page, Set<ContributorType> editors,
			PageOrga orga, TextAnalysis textAnalysis){
		
		writer = new OutputWriter(OUTPUT_DIR, page.getTitle());
		
		for(int index = 0 ; index < textAnalysis.getTokenizedRevisionSize() ; index++){
			writer.writeTokenOutput(textAnalysis.getTokens(index), index);
		}
		
		int latestVersionIndex = textAnalysis.getTokenizedRevisionSize()-1;
		int latestVersionTotal = textAnalysis.getTokens(latestVersionIndex).size();
		
		for(ContributorType editor : editors){
			/* TODO Still ignores anonym editors.
			 */
			if(editor.getId()!=null){
				
				BigInteger editorId = editor.getId();
				
				double editq = orga.judgeEdits(editorId, 10);
				double textq = orga.judgeText(editorId, 10);
				
				int revisionCount = orga.getAllRevisionsByEditorId(editorId).size();
				int wordCount = orga.getWordCount(editorId);
				int namedEntityCount = orga.getNamedEntityCount(editorId);
				int mathEntityCount = orga.getMathTokenCount(editorId);
				int persistentText = orga.countPersistentText(editorId);
				
				int latestVersionCount = 0;
				for(Object revision : page.getRevisionOrUpload()){
					if(editorId.equals(((RevisionType) revision).getContributor().getId())){
						latestVersionCount += textAnalysis.countWords(latestVersionIndex, 
								((RevisionType) revision).getId());
					}
				}
				
				double percent = ((double) latestVersionCount / (double) latestVersionTotal) * 100;
				double efficiency = ((double) persistentText / (double) revisionCount);
				
				writer.addEditor(
						editor.getUsername(), 
						editorId, 
						editq, 
						textq, 
						revisionCount, 
						wordCount, 
						namedEntityCount, 
						mathEntityCount, 
						percent,
						efficiency);
			}
		}
		writer.writeEditorOutput();
	}
	
	private static void setStopWords(String dir){
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(dir)));
			
			String stopword;
			try {
				while((stopword = br.readLine())!=null){
					stopWordList.add(stopword);
				}
			} catch (IOException e) {
				logger.error("Stopwords could not be read.",e);
			}
		} catch (FileNotFoundException e) {
			logger.error("Stopword File could not be read.",e);
		}
	}

}
