package ao.thesis.wikianalyse.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.Main;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.procession.EditRating;
import ao.thesis.wikianalyse.analysis.procession.TextRating;
import ao.thesis.wikianalyse.utils.HDDiffUtils;
import ao.thesis.wikianalyse.utils.SwebleException;
import au.com.bytecode.opencsv.CSVWriter;
import de.fau.cs.osr.utils.visitor.VisitingException;

/**
 * WikiTrustDataWriter
 * 
 * This class writes the following data for every processed revision:
 * |Timestamp|ID|ContributorName|PageTitle|EditDistance|EditLongevity(3)|InsertedWords|TextDecayQuality(10)|
 * 
 * Followed by data of the previous three revisions:
 * |ContributorName|EditRelatedUpdate|TextRelatedUpdate|
 * 
 *  And by data of seven further revisions before:
 * |ContributorName|TextRelatedUpdate|
 * 
 * The data can later be used to read in contributors and update their reputation chronologically.
 * 
 * @author Anna Opaska
 *
 */
public class WikiTrustDataOutputWriter {
	
	private static final Logger LOGGER = Logger.getLogger(WikiTrustDataOutputWriter.class);
	
	public static final int TL_DATE = 0;
	public static final int TL_ID 	= 1;
	public static final int TL_CON 	= 2;
	public static final int TL_PAGE = 3;
	public static final int TL_ED 	= 4;
	public static final int TL_EL 	= 5;
	public static final int TL_IW 	= 6;
	public static final int TL_TDQ 	= 7;
	public static final int TL_MIW 	= 8;
	public static final int TL_MTDQ = 9;
	
	/*
	 * All values by Thomas Adler and WikiTrust
	 */
	private static double cScale 	= 13.08;
	private static double cText 	= 0.6;
	private static double cLen 		= 0.6;
	private static double cSlack 	= 2.2;
	private static double cPunish 	= 19.09;
	
	private final String dir;
	
	public WikiTrustDataOutputWriter(String filename){
		this.dir = Main.DEF_OUTPUT_DIR+"/"+filename+".csv";
	}
	
	public void writeRevisions(List<ProcessedRevision> revisions, PageTitle title){

		try (
			FileWriter file = new FileWriter(dir, true);
			CSVWriter writer = new CSVWriter(file)
		) {
			writer.writeAll(buildOutput(revisions, title));
			
			file.close();
			writer.close();
			
			LOGGER.info("Wrote output \""+title.getTitle()+"\".");
		} catch (IOException e) {
			LOGGER.error("Output could not be written", e);
		}
	}
	
	/**
	 * 
	 * @param revisions	chronologically sorted revision of an preprocessed input dump
	 */
	public static List<String[]> buildOutput(List<ProcessedRevision> revisions, PageTitle title){
		
		List<String[]> output = new ArrayList<>(revisions.size());
		
		for(int index = 0 ; index < revisions.size() ; index++){
			
			ProcessedRevision judge = revisions.get(index);
			ProcessedRevision[] judged;
			String[] outputLine = new String[TL_MTDQ + 1 + (judged = judge.getJudged()).length * 4];
			buildRevisionOutput(judge, judged, title, outputLine);
			output.add(outputLine);
		} 
		return output;
	}
	
	public static void buildRevisionOutput(ProcessedRevision judge, ProcessedRevision[] judged, PageTitle title, String[] outputLine){

		outputLine[TL_DATE] 	= judge.getTimestamp().toString();
		outputLine[TL_ID] 	= String.valueOf(judge.getID());
		outputLine[TL_PAGE] 	= judge.getPageTitle().getTitle();
		outputLine[TL_CON] 	= judge.getContributorName();
		
		//judge edit info
		outputLine[TL_ED] = String.valueOf(judge.getEditDistance());
		try {
			
			// Edit longevity for three judges
			ProcessedRevision[] judges;
			if(judge.getJudging().length > 3){
				judges = new ProcessedRevision[]{
						judge.getJudging()[0],
						judge.getJudging()[1],
						judge.getJudging()[2]};
			} else {
				judges = judge.getJudging();
			}
			
			outputLine[TL_EL]= String.valueOf(EditRating.calculateAverageEditLongevity(judge, judge.getPrev(), judge.getJudging(), judge.getPageTitle()));
		} catch (VisitingException | SwebleException e) {
			outputLine[TL_EL]="ERROR";
			LOGGER.warn(e);
		}
		
		//judge text info
		outputLine[TL_IW] = String.valueOf(judge.getInsertedWhiteSpaceSegWords());
		outputLine[TL_TDQ]= String.valueOf(TextRating.calculateWhitespaceTextDecayQuality(judge, judge.getJudging()));
		
		//judge text info
		outputLine[TL_MIW] = String.valueOf(judge.getInsertedMarkupSegWords());
		outputLine[TL_MTDQ]= String.valueOf(TextRating.calculateMarkupSegTextDecayQuality(judge, judge.getJudging()));
		
		if(Objects.nonNull(judged) && judged.length != 0){
			
			for(int judgedIndex = 0; judgedIndex < judged.length; judgedIndex++){
				ProcessedRevision currJudged = judged[judged.length-1-judgedIndex];
				buildJudgedOutput(judge, currJudged, title, outputLine, judgedIndex);
			}
		}
	}
	
	
	public static void buildJudgedOutput(ProcessedRevision judge, ProcessedRevision currJudged, PageTitle title, String[] outputLine, int judgedIndex){	
		int outputIndex = TL_MTDQ+1;
		outputLine[outputIndex + (judgedIndex * 4)] = currJudged.getContributorName();
		
		// edit info
		double editLongevity = 0.0;
		try {
			
			double prevToJudge = EditRating.calculateAlternativeEditDistance(HDDiffUtils.buildEditScript(currJudged.getPrev(), judge, title));
			double currToJudge = EditRating.calculateAlternativeEditDistance(HDDiffUtils.buildEditScript(currJudged, judge, title));
			double prevToCurr = EditRating.calculateAlternativeEditDistance(HDDiffUtils.buildEditScript(currJudged.getPrev(), currJudged, title));
			
			if(prevToCurr != 0.0){
				editLongevity = (cSlack * prevToJudge - currToJudge)/prevToCurr;
			}
			
		} catch (VisitingException | SwebleException e) {
			LOGGER.warn(e);
		}
		outputLine[outputIndex + ((judgedIndex * 4) + 1)] = String.valueOf(calculateEditReputationUpdate(editLongevity, currJudged.getEditDistance()));
		
		// ws text info
		int insertedText = 0;
		if(currJudged.getWhiteSpaceSegWords().containsKey(currJudged.getID())){
			insertedText = currJudged.getWhiteSpaceSegWords().get(currJudged.getID());
		}
		double textUpdate = 0;
		if(insertedText != 0){
			int survivedText = 0;
			if(judge.getWhiteSpaceSegWords().containsKey(currJudged.getID())){
				survivedText = judge.getWhiteSpaceSegWords().get(currJudged.getID());
			}
			textUpdate = calculateTextReputationUpdate(survivedText, insertedText);
		}
		outputLine[outputIndex+ ((judgedIndex * 4) + 2)] = String.valueOf(textUpdate);
		
		// markup text info
		int markupinsertedText = 0;
		if(currJudged.getMarkupSegWords().containsKey(currJudged.getID())){
			markupinsertedText = currJudged.getMarkupSegWords().get(currJudged.getID());
		}
		double markuptextUpdate = 0;
		if(markupinsertedText != 0){
			int survivedText = 0;
			if(judge.getMarkupSegWords().containsKey(currJudged.getID())){
				survivedText = judge.getMarkupSegWords().get(currJudged.getID());
			}
			markuptextUpdate = calculateTextReputationUpdate(survivedText, markupinsertedText);
		}
		outputLine[outputIndex+ ((judgedIndex * 4) + 3)] = String.valueOf(markuptextUpdate);
	}
	
	
	
	/**
	 * This method calculates the value by which the reputation of a contributor should be updated considering the survival 
	 * of his/her introduced text. 
	 * 
	 * @param survivedText	number of survived words in the judging revision
	 * @param insertedText	number of inserted words by the judged contributor
	 * @param reputation	reputation of the judging contributor at the moment of his/her revision
	 * @return reputation update due to text introduction of the judged contributor
	 */
	static public double calculateTextReputationUpdate(int survivedText, int insertedText){
		return cScale * cText * ((double)survivedText/(double)insertedText) * Math.pow((double)insertedText, cLen);
	}
	
	/**
	 * This method calculates the value by which the reputation of a contributor should be updated considering the survival 
	 * of his/her introduced text. 
	 * 
	 * @param editLongevity	edit longevity up to the judging revision
	 * @param editDistance	edit distance made by the judged contributor
	 * @param reputation	reputation of the judging contributor at the moment of his/her revision
	 * @return reputation update due to edit work of the judged contributor
	 */
	static public double calculateEditReputationUpdate(double editLongevity, double editDistance){
		double q = editLongevity; //if no editLongevity can be calculated q is assigned to 0.
		if(q < 0){
			q = q * cPunish;
		}
		return q * cScale * (((double)1) - cText) * Math.pow(editDistance, cLen);
	}

}
