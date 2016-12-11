package ec.app.BusStopRelocationProblem.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parametros {
	public static String getWorkingPath(){
		return System.getProperty("user.dir") + File.separator;
	}
	
	public static String getParameterString(String ParameterName){
		String valor = "";

		try {
			String parameterPath = getWorkingPath() + "parametros";
			BufferedReader parametros = new BufferedReader(new FileReader(parameterPath));
			String line = parametros.readLine();
			
			while (line != null &&  valor.equals("")) {
				String[] tokens = line.split(":");
			    String token = tokens[0].trim();
			    if (ParameterName.trim().equals(token)){
			    	valor = tokens[1];
			    }
			    line = parametros.readLine();
			}
				
			parametros.close();
		} catch(IOException e){
			System.out.println("Parametros:getParameter - Archivo no encontrado");
			System.exit(-1);
		}
		
		return valor;
	}
	
	public static int getParameterInt(String ParameterName){
		int valor = 0;

		try {
			String parameterPath = getWorkingPath() + "parametros";
			BufferedReader parametros = new BufferedReader(new FileReader(parameterPath));
			String line = parametros.readLine();
			
			while (line != null &&  valor == 0) {
				String[] tokens = line.split(":");
			    String token = tokens[0].trim();
			    if (ParameterName.trim().equals(token)){
			    	valor = Integer.parseInt(tokens[1]);
			    }
			    line = parametros.readLine();
			}
				
			parametros.close();
		} catch(IOException e){
			System.out.println("Parametros:getParameter - Archivo no encontrado");
			System.exit(-1);
		}
		
		return valor;
	}
}
