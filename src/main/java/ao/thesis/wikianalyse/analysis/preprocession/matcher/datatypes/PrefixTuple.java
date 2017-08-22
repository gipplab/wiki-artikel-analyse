package ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes;

import java.util.Arrays;

import ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens.Token;

public class PrefixTuple {
	
	public Token[] tokens;
	
	public PrefixTuple(Token[] tokens){
		this.tokens=tokens;
	}
	
	@Override
	public int hashCode(){
		StringBuilder bld = new StringBuilder();
		for(int i=0; i<tokens.length;i++){
			bld.append(tokens[i]+" ");
		}
		return bld.toString().trim().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof PrefixTuple && Arrays.equals(tokens, ((PrefixTuple)o).tokens));
	}
	
	@Override
	public String toString(){
		StringBuilder bld = new StringBuilder();
		for(int i=0; i<tokens.length;i++){
			bld.append(tokens[i]+" ");
		}
		return "["+bld.toString().trim()+"]";
	}
	
	public void setNoMatching(){
		for(int i=0; i<tokens.length;i++){
			tokens[i].setSourceId(-1);
		}
	}
	
}