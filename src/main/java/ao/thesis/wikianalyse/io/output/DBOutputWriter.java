package ao.thesis.wikianalyse.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.Main;
import ao.thesis.wikianalyse.analysis.ReputationDatabase;
import au.com.bytecode.opencsv.CSVWriter;

public class DBOutputWriter {
	
	private static final Logger LOGGER = Logger.getLogger(DBOutputWriter.class);
	
	public void write(ReputationDatabase reputationDB){
		HashMap<String, Double> contributorReputation = (HashMap<String, Double>) reputationDB.getDatabase();
		List<String[]> output = new ArrayList<>();
		
		for(Entry<String, Double> entry : contributorReputation.entrySet()){
			String[] line = {entry.getKey(), String.valueOf(entry.getValue())};
			output.add(line);
		}
		writeFile("/Reputation.csv", output);
	}
	
	public void writeFile(String filename, List<String[]> output) {
		try (
			FileWriter file = new FileWriter(Main.DEF_OUTPUT_DIR + filename);
			CSVWriter writer = new CSVWriter(file);
		){
			writer.writeAll(output);
			writer.close();
			file.close();
			
			LOGGER.info("Wrote output \""+filename+"\".");
		} catch (IOException e) {
			LOGGER.error("Output could not be written", e);
		}
	}

}
