package ao.thesis.wikianalyse.io.input;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.Main;
import au.com.bytecode.opencsv.CSVWriter;

public class BarnstarReader {

private static final Logger LOGGER = Logger.getLogger(BarnstarReader.class);
	
	String dir;
	
	public BarnstarReader(String filename){
		this.dir = Main.DEF_OUTPUT_DIR+"/Barnstars/"+filename+".csv";
	}
	
	
	
	public void writeBarnstarOutput(String contributor, String numberOfBarnstars){

		try (
			FileWriter file = new FileWriter(dir, true);
			CSVWriter writer = new CSVWriter(file)
		) {
			String[] output = new String[]{contributor, numberOfBarnstars};
			writer.writeNext(output);
			
			file.close();
			writer.close();
			
			LOGGER.info("Wrote Barnstar output \""+contributor+"\".");
		} catch (IOException e) {
			LOGGER.error("Barnstar Output could not be written", e);
		}
	}
	
}
