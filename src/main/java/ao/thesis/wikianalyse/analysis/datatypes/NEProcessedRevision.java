package ao.thesis.wikianalyse.analysis.datatypes;

import java.util.HashMap;

import org.joda.time.DateTime;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;

public class NEProcessedRevision extends ProcessedRevision {
	
	private int insertedNEs = 0;
	
	private int insertedNewNEs = 0;
	
	private int insertedPersons = 0;
	
	private int insertedNewPersons = 0;
	
	private int insertedLocations = 0;
	
	private int insertedNewLocatons = 0;
	
	private int insertedOrganizations = 0;
	
	private int insertedNewOrganizations = 0;
	
	private int insertedMiscs = 0;
	
	private int insertedNewMiscs = 0;
	
	private HashMap<Integer, Integer> nEs = null;
	
	private HashMap<Integer, Integer> newNEs = null;
	
	private HashMap<Integer, Integer> personNEs = null;
	
	private HashMap<Integer, Integer> newPersonNEs = null;
	
	private HashMap<Integer, Integer> locationNEs = null;
	
	private HashMap<Integer, Integer> newLocationNEs = null;
	
	private HashMap<Integer, Integer> organizationNEs = null;
	
	private HashMap<Integer, Integer> newOrganizationNEs = null;
	
	private HashMap<Integer, Integer> miscNEs = null;
	
	private HashMap<Integer, Integer> newMiscNEs = null;
	
	// References
	
	private ProcessedRevision prev = null;

	/**
	 * Revisions that follow this revisions and have a different editor thus can function as judges for this
	 * revision.
	 * WikiTrust TextDecayQuality: 	ten following revisions 
	 * WikiTrust EditLongevity:		three following revisions
	 * Efficiency:					three revisions after two weeks (in this program)
	 */
	private ProcessedRevision[] judging = null;
	
	/**
	 * Revisions that go before this revisions and have a different editor thus can be judges by this
	 * revision.
	 * WikiTrust TextDecayQuality: 	ten previous revisions 
	 * WikiTrust EditLongevity:		three previous revisions
	 * Efficiency:					three revisions before two weeks (in this program)
	 */
	private ProcessedRevision[] judged = null;
	
	public NEProcessedRevision(int id, String contributorName, DateTime timestamp, PageId pageId, PageTitle pageTitle, EngProcessedPage procPage) {
		super(id, contributorName, timestamp, pageId, pageTitle, procPage);
	}

	public ProcessedRevision[] getJudging() {
		return judging;
	}

	public void setJudging(ProcessedRevision[] judging) {
		this.judging = judging;
	}

	public ProcessedRevision[] getJudged() {
		return judged;
	}

	public void setJudged(ProcessedRevision[] judged) {
		this.judged = judged;
	}

	public ProcessedRevision getPrev() {
		return prev;
	}

	public void setPrev(ProcessedRevision prev) {
		this.prev = prev;
	}

	public int getInsertedNEs() {
		return insertedNEs;
	}

	public void setInsertedNEs(int insertedNEs) {
		this.insertedNEs = insertedNEs;
	}

	public int getInsertedPersons() {
		return insertedPersons;
	}

	public void setInsertedPersons(int insertedPersons) {
		this.insertedPersons = insertedPersons;
	}

	public int getInsertedNewPersons() {
		return insertedNewPersons;
	}

	public void setInsertedNewPersons(int insertedNewPersons) {
		this.insertedNewPersons = insertedNewPersons;
	}

	public int getInsertedLocations() {
		return insertedLocations;
	}

	public void setInsertedLocations(int insertedLocations) {
		this.insertedLocations = insertedLocations;
	}

	public int getInsertedNewLocatons() {
		return insertedNewLocatons;
	}

	public void setInsertedNewLocatons(int insertedNewLocatons) {
		this.insertedNewLocatons = insertedNewLocatons;
	}

	public int getInsertedOrganizations() {
		return insertedOrganizations;
	}

	public void setInsertedOrganizations(int insertedOrganizations) {
		this.insertedOrganizations = insertedOrganizations;
	}

	public int getInsertedNewOrganizations() {
		return insertedNewOrganizations;
	}

	public void setInsertedNewOrganizations(int insertedNewOrganizations) {
		this.insertedNewOrganizations = insertedNewOrganizations;
	}

	public int getInsertedMiscs() {
		return insertedMiscs;
	}

	public void setInsertedMiscs(int insertedMiscs) {
		this.insertedMiscs = insertedMiscs;
	}

	public int getInsertedNewMiscs() {
		return insertedNewMiscs;
	}

	public void setInsertedNewMiscs(int insertedNewMiscs) {
		this.insertedNewMiscs = insertedNewMiscs;
	}

	public HashMap<Integer, Integer> getNEs() {
		return nEs;
	}

	public void setNEs(HashMap<Integer, Integer> nEs) {
		this.nEs = nEs;
	}

	public HashMap<Integer, Integer> getNewNEs() {
		return newNEs;
	}

	public void setNewNEs(HashMap<Integer, Integer> newNEs) {
		this.newNEs = newNEs;
	}

	public HashMap<Integer, Integer> getPersonNEs() {
		return personNEs;
	}

	public void setPersonNEs(HashMap<Integer, Integer> personNEs) {
		this.personNEs = personNEs;
	}

	public HashMap<Integer, Integer> getNewPersonNEs() {
		return newPersonNEs;
	}

	public void setNewPersonNEs(HashMap<Integer, Integer> newPersonNEs) {
		this.newPersonNEs = newPersonNEs;
	}

	public HashMap<Integer, Integer> getLocationNEs() {
		return locationNEs;
	}

	public void setLocationNEs(HashMap<Integer, Integer> locationNEs) {
		this.locationNEs = locationNEs;
	}

	public HashMap<Integer, Integer> getNewLocationNEs() {
		return newLocationNEs;
	}

	public void setNewLocationNEs(HashMap<Integer, Integer> newLocationNEs) {
		this.newLocationNEs = newLocationNEs;
	}

	public HashMap<Integer, Integer> getOrganizationNEs() {
		return organizationNEs;
	}

	public void setOrganizationNEs(HashMap<Integer, Integer> organizationNEs) {
		this.organizationNEs = organizationNEs;
	}

	public HashMap<Integer, Integer> getNewOrganizationNEs() {
		return newOrganizationNEs;
	}

	public void setNewOrganizationNEs(HashMap<Integer, Integer> newOrganizationNEs) {
		this.newOrganizationNEs = newOrganizationNEs;
	}

	public HashMap<Integer, Integer> getMiscNEs() {
		return miscNEs;
	}

	public void setMiscNEs(HashMap<Integer, Integer> miscNEs) {
		this.miscNEs = miscNEs;
	}

	public HashMap<Integer, Integer> getNewMiscNEs() {
		return newMiscNEs;
	}

	public void setNewMiscNEs(HashMap<Integer, Integer> newMiscNEs) {
		this.newMiscNEs = newMiscNEs;
	}

	public int getInsertedNewNEs() {
		return insertedNewNEs;
	}

	public void setInsertedNewNEs(int insertedNewNEs) {
		this.insertedNewNEs = insertedNewNEs;
	}

}
