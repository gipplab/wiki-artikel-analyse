package ao.thesis.wikianalyse.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import ao.thesis.wikianalyse.Main;
import ao.thesis.wikianalyse.io.output.DBOutputWriter;
import ao.thesis.wikianalyse.io.output.WikiTrustDataOutputWriter;
import au.com.bytecode.opencsv.CSVWriter;

public class ReputationUpdater {
	
	public static final Logger LOGGER = Logger.getLogger(ReputationUpdater.class);
	
	private final ReputationDatabase reputationDB;
	
	private Set pages = new HashSet<String>();
	
	public ReputationUpdater(ReputationDatabase reputationDB){
		this.reputationDB = reputationDB;
	}
	
	public static void main(String[] args){
		ReputationDatabase repDB = new ReputationDatabase();
		ReputationUpdater reader = new ReputationUpdater(repDB);
		
		reader.readTimelineAndUpdateReputation();
		
		DBOutputWriter writer = new DBOutputWriter();
		writer.write(repDB);
	}
	
	public void readTimelineAndUpdateReputation(){
		
		try (
			FileReader reader = new FileReader(new File(Main.DEF_INPUT_DIR+"/SortedDreiTimeline.csv"));
			BufferedReader br = new BufferedReader(reader);
			FileWriter fileA = new FileWriter(Main.DEF_OUTPUT_DIR + "/PrecisionRecallData_a.csv", true);
			CSVWriter writerA = new CSVWriter(fileA);
			FileWriter fileB = new FileWriter(Main.DEF_OUTPUT_DIR + "/PrecisionRecallData_b.csv", true);
			CSVWriter writerB = new CSVWriter(fileB);
			FileWriter fileC = new FileWriter(Main.DEF_OUTPUT_DIR + "/PrecisionRecallData_c.csv", true);
			CSVWriter writerC = new CSVWriter(fileC);
		){
			
			double SLp_e = 0.0;
			double Lp_e = 0.0;
			double Sp_e = 0.0;
			
			double SLp_t = 0.0;
			double Lp_t = 0.0;
			double Sp_t= 0.0;
			
			double SLp_m = 0.0;
			double Lp_m = 0.0;
			double Sp_m = 0.0;
			
			int lowR_count = 0;
			int lowE_count = 0;
			int lowT_count = 0;
			int lowM_count = 0;
			
			int lowE_count_rep = 0;
			int lowT_count_rep = 0;
			int lowM_count_rep = 0;
			
			String textline;
			
			int all = 0;
			
			while((textline = br.readLine())!=null){
				
				String[] textinput = textline.split("\",\"");
				
				//skip empty lines
				if(textinput.length < 9){
					continue;
				}
				
				String judgingContributor = textinput[WikiTrustDataOutputWriter.TL_CON].replaceAll("\"", "");
				LOGGER.info("Read Contributor: "+judgingContributor+" "+textinput[WikiTrustDataOutputWriter.TL_ID]);
				
				reputationDB.addContributor(judgingContributor);
				
				double reputation = reputationDB.getReputation(judgingContributor);
				
				if(!judgingContributor.equals("Anonymous")){
					
					String page = textinput[WikiTrustDataOutputWriter.TL_PAGE].replaceAll("\"", "").replaceAll(",", "");
					
					pages.add(page);
					
					String distance = textinput[WikiTrustDataOutputWriter.TL_ED].replaceAll("\"", "").replaceAll(",", "");
					String avgEditLongevity = textinput[WikiTrustDataOutputWriter.TL_EL].replaceAll("\"", "").replaceAll(",", "");
					
					String insertedtext = textinput[WikiTrustDataOutputWriter.TL_IW].replaceAll("\"", "");
					String textDecayQuality = textinput[WikiTrustDataOutputWriter.TL_TDQ].replaceAll("\"", "");
					String insertedmarkup = textinput[WikiTrustDataOutputWriter.TL_MIW].replaceAll("\"", "");
					String markuptextDecayQuality = textinput[WikiTrustDataOutputWriter.TL_MTDQ].replaceAll("\"", "");
					
					int hasLowReputation = 0;
					int hasLowEditLongevity = 0;
					int hasLowTextDecayQuality = 0;
					int hasLowMarkupTextDecayQuality = 0;
					
					if(!DateTime.parse(textinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", "")).isAfter(new DateTime(2005, 10, 31, 23, 59))
//							&& !DateTime.parse(textinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", "")).isBefore(new DateTime(2004, 12, 31, 23, 59))
							){
						
						all++;
					
						if(Math.log(reputation+1) <= ((double)Math.log(ReputationDatabase.MAX_REPUTATION+1)/(double)5)){
							hasLowReputation = 1;
							lowR_count++;
						}
						if(Double.valueOf(avgEditLongevity) <= -0.8){
							hasLowEditLongevity = 1;
						}
						if(Double.valueOf(textDecayQuality) <= 0.2){
							hasLowTextDecayQuality = 1;
						}
						if(Double.valueOf(markuptextDecayQuality) <= 0.2){
							hasLowMarkupTextDecayQuality = 1;
						}
						
						SLp_e += hasLowEditLongevity*hasLowReputation*Double.valueOf(distance);
						SLp_t += hasLowTextDecayQuality*hasLowReputation*Double.valueOf(insertedtext);
						SLp_m += hasLowMarkupTextDecayQuality*hasLowReputation*Double.valueOf(insertedmarkup);
						
						Lp_e += hasLowReputation*Double.valueOf(distance);
						Lp_t += hasLowReputation*Double.valueOf(insertedtext);
						Lp_m += hasLowReputation*Double.valueOf(insertedmarkup);
						
						Sp_e += hasLowEditLongevity*Double.valueOf(distance);
						Sp_t += hasLowTextDecayQuality*Double.valueOf(insertedtext);
						Sp_m += hasLowMarkupTextDecayQuality*Double.valueOf(insertedmarkup);
						
						if(hasLowEditLongevity*Double.valueOf(distance) > 0){
							lowE_count++;
							if(hasLowReputation == 1){
									lowE_count_rep++;
							}
						}
						if(hasLowTextDecayQuality*Double.valueOf(insertedtext) > 0){
							lowT_count++;
							if(hasLowReputation == 1){
									lowT_count_rep++;
							}
						}
						if(hasLowMarkupTextDecayQuality*Double.valueOf(insertedmarkup) > 0){
							lowM_count++;
							if(hasLowReputation == 1){
									lowM_count_rep++;
							}
						}
						
						writerA.writeNext(new String[]{judgingContributor, page, String.valueOf(reputation), avgEditLongevity, textDecayQuality, markuptextDecayQuality});
						writerB.writeNext(new String[]{judgingContributor, page, String.valueOf(hasLowReputation), distance, String.valueOf(hasLowEditLongevity), insertedtext, String.valueOf(hasLowTextDecayQuality)});
						writerC.writeNext(new String[]{judgingContributor, page, String.valueOf(hasLowReputation), distance, String.valueOf(hasLowEditLongevity), insertedmarkup, String.valueOf(hasLowMarkupTextDecayQuality)});
					}
//				}
				
				int inputIndex = WikiTrustDataOutputWriter.TL_MTDQ + 1;
				int textvalues = 4;
				
				if(inputIndex < textinput.length){
					
					for(int i = 0; (((i * textvalues) + (textvalues-1)) + inputIndex) < textinput.length; i++){
	
						String judgedContributorTexts = textinput[inputIndex + (i * textvalues)].replaceAll("\"", "");

						if(inputIndex + ((3 * textvalues))  >= textinput.length - (i * textvalues)){
							double editUpdate = (Double.valueOf(textinput[inputIndex + ((i * textvalues) + 1)].replaceAll("\"", "")))*(Math.log((reputation+1)));
							reputationDB.updateReputation(judgedContributorTexts, editUpdate);
						}
						
						double textUpdate = Double.valueOf(textinput[inputIndex+ ((i * textvalues) + 2)].replaceAll("\"", ""))*(Math.log((reputation+1)));
//						double textUpdate = Double.valueOf(textinput[inputIndex+ ((i * 4) + 3)].replaceAll("\"", ""))*(Math.log((reputation+1)));
						
						reputationDB.updateReputation(judgedContributorTexts, textUpdate);
					}
					
				}
				reputationDB.correctReputation();
				}
			}
			
			double prec_e = SLp_e/Lp_e;
			double prec_t = SLp_t/Lp_t;
			double prec_m = SLp_m/Lp_m;
			
			double rec_e = SLp_e/Sp_e;
			double rec_t = SLp_t/Sp_t;
			double rec_m = SLp_m/Sp_m;
			
			LOGGER.info("prec_e:"+prec_e*100+"% - 30,57% ; prec_t:"+prec_t*100+"% - 7.64% ; prec_m:"+prec_m*100+"% - 7.64% ;"); 
			LOGGER.info("rec_e:"+rec_e*100+"% - 71,29% ; rec_t:"+rec_t*100+"% - 84,09% ; rec_m:"+rec_m*100+"% - 84,09% ;");
			LOGGER.info("low_r:"+lowR_count+" ; low_e:"+lowE_count+" ; low_t:"+lowT_count+" ; low_m:"+lowM_count+" ;");
			LOGGER.info("low_e_rep:"+lowE_count_rep+" ; low_t_rep:"+lowT_count_rep+" ; low_m_rep:"+lowM_count_rep+" ;");
			
			LOGGER.info("Number of Pages: "+pages.size());
			LOGGER.info("Number of Revisions: "+all);
			
			br.close();
			reader.close();
			fileA.close();
			writerA.close();
			fileB.close();
			writerB.close();
			fileC.close();
			writerC.close();
			
		} catch (IOException e) {
			LOGGER.error("Timeline could not be read.",e);
		}
	}
}
