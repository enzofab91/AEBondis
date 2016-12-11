package ec.app.BusStopRelocationProblem.SDTs;

public class SDTCoordenadas {
	private double latitud;
	private double longitud;
	
	public SDTCoordenadas(double latitud, double longitud){
		this.latitud = latitud;
		this.longitud = longitud;
	}
	
	public double getLatitud() {
		return latitud;
	}
	
	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}
	
	public double getLogitud() {
		return longitud;
	}
	
	public void setLogitud(double logitud) {
		this.longitud = logitud;
	}
}
