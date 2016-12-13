package ec.app.BusStopRelocationProblem;

public class CalcularDistancia {
	public static double calcularDistancia(double lat1, double lon1, double lat2, double lon2){
		double distancia;
		
		final int R = 6371; //radius of the earth
		
		// convert decimal degrees to radians 
	    //haversine formula 
	    double dlon = lon2 - lon1;
	    double dlat = lat2 - lat1;
	    
	    double a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
	    double c = 2 * Math.asin(Math.sqrt(a));
	    
	    distancia = R * c * 1000;
	    
	    //se devuelven metros
	    return distancia;
	}
}
