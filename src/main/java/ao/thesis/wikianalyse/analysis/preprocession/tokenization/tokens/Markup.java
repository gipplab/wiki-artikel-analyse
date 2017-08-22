package ao.thesis.wikianalyse.analysis.preprocession.tokenization.tokens;

public enum Markup
{
	HEADER, LINK, EXTERNLINK, FILE, CATEGORY, IMAGELINK, BOLD, ITALIC, MATH;

	@Override
	public String toString()
	{
		switch(this)
		{
			case IMAGELINK: 
				return "imagelink";
			case MATH: 
				return "math";
			case EXTERNLINK: 
				return "extern";
			case CATEGORY: 
				return "category";
			case FILE: 
				return "file";
			case LINK: 
				return "link";
			case HEADER: 
				return "header";
			case BOLD: 
				return "bold";
			case ITALIC: 
				return "italic";
			default:
				return "";
		}
	}
}
