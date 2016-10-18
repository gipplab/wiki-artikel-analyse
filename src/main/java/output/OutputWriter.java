package output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.sweble.wikitext.dumpreader.export_0_10.ContributorType;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;

import au.com.bytecode.opencsv.CSVWriter;
import main.Main;
import tokenizer.Token;


public class OutputWriter {
	
	private CSVWriter writer;
	
	private PageType page;
	
	private ArrayList<ArrayList<Token>> revisions;
	
	private HashSet<ContributorType> editors = new HashSet<ContributorType>();
	
	public OutputWriter(PageType page, ArrayList<ArrayList<Token>> revisions) 
	{
		this.page=page;
		this.revisions=revisions;
		
		for (Object item : page.getRevisionOrUpload())
		{
			RevisionType revision= (RevisionType) item;
			editors.add(revision.getContributor());
		}
		
	}
	
	public void write() throws IOException
	{
		System.out.println("write output for \""+page.getTitle()+"\"");
		
		String folder = Main.OUTPUT_DIR+"/"+page.getTitle()+"/";
		
		FileWriter fw1 = new FileWriter(folder+"WordCounts.csv");
		FileWriter fw2 = new FileWriter(folder+"TokensInNewestVersion.csv");
		
		try {
			
			writer = new CSVWriter(fw1);
			writer.writeAll(buildPageHistoryOutput());
			writer.close();

			writer = new CSVWriter(fw2);
			writer.writeAll(buildTokenOutput(revisions.get(revisions.size()-1)));
			writer.close();
			
		} catch (IOException e) {
			System.err.println("Outputfiles could not be written");
		}
	}

	
	private ArrayList<String[]> buildPageHistoryOutput()
	{
		ArrayList<String[]> data = new ArrayList<String[]>();
				
		String[] column = new String[editors.size()+2];
		
		column[0]="Timestamp";
		column[1]="Username";
		int counter=2;
		for(ContributorType editor : editors)
		{
			column[counter]=editor.getUsername();
			counter++;
		}
		
		data.add(column);
		
		int revisioncounter=0;
		for (Object item : page.getRevisionOrUpload())
		{
			RevisionType revision= (RevisionType) item;
			
			column = new String[editors.size()+2];
			column[0]=String.valueOf(revision.getTimestamp());
			column[1]=revision.getContributor().getUsername();
			
			counter=2;
			int wordCounter;
			for(ContributorType editor : editors)
			{
				wordCounter=0;
				for(Token token: revisions.get(revisioncounter))
					if((editor.getId()!=null && token.getContributorID()!=null 
							&& token.getContributorID().equals(editor.getId())
							|| (editor.getId()==null) && token.getContributorID()==null))
						wordCounter++;
				column[counter]=String.valueOf(wordCounter);
				counter++;
			}
			
			data.add(column);
			revisioncounter++;
		}
		return data;
	}
	
	private ArrayList<String[]> buildTokenOutput(ArrayList<Token> revision){
		
		ArrayList<String[]> data = new ArrayList<String[]>();
		
		String[] column = new String[5];
		column[0]="Token";
		column[1]="Author";
		column[2]="Insert in Revision Nr.";
		column[3]="Markup";
		column[4]="Named Entity";
		
		data.add(column);
		
		for(Token token : revision){
			column = new String[7];
			String needQuote="";
			
			if(token.getContent().equals(","))
				needQuote="\"";
			
			column[0]=needQuote+token.getContent()+needQuote;
			column[1]=String.valueOf(token.getContributorID());
			column[2]=String.valueOf(token.getSourceRevision());
			column[3]=token.getMarkup().toString();
			column[4]=token.getNamedEntity();
			data.add(column);
			
			needQuote="";
		}
		
		return data;
	}
	
}
