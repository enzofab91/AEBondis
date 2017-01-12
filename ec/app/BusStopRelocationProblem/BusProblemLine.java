package ec.app.BusStopRelocationProblem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ec.EvolutionState;
import ec.Population;
import ec.app.BusStopRelocationProblem.BusStop;
import ec.app.BusStopRelocationProblem.SDTs.SDTCoordenadas;
import ec.app.BusStopRelocationProblem.SDTs.SDTSubenBajan;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.app.BusStopRelocationProblem.utils.Parametros;
import ec.util.MersenneTwister;
import ec.vector.Gene;
import ec.vector.VectorIndividual;

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
		int cantidadMaximaPasajeros = Parametros.getParameterInt("CantidadMaximaPasajeros");
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
		List<BusStop> paradas_nuevas = new LinkedList<BusStop>();
		
		Iterator<BusStop> it = paradas.iterator();
		
		while(it.hasNext()){
			BusStop bs = it.next();
			paradas_nuevas.add(bs);
		}
		
		BusStop bps_new = funcionMagica(paradas_nuevas, parada, desplazamiento);
		
		paradas_nuevas.add(parada,bps_new);
		this.paradas = paradas_nuevas;
		
	}
	
	public void quitarParada(int parada){
		paradas.get(parada).setEstado(EstadoParada.ELIMINADA);
	}
	
	private BusStop funcionMagica(List<BusStop> paradas, int indice, int desplazamiento){
		//Esta funcion magica se encarga de tomar gente que sube/baja de las paradas siguiente y anterior de la nueva parada
		// que corresponden a la parada misma y la siguiente
		//Ademas se quitan de la parada siguiente/anterior los valores obtenidos de personas que suben y bajan
		//Falta definir la posibilidad de aceptar nueva gente que suba al bus
		
		BusStop parada_siguiente = paradas.get(indice + 1); 
		BusStop parada_anterior = paradas.get(indice);
		
		int quito_anterior_suben = mt.nextInt(parada_anterior.getSuben());
		int quito_anterior_bajan  = mt.nextInt(parada_anterior.getBajan());
		int quito_siguiente_suben = mt.nextInt(parada_siguiente.getSuben());
		int quito_siguiente_bajan = mt.nextInt(parada_siguiente.getBajan());
		
		parada_siguiente.setSuben( parada_siguiente.getSuben() - quito_siguiente_suben);
		parada_siguiente.setBajan(parada_siguiente.getBajan() - quito_siguiente_bajan);
		parada_anterior.setSuben( parada_anterior.getSuben() - quito_anterior_suben);
		parada_anterior.setBajan(parada_anterior.getBajan() - quito_anterior_bajan);
		
		//Aca se calculan las coordenadas de la nueva parada a partir de la parada anterior y el desplazamiento
		double[] coordinates = Operaciones.nuevaUbicacion(parada_anterior.getLatitud(), parada_anterior.getLongitud(), desplazamiento);
		double nueva_longitud = coordinates[0], nueva_latitud = coordinates[1];
		
		BusStop bps = new BusStop(quito_anterior_suben + quito_siguiente_suben, 
				 			quito_anterior_bajan + quito_siguiente_bajan, BusStop.getNuevoIdentificador(),
				 			nueva_longitud, nueva_latitud, EstadoParada.DESPLAZADA, desplazamiento);
		 
		 return bps;
	}
	
	public int getPseudoRandomStop(){
		return paradas.get(mt.nextInt(paradas.size()-1)).getParada();
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
		this.asientosDisponibles = Parametros.getParameterInt("CantidadMaximaPasajeros");
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
	}
		
	public void mutate(EvolutionState state, int thread) {
		String mutacion = Parametros.getParameterString("Mutacion");
		
		if (mutacion.equals("ElegirAccion"))
			mutateElegirAccion(state, thread);
		else
			mutateBusquedaParadaInnecesaria(state, thread);
	}
	
	/* MUTACION DE BUS STOP RELOCATION PROBLEM */
	private void mutateElegirAccion(EvolutionState state, int thread) {
		
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
			
			if (stop1.getSuben() == 0){
				stop2 = iter.next();
				if (stop2.getSuben() == 0){
					/* CONSECUTIVAS. Corto la busqueda */
					posicion = indice;
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
				quitarParada(0);
			} else if (posicion == paradas.size() - 1){
				quitarParada(paradas.size() - 1);
			} else {
				double coordinates[] = Operaciones.puntoMedio(stop1.getLatitud(), stop1.getLongitud(), stop2.getLatitud(), stop2.getLongitud());
				double latitud = coordinates[0], longitud = coordinates[1];
				
				BusStop nuevaParada = new BusStop(0,stop1.getBajan()+stop2.getBajan(),BusStop.getNuevoIdentificador(),latitud,longitud,EstadoParada.NUEVA,0);
				agregarParada(nuevaParada);
			}
			
		}
		
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
