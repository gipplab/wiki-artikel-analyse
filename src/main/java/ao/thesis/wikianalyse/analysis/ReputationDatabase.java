package ao.thesis.wikianalyse.analysis;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.helper.Validate;


/**
 * ReputationDatabase
 * 
 * Represents all registered contributors
 * 
 * @author Anna Opaska
 *
 */
public class ReputationDatabase{
	
	public static final double INIT_REPUTATION 	= 0.1;
	public static final double MAX_REPUTATION 	= 22026.0;
	
	private HashMap<String, Double> contributorReputation;

	public ReputationDatabase(int limitRevisions){
		init(limitRevisions);
	}
	
	public ReputationDatabase(){
		init();
	}
	
	private void init(int limitRevisions){
		contributorReputation = new HashMap<>(limitRevisions);
		contributorReputation.put("Anonymous", INIT_REPUTATION); //single anonymous editor
	}
	
	private void init(){
		contributorReputation = new HashMap<>();
		contributorReputation.put("Anonymous", INIT_REPUTATION); //single anonymous editor
	}
	
	public void updateReputation(String contributor, double value){
		if(contributorReputation.containsKey(contributor)){
			double update = getReputation(contributor) + value;
			setReputation(contributor, update);
		}
	}
	
	public void correctReputation(){
		for(String contributor : contributorReputation.keySet()){
			double reputation = getReputation(contributor);
			if(reputation > MAX_REPUTATION){
				setReputation(contributor, MAX_REPUTATION);
			} else if(reputation < INIT_REPUTATION){
				setReputation(contributor, MAX_REPUTATION);
			}
		}
	}

	public void addContributor(String contributor) {
		if(!contributorReputation.containsKey(contributor)){
			setReputation(contributor, INIT_REPUTATION);
		}
	}
	
	public Map getDatabase(){
		return contributorReputation;
	}

	public double getReputation(String contributor) {
		try{
		Validate.isTrue(contributorReputation.containsKey(contributor));
		}catch(IllegalArgumentException e){
			System.out.println(contributor);
			e.getStackTrace();
		}
		return contributorReputation.get(contributor);
	}

	public void setReputation(String contributor, double rep) {
		contributorReputation.put(contributor, rep);
	}
}
