package tokenizer;


import java.math.BigInteger;

public class Token{
	
	public enum Markup{
		
		TEXT, HEADER, LINK, EXTERNLINK, FILE, CATEGORY, MATH, BOLD, ITALIC;

		public String toString()
		{
			switch(this)
			{
				case EXTERNLINK: return "extern";
				case CATEGORY: return "category";
				case FILE: return "file";
				case MATH: return "math";
				case LINK: return "link";
				case HEADER: return "header";
				case BOLD: return "bold";
				case ITALIC: return "italic";
				default: return "";
			}
		}
	}
	
	private Markup markup;

	private String content;
	
	private String linkReference;
	
	private String namedEntity;
	
	//-------------------------------------------
	
	private BigInteger sourceAuthorID;
	
	private int sourceRevNumber;
	
	private int sourcePosition;
	
	//-------------------------------------------
	
	@Override
	public int hashCode()
	{
		return this.content.hashCode();
	};
	
	@Override
	public boolean equals(Object c)
	{
		return (this.content.equalsIgnoreCase(((Token)c).content) &&
				this.linkReference.equalsIgnoreCase(((Token)c).linkReference) &&
				this.markup.equals(((Token)c).markup) &&
				this.namedEntity.equals(((Token)c).namedEntity));
	};

	public Token(String textContent, String linkReference, Markup markup)
	{
		this.content=textContent;
		this.linkReference=linkReference;
		this.markup=markup;
	};
	
	public void setSource(BigInteger aID, int sourceRevNumber, int sourcePosition)
	{
		this.sourceAuthorID=aID;
		this.sourceRevNumber=sourceRevNumber;
		this.sourcePosition=sourcePosition;
	}

	public void setNamedEntity(String namedEntity)
	{
		this.namedEntity=namedEntity;
	}

	public String getNamedEntity()
	{
		return this.namedEntity;
	}
	
	public int getSourceRevision()
	{
		return this.sourceRevNumber;
	}
	
	public int getSourcePosition()
	{
		return this.sourcePosition;
	}
	
	public BigInteger getContributorID()
	{
		return this.sourceAuthorID;
	}
	
	public Markup getMarkup()
	{
		return this.markup;
	}

	public String getContent()
	{
		return this.content;
	}

}
