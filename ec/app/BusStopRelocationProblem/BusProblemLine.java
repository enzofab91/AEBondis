package ec.app.BusStopRelocationProblem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import ec.EvolutionState;
import ec.app.BusStopRelocationProblem.BusStop;
import ec.app.BusStopRelocationProblem.SDTs.SDTCoordenadas;
import ec.app.BusStopRelocationProblem.SDTs.SDTSubenBajan;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.util.MersenneTwister;
import ec.vector.Gene;

public class BusProblemLine extends Gene{
	private int linea;
	private int asientosDisponibles;
	private List<BusStop> paradas;
	
	MersenneTwister mt = new MersenneTwister();
	
	/* FUNCIONES PROPIAS DE BUS PROBLEM LINE */
	public BusProblemLine(){};
	
	public BusProblemLine(int linea, int asientosDisponibles){
		this.linea = linea;
		this.asientosDisponibles = asientosDisponibles;
	}
	
	public int getLine(){
		return this.linea;
	}
	
	public int getAsientosDisponibles(){
		return this.asientosDisponibles;
	}
	
	public List<BusStop> getParadas(){
		return paradas;
	}
	
	public void setAsientosDisponibles(int asientosDisponibles) {
		BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
		int cantidadMaximaPasajeros = information.getCantidadMaximaPasajeros();
		
		if(this.asientosDisponibles > cantidadMaximaPasajeros)
			this.asientosDisponibles = cantidadMaximaPasajeros;
		else if(this.asientosDisponibles < 0)
			this.asientosDisponibles = 0;
		else
			this.asientosDisponibles = asientosDisponibles;
	}
	
	public void setParadas(List<BusStop> paradas){
		this.paradas = paradas;
	}
	
	public void agregarParada(BusStop stop){
		paradas.add(stop);
	}
    
	public void nuevaParada(int parada, int desplazamiento){
		/* La funcion toma gente que sube/baja de las paradas siguiente y anterior 	 */
		/* de la nueva parada a agregar, y las distribuye entre la nueva y estas dos */
		/* Las que se modifican son la inmediatamente siguiente y posterior			 */
		
		BusStop parada_siguiente = paradas.get(parada + 1); //parada actual (cuando se agregue la nueva sera la inmediata anterior) 
		BusStop parada_anterior = paradas.get(parada); //parada siguiente
		
		int quito_anterior_suben = 0, quito_anterior_bajan = 0, quito_siguiente_suben = 0, quito_siguiente_bajan = 0;
		
		//next int espera un numero mayor a 0 sino explota
		if (quito_anterior_suben > 0)
			quito_anterior_suben = mt.nextInt(parada_anterior.getSuben());
		if (quito_anterior_bajan > 0)
			quito_anterior_bajan  = mt.nextInt(parada_anterior.getBajan());
		if (quito_siguiente_suben > 0)
			quito_siguiente_suben = mt.nextInt(parada_siguiente.getSuben());
		if (quito_siguiente_bajan > 0)
			quito_siguiente_bajan = mt.nextInt(parada_siguiente.getBajan());
		
		parada_siguiente.setSuben(parada_siguiente.getSuben() - quito_siguiente_suben);
		parada_siguiente.setBajan(parada_siguiente.getBajan() - quito_siguiente_bajan);
		parada_anterior.setSuben(parada_anterior.getSuben() - quito_anterior_suben);
		parada_anterior.setBajan(parada_anterior.getBajan() - quito_anterior_bajan);
		
		/* Se calculan las coordenadas de la nueva parada a partir de la parada anterior y el desplazamiento */
		double[] coordinates = Operaciones.nuevaUbicacion(parada_anterior.getLatitud(), 
				parada_anterior.getLongitud(), desplazamiento);
		double nueva_longitud = coordinates[0], nueva_latitud = coordinates[1];
		
		/* Se crea la nueva parada, a partir de las coordenadas, y las personas que se sacaron de las paradas 	*/
		/* inmediatamente anterior y siguiente																	*/
		BusStop parada_nueva = new BusStop(quito_anterior_suben + quito_siguiente_suben, 
				 			quito_anterior_bajan + quito_siguiente_bajan, BusStop.getNuevoIdentificador(),
				 			nueva_longitud, nueva_latitud, EstadoParada.DESPLAZADA, desplazamiento);

		this.paradas.add(parada,parada_nueva);
	}
	
	public void quitarParada(int parada){
		paradas.get(parada).setEstado(EstadoParada.ELIMINADA);
	}
	
	
	public BusStop getStop(int index){
		return paradas.get(index);
	}
	
	public BusStop checkBusStopInLine(int nro_parada){
		Iterator<BusStop> it = paradas.listIterator();
		
		BusStop bps = null;
		BusStop bps_aux = null;
		
		while(it.hasNext()){
			bps_aux = it.next();
			
			if(bps_aux.getParada() == nro_parada){
				bps = bps_aux;
				break;
			}
			
		}
		
		return bps;
	}
	
	/* FUNCIONES DE EJC */
	public void reset(EvolutionState state, int thread) {
		/* INICIALIZAR LA POBLACION */
		BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
		SDTSubenBajan[][] matrizPasajeros = information.getMatrizDemanda();
		Map<Integer, SDTCoordenadas> coordenadas = information.getCoordenadas();
		Map<Integer, Integer> lineas = information.getCorrelacion();
		
		int posicion = information.getLineaActual(); //entry.getKey();
		int linea = 0;
		
		for (Entry<Integer, Integer> entry : lineas.entrySet()) {
			if (entry.getValue().equals(posicion))
				linea = entry.getKey();
		}
			
		this.linea = linea;
		this.asientosDisponibles = information.getCantidadMaximaPasajeros();
		Iterator<Integer> ordenParadas = information.getOrdenParadas().get(linea).iterator();
		
		this.paradas = new LinkedList<BusStop>();
		while(ordenParadas.hasNext()){
			int aux = ordenParadas.next();
		    if(matrizPasajeros[posicion][aux] != null){
		    	BusStop bps = null;
			    	
			    int bajan = 0;
			    int suben = 0;
			    	
			    if (matrizPasajeros[posicion][aux].getBajan() > 0)
					bajan = mt.nextInt(matrizPasajeros[posicion][aux].getBajan());
			    	
			    if (matrizPasajeros[posicion][aux].getSuben() > 0)
			    	suben = mt.nextInt(matrizPasajeros[posicion][aux].getSuben());
			
			    			
		    	if(getAsientosDisponibles() >= (suben - bajan)){
		    		bps = new BusStop(suben,bajan,aux,coordenadas.get(aux).getLatitud(),coordenadas.get(aux).getLogitud(),EstadoParada.ACTUAL,0); 
		    	} else {
		    		bps = new BusStop(getAsientosDisponibles() + bajan,bajan,aux,coordenadas.get(aux).getLatitud(),coordenadas.get(aux).getLogitud(),EstadoParada.ACTUAL,0); 
		    	}
		    	
		    	setAsientosDisponibles(getAsientosDisponibles() + bps.getBajan());
		    	setAsientosDisponibles(getAsientosDisponibles() - bps.getSuben());

		    	agregarParada(bps);
			  }
		}
		
		//para agregar diversidad, se redistribuyen pasajeros y se modifican paradas
		int parada_modificar_diversidad = 0;
		
		//busco parada que no sea ni la primera ni la ultima para modificar la matriz OD
		while ((parada_modificar_diversidad == 0) || (parada_modificar_diversidad == paradas.size() - 1)){
			parada_modificar_diversidad = mt.nextInt(paradas.size());
		}

		BusStop parada_modificar = paradas.get(parada_modificar_diversidad);
		BusStop parada_anterior = paradas.get(parada_modificar_diversidad - 1);
		BusStop parada_siguiente = paradas.get(parada_modificar_diversidad + 1);
				
		int cant_suben_modificar = 0, cant_bajan_modificar = 0;
		//next int requiere un numero positivo
		if (parada_modificar.getSuben() > 0)
			cant_suben_modificar = mt.nextInt(parada_modificar.getSuben());
		if (parada_modificar.getBajan() > 0)
			cant_bajan_modificar = mt.nextInt(parada_modificar.getBajan());
		
		//distribuyo los que se mueven de parada en la siguiente y anterior
		parada_modificar.setSuben(parada_modificar.getSuben() - cant_suben_modificar);
		parada_anterior.setSuben(parada_anterior.getSuben() + cant_suben_modificar/2);
		
		if (cant_suben_modificar % 2 == 1) //hay que igualar los que se van de la parada a modificar
			parada_siguiente.setSuben(parada_siguiente.getSuben() + 1 + cant_suben_modificar/2);
		else
			parada_siguiente.setSuben(parada_siguiente.getSuben() + cant_suben_modificar/2);
		
		parada_modificar.setBajan(parada_modificar.getBajan() - cant_bajan_modificar);
		parada_anterior.setBajan(parada_anterior.getBajan() + cant_bajan_modificar/2);
		
		if (cant_bajan_modificar % 2 != 0)
			parada_siguiente.setBajan(parada_siguiente.getBajan() + 1 + cant_bajan_modificar/2);
		else
			parada_siguiente.setBajan(parada_siguiente.getBajan() + cant_bajan_modificar/2);
		
		//busco parada que no sea ni la primera ni la ultima para modificar su ubicacion
		parada_modificar_diversidad = 0;
		while ((parada_modificar_diversidad == 0) || (parada_modificar_diversidad == paradas.size() - 1)){
			parada_modificar_diversidad = mt.nextInt(paradas.size());
		}
		
		int cant_maxima_desplazar = information.getNuevaDistanciaMaxima();
		int cant_desplazar = mt.nextInt(cant_maxima_desplazar);
		
		/* Se calculan las coordenadas de la nueva parada a partir de la parada anterior y el desplazamiento */
		BusStop parada_desplazar = paradas.get(parada_modificar_diversidad);
		double[] coordinates = Operaciones.nuevaUbicacion(parada_desplazar.getLatitud(),
				parada_desplazar.getLongitud(), cant_desplazar);
		double nueva_latitud = coordinates[0], nueva_longitud = coordinates[1];
		
		/* Se crea la nueva parada, a partir de las coordenadas, y las personas que */
		/* se encontraban en la parada												*/
		BusStop parada_nueva = new BusStop(parada_desplazar.getSuben(), parada_desplazar.getBajan(), 
				parada_desplazar.getParada(), nueva_latitud, nueva_longitud, EstadoParada.DESPLAZADA, cant_desplazar);
		
		//elimino la parada a desplazar y agrego la misma pero desplazada
		paradas.remove(parada_modificar_diversidad);
		paradas.add(parada_modificar_diversidad, parada_nueva);
	}
		
	public void mutate(EvolutionState state, int thread) {
		BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
		String mutacion = information.getTipoMutacion();
		
		if (mutacion.equals("ElegirAccion")){
			mutateElegirAccion(state, thread);
		
			//funcion correctiva
			mutateBusquedaParadaInnecesaria(state, thread);
		} else
			mutateBusquedaParadaInnecesaria(state, thread);
	}
	
	/* MUTACION DE BUS STOP RELOCATION PROBLEM */
	private void mutateElegirAccion(EvolutionState state, int thread) {
		//obtenemos que parada modificar y si se queda donde esta, se saca o se modifica (sin ser la primera y ultima)
		int parada_mutar = 0;
		
		while ((parada_mutar == 0) || (parada_mutar == paradas.size() - 1)){
			parada_mutar = mt.nextInt(paradas.size());
		}
		
		BusProblemInformation info = BusProblemInformation.getBusProblemInformation();
		int distanciaMaxima = info.getNuevaDistanciaMaxima(); 
		
		/* La accion determina que se hace con la parada: 	*/
		/* 0: la parada a mutar se deja invariable			*/
		/* -1: la parada a mutar se quita					*/
		/* num: se crea una nueva parada a partir de la 	*/
		/* parada a mutar y se mueve una distancia 			*/
		
		/* nextInt devuelve entre 0 y distancia-1 ambos 	*/
		/* incluidos. Esto abarca todo el rango posible 	*/
		int accion = mt.nextInt(distanciaMaxima + 2) - 1;
		
		//mutamos la parada si corresponde
		if (accion != 0){
			if(accion == -1){
				
				//trasladamos personas a parada siguiente y anterior
				BusStop parada_siguiente = paradas.get(parada_mutar + 1); //parada siguiente
				BusStop parada_anterior = paradas.get(parada_mutar - 1); //parada anterior
				
				parada_siguiente.setSuben(parada_siguiente.getSuben() + paradas.get(parada_mutar).getSuben());
				parada_siguiente.setBajan(parada_siguiente.getBajan() + paradas.get(parada_mutar).getBajan());
				parada_anterior.setSuben(parada_anterior.getSuben() + paradas.get(parada_mutar).getSuben());
				parada_anterior.setBajan(parada_anterior.getBajan() + paradas.get(parada_mutar).getBajan());
				
				quitarParada(parada_mutar);
			}
			else {
				nuevaParada(parada_mutar,accion);
			}
		}
	}
	
	private void mutateBusquedaParadaInnecesaria(EvolutionState state, int thread) {
		/* Se elige una linea de la solucion aleatoriamente. Luego, se busca si existen dos paradas */
		/* consecutivas que no suba gente y si las hay se eliminan esas dos y se juntan en una 		*/
		/* unica parada en el punto medio de la distancia entre ambas								*/
		
		/* Busco si existen dos paradas consecutivas que no se suba nadie */
		Iterator<BusStop> iter = paradas.listIterator();
		
		BusStop stop1 = null;
		BusStop stop2 = null;
		
		boolean existeCombinacion = false;
		int posicion = 0, indice = 0;

		while (!existeCombinacion && iter.hasNext()){
			stop1 = iter.next();
			
			if ((stop1.getEstado() != EstadoParada.ELIMINADA) && (stop1.getSuben() == 0) && (iter.hasNext())){
				stop2 = iter.next();
				if ((stop2.getEstado() != EstadoParada.ELIMINADA) && (stop2.getSuben() == 0)){
					/* CONSECUTIVAS. Corto la busqueda */
					posicion = indice;
					
					//chequeo que no sea la ultima
					if (iter.hasNext())
						existeCombinacion = true;
				}
			}
			
			indice++;
		}
		
		if (existeCombinacion){
			/* Hay 3 casos: si es la parada origen y la siguiente, simplemente elimino la siguiente. */
			/* Si es la parada destino y la anterior, simplemente elimino la anterior. Esto es asi	 */
			/* porque los origenes y destinos de las lineas no pueden ser modificados. Si son		 */
			/* un par cualquiera del medio, ahi si se quitan y se agrega la del punto medio			 */
			
			if (posicion == 0){
				quitarParada(1);
			} else if (posicion == paradas.size() - 2){
				quitarParada(paradas.size() - 1);
			} else {
				double coordinates[] = Operaciones.puntoMedio(stop1.getLatitud(), stop1.getLongitud(), stop2.getLatitud(), stop2.getLongitud());
				double latitud = coordinates[0], longitud = coordinates[1];
				
				BusStop nuevaParada = new BusStop(0,stop1.getBajan()+stop2.getBajan(),BusStop.getNuevoIdentificador(),latitud,longitud,EstadoParada.NUEVA,0);
				agregarParadaPosicion(nuevaParada, posicion);
			}
			
		}
		
	}
	
	private void agregarParadaPosicion(BusStop nuevaParada, int posicion){
		//remueve las dos paradas de la posicion y la siguiente
		paradas.remove(posicion + 1);
		paradas.remove(posicion);
		
		//agrega la nueva parada
		paradas.add(posicion, nuevaParada);
	}
	
	/* FIN DE MUTACION DE BUS STOP RELOCATION PROBLEM */
	
	public int hashCode() {
		return linea;
		//long a = Double.doubleToRawLongBits(x); long b = Double.doubleToRawLongBits(y);
		//return (int) ((a & (int)-1) ^ (a >> 32) (b & (int)-1) ^ (b >> 32));
	}
		
	public boolean equals(Object other) {
		return (other != null && other instanceof BusProblemLine &&
		((BusProblemLine)other).linea == linea && ((BusProblemLine)other).asientosDisponibles == asientosDisponibles && ((BusProblemLine)other).paradas == paradas);
	}
	
	public String printGeneToStringForHumans() { 
		return toString();
	}
		
	public String printGeneToString() {
		return toString();
	}
	
	public String toString() {
		String ret = "";
		
		ret = "BusProblemLine {Linea: " + this.linea + ", asientosDisponibles: " + this.asientosDisponibles + ", Paradas: [";
		ret += this.paradas.toString();
		ret += "]}";
		
		return ret;
	}
	
	/*public void readGeneFromString(String string, EvolutionState state) {
		string = string.trim().substring(0); // get rid of the ">"
		//DecodeReturn dr = new DecodeReturn(string);
		//Code.decode(dr); x = dr.d; // no error checking
		//Code.decode(dr); y = dr.d;
	}*/

	//public void writeGene(EvolutionState state, DataOutput out) throws IOException {
	//	out.writeDouble("123"); out.writeDouble(y);
	//}
		
	//public void readGene(EvolutionState state, DataOutput in) throws IOException {
	//	x = in.readDouble(); y = in.readDouble();
	//}
}
