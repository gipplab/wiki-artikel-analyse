package output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVWriter;
import main.Main;
import comparer.Edit;
import comparer.EditScript;

public class EditScriptWriter {
	
	private CSVWriter csvwriter;
	
	private static EditScriptWriter writer = new EditScriptWriter();
	
	public static EditScriptWriter getWriter(){return writer;}
	
	private EditScriptWriter(){} 
	
	//-------------------------------------------------
	
	public void write(String pageTitle, EditScript script)
	{	
		script.sortPositions();
		
		ArrayList<String[]> data = new ArrayList<String[]>(script.size());

		String[] column = new String[3];
		column[0]="SourcePosition";
		column[1]="TargetPosition";
		column[2]="Length";

		data.add(column);
		
		for(Edit edit : script){
			column = new String[3];
			column[0]=String.valueOf(edit.sourcePosition);
			column[1]=String.valueOf(edit.targetPosition);
			column[2]=String.valueOf(edit.length);
			
			data.add(column);
		}
		
		String folder = Main.OUTPUT_DIR+"/"+pageTitle+"/EditScripts/";
		File file = new File(folder+script.target+"/");
		file.mkdirs();
		
		try {
			FileWriter fw = new FileWriter(file.getAbsolutePath()+"/"+script.source);
			csvwriter = new CSVWriter(fw);
			csvwriter.writeAll(data);
			csvwriter.close();

		} catch (IOException e) {
			System.err.println("Edit script could not be written");
		}
			
	}
}
