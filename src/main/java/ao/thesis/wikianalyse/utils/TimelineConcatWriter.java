package ao.thesis.wikianalyse.utils;

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
import ao.thesis.wikianalyse.io.output.WikiTrustDataOutputWriter;
import au.com.bytecode.opencsv.CSVWriter;

public class TimelineConcatWriter {
	
	public static final Logger LOGGER = Logger.getLogger(TimelineConcatWriter.class);
	
	private Set<String> readPages = new HashSet<String>();
	private static Set<String> leftOutPages = new HashSet<String>();
	
	public TimelineConcatWriter(){}
	
	public static void main(String[] args){
		TimelineConcatWriter writer = new TimelineConcatWriter();
		
		writer.concat();
		
		leftOutPages.forEach(p -> System.out.println(p));
	}
	
	public void concat(){
		
		try (
			FileReader textrating_reader = new FileReader(new File(Main.DEF_INPUT_DIR+"/Timeline_textrating_sorted.csv"));
			BufferedReader tBr = new BufferedReader(textrating_reader);
			FileReader editrating_reader = new FileReader(new File(Main.DEF_INPUT_DIR+"/Timeline_editrating_sorted.csv"));
			BufferedReader eBr = new BufferedReader(editrating_reader);
			FileWriter fileA = new FileWriter(Main.DEF_OUTPUT_DIR + "/Concated_Timeline.csv", true);
			CSVWriter writerA = new CSVWriter(fileA);
		){
			
			String[] output;
			
			String textline;
			String editline;
			
			boolean keepEditLine = false;
			String saveEditLine = "";
			
			while((textline = tBr.readLine())!=null){
				
				String[] textinput = textline.split("\",\"");
				output = new String[textinput.length];
				
				String[] editinput = null;
				do{
					if(keepEditLine){
						editline = saveEditLine;
						editinput = editline.split("\"(,)+\"");
						keepEditLine = false;
					} else {
						if((editline = eBr.readLine()) != null){
							editinput = editline.split("\"(,)+\"");
							saveEditLine = editline;
						}
						if((new DateTime(editinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", ""))).isAfter(new DateTime(textinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", "")))){
							keepEditLine = true;
							break;
						}
					}
				} while (!editinput[WikiTrustDataOutputWriter.TL_ID].equals(textinput[WikiTrustDataOutputWriter.TL_ID]));
				
				if(keepEditLine){
					continue;
				}
				
				String page = textinput[WikiTrustDataOutputWriter.TL_PAGE].replaceAll("\"", "");
				
				if(leftOutPages.contains(page)){
					continue;
				}
				
				String contributor = textinput[WikiTrustDataOutputWriter.TL_CON].replaceAll("\"", "");
				
				int inputIndex = WikiTrustDataOutputWriter.TL_MTDQ + 1;
				int textvalues = 4;
				
				if(inputIndex < textinput.length){
					if(readPages.contains(page)){
						
						boolean skip = false;
						for(int i = 0; (((i * textvalues) + (textvalues-1)) + inputIndex) < textinput.length; i++){
		
							String textcontributor = textinput[inputIndex + (i * textvalues)].replaceAll("\"", "");
							
							String editcontributor = "";
							if(editinput.length - 1 > 6 + (i * 2)){
								editcontributor = editinput[6 + (i * 2)].replaceAll("\"", "");
							} else{
								leftOutPages.add(page);
								skip = true;
								break;
							}
							
							if(editcontributor.equals(textcontributor)){
								output[inputIndex + (i * textvalues)] = textcontributor;
								
								String textupdate = textinput[inputIndex + (i * textvalues) + 2].replaceAll("\"", "");
								String markuptextupdate = textinput[inputIndex + (i * textvalues) + 3].replaceAll("\"", "");
								
								output[inputIndex + (i * textvalues) + 2] = textupdate;
								output[inputIndex + (i * textvalues) + 3] = markuptextupdate;
								
								if(editinput.length - 1 > 6 + (i * 2)){
									
									String editupdate = editinput[6 + (i * 2) + 1].replaceAll("\"", "").replaceAll(",", "");
									output[inputIndex + (i * textvalues) + 1] = editupdate;
									
								}
							} else{
								leftOutPages.add(page);
								skip = true;
								break;
							}
						}
						if(skip){
							continue;
						}
						
					} else if (!readPages.contains(page)){
						leftOutPages.add(page);
						continue;
					}
				} else {
					readPages.add(page);
				}
				
				output[WikiTrustDataOutputWriter.TL_DATE] = textinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", "");
				output[WikiTrustDataOutputWriter.TL_ID] = textinput[WikiTrustDataOutputWriter.TL_ID].replaceAll("\"", "");
				output[WikiTrustDataOutputWriter.TL_PAGE] = page;
				output[WikiTrustDataOutputWriter.TL_CON] = contributor;
				
				output[WikiTrustDataOutputWriter.TL_ED] = editinput[WikiTrustDataOutputWriter.TL_ED].replaceAll("\"", "").replaceAll(",", "");
				output[WikiTrustDataOutputWriter.TL_EL] = editinput[WikiTrustDataOutputWriter.TL_EL].replaceAll("\"", "").replaceAll(",", "");
				output[WikiTrustDataOutputWriter.TL_IW] = textinput[WikiTrustDataOutputWriter.TL_IW].replaceAll("\"", "");
				output[WikiTrustDataOutputWriter.TL_TDQ] = textinput[WikiTrustDataOutputWriter.TL_TDQ].replaceAll("\"", "");
				output[WikiTrustDataOutputWriter.TL_MIW] = textinput[WikiTrustDataOutputWriter.TL_MIW].replaceAll("\"", "");
				output[WikiTrustDataOutputWriter.TL_MTDQ] = textinput[WikiTrustDataOutputWriter.TL_MTDQ].replaceAll("\"", "");				

				LOGGER.info(textinput[WikiTrustDataOutputWriter.TL_DATE].replaceAll("\"", "")+" "+textinput[WikiTrustDataOutputWriter.TL_ID].replaceAll("\"", "")+" "+contributor);
				
				writerA.writeNext(output);
			}
			
			tBr.close();
			textrating_reader.close();
			eBr.close();
			editrating_reader.close();
			
			fileA.close();
			writerA.close();
		} catch (IOException e) {
			LOGGER.error("Timeline could not be read.",e);
		}
	}

}
