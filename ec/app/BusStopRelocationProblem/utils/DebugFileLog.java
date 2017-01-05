package ec.app.BusStopRelocationProblem.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class DebugFileLog {
	public final static void DebugFileLog(String fileName, String line){
		try {
			String filesPath = Parametros.getParameterString("RutaArchivos");
			Writer output = new BufferedWriter(new FileWriter(filesPath + "debug/" +  fileName, true));

			output.append(line + "\n");
			
			output.close();
		} catch(Exception e){
  	  		System.out.println("ERROR: Fallo en DebugFileLog. STACK = " + e);
  	  	}
	}
}
