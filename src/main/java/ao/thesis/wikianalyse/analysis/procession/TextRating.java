package ao.thesis.wikianalyse.analysis.procession;

import ao.thesis.wikianalyse.analysis.datatypes.NEProcessedRevision;
import ao.thesis.wikianalyse.analysis.datatypes.ProcessedRevision;

public class TextRating {
	
	private TextRating(){}
	
	/**
	 * Ten Revisions
	 * 
	 * (0; +10) Used for Reputation Update
	 * 
	 * @param revision
	 * @param judging
	 * @return
	 */
	public static double calculateNumberOfSurvivingWhitespaceWords(ProcessedRevision revision, ProcessedRevision[] judging){
		
		if(judging.length!=0){
			int insertedWords = revision.getInsertedWhiteSpaceSegWords();
			if(insertedWords != 0){
				int totalSurvivedWords = 0;
				for(int i=0; i<judging.length;i++){
					if(judging[i].getWhiteSpaceSegWords().containsKey(revision.getID())){
						totalSurvivedWords += judging[i].getWhiteSpaceSegWords().get(revision.getID());
					}
				}
				return totalSurvivedWords;
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}
	
	/**
	 * Ten Revisions
	 * 
	 * (0; +10) Used for Reputation Update
	 * 
	 * @param revision
	 * @param judging
	 * @return
	 */
	public static double calculateNumberOfSurvivingMarkupSegWords(ProcessedRevision revision, ProcessedRevision[] judging){
		
		if(judging.length!=0){
			int insertedWords = revision.getInsertedMarkupSegWords();
			if(insertedWords != 0){
				int totalSurvivedWords = 0;
				for(int i=0; i<judging.length;i++){
					if(judging[i].getMarkupSegWords().containsKey(revision.getID())){
						totalSurvivedWords += judging[i].getMarkupSegWords().get(revision.getID());
					}
				}
				return totalSurvivedWords;
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}
	
	/**
	 * Text Decay Quality
	 * 
	 * (0;1) Used for Precision Recall
	 * 
	 * @param revision
	 * @param judging
	 * @return
	 */
	public static double calculateWhitespaceTextDecayQuality(ProcessedRevision revision, ProcessedRevision[] judging){
		
		if(judging.length!=0){
			int insertedWords = revision.getInsertedWhiteSpaceSegWords();
			if(insertedWords != 0){
				int totalSurvivedWords = insertedWords;
				for(int i=0; i<judging.length;i++){
					if(judging[i].getWhiteSpaceSegWords().containsKey(revision.getID())){
						totalSurvivedWords += judging[i].getWhiteSpaceSegWords().get(revision.getID());
					}
				}
				return calculateNewtonMethod(insertedWords, totalSurvivedWords, judging.length, 5);
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}
	
	
	/**
	 * Text Decay Quality
	 * 
	 * (0;1) Used for Precision Recall
	 * 
	 * @param revision
	 * @param judging
	 * @return
	 */
	public static double calculateMarkupSegTextDecayQuality(ProcessedRevision revision, ProcessedRevision[] judging){
		
		if(judging.length!=0){
			int insertedWords = revision.getInsertedMarkupSegWords();
			if(insertedWords != 0){
				int totalSurvivedWords = insertedWords;
				for(int i=0; i<judging.length;i++){
					if(judging[i].getMarkupSegWords().containsKey(revision.getID())){
						totalSurvivedWords += judging[i].getMarkupSegWords().get(revision.getID());
					}
				}
				return calculateNewtonMethod(insertedWords, totalSurvivedWords, judging.length, 5);
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}
	
	
	private static double calculateNewtonMethod(int insertedWords, int totalSurvivedWords, int numberOfJudgingRevisions, int limit){
		double result = 0.0;
		for(int j = 0 ; j < limit ; j++){
			
			double factor = Math.pow(result, (double)numberOfJudgingRevisions + 1) - 1;
			double function = (factor * insertedWords) + ((1 - result) * totalSurvivedWords);
			
			double factor_1 = (numberOfJudgingRevisions + 1) * Math.pow(result, (double)numberOfJudgingRevisions);
			double function_1 = (factor_1 * insertedWords) - totalSurvivedWords;
			
			result = result - (function / function_1);
		}
		return result;
	}

	public static double calculateNETextDecayQuality(ProcessedRevision revision, ProcessedRevision[] judging, String entity) {
		
		NEProcessedRevision nerevision = (NEProcessedRevision) revision;
		
		if(judging.length!=0){
			
			int insertedWords = 0;
			if(entity.equals("Person")){
				insertedWords = nerevision.getInsertedPersons();
			} else if(entity.equals("Location")){
				insertedWords = nerevision.getInsertedLocations();
			} else if(entity.equals("Organization")){
				insertedWords = nerevision.getInsertedOrganizations();
			} else if(entity.equals("Misc")){
				insertedWords = nerevision.getInsertedMiscs();
			}
		
			if(insertedWords != 0){
				int totalSurvivedWords = insertedWords;
				for(int i=0; i<judging.length;i++){
						
					if(entity.equals("Person")){
						if(((NEProcessedRevision)judging[i]).getPersonNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getPersonNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Location")){
						if(((NEProcessedRevision)judging[i]).getLocationNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getLocationNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Organization")){
						if(((NEProcessedRevision)judging[i]).getOrganizationNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getOrganizationNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Misc")){
						if(((NEProcessedRevision)judging[i]).getMiscNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getMiscNEs().get(nerevision.getID());
						}
					}

				}
				return calculateNewtonMethod(insertedWords, totalSurvivedWords, judging.length, 5);
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}
	
	public static double calculateNewNETextDecayQuality(ProcessedRevision revision, ProcessedRevision[] judging, String entity) {
		
		NEProcessedRevision nerevision = (NEProcessedRevision) revision;
		
		if(judging.length!=0){
			
			int insertedWords = 0;
			if(entity.equals("Person")){
				insertedWords = nerevision.getInsertedNewPersons();
			} else if(entity.equals("Location")){
				insertedWords = nerevision.getInsertedNewLocatons();
			} else if(entity.equals("Organization")){
				insertedWords = nerevision.getInsertedNewOrganizations();
			} else if(entity.equals("Misc")){
				insertedWords = nerevision.getInsertedNewMiscs();
			}
		
			if(insertedWords != 0){
				int totalSurvivedWords = insertedWords;
				for(int i=0; i<judging.length;i++){
						
					if(entity.equals("Person")){
						if(((NEProcessedRevision)judging[i]).getNewPersonNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getNewPersonNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Location")){
						if(((NEProcessedRevision)judging[i]).getNewLocationNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getNewLocationNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Organization")){
						if(((NEProcessedRevision)judging[i]).getNewOrganizationNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getNewOrganizationNEs().get(nerevision.getID());
						}
					} else if(entity.equals("Misc")){
						if(((NEProcessedRevision)judging[i]).getNewMiscNEs().containsKey(revision.getID())){
							totalSurvivedWords += ((NEProcessedRevision)judging[i]).getNewMiscNEs().get(nerevision.getID());
						}
					}

				}
				return calculateNewtonMethod(insertedWords, totalSurvivedWords, judging.length, 5);
			} else {
				return 0.0;
			}
		} else {
			return 0.0;
		}
	}

}
