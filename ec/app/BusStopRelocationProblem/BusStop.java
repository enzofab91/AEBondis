package ec.app.BusStopRelocationProblem;

public class BusStop {
	private int suben;
	private int bajan;
	private int parada;
	private EstadoParada estado;
	private int desplazamiento;
	
	private double longitud;
	private double latitud;
	
	private static int nuevoIdentificador = 10000;
	
	public BusStop(int suben, int bajan, int parada, double latitud, double longitud, EstadoParada estado, int desplazamiento){
		this.suben = suben;
		this.bajan = bajan;
		this.parada = parada;
		this.longitud = longitud;
		this.latitud = latitud;
		this.estado = estado;
		this.desplazamiento = desplazamiento;
	}
	
	public int getSuben(){
		return this.suben;
	}
	
	public int getBajan(){
		return this.bajan;
	}
	
	public int getParada(){
		return this.parada;
	}
	
	public int getDesplazamiento(){
		return this.desplazamiento;
	}
	
	public EstadoParada getEstado(){
		return this.estado;
	}
	
	public void setDesplazamiento(int desplazamiento){
		this.desplazamiento = desplazamiento;
	}
	
	public void setSuben(int suben){
		this.suben = suben;
	}
	
	public void setBajan(int bajan){
		this.bajan = bajan;
	}
	
	public void setEstado(EstadoParada estado){
		this.estado = estado;
	}
	
	public static int getNuevoIdentificador(){
		return ++nuevoIdentificador;
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
	
	@Override
	public String toString(){
		String ret = "";
		ret = "BusStop {Parada: " + this.parada + ", Suben: " + this.suben + ", Bajan: " + this.bajan + ", Estado: ";
		ret += this.estado + ", Desplazamiento: " + this.desplazamiento + ", Longitud: " + this.longitud + ", Latitud: ";
		ret += this.latitud + "}";
		return ret;
	}
}
