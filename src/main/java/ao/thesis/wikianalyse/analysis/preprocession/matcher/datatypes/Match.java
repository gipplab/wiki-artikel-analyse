package ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes;

import java.util.Objects;


public class Match implements Comparable<Match>{
	
	final private int length;
	final private int sPos;
	final private int tPos;
	private int sTotalLen = -1;
	private int tTotalLen = -1;
	final private int chunk;
	final private double relPos;
	
	/**
	 * Represents a match between two revisions.
	 * 
	 * @param length	length of the match
	 * @param sPos		starting position of the match in the source revision
	 * @param tPos		starting position of the match in the target revision
	 * @param sTotalLen	size of the source revision
	 * @param tTotalLen	size of the target revision
	 * @param chunk		number of revisions between the source and the target revision
	 */
	public Match(int length, 
			int sPos, 
			int tPos, 
			int sTotalLen, 
			int tTotalLen, 
			int chunk){
		
		validate(length, 1);
		validate(sPos, 0);
		validate(tPos, 0);
		validate(sTotalLen, 1);
		validate(tTotalLen, 1);
		validate(chunk, 0);

		this.length=length;
		this.sPos=sPos;
		this.tPos=tPos;
		this.sTotalLen=sTotalLen;
		this.tTotalLen=tTotalLen;
		this.chunk=chunk;
		
		this.relPos=calculateBlockMovement();
	}
	
	/**
	 * Validates the given value by checking if it is not null and bigger or equal the second argument.
	 * 
	 * @param value		integer to be validated
	 * @param min		smallest possible value
	 * @throws IllegalArgumentException	if value is null or smaller than min
	 */
	private void validate(int value, int min){
		if(Objects.isNull(value) || value < min){
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Calculates the relative movement of an element from s to t.
	 * 
	 * @return
	 */
	private double calculateBlockMovement(){
		double halfLength= ((double)this.getLength()/2);
		double blockMovement = (this.getSourcePos()+halfLength)/sTotalLen 
				- (this.getTargetPos()+halfLength)/tTotalLen;
		return -Math.abs(blockMovement);
	}
	
	@Override
	public int compareTo(Match other) {
		int result;
		if((result = Integer.compare(this.getChunk(), other.getChunk())) == 0){
			if((result = Integer.compare(this.getLength(), other.getLength())) == 0){
				return Double.compare(this.getRelPos(), other.getRelPos());
			} else
				return -result;
		} else
			return result;
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof Match
				&& this.length == ((Match)other).length
				&& this.sPos == ((Match)other).sPos
				&& this.tPos == ((Match)other).tPos
				&& this.chunk == ((Match)other).chunk
				&& this.relPos == ((Match)other).relPos;
	}

	public int getLength() {
		return length;
	}

	public int getSourcePos() {
		return sPos;
	}

	public int getTargetPos() {
		return tPos;
	}

	public int getChunk() {
		return chunk;
	}

	public double getRelPos() {
		return relPos;
	}
	
	@Override
	public String toString(){
		return "Match["
				+ "len: "+getLength()+"; "
				+ "sPos: "+getSourcePos()+"/"+(sTotalLen-1)+"; "
				+ "tPos: "+getTargetPos()+"/"+(tTotalLen-1)+"; "
				+ "c: "+getChunk()+"; "
				+ "relPos: "+getRelPos()
				+"]";
	}


	public int getTotalSourceLength() {
		return sTotalLen;
	}


	public int getTotalTargetLength() {
		return tTotalLen;
	}
}
