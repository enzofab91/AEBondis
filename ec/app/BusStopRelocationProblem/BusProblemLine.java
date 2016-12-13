package ec.app.BusStopRelocationProblem;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ec.EvolutionState;
import ec.app.BusStopRelocationProblem.BusStop;
import ec.util.MersenneTwister;
import ec.util.MersenneTwisterFast;
import ec.vector.Gene;

public class BusProblemLine extends Gene{
	private int linea;
	private int k;
	private List<BusStop> paradas = new LinkedList<BusStop>();
	
	MersenneTwister mt = new MersenneTwister();
	
	/* FUNCIONES PROPIAS DE BUS PROBLEM LINE */
	
	public BusProblemLine(){};
	
	public BusProblemLine(int linea, int k){
		this.linea = linea;
		this.k = k;
	}
	
	public int getLine(){
		return linea;
	}
	
	public int getK(){
		return k;
	}
	
	public List<BusStop> getParadas(){
		return paradas;
	}
	
	public void setK(int k) {
		if(k > 50)
			this.k = 50;
		else if(k < 0)
			this.k = 0;
		else
			this.k = k;
	}
	
	public void setParadas(List<BusStop> paradas){
		this.paradas = paradas;
	}
	
	public void agregarParada(BusStop stop){
		paradas.add(stop);
	}
	
	public void nuevaParada(int parada, int offset){
		List<BusStop> paradas_nuevas = new LinkedList<BusStop>();
		
		Iterator<BusStop> it = paradas.iterator();
		
		int indice = 0;
		boolean encontre = false;
		
		while(it.hasNext()){
			BusStop bs = it.next();
			paradas_nuevas.add(bs);
		}
		
		BusStop bps_new = funcionMagica(paradas_nuevas, parada, offset);
		
		paradas_nuevas.add(parada,bps_new);
		this.paradas = paradas_nuevas;
		
	}
	
	public void quitarParada(int parada){
		paradas.get(parada).setOffset(-1);
	}
	
	private BusStop funcionMagica(List<BusStop> paradas, int indice, int offset){
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
		
		//Aca se calculan las coordenadas de la nueva parada a partir de la parada anterior y el offset
		/* http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters */
		
		//Earth’s radius, sphere
		int R = 6378137;
	
		//Coordinate offsets in radians
		double dLat = (offset*100)/R;
		double dLon = (offset*100)/(R*Math.cos(Math.PI*parada_anterior.getLatitud()/180));
	
		//OffsetPosition, decimal degrees
		double nueva_longitud = parada_anterior.getLatitud() + dLat * 180/Math.PI;
		double nueva_latitud = parada_anterior.getLongitud() + dLon * 180/Math.PI; 
		 
		BusStop bps = new BusStop(quito_anterior_suben + quito_siguiente_suben, 
				 			quito_anterior_bajan + quito_siguiente_bajan, BusStop.getMAX_K(),
				 			nueva_longitud, nueva_latitud, offset);
		 
		 return bps;
	}
	
	/*public int getPseudoRandomStop(){
		return paradas.get(randomGenerator.nextInt(0, paradas.size()-1)).getParada();
	}*/
	
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
		//double alpha = state.random[thread].nextDouble() * Math.PI * 2;
		//x = Math.cos(alpha); y = Math.sin(alpha);
	}
		
	public void mutate(EvolutionState state, int thread) {
		//double alpha = Math.atan2(y,x);
		//double dalpha = (state.random[thread].nextDouble() - 0.5) * Math.PI * 2 / 100.0;
		//x = Math.cos(alpha + dalpha); y = Math.sin(alpha + dalpha);
		reset(state, thread);
	}
	
	public int hashCode() {
		return linea;
		//long a = Double.doubleToRawLongBits(x); long b = Double.doubleToRawLongBits(y);
		//return (int) ((a & (int)-1) ^ (a >> 32) (b & (int)-1) ^ (b >> 32));
	}
		
	public boolean equals(Object other) {
		return (other != null && other instanceof BusProblemLine &&
		((BusProblemLine)other).linea == linea && ((BusProblemLine)other).k == k && ((BusProblemLine)other).paradas == paradas);
	}
	
	public String printGeneToStringForHumans() { 
		return ">";
	}
		
	public String printGeneToString() {
		return ">";
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
