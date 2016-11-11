package ao.thesis.wikianalyse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ao.thesis.wikianalyse.matcher.Match;
import ao.thesis.wikianalyse.matcher.Matcher;


public class BestMatchTest {
	
	 @Test
	    public void compareMovedStrings() throws Exception{
		 
		 	Matcher matcher = new Matcher(50,1);
		 	
		 	List<Object> revision1 = new ArrayList<Object>();
		 	List<Object> revision2 = new ArrayList<Object>();
		 	
		 	String[] s1 = "Do testing. Test these matches. Lorem Ipsum.".split(" ");
		 	String[] s2 = "Test these matches. Do testing. Lorem Ipsum.".split(" ");
		 	
		 	for(String s : s1)
		 		revision1.add(s);
		 	
		 	for(String s : s2)
		 		revision2.add(s);
		 	
		 	List<List<Object>> revisions = new ArrayList<List<Object>>();
		 	
		 	revisions.add(revision1);
		 	revisions.add(revision2);
		 	
		 	List<List<Match>> match = matcher.match(revisions);
	
	        Assert.assertEquals(3, match.get(0).size());
	    }
	 
	 @Test
	    public void compareUpdate() throws Exception{
		 
		 	Matcher matcher = new Matcher(50,1);
		 	
		 	List<Object> revision1 = new ArrayList<Object>();
		 	List<Object> revision2 = new ArrayList<Object>();
		 	
		 	revision1.add("Test");
		 	revision1.add("original");
		 	
		 	revision2.add("Test");
		 	revision2.add("new");
		 	
		 	List<List<Object>> revisions = new ArrayList<List<Object>>();
		 	
		 	revisions.add(revision1);
		 	revisions.add(revision2);
		 	
		 	List<List<Match>> bestmatches = matcher.match(revisions);
	
	        Assert.assertEquals(1, bestmatches.get(0).size());
	    }
	 
	 @Test
	    public void compareThreeRevisions() throws Exception{
		 
		 	Matcher matcher = new Matcher(50,1);
		 	
		 	List<Object> revision1 = new ArrayList<Object>();
		 	List<Object> revision2 = new ArrayList<Object>();
		 	List<Object> revision3 = new ArrayList<Object>();
		 	
		 	String[] s1 = "Do testing. Do something new.".split(" ");
		 	String[] s2 = "Lorem Ipsum. Do testing.".split(" ");
		 	String[] s3 = "Do something new. Lorem Ipsum. Do testing.".split(" ");
		 	
		 	for(String s : s1)
		 		revision1.add(s);
		 	
		 	for(String s : s2)
		 		revision2.add(s);
		 	
		 	for(String s : s3)
		 		revision3.add(s);
		 	
		 	List<List<Object>> revisions = new ArrayList<List<Object>>();
		 	
		 	revisions.add(revision1);
		 	revisions.add(revision2);
		 	revisions.add(revision3);
		 	
		 	List<List<Match>> match = matcher.match(revisions);
		 	
		 	Match dotesting = new Match(0,2,2,5,4,0);
	        Match ipsumloremdosometesting = new Match(0,3,4,4,7,1);
	        Match do_single = new Match(2,0,1,4,7,1);
	        Match somethingnew = new Match(3,1,2,5,7,0);
	
		 	Assert.assertEquals(2, match.size());
	        Assert.assertEquals(1, match.get(0).size());
	        Assert.assertEquals(3, match.get(1).size());

	        Assert.assertTrue(match.get(0).get(0).equals(dotesting));
	        Assert.assertTrue(match.get(1).get(0).equals(ipsumloremdosometesting));
	        Assert.assertTrue(match.get(1).get(1).equals(do_single));
	        Assert.assertTrue(match.get(1).get(2).equals(somethingnew));
	    }
}
