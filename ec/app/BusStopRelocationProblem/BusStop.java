package ec.app.BusStopRelocationProblem;

public class BusStop {
	private int suben;
	private int bajan;
	private int parada;
	private int offset;
	//En caso de que la parada sea nueva, las coordenadas las originalies desplazadas
	private double longitud;
	private double latitud;
	private static int MAX_K = 10000;
	
	public BusStop(int suben, int bajan, int parada, double latitud, double longitud, int offset){
		this.suben = suben;
		this.bajan = bajan;
		this.parada = parada;
		this.longitud = longitud;
		this.latitud = latitud;
		this.offset = offset;
	}
	
	public int getSuben(){
		return suben;
	}
	
	public int getBajan(){
		return bajan;
	}
	
	public int getParada(){
		return parada;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public void setOffset(int offset){
		this.offset = offset;
	}
	
	public void setSuben(int suben){
		this.suben = suben;
	}
	
	public void setBajan(int bajan){
		this.bajan = bajan;
	}
	
	public static int getMAX_K(){
		return ++MAX_K;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}
}
