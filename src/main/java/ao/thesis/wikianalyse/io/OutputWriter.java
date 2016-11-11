package ao.thesis.wikianalyse.io;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ao.thesis.wikianalyse.tokens.MarkupToken;
import ao.thesis.wikianalyse.tokens.MathToken;
import ao.thesis.wikianalyse.tokens.Token;

import au.com.bytecode.opencsv.CSVWriter;


public class OutputWriter {
	
	private String folder;
	private CSVWriter writer;
	private static Logger logger = Logger.getLogger(OutputWriter.class);
	
	List<String[]> editorOutput;
		
	public OutputWriter(String outputDir, String pageTitle) {
		
		this.folder = outputDir+"/"+pageTitle+"/";
		
		try {
			Files.createDirectories(Paths.get(folder));
		} catch (IOException e) {
			logger.error("Output writer could not create output folder.", e);
		}
		
		editorOutput = new ArrayList<String[]>();
		String[] column = new String[10];
		column[0]="Username";
		column[1]="UserId";
		column[2]="RevisionCount";
		column[3]="WordCount";
		column[4]="NECount";
		column[5]="MathCount";
		column[6]="Average Edit Longevity";
		column[7]="Average Text Quality";
		column[8]="Efficiency";
		column[9]="Content in latest version";
		editorOutput.add(column);
	}

	public void addEditor(String username, BigInteger editorId, double editq,
			double textq, int revisionCount, int wordCount,
			int namedEntityCount, int mathEntityCount, double percent, 
			double efficiency) {
		
		String[] column = new String[10];
		column[0]=username;
		column[1]=String.valueOf(editorId);
		column[2]=String.valueOf(revisionCount);
		column[3]=String.valueOf(wordCount);
		column[4]=String.valueOf(namedEntityCount);
		column[5]=String.valueOf(mathEntityCount);
		column[6]=String.valueOf(editq);
		column[7]=String.valueOf(textq);
		column[8]=String.valueOf(efficiency);
		column[9]=String.valueOf(percent);
		editorOutput.add(column);
	}

	public void writeEditorOutput() {
		try {
			FileWriter repuFile = new FileWriter(folder+"ReputationValues.csv");
			writer = new CSVWriter(repuFile);
			writer.writeAll(editorOutput);
			writer.close();
			
			logger.info("Wrote editor output for "+editorOutput.size()+" editors.");
		} catch (IOException e) {
			logger.error("Editor output could not be written",e);
		}
	}

	public void writeTokenOutput(List<Object> tokens, int index) {
		
		List<String[]> tokenOutput = new ArrayList<String[]>();
		
		String[] column = new String[6];
		column[0]="Content";
		column[1]="ID of source revision";
		column[2]="Named Entity";
		column[3]="Reference";
		column[4]="Markup";
		column[5]="Position (MathToken)";
		tokenOutput.add(column);
		
		for(Object item : tokens){
			if(item instanceof Token){
				Token token = (Token) item;
	
				column = new String[6];
				column[0]="\""+token.getTextContent()+"\"";
				column[1]=String.valueOf(token.getSourceID());
				column[2]=token.getEntity();
				
				if(token instanceof MarkupToken){
					column[3]=((MarkupToken) token).getLinkReference();
					column[4]=((MarkupToken) token).getMarkup().toString();
					column[5]="/";
				} else if(token instanceof MathToken){
					column[3]=((MathToken) token).getFormula();
					column[4]="math";
					column[5]=String.valueOf(((MathToken) token).getPositionInFormula());
				}
				tokenOutput.add(column);
			}
		}
		
		try {
			FileWriter tokenFile = new FileWriter(folder+"Tokens"+index+".csv");
			writer = new CSVWriter(tokenFile);
			writer.writeAll(tokenOutput);
			writer.close();
			
			logger.info("Wrote token output "+index+".");
		} catch (IOException e) {
			logger.error("Token output "+index+" could not be written",e);
		}
	}
}
