package tokenizer;

import java.util.ArrayList;

import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public class Tokenizer {
	
	private static Tokenizer tokenizer = new Tokenizer();
	
	private TextConverter converter;
	
	public static Tokenizer getTokenizer(){return tokenizer;}
	
	private Tokenizer(){}
	
	//------------------------------------------
	
	public ArrayList<ArrayList<Token>> tokenize(PageType page) throws LinkTargetException, EngineException
	{
		int numberOfRevisions = page.getRevisionOrUpload().size();
		
		ArrayList<ArrayList<Token>> tokenizedRevisions = new ArrayList<ArrayList<Token>>(numberOfRevisions+1);
		
		ArrayList<Token> emptyRevision = new ArrayList<Token>();
		tokenizedRevisions.add(emptyRevision);
		
		System.out.println("page \""+page.getTitle()+"\" has "+page.getRevisionOrUpload().size()+" revisions");
		
		int revisionNumber=1;
		
		for(Object item : page.getRevisionOrUpload())
		{
			RevisionType revision = (RevisionType) item;
			
			ArrayList<Token> tokens = new ArrayList<Token>();

			converter = new TextConverter(page.getTitle(),revision.getText().getValue());
	
			tokens = (ArrayList<Token>) converter.go(converter.cp.getPage());

			int sourcePosition=0;
			for(Token token : tokens)
			{
				token.setSource(revision.getContributor().getId(), revisionNumber, sourcePosition);
				sourcePosition++;
			}
			tokenizedRevisions.add(tokens);
			
			System.out.println("set tokens: "+revisionNumber);
			
			revisionNumber++;
		}
		return tokenizedRevisions;
	}

}
