package comparer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import comparer.Edit;
import comparer.EditScript;
import comparer.Match;
import tokenizer.Token;

public class Matcher {
	
	private ArrayList<ArrayList<Token>> tokens;
	
	private ArrayList<HashMap<Token, LinkedList<Integer>>> prefixPositions; 

	public Matcher(ArrayList<ArrayList<Token>> tokens)
	{
		this.tokens=tokens;
	}
	
	//---------------------------------------------
	
	private void setup(){
		
		prefixPositions = new ArrayList<HashMap<Token, LinkedList<Integer>>>(tokens.size());
		
		for(ArrayList<Token> revision : tokens)
			prefixPositions.add(buildPrefixHashMap(revision));
	}
	
	public HashMap<Integer, ArrayList<EditScript>> compareAndGetEditScripts()
	{
		if(tokens.isEmpty())
		{
			System.err.println("Matcher: empty token list");
			return null;
		}
		
		setup();
		
		HashMap<Integer, ArrayList<EditScript>> editScripts = new HashMap<Integer, ArrayList<EditScript>>(tokens.size());
		
		for(int targetNumber=1; targetNumber<tokens.size(); targetNumber++)
		{
			System.out.println("set edit scripts: "+targetNumber);
			
			editScripts.put(targetNumber, compareToPreviousRevisions(targetNumber));
		}
		return editScripts;
	}
	
	private ArrayList<EditScript> compareToPreviousRevisions(int targetNumber) 
	{		
		ArrayList<EditScript> edits;
		
		//TODO restrict matches to max revisions
		
		edits = new ArrayList<EditScript>(targetNumber);

		ArrayList<Match> allMatches=new ArrayList<Match>();
		
		LinkedList<Match> versionMatches;
		
		for(int sourceNumber = targetNumber-1; 0 <= sourceNumber; sourceNumber--)
		{
			edits.add(new EditScript(sourceNumber, targetNumber));
			versionMatches = matchTokens(sourceNumber, targetNumber);
			allMatches.addAll(versionMatches);
			
			//System.out.println("matched "+targetNumber+" to "+sourceNumber);

		}
		
		Token target[] = getTokenArray(tokens.get(targetNumber));
		
		if(!allMatches.isEmpty())
		{
			PriorityQueue<Match> queue = buildQueue(allMatches.size());
			
			queue.addAll(allMatches);
			
			while (!queue.isEmpty())
			{	
				Match currentMatch = queue.peek();

				boolean completeMatch=true;
				
				for(int matchlength = 0; matchlength < currentMatch.length; matchlength++)
					if(target[currentMatch.targetPosition+matchlength]==null)
					{
						completeMatch=false;
						break;
					}
				
				if(completeMatch) //Add Move To Edit Script
				{
					Edit edit = new Edit(currentMatch.sourcePosition, currentMatch.targetPosition, currentMatch.length);
					EditScript script;
					
					if((script = edits.get(currentMatch.sourceNumber))!=null)
						script.add(edit);
					else 
					{
						script = new EditScript(currentMatch.sourceNumber, targetNumber);
						script.add(edit);
						edits.set(currentMatch.sourceNumber, script);
					}
					
					for(int matchlength = 0; matchlength < currentMatch.length; matchlength++)
					{
						target[currentMatch.targetPosition+matchlength]=null;
					}
				}
				queue.poll();
			}
		}
		
		//Deletes

		Token[] source;
		for(int sourceNumber = 0; sourceNumber < targetNumber; sourceNumber++)
		{
			source = getTokenArray(tokens.get(sourceNumber));
			
			for(int matchcounter=0; matchcounter< allMatches.size(); matchcounter++)
			{
				Match match = allMatches.get(matchcounter);
				
				if(match.sourceNumber == sourceNumber)
				{
					for(int matchlength=0; matchlength < match.length; matchlength++)
							source[match.sourcePosition + matchlength]=null;
					allMatches.remove(matchcounter);
					matchcounter--;
				}
			}
			
			for(int startPosition=0; startPosition < source.length; startPosition++)
			{
				int length=0;
				int current=startPosition;
				
				while(current<source.length && source[current]!=null)
				{
					length++;
					current++;
					if((current<source.length && source[current]==null)
						|| current==source.length)
					{
						Edit edit = new Edit(startPosition, -1, length);
						EditScript script;
						
						if((script = edits.get(sourceNumber))!=null)
							script.add(edit);
						else 
						{
							script = new EditScript(sourceNumber, targetNumber);
							script.add(edit);
							edits.set(sourceNumber, script);
						}

						startPosition=current-1;
						continue;
					}
				}
			}		
		}
	
		//Inserts
		
		for(int startPosition=0; startPosition<target.length; startPosition++){
			int length=0;
			int current=startPosition;
			while(current<target.length && target[current]!=null)
			{
				length++;
				current++;
				if((current<target.length && target[current]==null)
					|| current==target.length)
				{
					Edit edit = new Edit(-1, startPosition, length);
					EditScript script;
					
					if((script = edits.get(targetNumber-1))!=null)
						script.add(edit);
					else 
					{
						script = new EditScript(targetNumber-1, targetNumber);
						script.add(edit);
						edits.set(targetNumber-1, script);
					}

					startPosition=current-1;
					continue;
				}
			}
		}
		return edits;
	}

	/**
	 * Sources:
	 * "WikiTrust"; Adler
	 * "A linear time, constant space differencing algorithm"; Randal C. Burns, Darrell D. E. Long
	 */
	private LinkedList<Match> matchTokens(int sourceNumber,int targetNumber)
	{	
		LinkedList<Match> matches = new LinkedList<Match>();
		
		Token[] target = getTokenArray(tokens.get(targetNumber));
		
		HashMap<Token, LinkedList<Integer>> sourceTokenPositions = prefixPositions.get(sourceNumber);
		
		int sourceSize = tokens.get(sourceNumber).size();
		
		int length=0;
		
		Token currenttoken;
		
		for(int positionInTarget=0;positionInTarget < target.length; positionInTarget++){
			currenttoken = target[positionInTarget];
			
			if(sourceTokenPositions.containsKey(currenttoken))
				for(Integer positionInSource :prefixPositions.get(sourceNumber).get(currenttoken))
				{
					length = 1;
					while(positionInSource+length < sourceSize 
							&& positionInTarget+length < target.length 
							&& sourceTokenPositions.containsKey(target[positionInTarget+length]) 
							&& sourceTokenPositions.get(target[positionInTarget+length]).contains(positionInSource+length))
						
						length++;
					
					Match match = new Match(
							positionInSource, 
							positionInTarget, 
							length, 
							sourceSize, 
							target.length, 
							sourceNumber);
					
					matches.add(match);
				}
		}
		return matches;
	}
	
	private PriorityQueue<Match> buildQueue(int size)
	{	
		PriorityQueue<Match> queue = new PriorityQueue<Match>(size, 
			(new Comparator<Match>(){

				public int compare(Match m1, Match m2) 
				{
					if(m1.sourceNumber > m2.sourceNumber)
						return -1;
					if(m1.sourceNumber == m2.sourceNumber)
					{
						if(m1.length > m2.length)
							return -1;
						if (m2.length == m1.length)
						{
							if(m1.relPos < m2.relPos)
								return -1;
							if(m1.relPos == m2.relPos)
								return 0;
						}
					}
					return 1;
				}
			})
		);
		return queue;		
	}

	/**
	 * Map for the positions of all tokens
	 */
	private HashMap<Token, LinkedList<Integer>> buildPrefixHashMap(ArrayList<Token> tokens)
	{
		HashMap<Token, LinkedList<Integer>> positionsOfStringPrefixes = new HashMap<Token, LinkedList<Integer>>();
		
		int position=0;
		LinkedList<Integer> positions;
		for(Token token : tokens)
		{
			if(positionsOfStringPrefixes.containsKey(token))
				positionsOfStringPrefixes.get(token).add(position);
			else
			{
				positions = new LinkedList<Integer>();
				positions.add(position);
				positionsOfStringPrefixes.put(token, positions);
			}
			position++;
		}
		return positionsOfStringPrefixes;
	}
	
	
	private Token[] getTokenArray(ArrayList<Token> tokenlist)
	{
		Token[] target = new Token[tokenlist.size()];
		tokenlist.toArray(target);
		
		return target;
	}
}
