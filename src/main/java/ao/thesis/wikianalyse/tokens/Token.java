package ao.thesis.wikianalyse.tokens;

import java.math.BigInteger;

public abstract class Token {
	
	final private String content;
	
	private BigInteger sourceId;
	
	private String namedEntity;
	
	public Token(String content){
		this.content=content;
		this.namedEntity="O";
	}
	
	public String getTextContent() {
		return content;
	}
	
	public BigInteger getSourceID() {
		return sourceId;
	}
	
	public void setSourceID(BigInteger sourceId) {
		this.sourceId=sourceId;
	}
	
	public String getEntity() {
		return namedEntity;
	}
	
	public void setEntity(String entity) {
		this.namedEntity=entity;
	}
	
	@Override
	public String toString() {
		return content;
	}
	
	@Override
	public int hashCode() {
		return this.content.hashCode();
	};
	
	@Override
	public boolean equals(Object c) {
		return (this.content.equalsIgnoreCase(((Token)c).content) &&
				this.namedEntity.equals(((Token)c).namedEntity));
	};


}
