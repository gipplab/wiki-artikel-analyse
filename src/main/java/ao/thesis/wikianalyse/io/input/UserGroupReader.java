package ao.thesis.wikianalyse.io.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;


public class UserGroupReader {
	
	private final static Logger LOGGER = Logger.getLogger(UserGroupReader.class);
	
	private static final String USERGROUPFILE = System.getProperty("user.dir")+"/resources/enwiki-20170801-user_groups.sql";
	
	private static final String INSERT_QUERY = "INSERT INTO `user_groups` VALUES ";
	
	private static Map<BigInteger, String> usergroups = new HashMap<BigInteger, String>();
	
	FileWriter file = null;
	CSVWriter writer = null;
	
	
	public String getUsergroup(BigInteger id){
		if(usergroups.containsKey(id))
			return usergroups.get(id);
		else return "/";
	}
	
	public void print(BigInteger id, String username){
		if(usergroups.containsKey(id)){
			String[] line = new String[]{username, usergroups.get(id)}; 
			writer.writeNext(line);
		} else {
			String[] line = new String[]{username, "none"}; 
			writer.writeNext(line);
		}
	}

//	public static void main(String[] args) throws IOException{
//		
//		UserGroupReader reader = new UserGroupReader();
//		
//		for(BigInteger id : usergroups.keySet()){
//			writer.writeNext(new String[]{String.valueOf(id), usergroups.get(id)});
//		}
//	}
	
	
	public UserGroupReader() throws IOException {
		
		file = new FileWriter(System.getProperty("user.dir")+"/resources/usergroups.csv");
		writer = new CSVWriter(file);
		
		try {
			read(new File(USERGROUPFILE));
		} catch (IOException e) {
			LOGGER.error("Usergroups could not be read.", e);
		}
	}

	public void read(File file) throws IOException {
		
		String text = "";
		BufferedReader br = null;
		
	    try{
	    	br = new BufferedReader(new FileReader(file));
	    	String line;
	        while((line = br.readLine()) != null){
	        	text += line;
	        }
	    } finally {
	    	br.close();
	    }
	    
	    for(String s : text.split(";")){
	    	if(s.startsWith(INSERT_QUERY)){
	    		
	    		for(String entry : s.substring(INSERT_QUERY.length(), s.length()).split("(?=\\()")){
	    			
	    			String[] data = entry.trim().split(",");
	    			
	    			BigInteger id = BigInteger.valueOf(Long.valueOf(data[0].replace("(", "")));
	    			String usergroup = data[1].replaceAll("\\)|,|'", "");
	    			
	    			usergroups.put(id, usergroup);
	    		}
	    	}
	    }
	    if(usergroups.isEmpty()){
	    	LOGGER.error("Usergroups could not be read.");
	    }
	}

	public void close() throws IOException {
		if(file != null){
			file.close();
		}
		if(writer != null)
			writer.close();
	}
}
