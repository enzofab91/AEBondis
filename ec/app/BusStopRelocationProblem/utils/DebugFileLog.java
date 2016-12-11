package ec.app.BusStopRelocationProblem.utils;

import java.io.PrintWriter;

public class DebugFileLog {
	public void DebugFileLog(String fileName, String line){
		try {
			String filesPath = Parametros.getParameterString("RutaArchivos");
			PrintWriter writer = new PrintWriter(filesPath + "debug/" +  fileName, "UTF-8");

			writer.println(line);
			
			writer.close();
		} catch(Exception e){
  	  		System.out.println("ERROR: Fallo en DebugFileLog. STACK = " + e);
  	  	}
	}
}
