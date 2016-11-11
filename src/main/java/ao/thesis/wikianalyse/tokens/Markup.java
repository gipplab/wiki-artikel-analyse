package ao.thesis.wikianalyse.tokens;

public enum Markup
{
	TEXT, HEADER, LINK, EXTERNLINK, FILE, CATEGORY, BOLD, ITALIC;

	@Override
	public String toString()
	{
		switch(this)
		{
			case EXTERNLINK: return "extern";
			case CATEGORY: return "category";
			case FILE: return "file";
			case LINK: return "link";
			case HEADER: return "header";
			case BOLD: return "bold";
			case ITALIC: return "italic";
			default: return "text";
		}
	}
}
