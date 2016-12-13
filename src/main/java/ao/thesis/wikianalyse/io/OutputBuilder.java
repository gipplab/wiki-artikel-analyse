package ao.thesis.wikianalyse.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import ao.thesis.wikianalyse.model.RevisionID;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MarkupToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.MathFormula;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.NEToken;
import ao.thesis.wikianalyse.utils.textanalyse.tokens.Token;


public class OutputBuilder {
	
	private String[] tokenInfoHeadlines = new String[]{"Content", "Info", "Reference", "Editor", "Source Index", "Source Timestamp", };
	private String[] revInfoHeadlines = new String[]{"Index","Pagetitle", "Time", "Editor"};
	private String[] editorInfoHeadlines = new String[]{"Editorname","EditorId", "Usergroup"}; //TODO barnstars
	

	private String[] setPageEditorHeadLine(Set<RevisionID> pageIds, String[] ratingHeadlines){
		
		String[] editornames = (String[]) pageIds.stream().map(id -> id.getUsername()).distinct().toArray();
		String[] ratingforAllUsersHL = new String[editornames.length*ratingHeadlines.length];
		
		for(int i=0 ; i < editornames.length ; i++){
			for(int j=0 ; j < ratingHeadlines.length ; j++){
				ratingforAllUsersHL[i] = editornames[i]+" ("+ratingHeadlines[j]+")";
			}
		}
		return (String[]) ArrayUtils.addAll(revInfoHeadlines, ratingforAllUsersHL);
	}
	
	public List<String[]> buildPageDevelopmentOutput(Map<RevisionID, String[]> ratings, String[] ratingheadlines){
		
		List<String[]> output = new ArrayList<String[]>(ratings.size()+1);
		output.add(setPageEditorHeadLine(ratings.keySet(), ratingheadlines));
		
		for(Entry<RevisionID, String[]> rating : ratings.entrySet()){
			if(!rating.getKey().isNullRevision()){
				output.add((String[]) ArrayUtils.addAll(rating.getKey().getOutputInfoLine(), rating.getValue()));
			}
		}
		return output;
	}
	
	public List<String[]> buildTimeline(Map<RevisionID, String[]> ratings, String[] ratingheadlines){
		
		List<String[]> output = new ArrayList<String[]>(ratings.size()+1);
		
		output.add((String[]) ArrayUtils.addAll(revInfoHeadlines, ratingheadlines));
		for(Entry<RevisionID, String[]> rating : ratings.entrySet()){
			if(!rating.getKey().isNullRevision()){
				output.add((String[]) ArrayUtils.addAll(rating.getKey().getOutputInfoLine(), rating.getValue()));
			}
		}
		return output;
	}
	
	public List<String[]> buildEditorOutput(Map<RevisionID, String[]> ratings, String[] ratingheadlines) {
		
		List<String[]> output = new ArrayList<String[]>(ratings.size()+1);
		output.add((String[]) ArrayUtils.addAll(editorInfoHeadlines, ratingheadlines));
		
		return output;
	}
	
	public List<String[]> buildTokenOutput(List<Token> revision){
		
		List<String[]> output = new ArrayList<String[]>();
		output.add(tokenInfoHeadlines);
		
		for(Token item : revision){

			if(item instanceof MathFormula){
				if(((MathFormula) item).isEmpty()){
					output.add(getTokenLine(item, "math",((MathFormula) item).getText()));
				} else {
					for(Token token : (MathFormula) item){
						output.add(getTokenLine(token, "math", ((MathFormula) item).getText()));
					}
				}
			} else if(item instanceof NEToken){
				for(Token neToken : (NEToken) item){
					output.add(getTokenLine(neToken, ((NEToken) item).getEntity(), ((NEToken) item).getText()));
				}
			} else output.add(getTokenLine((MarkupToken) item, ((MarkupToken) item).getMarkup().toString(), ((MarkupToken) item).getLinkReference()));
		}
		return output;
	}
	
	private String[] getTokenLine(Token token, String info, String ref){
		return new String[]{
				((Token) token).getText(), 
				info,
				ref,
				token.getSourceId().getUsername(),
				String.valueOf(token.getSourceId().getIndex()),
				String.valueOf(token.getSourceId().getTimestamp())};
	}
}
