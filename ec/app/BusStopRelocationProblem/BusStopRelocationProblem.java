package ec.app.BusStopRelocationProblem;

import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
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

		        //IntegerVectorIndividual ind2 = (IntegerVectorIndividual)ind;
		        GeneVectorIndividual ind2 = (GeneVectorIndividual)ind;
		        GeneVectorSpecies t_spe = (GeneVectorSpecies)ind2.species;
		        
		        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();
		        
		        //CALCULO EL FITNESS
		        double fitness1 = 0; //minimizar tiempo de viaje
		  	  	double fitness2 = 0; //maximizar ganancia
		  	  	
		  	  	BusProblemLine busLine;
		  	  	
		  	  	DebugFileLog.DebugFileLog("pruebaFitness", "entro");
		  	  	
		  	  	/* Obtengo constantes del problema */
		  	  	int cantidadLineas = t_spe.getCantidadLineas();
		  	  	int gananciaPorViaje = t_spe.getGananciaPorViaje(); 
		  	  	int costoCombustible = t_spe.getCostoCombustible();
		  	  	int costoSalario = t_spe.getCostoSalario();
		  	  	
		  	  	int stopTime = t_spe.getStopTime(); 
		  	  	int boardTime = t_spe.getBoardTime();
		  	  	int alightTime = t_spe.getAlightTime();
		  	  	int walkingSpeed = t_spe.getWalkingSpeed();

		  	  	for (int i = 0; i < cantidadLineas; i++) {
		  	  		/* Para cada linea recorro sus paradas */
		  	  		busLine = (BusProblemLine)ind2.genome[i];
		  	  		
		  	  		List<BusStop> paradas = busLine.getParadas();
		  	  		
		  	  		
		  	  		DebugFileLog.DebugFileLog("pruebaFitness", "LINEA = " + busLine.toString());
		  	  		for (int j = 0; j < paradas.size()-1 ; j++){
		  	  			DebugFileLog.DebugFileLog("pruebaFitness", "parada(" + Integer.toString(j) + ") = " + paradas.get(j).toString());
			  			/* Me aseguro que la parada existe */
			  			/* Busco la siguiente parada */
			  			int t = j + 1;
			  			BusStop j_esimaParada;
			  			while(t < paradas.size() && paradas.get(t).getEstado() != EstadoParada.ELIMINADA){
			  				j_esimaParada = paradas.get(j);
			  				
			  				double distancia = Operaciones.calcularDistancia(j_esimaParada.getLatitud(),paradas.get(j).getLongitud(),
			  						j_esimaParada.getLatitud(),paradas.get(t).getLongitud());
			  				
			  				double tiempoEntreParadas = Operaciones.calcularDistancia(j_esimaParada.getLatitud(),j_esimaParada.getLongitud(),
			  						j_esimaParada.getLatitud(),j_esimaParada.getLongitud()); // / 12.5) /// 60;
			  				
			  				// Fitness1: minimizar el tiempo de recorrido
			  				fitness1 += stopTime + (j_esimaParada.getSuben() * boardTime) + tiempoEntreParadas;
			  				
			  				// Fitness 2: maximizar la ganancia de la empresa (ganancia - costos)
			  				fitness2 += (j_esimaParada.getSuben() * gananciaPorViaje) - 
			  						(costoCombustible + costoSalario) * distancia/1000;
			  				
			  				t++;
			  			}
		  	  		}
		  	  	}
		  	  	
		        /* Asigno el fitness al individuo */
		        /*if (!(ind2.fitness instanceof SimpleFitness))
		            state.output.fatal("Error. No es un SimpleFitness",null);
		        ((SimpleFitness)ind2.fitness).setFitness(state,fitness*(-1), fitness == 0);*/
		  	  	
		        objectives[0] = (-1)*fitness1;
		        objectives[0] = fitness2;
		        
		        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
		        ind.evaluated = true;
			}
	}
