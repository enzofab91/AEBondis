package ec.app.BusStopRelocationProblem;

import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.BusStopRelocationProblem.SDTs.SDTDistancias;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.GeneVectorIndividual;
import ec.vector.GeneVectorSpecies;

public class BusStopRelocationProblem extends Problem implements SimpleProblemForm {

	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
		    final int threadnum)
			{
				if (ind.evaluated) return;

		        if (!(ind instanceof GeneVectorIndividual))
		        	state.output.fatal("ERROR: No es un vector de tipo GeneVectorIndividual!",null);
		        
		        GeneVectorIndividual ind2 = (GeneVectorIndividual)ind;
		        GeneVectorSpecies t_spe = (GeneVectorSpecies)ind2.species;
		        
		        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

		        //CALCULO EL FITNESS
		        double fitness1 = 0; //minimizar tiempo de viaje
		  	  	double fitness2 = 0; //maximizar ganancia
		  	  	
		  	  	BusProblemLine busLine;
		  	  	
		  	  	/* Obtengo constantes del problema */
		  	  	int cantidadLineas = t_spe.getCantidadLineas();
		  	  	int gananciaPorViaje = t_spe.getGananciaPorViaje(); 
		  	  	int costoCombustible = t_spe.getCostoCombustible();
		  	  	int costoSalario = t_spe.getCostoSalario();

		  	  	int stopTime = t_spe.getStopTime(); 
		  	  	int boardTime = t_spe.getBoardTime();
		  	  	int alightTime = t_spe.getAlightTime();
		  	  	int walkingSpeed = t_spe.getWalkingSpeed();
		  	  	
		  	  	BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
		  	  	String tipo_distancia = information.getTipoDistancia();
		  	  	
		  	  	double distancia, tiempoEntreParadas, velocidad;
		  	  	double dist_a_nueva_parada = 0;
		  	  	
		  	  	for (int i = 0; i < cantidadLineas; i++) {
		  	  		/* Para cada linea recorro sus paradas */
		  	  		busLine = (BusProblemLine)ind2.genome[i];
		  	  		List<BusStop> paradas = busLine.getParadas();
		  	  		List<SDTDistancias> distancias = t_spe.getTiempos().get(busLine.getLine());
		  	  		BusStop parada_anterior = null;
		  	  		
		  	  		for (int j = 0; j < paradas.size()-1 ; j++){
			  			/* Me aseguro que la parada existe */
		  	  			if (paradas.get(j).getEstado() != EstadoParada.ELIMINADA){
				  			/* Busco la siguiente parada que no este eliminada */
				  			int t = j + 1;
				  			BusStop j_esimaParada, t_esimaParada;
				  			while(t < paradas.size() && paradas.get(t).getEstado() == EstadoParada.ELIMINADA){
				  				t++;
				  			}
			  				
				  			if (t < paradas.size()){
					  			j_esimaParada = paradas.get(j);
				  				t_esimaParada = paradas.get(t);
				  				
				  				if (tipo_distancia.equals("Haversine")){
					  				distancia = Operaciones.calcularDistancia(j_esimaParada.getLatitud(),j_esimaParada.getLongitud(),
					  						t_esimaParada.getLatitud(),t_esimaParada.getLongitud());
					  				
					  				//La velocidad esta en km/h, se multiplica para pasar a m/s
					  				velocidad = (double)information.getVelocidadPromedio() * 3.6;
					  				tiempoEntreParadas = distancia / velocidad;
				  				} else {
				  					distancia = Operaciones.obtenerDistancia(distancias, j_esimaParada, t_esimaParada);
				  					velocidad = Operaciones.obtenerVelocidad(distancias, j_esimaParada, t_esimaParada);
				  					
				  					tiempoEntreParadas = distancia / velocidad;
				  				}
				  				
				  				//como la parada origen no se modifica, la primera vez no va a entrar aca.
				  				//la siguiente vez ya esta inicializada la parada anterior
				  				if (j_esimaParada.getParada() >= 10000){
				  					//es nueva, por lo tanto calculo la distancia a la mas cercana
				  					//no se puede usar la distancia real porque es nueva y no existe
			  						double dist_peaton_sig = Operaciones.calcularDistancia(j_esimaParada.getLatitud(), j_esimaParada.getLongitud(),
			  								t_esimaParada.getLatitud(), t_esimaParada.getLongitud());
			  						
			  						double dist_peaton_ant = Operaciones.calcularDistancia(parada_anterior.getLatitud(), parada_anterior.getLongitud(),
			  								j_esimaParada.getLatitud(), j_esimaParada.getLongitud());
			  						
			  						if (dist_peaton_ant > dist_peaton_sig)
			  							dist_a_nueva_parada = dist_peaton_ant;
			  						else
			  							dist_a_nueva_parada = dist_peaton_sig;
				  				}
					  			
				  				parada_anterior = j_esimaParada;
				  				
				  				// Fitness1: minimizar el tiempo de recorrido
				  				fitness1 += stopTime + (j_esimaParada.getSuben() * boardTime) + 
				  						(alightTime * j_esimaParada.getBajan()) + tiempoEntreParadas +
				  						((walkingSpeed * 3.6) * dist_a_nueva_parada); //walking speed de km/h a m/s
				  				
				  				// Fitness 2: maximizar la ganancia de la empresa (ganancia - costos)
				  				//el 10k es para que el fitness de un valor positivo
				  				fitness2 += 10000 * (j_esimaParada.getSuben() * gananciaPorViaje) - 
				  						(costoCombustible + costoSalario) * (distancia / 1000); //distancia de m a km
				  			}
		  	  			}
		  	  		}
		  	  	}
		  	  	//DebugFileLog.DebugFileLog("Fitness", "Fitness 1 = " + fitness1 + " fitness2 = " + fitness2);
		        objectives[0] = fitness1;
		        objectives[1] = fitness2;

		        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
		        ind.evaluated = true;
			}
	}
