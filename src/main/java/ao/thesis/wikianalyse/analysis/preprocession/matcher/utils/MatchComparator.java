package ao.thesis.wikianalyse.analysis.preprocession.matcher.utils;

import java.util.Comparator;

import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.FormulaMatch;
import ao.thesis.wikianalyse.analysis.preprocession.matcher.datatypes.Match;

public class MatchComparator {
	
	public static Comparator<Match> getDefaultComparator(){
		return new Comparator<Match>(){
			public int compare(Match m1, Match m2){
				return m1.compareTo(m2);
			}
		};
	}
	
	public static Comparator<FormulaMatch> getDefaultMathComparator(){
		return new Comparator<FormulaMatch>(){
			public int compare(FormulaMatch m1, FormulaMatch m2){
				return m1.compareTo(m2);
			}
		};
	}

}
