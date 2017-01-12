package ec.app.BusStopRelocationProblem;

import ec.app.BusStopRelocationProblem.utils.DebugFileLog;

public class Operaciones {
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
	
	public static double[] puntoMedio(double lat1,double lon1,double lat2,double lon2){
		/* Devuelve el punto medio a partir de dos coordenadas geograficas */
		/* http://stackoverflow.com/questions/4656802/midpoint-between-two-latitude-and-longitude */
	    double[] coordinates = new double[2];
	    
		double dLon = Math.toRadians(lon2 - lon1);

	    //decimal degrees to radians
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    lon1 = Math.toRadians(lon1);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double nueva_longitud = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double nueva_latitud = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
	    
	    coordinates[0] = nueva_longitud;
	    coordinates[1] = nueva_latitud;
	    
	    return coordinates;
	}
	
	public static double[] nuevaUbicacion(double lat,double lon, int desplazamiento){
		/* http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters */
		double[] coordinates = new double[2];
		
		//Earth’s radius, sphere
		final int R = 6378137;
	
		//Coordinate offsets in radians
		double dLat = (desplazamiento*100)/R;
		double dLon = (desplazamiento*100)/(R*Math.cos(Math.PI*lat/180));
	
		//OffsetPosition, decimal degrees
		double nueva_longitud = lat + dLat * 180/Math.PI;
		double nueva_latitud = lon + dLon * 180/Math.PI;
		
		coordinates[0] = nueva_longitud;
	    coordinates[1] = nueva_latitud;
	    
	    return coordinates;
	}
}
