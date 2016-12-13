package ao.thesis.wikianalyse.model;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class Editor {

	private final String username; //or ip
	
	private final BigInteger id;

//	private Map<RevisionID, Double> reputationTimeline = new HashMap<RevisionID, Double>();
	
	double reputation = 0.1;
	
	public Editor(String username, BigInteger id){
		this.username = username;
		this.id = id;
	}
	
//	public void setReputation(RevisionID id, double value){
//		reputationTimeline.put(id, value);
//	}
	
	public void updateReputation(double value, double judgingReputation){
		reputation += value * judgingReputation;
	}

	public Double getReputation() {
		return reputation;
	}

	String getUsername() {
		return username;
	}

	BigInteger getId() {
		return id;
	}
	
	String getIp() {
		if(id.equals(BigInteger.ZERO)){
			return username;
		} else return null;
	}
	
//	/**
//	 * reduces all anonymous editors to one.
//	 */
//	public boolean equals(Editor e){
//		return this.id.equals(e.getId());
//	}
	
	public boolean equals(Editor e){
		return (id.equals(e.getId()) && username.equals(e.getUsername()));
	}

	public void setBarnstarInformation(Object parseUserPage) {
		// TODO Auto-generated method stub
		
	}
}
