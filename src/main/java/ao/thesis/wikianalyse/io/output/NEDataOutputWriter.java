package ao.thesis.wikianalyse.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.Main;
import ao.thesis.wikianalyse.analysis.datatypes.NEProcessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.procession.TextRating;
import au.com.bytecode.opencsv.CSVWriter;

public class NEDataOutputWriter {
	
	private static final Logger LOGGER = Logger.getLogger(NEDataOutputWriter.class);
	private final String dir;
	
	public NEDataOutputWriter(String filename){
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
	
	public static List<String[]> buildOutput(List<ProcessedRevision> revisions, PageTitle title){
		
		List<String[]> output = new ArrayList<>(revisions.size());
		
		for(int index = 0 ; index < revisions.size() ; index++){
			ProcessedRevision judge = revisions.get(index);
			String[] outputLine = new String[12];
			buildRevisionOutput(judge, title, outputLine);
			output.add(outputLine);
		} 
		return output;
	}
	
	public static void buildRevisionOutput(ProcessedRevision judge, PageTitle title, String[] outputLine){

		outputLine[0] 	= judge.getTimestamp().toString();
		outputLine[1] 	= String.valueOf(judge.getID());
		outputLine[2] 	= judge.getPageTitle().getTitle();
		outputLine[3] 	= judge.getContributorName();
		
		outputLine[4]	= String.valueOf(((NEProcessedRevision)judge).getInsertedPersons());
		outputLine[5]	= String.valueOf(((NEProcessedRevision)judge).getInsertedLocations());
		outputLine[6]	= String.valueOf(((NEProcessedRevision)judge).getInsertedOrganizations());
		outputLine[7]	= String.valueOf(((NEProcessedRevision)judge).getInsertedMiscs());
		
		outputLine[8]	= String.valueOf(TextRating.calculateNETextDecayQuality(judge, judge.getJudging(), "Person"));
		outputLine[9]	= String.valueOf(TextRating.calculateNETextDecayQuality(judge, judge.getJudging(), "Location"));
		outputLine[10]	= String.valueOf(TextRating.calculateNETextDecayQuality(judge, judge.getJudging(), "Organization"));
		outputLine[11]	= String.valueOf(TextRating.calculateNETextDecayQuality(judge, judge.getJudging(), "Misc"));
		
	}

}
