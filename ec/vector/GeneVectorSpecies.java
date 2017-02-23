/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ec.*;
import ec.app.BusStopRelocationProblem.BusProblemInformation;
import ec.app.BusStopRelocationProblem.SDTs.SDTCoordenadas;
import ec.app.BusStopRelocationProblem.SDTs.SDTDistancias;
import ec.app.BusStopRelocationProblem.SDTs.SDTSubenBajan;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.app.BusStopRelocationProblem.utils.Parametros;
import ec.util.*;

/* 
 * GeneVectorSpecies.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Sean Luke
 */

/**
 * GeneVectorSpecies is a subclass of VectorSpecies with special
 * constraints for GeneVectorIndividuals.
 *
 * <p>At present there is exactly one item stored in GeneVectorSpecies:
 * the prototypical Gene that populates the genome array stored in a
 * GeneVectorIndividual.
 *
 * @author Sean Luke
 * @version 1.0 
 
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><tt>gene</tt><br>
 <font size=-1>classname, inherits and != ec.Gene</font></td>
 <td valign=top>(the prototypical gene for this kind of individual)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>gene</tt></td>
 <td>The prototypical gene for this kind of individual</td></tr>
 </table>

*/
 
public class GeneVectorSpecies extends VectorSpecies
    {
	/*****************************************************************/
	/* VARIABLES DEL PROBLEMA PARTICULAR BUS STOP RELOCATION PROBLEM */
	/*****************************************************************/
	
	private int cantLines; // Cantidad de lineas del problema 
	private int cantParadas = Parametros.getParameterInt("CantidadParadas"); // Cantidad de paradas del problema
	private int cantMaximaPasajeros = Parametros.getParameterInt("CantidadMaximaPasajeros"); // Cantidad de pasajeros maxima por bus
			
	private int gananciaPorViaje = Parametros.getParameterInt("GananciaPorViaje");
	private int costoCombustible = Parametros.getParameterInt("CostoCombustible");
	private int costoSalario = Parametros.getParameterInt("CostoSalario");
	
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
	
	/* Variables de ECJ */
    private static final long serialVersionUID = 1;

    public static final String P_GENE = "gene";
    public Gene genePrototype;
    
    /****************************************************/
    /*********** GETTERS DE INFO DEL PROBLEMA ***********/
    /********* Lineas | Pasajeros | Coordenadas *********/
    /****************************************************/
    public int getCantidadLineas(){
    	return this.cantLines;
    }
    
    public int getCantidadDeParadas() {
		return this.cantParadas;
	}
    
    public int getCantidadMaximaPasajeros() {
		return this.cantMaximaPasajeros;
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
    
    public final void printProblem(){
    	/* Imprime todas las variables del problema. Utilizado para verificar correecta carga de informacion */
    	try {
    		Iterator<Map.Entry<Integer, Integer>> it = this.correlacion.entrySet().iterator();
  		  	PrintWriter writer = null;
  		  	String filesPath = Parametros.getParameterString("RutaArchivos");
  		  	
  		  	while (it.hasNext()) {
  		  		Map.Entry<Integer, Integer> pair = it.next();
  		      
  		  		/* Imprime matriz de demanda */
  		  		writer = new PrintWriter(filesPath + "debug/debug_" + Integer.toString(pair.getKey()) + "_pasajeros", "UTF-8");
  		  		
  		  		for(int i = 1; i < this.cantParadas; i++){
  		    		if (this.MatrizDemanda[pair.getValue()][i] != null)
  		    			writer.println("(" + Integer.toString(this.MatrizDemanda[pair.getValue()][i].getSuben()) + "," + Integer.toString(this.MatrizDemanda[pair.getValue()][i].getBajan())+ ") ");
  		    		else
  		    			writer.println("null ");
  		  		}
  		  		writer.println();
  		  		writer.close();
  		      
  		  		/* Imprime orden de paradas */ 
  		  		writer = new PrintWriter(filesPath + "debug/debug_" + Integer.toString(pair.getKey()) + "_orden", "UTF-8");
  		  		Iterator<Integer> it2 = this.ordenParadas.get(pair.getKey()).iterator();
  		      
  		  		while(it2.hasNext())
  		  			writer.println(it2.next());

  		  		writer.println();
  		  		writer.close();  
  		  		
  		  		/* Imprime tiempos de recorrido */ 
  		  		writer = new PrintWriter(filesPath + "debug/debug_" + Integer.toString(pair.getKey()) + "_tiempos", "UTF-8");
  		  		Iterator<SDTDistancias> it3 = this.tiempos.get(pair.getKey()).iterator();
  		      
  		  		while(it3.hasNext())
  		  			writer.println(it3.next().toString());

  		  		writer.println();
  		  		writer.close(); 
  		  	}

  		  	writer = new PrintWriter(filesPath + "debug/debug_coordenadas", "UTF-8");
  		  	Iterator<Map.Entry<Integer, SDTCoordenadas>> it3 = this.coordenadas.entrySet().iterator();
  		  	
  		  	writer.println("Coordenadas");
  		  	while(it3.hasNext()){
  		  		Map.Entry<Integer, SDTCoordenadas> pair = it3.next();
  			  
  		  		int parada = pair.getKey();
  		  		double X = pair.getValue().getLatitud();
  		  		double Y = pair.getValue().getLogitud();
  			  
  		  		writer.println(Integer.toString(parada) + ',' + Double.toString(X) + ',' + Double.toString(Y));
  		  	}

  		  	writer.close();
  		  	System.out.println("termino");
  	  	} catch(Exception e){
  	  		System.out.println("ERROR: Fallo en printProblem. STACK = " + e);
  	  	}
    }
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();
        
        genePrototype = (Gene)(state.parameters.getInstanceForParameterEq(
                base.push(P_GENE),def.push(P_GENE),Gene.class));
        genePrototype.setup(state,base.push(P_GENE));

    	BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
	    
	    this.cantLines = information.getCantidadLineas();	    
	    this.MatrizDemanda = information.getMatrizDemanda();
	    this.correlacion = information.getCorrelacion();
	    this.ordenParadas = information.getOrdenParadas();
        this.coordenadas = information.getCoordenadas();
        this.tiempos = information.getTiempos();
        
        /* Imprimo el problema para ver que todo fue cargado correctamente */
        //printProblem();
        
        // make sure that super.setup is done AFTER we've loaded our gene prototype.
        super.setup(state,base);
        }
        
    }

