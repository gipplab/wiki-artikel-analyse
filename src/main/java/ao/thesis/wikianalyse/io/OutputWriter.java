package ao.thesis.wikianalyse.io;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;
import au.com.bytecode.opencsv.CSVWriter;


public class OutputWriter {
	
	private static Logger logger = Logger.getLogger(OutputWriter.class);
	
	private CSVWriter writer;
	private OutputBuilder builder = new OutputBuilder();

	private String dir;
	
	
	public OutputWriter(String outputDir, Set<String> pages) {
		this.dir = outputDir;
		pages.stream().forEach(page -> writeFolder(dir, page));
	}
	
	private void writeFolder(String dir, String page){
		try {
			Files.createDirectories(Paths.get(dir+"/"+page.replaceAll("[^\\w\\-]", "_")+"/"));
		} catch (IOException e) {
			logger.error("Output writer could not build output folder "+page+".", e);
		}
	}
	
	public void writeFile(String filename, List<String[]> output) {
		try {
			FileWriter file = new FileWriter(dir + filename);
			writer = new CSVWriter(file);
			writer.writeAll(output);
			writer.close();
			
			logger.info("Wrote output \""+filename+"\".");
		} catch (IOException e) {
			logger.error("Output could not be written", e);
		}
	}
	
	public void writeTokenOutput(List<List<Token>> revisions, String title, String folder) throws IOException {
		String newtitle = title.replaceAll("[^\\w\\-]", "_");
		writeFolder(dir+"/"+newtitle, folder);
		int index = 0;
		for(List<Token> revision : revisions){
			writeFile("/"+newtitle+"/"+folder+"/Tokens"+index+".csv", builder.buildTokenOutput(revision));
			index++;
		}	
	}
	
	public void writeRatingsOutput(Map<RevisionID, String[]> ratings, String[] ratingheadlines, String title, String folder) throws IOException {
		String newtitle = title.replaceAll("[^\\w\\-]", "_");
		writeFolder(dir+"/"+newtitle, folder);
		writeFile("/"+newtitle+"/"+folder+"/Rating.csv", builder.buildTimeline(ratings, ratingheadlines));
	}

	public void writeTimelineOutput(Map<RevisionID, String[]> allratings, String[] ratingheadlines) {
		writeFile("/Timeline.csv", builder.buildTimeline(allratings, ratingheadlines));
	}

//	public void writeEditorOutput(Map<RevisionID, String[]> ratings, String[] ratingheadlines, String title) {
//		String newtitle = title.replaceAll("[^\\w\\-]", "_");
//		writeFile("/"+newtitle+"/"+"/Editors.csv", builder.buildEditorOutput(ratings, ratingheadlines));
//	}
	
}
