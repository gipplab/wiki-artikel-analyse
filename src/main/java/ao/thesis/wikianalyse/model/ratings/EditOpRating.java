package ao.thesis.wikianalyse.model.ratings;

import ao.thesis.wikianalyse.model.Rating;


public class EditOpRating implements Rating {
	
	private long insertedWords = 0;	
	
	private long deletedWords = 0;	
	
	private long movedWords = 0;
	
	private long updatedWords = 0;
	
	
	public void setInserted(long inserts){
		this.insertedWords=inserts;
	}
	
	public void setDeleted(long deletes) {
		this.deletedWords = deletes;	
	}
	
	public void setMoved(long moves){
		this.movedWords = moves;
	}
	
	public void setUpdated(long updates){
		this.updatedWords = updates;
	}

	@Override
	public String[] buildOutputHeadlines() {
		return new String[] {
			"Inserted Words",
			"Deleted Words",
			"Moved Words",
			"Updated Words"
		};
	}

	@Override
	public String[] buildOutputLine() {
		return new String[] {
			String.valueOf(insertedWords),
			String.valueOf(deletedWords),
			String.valueOf(movedWords),
			String.valueOf(updatedWords)
		};
	}

	@Override
	public void setEditorReputation() {
		// TODO Auto-generated method stub
		
	}

}
