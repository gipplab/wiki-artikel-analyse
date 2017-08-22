package ao.thesis.wikianalyse.analysis.procession;

import java.util.List;
import java.util.stream.Collectors;

import ao.thesis.wikianalyse.analysis.datatypes.MarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NEProcessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.NERevision;
import ao.thesis.wikianalyse.analysis.datatypes.PreprocessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.SwebleRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTMarkupSegmentedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.WTRevision;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.utils.WordNumberHashMapBuilder;
import ao.thesis.wikianalyse.utils.HDDiffUtils;
import ao.thesis.wikianalyse.utils.SwebleException;
import de.fau.cs.osr.utils.visitor.VisitingException;

public class Procession {
	
	private Procession(){}
	
	public static List<ProcessedRevision> processRevisions(List<PreprocessedRevision> revisions){
		return revisions.stream()
				.map(r -> buildProcessedRevision(r))
				.collect(Collectors.toList());
	}
	
	public static ProcessedRevision buildProcessedRevision(PreprocessedRevision revision){
		
		if(revision instanceof WTRevision){
			return buildProcessedRevision((WTRevision)revision);
		} else if (revision instanceof WTMarkupSegmentedRevision){
			return buildProcessedRevision((WTMarkupSegmentedRevision)revision);
		} else if (revision instanceof NERevision){
			return buildProcessedRevision((NERevision)revision);
		} else if (revision instanceof SwebleRevision){
			return buildProcessedRevision((SwebleRevision)revision);
		} else
			return null;
	}
	
	public static ProcessedRevision buildProcessedRevision(SwebleRevision revision){
		return new ProcessedRevision(
				revision.getID(), 
				revision.getContributorName(), 
				revision.getTimestamp(),
				revision.getPageId(),
				revision.getPageTitle(), 
				revision.getEngProcessedPage());
	}
	
	public static ProcessedRevision buildProcessedRevision(NERevision revision){
		
		NEProcessedRevision processedRevision = new NEProcessedRevision(
				revision.getID(), 
				revision.getContributorName(), 
				revision.getTimestamp(),
				revision.getPageId(),
				revision.getPageTitle(), 
				revision.getEngProcessedPage());
		
		processedRevision.setNewPersonNEs(WordNumberHashMapBuilder.getNewNENumberMaps(revision, "Person"));
		processedRevision.setNewLocationNEs(WordNumberHashMapBuilder.getNewNENumberMaps(revision, "Location"));
		processedRevision.setNewOrganizationNEs(WordNumberHashMapBuilder.getNewNENumberMaps(revision, "Organization"));
		processedRevision.setNewMiscNEs(WordNumberHashMapBuilder.getNewNENumberMaps(revision, "Misc"));
		
		processedRevision.setPersonNEs(WordNumberHashMapBuilder.getAllNENumberMaps(revision, "Person"));
		processedRevision.setLocationNEs(WordNumberHashMapBuilder.getAllNENumberMaps(revision, "Location"));
		processedRevision.setOrganizationNEs(WordNumberHashMapBuilder.getAllNENumberMaps(revision, "Organization"));
		processedRevision.setMiscNEs(WordNumberHashMapBuilder.getAllNENumberMaps(revision, "Misc"));

//		// All NEs
//		
//		if(processedRevision.getNEs().containsKey(processedRevision.getID())){
//			processedRevision.setInsertedNEs(processedRevision.getNEs().get(processedRevision.getID()));
//		} else {
//			processedRevision.setInsertedNEs(0);
//		}
		
		// Persons
		
		if(processedRevision.getPersonNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedPersons(processedRevision.getPersonNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedPersons(0);
		}
		
		if(processedRevision.getNewPersonNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedNewPersons(processedRevision.getNewPersonNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedNewPersons(0);
		}
		
		// Locations
		
		if(processedRevision.getLocationNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedLocations(processedRevision.getLocationNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedLocations(0);
		}
		
		if(processedRevision.getNewLocationNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedNewLocatons(processedRevision.getNewLocationNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedNewLocatons(0);
		}
		
		// Organisations
		
		if(processedRevision.getOrganizationNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedOrganizations(processedRevision.getOrganizationNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedOrganizations(0);
		}
		
		if(processedRevision.getNewOrganizationNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedNewOrganizations(processedRevision.getNewOrganizationNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedNewOrganizations(0);
		}
		
		// Misc
		
		if(processedRevision.getMiscNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedMiscs(processedRevision.getMiscNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedMiscs(0);
		}
		
		if(processedRevision.getNewMiscNEs().containsKey(processedRevision.getID())){
			processedRevision.setInsertedNewMiscs(processedRevision.getNewMiscNEs().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedNewMiscs(0);
		}
		
		return processedRevision;
	}
	
	public static ProcessedRevision buildProcessedRevision(WTRevision revision){
		
		ProcessedRevision processedRevision = buildProcessedRevision((SwebleRevision)revision);
		
		processedRevision.setWhiteSpaceSegWords(WordNumberHashMapBuilder.getWordNumberMap(revision.getWhitespaceRevision()));

		if(processedRevision.getWhiteSpaceSegWords().containsKey(processedRevision.getID())){
			processedRevision.setInsertedWhiteSpaceSegWords(processedRevision.getWhiteSpaceSegWords().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedWhiteSpaceSegWords(0);
		}
		return processedRevision;
	}
	
	public static ProcessedRevision buildProcessedRevision(WTMarkupSegmentedRevision revision){
		
		ProcessedRevision processedRevision = buildProcessedRevision((SwebleRevision)revision);
		
		processedRevision.setMarkupSegWords(WordNumberHashMapBuilder.getWordNumberMap((MarkupSegmentedRevision)revision));
		processedRevision.setWhiteSpaceSegWords(WordNumberHashMapBuilder.getWordNumberMap(revision.getWhitespaceRevision()));
		
		processedRevision.math = WordNumberHashMapBuilder.getWordNumberMap(((MarkupSegmentedRevision)revision).getMathFormulas(), revision.getID());

		if(processedRevision.math.containsKey(processedRevision.getID())){
			processedRevision.insertedMath = processedRevision.math.get(processedRevision.getID());
		}
		
		if(processedRevision.getMarkupSegWords().containsKey(processedRevision.getID())){
			processedRevision.setInsertedMarkupSegWords(processedRevision.getMarkupSegWords().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedMarkupSegWords(0);
		}
		
		if(processedRevision.getWhiteSpaceSegWords().containsKey(processedRevision.getID())){
			processedRevision.setInsertedWhiteSpaceSegWords(processedRevision.getWhiteSpaceSegWords().get(processedRevision.getID()));
		} else {
			processedRevision.setInsertedWhiteSpaceSegWords(0);
		}
		return processedRevision;
	}
	
	
	public static void setJudging(ProcessedRevision revision, List<ProcessedRevision> judging){
		revision.setJudging(judging.toArray(new ProcessedRevision[0]));
	}
	
	public static void setJudged(ProcessedRevision revision, List<ProcessedRevision> judged){
		revision.setJudged(judged.toArray(new ProcessedRevision[0]));
	}
	
	public static void setEditDistance(ProcessedRevision revision) throws ProcessionException{
		double distance = 0.0;
		try {
			distance = EditRating.calculateAlternativeEditDistance(
					HDDiffUtils.buildEditScript(revision.getPrev(), revision, revision.getPageTitle()));
		} catch (VisitingException | SwebleException e) {
			throw new ProcessionException(e);
		}
		revision.setEditDistance(distance);
	}

}
