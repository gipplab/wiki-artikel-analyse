package ao.thesis.wikianalyse.analysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.helper.Validate;

public class PagesPerContributorDB {

	private HashMap<String, List> pagesPerContributor;

	public PagesPerContributorDB(){
		init();
	}
	
	private void init(){
		pagesPerContributor = new HashMap<>();
		pagesPerContributor.put("Anonymous", null); //single anonymous editor
	}

	public void addContributor(String contributor) {
		if(!pagesPerContributor.containsKey(contributor)){
			pagesPerContributor.put(contributor, new LinkedList<String>());
		}
	}
	
	public Map getDatabase(){
		return pagesPerContributor;
	}

	public int getNumberOfPages(String contributor) {
		if(!contributor.equals("Anonymous")){
			Validate.isTrue(pagesPerContributor.containsKey(contributor));
			return pagesPerContributor.get(contributor).size();
		} else 
			return -1;
	}

	public void addPageIfNew(String contributor, String page) {
		if(!contributor.equals("Anonymous") && !pagesPerContributor.get(contributor).contains(page)){
			pagesPerContributor.get(contributor).add(page);
		}
	}
	
}
