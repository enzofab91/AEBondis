package ec.app.BusStopRelocationProblem;

import java.util.List;

import ec.app.BusStopRelocationProblem.SDTs.SDTDistancias;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.app.BusStopRelocationProblem.utils.Parametros;

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
	
	public static double obtenerDistancia(List<SDTDistancias> distancias, BusStop parada_1, BusStop parada_2){
		double distancia = 0;
		
		if ((parada_1.getParada() >= 10000) || (parada_2.getParada() >= 10000)){
			/* son paradas nuevas, por lo que no hay datos, se calcula con la distancia haversine */
			distancia = calcularDistancia(parada_1.getLatitud(),parada_1.getLongitud(),
					parada_2.getLatitud(),parada_2.getLongitud());
		} else {
			int cantParadas = distancias.size();
			int iter = 0;
			boolean encontre = false;
			
			SDTDistancias parada;
			while ((iter < cantParadas) && !encontre){
				parada = distancias.get(iter);
				if ((parada.getParada1() == parada_1.getParada()) || 
					(parada.getParada2() == parada_2.getParada())){
					distancia = parada.getDistancia();
					encontre = true;
				}
				iter++;
			}
		}
		
		return distancia;
	}
	
	public static double obtenerVelocidad(List<SDTDistancias> distancias, BusStop parada_1, BusStop parada_2){
		double velocidad = 0;
		
		if ((parada_1.getParada() >= 10000) || (parada_2.getParada() >= 10000)){
			/* son paradas nuevas, por lo que no hay datos, se toma una promedio */
			velocidad = (double)Parametros.getParameterInt("VelocidadPromedio") / 3.6; //se divide para pasar de km/h a m/s
		} else {
			int cantParadas = distancias.size();
			int iter = 0;
			boolean encontre = false;
			
			SDTDistancias parada;
			while ((iter < cantParadas) && !encontre){
				parada = distancias.get(iter);
				if ((parada.getParada1() == parada_1.getParada()) || 
					(parada.getParada2() == parada_2.getParada())){
					velocidad = parada.getVelocidad();
					encontre = true;
				}
				iter++;
			}
		}
		
		return velocidad;
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
