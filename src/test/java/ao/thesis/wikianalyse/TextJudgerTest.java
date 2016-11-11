package ao.thesis.wikianalyse;

import org.junit.Assert;
import org.junit.Test;

import ao.thesis.wikianalyse.judger.TextJudger;

public class TextJudgerTest{

	 @Test
	    public void testNewtonMethod() throws Exception
	    {
		 	TextJudger judger = new TextJudger();
		 	
		 	double allsurvived = judger.calculateNewtonMethod(0.0, 3, 9, 2, 5);
		 	
		 	double nothingsurvived = judger.calculateNewtonMethod(0.0, 3, 3, 2, 5);
		 	
		 	double halfsurvived = judger.calculateNewtonMethod(0.0, 4, 8, 2, 100);
		 	
		 	double moreThanInserted = judger.calculateNewtonMethod(0.0, 3, 12, 2, 100);
		 	
		 	//double survived = judger.calculateNewtonMethod(0.0, 30, 40, 10, 20);
		 	
	        Assert.assertTrue(allsurvived > 0.9);
	        Assert.assertTrue(nothingsurvived == 0.0);
	        Assert.assertTrue(moreThanInserted == 1.0);
	        Assert.assertTrue(halfsurvived > 0.5);
	    }
}
