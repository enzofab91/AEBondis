package ec.app.BusStopRelocationProblem.SDTs;

public class SDTDistancias {
	private int parada_1;
	private int parada_2;
	private double distancia;
	private double velocidad;
	
	public SDTDistancias(int parada_1, int parada_2, double distancia, double velocidad){
		this.parada_1 = parada_1;
		this.parada_2 = parada_2;
		this.distancia = distancia;
		this.velocidad = velocidad;
	}
	
	public int getParada1() {
		return parada_1;
	}
	
	public int getParada2() {
		return parada_2;
	}
	
	public double getDistancia() {
		return distancia;
	}
	
	public double getVelocidad() {
		return velocidad;
	}
	
	@Override
	public String toString(){
		return "{Parada 1: " + parada_1 + ",Parada 2: " + parada_2 + ",Distancia: " + distancia + ",Velocidad: " + velocidad + "}";
	}
}
