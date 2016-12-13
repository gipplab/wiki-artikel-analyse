package ao.thesis.wikianalyse.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public class UserGroupReader {
	
	private Logger logger;
	
	private static final String INSERT_QUERY = "INSERT INTO `user_groups` VALUES ";
	
	private static Map<BigInteger, String> usergroups = new HashMap<BigInteger,String>();

	
	public String getUsergroup(BigInteger id){
		return usergroups.get(id);
	}

	public UserGroupReader(File file, Logger logger) {
		this.logger = logger;
		
		try {
			read(file);
		} catch (IOException e) {
			logger.error("Usergroups could not be read.", e);
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
	    	logger.error("Usergroups could not be read.");
	    }
	}
}
