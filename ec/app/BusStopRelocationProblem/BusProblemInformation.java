package ec.app.BusStopRelocationProblem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ec.app.BusStopRelocationProblem.SDTs.SDTCoordenadas;
import ec.app.BusStopRelocationProblem.SDTs.SDTDistancias;
import ec.app.BusStopRelocationProblem.SDTs.SDTSubenBajan;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.app.BusStopRelocationProblem.utils.Parametros;

public class BusProblemInformation {
	private static BusProblemInformation busProblemInformation;
	
	/*****************************************************************/
	/* VARIABLES DEL PROBLEMA PARTICULAR BUS STOP RELOCATION PROBLEM */
	/*****************************************************************/
	
	private int cantLines; // Cantidad de lineas del problema 
	private int cantParadas = Parametros.getParameterInt("CantidadParadas"); // Cantidad de paradas del problema
	private int cantMaximaPasajeros = Parametros.getParameterInt("CantidadMaximaPasajeros"); // Cantidad de pasajeros maxima por bus
	private int nuevaDistanciaMaxima = Parametros.getParameterInt("NuevaDistanciaMaxima"); // Distancia maxima que se desplaza una parada 
	
	private String tipoMutacion = Parametros.getParameterString("Mutacion");
	private String tipoDistancia = Parametros.getParameterString("Distancias");
	private String tipoCrossover = Parametros.getParameterString("Crossover");
	
	private int gananciaPorViaje = Parametros.getParameterInt("GananciaPorViaje");
	private int costoCombustible = Parametros.getParameterInt("CostoCombustible");
	private int costoSalario = Parametros.getParameterInt("CostoSalario");
	private int velocidadPromedio = Parametros.getParameterInt("VelocidadPromedio");
	
	private int stopTime = Parametros.getParameterInt("StopTime"); // Tiempo necesario del bus en parar
	private int boardTime = Parametros.getParameterInt("BoardTime"); // Tiempo necesario para que el pasajero suba
	private int alightTime = Parametros.getParameterInt("AlightTime"); // Tiempo necesario para que el pasajero baje
	
	private int walkingSpeed = Parametros.getParameterInt("WalkingSpeed"); // Velocidad promedio de una persona (en km/h)
	
	/* Tengo una matriz de pasajeros en donde se guarda en X = posición del array (línea), Y = un ID de parada. */
	/* En la posición se guarda un SDTParada. Este SDTParada tiene la cantidad de personas que suben y que 		*/
	/* bajan en esa parada para esa línea.																		*/
	private SDTSubenBajan[][] MatrizDemanda;
	
	/* Una lista de codigos variantes, cada uno de ellos tiene una matriz con los valores 		*/
	/* (bus_stop1,bus_stop2) -> tiempo que le lleva recorrerlo. Este tiempo se calcula como 	*/
	/* tiempo = distancia (en metros) / velocidad (en metros/segundo). Como son datos que no	*/
	/* cambian en el problema, se precalculan para que luego el algoritmo sea mas rapido		*/
	private Map<Integer, List<SDTDistancias>> tiempos = new HashMap<Integer, List<SDTDistancias>>();
	
	/* Un mapeo entre la parada (clave) y sus coordenadas en latitud y longitud (valor) */
	private Map<Integer, SDTCoordenadas> coordenadas = new HashMap<Integer, SDTCoordenadas>();
	
	/* Un mapa (correlacion) que dada una línea de ómnibus devuelve la posición del array (por ejemplo el 	*/
	/* omnibus 100 es la posición 0, el 102 es la posición 1..). Esto se utiliza para que sea mas facil 	*/
	/* y eficiente el almacenamiento. Si se utilizaran las lineas 100 y 582, la matriz de pasajeros deberia */
	/* ser de largo 582 y con esto alcanza largo 2.															*/
	private Map<Integer,Integer> correlacion = new HashMap<Integer,Integer>();
	
	/* Un map que almacena para cada linea, una lista de sus paradas en orden desde el origen la destino */
	private Map<Integer,List<Integer>> ordenParadas = new HashMap<Integer, List<Integer>>();
	
	/* Auxiliar que mantiene la linea actual. Para inicializar la poblacion */
	private int lineaActual = 0;
	
	/****************************************************/
    /*********** GETTERS DE INFO DEL PROBLEMA ***********/
    /********* Lineas | Pasajeros | Coordenadas *********/
    /****************************************************/
    public int getLineaActual(){
    	int aux = this.lineaActual;
    	this.lineaActual++;
    	
    	if (this.lineaActual == this.cantLines)
    		this.lineaActual = 0;
    	
    	return aux;
    }
    
	public int getCantidadLineas(){
    	return this.cantLines;
    }
    
    public int getCantidadDeParadas() {
		return this.cantParadas;
	}
    
    public int getCantidadMaximaPasajeros() {
		return this.cantMaximaPasajeros;
	}
    
    public int getNuevaDistanciaMaxima() {
		return this.nuevaDistanciaMaxima;
	}
    
    public String getTipoMutacion() {
		return this.tipoMutacion;
	}
    
    public String getTipoDistancia() {
		return this.tipoDistancia;
	}
    
    public int getVelocidadPromedio() {
		return this.velocidadPromedio;
	}
    
    public String getTipoCrossover() {
		return this.tipoCrossover;
	}
    
    public Map<Integer, SDTCoordenadas> getCoordenadas() {
		return this.coordenadas;
	}
    
    public Map<Integer, Integer> getCorrelacion() {
		return this.correlacion;
	}
    
    public SDTSubenBajan[][] getMatrizDemanda() {
		return this.MatrizDemanda;
	}
    
    public Map<Integer, List<SDTDistancias>> getTiempos(){
    	return this.tiempos;
    }
    
    public Map<Integer, List<Integer>> getOrdenParadas() {
		return this.ordenParadas;
	}
    
    public int getGananciaPorViaje() {
		return this.gananciaPorViaje;
	}
    
    public int getCostoCombustible() {
		return this.costoCombustible;
	}
    
    public int getCostoSalario() {
		return this.costoSalario;
	}
    
    public int getStopTime() {
		return this.stopTime;
	}
    
    public int getBoardTime() {
		return this.boardTime;
	}
    
    public int getAlightTime() {
		return this.alightTime;
	}
    
    public int getWalkingSpeed() {
		return this.walkingSpeed;
	}
    
    BusProblemInformation(){
    	try {
           	/* Obtengo la ruta donde se encuentran los archivos y el nombre del archivo de las lineas del problema   */
           	String filesPath = Parametros.getParameterString("RutaArchivos");
           	String linesPath = Parametros.getParameterString("ArchivoLineas");
           	String linesFile = filesPath + linesPath;
           	
           	/* Abro el archivo, y guardo la cantidad de lineas */
           	BufferedReader br = new BufferedReader(new FileReader(linesFile));

       		String line = br.readLine();
       	    String[] lineas = line.split(",");
       	    
       	    /* Seteo la cantidad de lineas del problema */
       	    this.cantLines = lineas.length;
       	    
       	    /* Creo la matriz de demanda */
       	    this.MatrizDemanda = new SDTSubenBajan[this.cantLines][this.cantParadas];
       	    
       	    /* 
       	    	Para cada linea de omnibus del problema, tengo que obtener sus paradas en orden
       	    	y la cantidad de pasajeros que suben y que bajan en cada parada. Ademas, se
       	    	guarda las coordenadas de las paradas
       	    */

       	    for(int i = 0; i < this.cantLines; i++){
       	    	/* Agrego la correlacion entre la parada y la posicion en la matriz de demanda */
       	    	this.correlacion.put(Integer.parseInt(lineas[i]), i);
       	    	
       	        /* Leo la cantidad de pasajeros que suben y bajan en cada parada de la linea y lo almaceno en la matriz de demanda */
       	        BufferedReader pasajeros = new BufferedReader(new FileReader(filesPath + lineas[i]+ "_pasajeros"));
       	        
       	    	line = pasajeros.readLine();
       	    	
       	    	/* Inicializo para la linea i, su lista de paradas */
       	    	this.ordenParadas.put(Integer.parseInt(lineas[i]), new LinkedList<Integer>());
       	    	
       	        while (line != null) {
       	        	String[] linea = line.split(",");
       	        	SDTSubenBajan SDT = new SDTSubenBajan(Integer.parseInt(linea[1]), Integer.parseInt(linea[2]));
       	        	
       	        	this.MatrizDemanda[i][Integer.parseInt(linea[0])] = SDT;
       	        	this.ordenParadas.get(Integer.parseInt(lineas[i])).add(Integer.parseInt(linea[0]));
       	        	
       	            line = pasajeros.readLine();
       	        }
       	        
       	        pasajeros.close();
       	    	
       	        //si el parametro es Haversine no utiliza archivos de distancias
       	        File existe_distancias = new File(filesPath + lineas[i]+ "_distancias");
       	        if (existe_distancias.exists()){
	       	        BufferedReader distancias = new BufferedReader(new FileReader(filesPath + lineas[i]+ "_distancias"));
	       	        line = distancias.readLine();
	       	        
	       	        //DebugFileLog.DebugFileLog("pruebaDistancias", "Voy a leer distancias!");
	       	        double tiempo = 0;
	       	        List<SDTDistancias> tiempoLinea = new ArrayList<SDTDistancias>();
	       	        
		 	        while (line != null) {
		 	        	String[] informacion = line.split(",");
		 	        	//DebugFileLog.DebugFileLog("pruebaDistancias", "linea = " + line);
		 	        	tiempoLinea.add(new SDTDistancias(Integer.parseInt(informacion[0]), Integer.parseInt(informacion[1]),
		 	        			Double.parseDouble(informacion[2]), Double.parseDouble(informacion[3])));
		 	        	
		 	            line = distancias.readLine();
		 	        }
		 	        
		 	       this.tiempos.put(Integer.parseInt(lineas[i]),tiempoLinea);
	 	        
		 	        distancias.close();
       	        }
       	    }
       	    br.close();
       	    
       	    /* Almaceno las coordenadas de las paradas */
   	    	BufferedReader coordenadas = new BufferedReader(new FileReader(filesPath + "coordenadas"));
   	    	line = coordenadas.readLine();

   	        while (line != null) {
   	        	String[] coordenada = line.split(",");
   	        	this.coordenadas.put(Integer.parseInt(coordenada[0]), new SDTCoordenadas(Double.parseDouble(coordenada[1]), Double.parseDouble(coordenada[2])));
   	            line = coordenadas.readLine();
   	        }
   	        
   	        coordenadas.close();
           } catch(IOException e){
               System.out.println("ERROR: Hubo algun problema con la lectura del archivo. STACK = " + e);
           }
   	}
    
    public  static BusProblemInformation getBusProblemInformation(){
	    if (busProblemInformation == null) {
	    	busProblemInformation = new BusProblemInformation();
	    }
	    return busProblemInformation;
    }	
}
