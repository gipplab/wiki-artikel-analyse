
package main;

import java.util.ArrayList;
import java.util.HashMap;

import org.sweble.wikitext.dumpreader.export_0_10.PageType;

import comparer.BestMatchFinder;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import input.InputReader;
import output.OutputWriter;
import comparer.EditScript;
import tokenizer.Token;
import tokenizer.Tokenizer;


public class Main {

	
	public static String INPUT_DIR = System.getProperty("user.dir")+"/input";
	
	public static String OUTPUT_DIR = System.getProperty("user.dir")+"/output";
	
	public static String STOPWORDS_DIR = System.getProperty("user.dir")+"/resources/engStopWords";
	
	private static CRFClassifier<CoreLabel> classifier;
	
	private static String EN_CLASSIFIER = System.getProperty("user.dir")+"/resources/classifiers/english.all.3class.distsim.crf.ser.gz";
	
	public static CRFClassifier<CoreLabel> getClassifier(){return classifier;}
	
	//----------------------------------
	// Compare texts
	
	public static int MAX_REVISIONS_TO_SET_EDITORS = 10;

	public static int MAX_JUDGING_REVISIONS = 10;
	
	//----------------------------------
	// edit scripts
	
	private static boolean USE_EDIT_SCRIPTS_FROM_FILE = false;
	
	public static boolean WRITE_EDIT_SCRIPTS = true;
	
	
	//=======================================================
	
	static InputReader reader = InputReader.getInputReader();
	
	static Tokenizer tokenizer = Tokenizer.getTokenizer();
	
	static BestMatchFinder editorFinder =  BestMatchFinder.getEditorFinder();
	
	//----------------------------------

	public static void main(String[] args) throws Exception
	{
		classifier = CRFClassifier.getClassifier(EN_CLASSIFIER);
		
		reader.read();
		
		ArrayList<PageType> pages = reader.getPages();
		
		ArrayList<ArrayList<Token>> revisions;
		
		HashMap<Integer, ArrayList<EditScript>> editsList;
		
		for(PageType page : pages){
			
			revisions = tokenizer.tokenize(page);
			
			if(!USE_EDIT_SCRIPTS_FROM_FILE)
			{
				editsList = editorFinder.buildEditScriptsAndSetEditors(page.getTitle(), revisions);
			} 
			else 
			{
				//TODO
			}
			
			//TODO judgeWork
			
			//TODO calculate reputation

			OutputWriter writer = new OutputWriter(page, revisions);
			
			writer.write();
		}
	}
}