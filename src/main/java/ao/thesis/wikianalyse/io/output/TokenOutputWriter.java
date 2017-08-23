package ao.thesis.wikianalyse.io.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sweble.wikitext.engine.PageTitle;

import ao.thesis.wikianalyse.Main;
import ao.thesis.wikianalyse.analysis.datatypes.MarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTMarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WhitespaceRevision;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MarkupToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.MathFormulaToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.NEToken;
import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;
import au.com.bytecode.opencsv.CSVWriter;

public class TokenOutputWriter {
	
	private static final Logger LOGGER = Logger.getLogger(TokenOutputWriter.class);
	
	private TokenOutputWriter(){}
	
	public static void writeTokens(List<PreprocessedRevision> revisions, PageTitle title){
		int counter = 0;
		for(PreprocessedRevision revision : revisions){
			
			if (revision instanceof WTMarkupSegmentedRevision){
				writeWhitespaceSegTokens(((WTMarkupSegmentedRevision) revision).getWhitespaceRevision(), title, counter);
				writeMarkupSegTokens((WTMarkupSegmentedRevision) revision, title, counter);
				
			} else if(revision instanceof WhitespaceRevision){
				writeWhitespaceSegTokens((WhitespaceRevision) revision, title, counter);
				
			} else if(revision instanceof MarkupSegmentedRevision){
				writeMarkupSegTokens((MarkupSegmentedRevision) revision, title, counter);
				
			} else if(revision instanceof WTRevision){
				writeWhitespaceSegTokens(((WTRevision) revision).getWhitespaceRevision(), title, counter);
				
			} else if(revision instanceof NERevision){
				writeWhitespaceSegTokens((NERevision) revision, title, counter);
				
			} 
					
			counter++;
		}
	}
	
	private static void writeWhitespaceSegTokens(NERevision revision, PageTitle title, int counter) {
		try (
				FileWriter file = new FileWriter(Main.TOKEN_DIR+title.getTitle().replaceAll(":", "_")+"_WSTokens"+counter+".csv");
				CSVWriter writer = new CSVWriter(file)
			) {
				writer.writeAll(buildTokenOutput((List)revision.getNETokens()));
				
				file.close();
				writer.close();
				
				LOGGER.info("Wrote output \""+title.getNormalizedFullTitle()+"_WSTokens"+counter+"\".");
			} catch (IOException e) {
				LOGGER.error("Token Output could not be written", e);
			}
	}

	public static void writeWhitespaceSegTokens(WhitespaceRevision revision, PageTitle title, int counter){
		try (
				FileWriter file = new FileWriter(Main.TOKEN_DIR+title.getTitle().replaceAll(":", "_")+"_WSTokens"+counter+".csv");
				CSVWriter writer = new CSVWriter(file)
			) {
				writer.writeAll(buildTokenOutput((List)revision.getWhiteSpaceSegmentedTokens()));
				
				file.close();
				writer.close();
				
				LOGGER.info("Wrote output \""+title.getNormalizedFullTitle()+"_WSTokens"+counter+"\".");
			} catch (IOException e) {
				LOGGER.error("Token Output could not be written", e);
			}
	}
	
	public static void writeMarkupSegTokens(MarkupSegmentedRevision revision, PageTitle title, int counter){
		try (
				FileWriter file = new FileWriter(Main.TOKEN_DIR+title.getTitle().replaceAll(":", "_")+"_MSTokens"+counter+".csv");
				CSVWriter writer = new CSVWriter(file);
				FileWriter mathfile = new FileWriter(Main.TOKEN_DIR+title.getTitle().replaceAll(":", "_")+"_MathTokens"+counter+".csv");
				CSVWriter mathwriter = new CSVWriter(mathfile);
			) {
				writer.writeAll(buildTokenOutput((List)revision.getMarkupSegmentedTokens()));
				List<String[]> output = new ArrayList<>();
				if(revision.getMathFormulas() != null){
					
					for(Token item : revision.getMathFormulas()){
						for(Object token : ((MathFormulaToken)item).getElements()){
							output.add(getTokenLine((Token)token, "math",((MathFormulaToken) item).getText()));
						}
					}
					mathwriter.writeAll(output);
				} else {
					mathwriter.writeNext(new String[]{});
				}
				
				file.close();
				writer.close();
				mathfile.close();
				mathwriter.close();
				
				LOGGER.info("Wrote output \""+title.getTitle().replaceAll(":", "_")+"_MSTokens"+counter+"\".");
			} catch (IOException e) {
				LOGGER.error("Token Output could not be written", e);
			}
	}
	
//	public static void writeMarkupSegTokens(MarkupRevision revision, PageTitle title, int counter){
//		try (
//				FileWriter file = new FileWriter(Main.TOKEN_DIR+title.getNormalizedFullTitle()+"_MTokens"+counter+".csv");
//				CSVWriter writer = new CSVWriter(file)
//			) {
//				writer.writeAll(buildTokenOutput((List)revision.getMarkupStringTokenizedText()));
//				
//				file.close();
//				writer.close();
//				
//				LOGGER.info("Wrote output \""+title.getNormalizedFullTitle()+"_MTokens"+counter+"\".");
//			} catch (IOException e) {
//				LOGGER.error("Token Output could not be written", e);
//			}
//	}
	
	
	public static List<String[]> buildTokenOutput(List<Token> tokens){
		
		List<String[]> output = new ArrayList<>();
		
		for(Token item : tokens){

			if(item instanceof MathFormulaToken){
				output.add(getTokenLine(item, "math",((MathFormulaToken) item).getText()));
				
//				for(Object token : ((MathFormulaToken)item).getElements()){
//					output.add(getTokenLine((Token)token, "math",((MathFormulaToken) item).getText()));
//				}
				
			} else if(item instanceof NEToken){
				output.add(getTokenLine(item, ((NEToken) item).getEntity(), ((NEToken) item).getText()));
				
			} else if(item instanceof MarkupToken) {
				output.add(getTokenLine(item, ((MarkupToken) item).getMarkup().toString(), ((MarkupToken) item).getLinkReference()));
				
			} else {
				output.add(getTokenLine(item, "/", "/"));
			}
		}
		return output;
	}
	
	
	private static String[] getTokenLine(Token token, String info, String ref){
		String text;
		if((text = token.getText()).equals(",")){
			text="\",\"";
		}
		return new String[]{text, info, ref, String.valueOf(token.getSourceId())};
	}

}
