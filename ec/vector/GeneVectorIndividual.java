/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;

import ec.*;
import ec.app.BusStopRelocationProblem.BusProblemInformation;
import ec.app.BusStopRelocationProblem.BusProblemLine;
import ec.app.BusStopRelocationProblem.BusStop;
import ec.app.BusStopRelocationProblem.EstadoParada;
import ec.app.BusStopRelocationProblem.utils.DebugFileLog;
import ec.util.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/*
 * GeneVectorIndividual.java
 * Created: Thu Mar 22 13:13:20 EST 2001
 */

/**
 * GeneVectorIndividual is a VectorIndividual whose genome is an array of Genes.
 * The default mutation method calls the mutate() method on each gene independently
 * with <tt>species.mutationProbability</tt>.  Initialization calls reset(), which
 * should call reset() on each gene.  Do not expect that the genes will actually
 * exist during initialization -- see the default implementation of reset() as an example
 * for how to handle this.
 *

 * <P><b>From ec.Individual:</b>  
 *
 * <p>In addition to serialization for checkpointing, Individuals may read and write themselves to streams in three ways.
 *
 * <ul>
 * <li><b>writeIndividual(...,DataOutput)/readIndividual(...,DataInput)</b>&nbsp;&nbsp;&nbsp;This method
 * transmits or receives an individual in binary.  It is the most efficient approach to sending
 * individuals over networks, etc.  These methods write the evaluated flag and the fitness, then
 * call <b>readGenotype/writeGenotype</b>, which you must implement to write those parts of your 
 * Individual special to your functions-- the default versions of readGenotype/writeGenotype throw errors.
 * You don't need to implement them if you don't plan on using read/writeIndividual.
 *
 * <li><b>printIndividual(...,PrintWriter)/readIndividual(...,LineNumberReader)</b>&nbsp;&nbsp;&nbsp;This
 * approach transmits or receives an indivdual in text encoded such that the individual is largely readable
 * by humans but can be read back in 100% by ECJ as well.  To do this, these methods will encode numbers
 * using the <tt>ec.util.Code</tt> class.  These methods are mostly used to write out populations to
 * files for inspection, slight modification, then reading back in later on.  <b>readIndividual</b> reads
 * in the fitness and the evaluation flag, then calls <b>parseGenotype</b> to read in the remaining individual.
 * You are responsible for implementing parseGenotype: the Code class is there to help you.
 * <b>printIndividual</b> writes out the fitness and evaluation flag, then calls <b>genotypeToString</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToString method in such
 * a way that parseGenotype can read back in the individual println'd with genotypeToString.  The default form
 * of genotypeToString simply calls <b>toString</b>, which you may override instead if you like.  The default
 * form of <b>parseGenotype</b> throws an error.  You are not required to implement these methods, but without
 * them you will not be able to write individuals to files in a simultaneously computer- and human-readable fashion.
 *
 * <li><b>printIndividualForHumans(...,PrintWriter)</b>&nbsp;&nbsp;&nbsp;This
 * approach prints an individual in a fashion intended for human consumption only.
 * <b>printIndividualForHumans</b> writes out the fitness and evaluation flag, then calls <b>genotypeToStringForHumans</b> 
 * and printlns the resultant string. You are responsible for implementing the genotypeToStringForHumans method.
 * The default form of genotypeToStringForHumans simply calls <b>toString</b>, which you may override instead if you like
 * (though note that genotypeToString's default also calls toString).  You should handle one of these methods properly
 * to ensure individuals can be printed by ECJ.
 * </ul>

 * <p>In general, the various readers and writers do three things: they tell the Fitness to read/write itself,
 * they read/write the evaluated flag, and they read/write the gene array.  If you add instance variables to
 * a VectorIndividual or subclass, you'll need to read/write those variables as well.
 <p><b>Default Base</b><br>
 vector.gene-vect-ind

 * @author Sean Luke
 * @version 1.0
 */

public class GeneVectorIndividual extends VectorIndividual
    {
    public static final String P_GENEVECTORINDIVIDUAL = "gene-vect-ind";
    public Gene[] genome;
    MersenneTwister mt = new MersenneTwister();
    
    public Parameter defaultBase()
        {
        return VectorDefaults.base().push(P_GENEVECTORINDIVIDUAL);
        }

    public Object clone()
        {
        GeneVectorIndividual myobj = (GeneVectorIndividual) (super.clone());

        // must clone the genome
        myobj.genome = (Gene[])(genome.clone());
        for(int x=0;x<genome.length;x++)
            myobj.genome[x] = (Gene)(genome[x].clone());
        
        return myobj;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);  // actually unnecessary (Individual.setup() is empty)

        // since VectorSpecies set its constraint values BEFORE it called
        // super.setup(...) [which in turn called our setup(...)], we know that
        // stuff like genomeSize has already been set...
        
        Parameter def = defaultBase();
        
        if (!(species instanceof GeneVectorSpecies)) 
            state.output.fatal("GeneVectorIndividual requires a GeneVectorSpecies", base, def);
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        
        // note that genome isn't initialized with any genes yet -- they're all null.
        // reset() needs
        genome = new Gene[s.genomeSize];
        reset(state,0);
        }
        
    public void defaultCrossover(EvolutionState state, int thread, VectorIndividual ind)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        GeneVectorIndividual i = (GeneVectorIndividual) ind;
        Gene tmp;
        int point;

        int len = Math.min(genome.length, i.genome.length);
        if (len != genome.length || len != i.genome.length)
            state.output.warnOnce("Genome lengths are not the same.  Vector crossover will only be done in overlapping region.");
        
        BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
        String crossover = information.getTipoCrossover();

        if (!crossover.equals("Custom")){
	        switch(s.crossoverType)
	            {
	            case VectorSpecies.C_ONE_POINT:
	//                point = state.random[thread].nextInt((len / s.chunksize)+1);
	                // we want to go from 0 ... len-1 
	                // so that there is only ONE case of NO-OP crossover, not TWO
	                point = state.random[thread].nextInt((len / s.chunksize));
	                for(int x=0;x<point*s.chunksize;x++)
	                    { 
	                    tmp = i.genome[x];
	                    i.genome[x] = genome[x]; 
	                    genome[x] = tmp; 
	                    }
	                break;
	            case VectorSpecies.C_ONE_POINT_NO_NOP:
	                point = state.random[thread].nextInt((len / s.chunksize) - 1) + 1;  // so it goes from 1 .. len-1
	                for(int x=0;x<point*s.chunksize;x++)
	                    { 
	                    tmp = i.genome[x];
	                    i.genome[x] = genome[x]; 
	                    genome[x] = tmp; 
	                    }
	                break;
	            case VectorSpecies.C_TWO_POINT: 
	            {
	//                int point0 = state.random[thread].nextInt((len / s.chunksize)+1);
	//                point = state.random[thread].nextInt((len / s.chunksize)+1);
	            // we want to go from 0 to len-1
	            // so that the only NO-OP crossover possible is point == point0
	            // example; len = 4
	            // possibilities: a=0 b=0       NOP                             [0123]
	            //                                a=0 b=1       swap 0                  [for 1, 2, 3]
	            //                                a=0 b=2       swap 0, 1               [for 2, 3]
	            //                                a=0 b=3       swap 0, 1, 2    [for 3]
	            //                                a=1 b=1       NOP                             [1230]
	            //                                a=1 b=2       swap 1                  [for 2, 3, 0]
	            //                                a=1 b=3       swap 1, 2               [for 3, 0]
	            //                                a=2 b=2       NOP                             [2301]
	            //                                a=2 b=3       swap 2                  [for 3, 0, 1]
	            //                                a=3 b=3   NOP                         [3012]
	            // All intervals: 0, 01, 012, 0123, 1, 12, 123, 1230, 2, 23, 230, 2301, 3, 30, 301, 3012
	            point = state.random[thread].nextInt((len / s.chunksize));
	            int point0 = state.random[thread].nextInt((len / s.chunksize));
	            if (point0 > point) { int p = point0; point0 = point; point = p; }
	            for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
	                {
	                tmp = i.genome[x];
	                i.genome[x] = genome[x];
	                genome[x] = tmp;
	                }
	            }
	            break;
	            case VectorSpecies.C_TWO_POINT_NO_NOP: 
	            {
	            point = state.random[thread].nextInt((len / s.chunksize));
	            int point0 = 0;
	            do { point0 = state.random[thread].nextInt((len / s.chunksize)); }
	            while (point0 == point);  // NOP
	            if (point0 > point) { int p = point0; point0 = point; point = p; }
	            for(int x=point0*s.chunksize;x<point*s.chunksize;x++)
	                {
	                tmp = i.genome[x];
	                i.genome[x] = genome[x];
	                genome[x] = tmp;
	                }
	            }
	            break;
	            case VectorSpecies.C_ANY_POINT:
	                for(int x=0;x<len/s.chunksize;x++) 
	                    if (state.random[thread].nextBoolean(s.crossoverProbability))
	                        for(int y=x*s.chunksize;y<(x+1)*s.chunksize;y++)
	                            {
	                            tmp = i.genome[y];
	                            i.genome[y] = genome[y];
	                            genome[y] = tmp;
	                            }
	                break;
	            }
        } else {
        	//customCrossover(state, thread, ind);
        	customSPCrossoverParadas(state, thread, ind);
        }}
    
    public void customCrossover(EvolutionState state, int thread, VectorIndividual ind1){
    	GeneVectorSpecies spe = (GeneVectorSpecies) species;
        GeneVectorIndividual ind2 = (GeneVectorIndividual) ind1;
		
		int cantidadLineas = spe.getCantidadLineas();
		int parada, iter, cantidadParadas;
		boolean encontre;
        
		for(int i = 0; i < cantidadLineas; i++){
			parada = 0;
			BusStop busStop1 = null;
			BusStop busStop2 = null;
			
			encontre = false;
			iter = 0;
			
			//obtengo la info de las lineas de ambos individuos a cruzar
			BusProblemLine linea1 = (BusProblemLine)genome[i];
			BusProblemLine linea2 = (BusProblemLine)ind2.genome[i];
			
			List<BusStop> paradas = linea1.getParadas();
			cantidadParadas = paradas.size();
			
			while(!encontre && iter < cantidadParadas){
				parada= linea1.getStop(iter).getParada();
				busStop2 = linea2.checkBusStopInLine(parada);
				
				if (busStop2 != null){
					encontre = true;
					busStop1 = linea1.checkBusStopInLine(parada);
				}
				
				iter++;
			}
			
			if(encontre){
				/* Se suman las cantidades de las personas que suben y bajan en esa	*/
				/* parada para la linea y se agregan a la parada del individuo 1	*/
				
				if (linea1.getAsientosDisponibles() >= busStop2.getSuben() - busStop2.getBajan()){
					/* hay lugar en la linea, actualizo la parada */
					busStop1.setSuben(busStop1.getSuben() + busStop2.getSuben());
					busStop1.setBajan(busStop1.getBajan() + busStop2.getBajan());
					linea1.setAsientosDisponibles(linea1.getAsientosDisponibles() - busStop2.getSuben() + busStop2.getBajan());
				}
			}
		}
    }
    
    public void customSPCrossoverParadas(EvolutionState state, int thread, VectorIndividual ind1){
    	GeneVectorSpecies spe = (GeneVectorSpecies) species;
        GeneVectorIndividual ind2 = (GeneVectorIndividual) ind1;
		
		int cantidadLineas = spe.getCantidadLineas();
		int cantidadParadas, puntoCorte;
		
		for(int i = 0; i < cantidadLineas; i++){
			//obtengo la info de la linea a cruzar, y el punto de corte al azar
			BusProblemLine linea1 = (BusProblemLine)genome[i];
			BusProblemLine linea2 = (BusProblemLine)ind2.genome[i];
	        
			List<BusStop> paradas1 = linea1.getParadas();
			List<BusStop> paradas2 = linea2.getParadas();

			cantidadParadas = paradas1.size();
			
			List<BusStop> nuevasParadasLinea1 = new LinkedList<BusStop>();
			List<BusStop> nuevasParadasLinea2 = new LinkedList<BusStop>();
			
			puntoCorte = mt.nextInt(cantidadParadas);
			
			// me aseguro que la linea 2 tenga al menos esa cantidad de paradas
			if (puntoCorte < paradas2.size()){
				// obtengo las paradas hasta el punto de corte primero,
				// y luego completo el resto de las paradas de cada solucion
				for (int iter = 0; iter <= puntoCorte; iter++){
					nuevasParadasLinea1.add(paradas2.get(iter));
					nuevasParadasLinea2.add(paradas1.get(iter));
				}
				
				// completo con las paradas de la solucion 1 luego del corte
				for (int iter = puntoCorte + 1; iter < paradas1.size(); iter++){
					nuevasParadasLinea1.add(paradas1.get(iter));
				}
				
				// completo con las paradas de la solucion 2 luego del corte
				for (int iter = puntoCorte + 1; iter < paradas2.size(); iter++){
					nuevasParadasLinea2.add(paradas2.get(iter));
				}
				
				//chequeo que no exceda la capacidad maxima con el cruzamiento
				//if (chequearCapacidad(linea1) && chequearCapacidad(linea2)){
				//no se controla mas, porque el maximo es tomando en cuenta una unica linea
				//y el algoritmo trabaja con franjas horarias, es decir muchas lineas
					linea1.setParadas(nuevasParadasLinea1);
					linea2.setParadas(nuevasParadasLinea2);
				//}
			}
		}
    }
    
    private boolean chequearCapacidad(BusProblemLine linea){
    	boolean lineaValida = false;
    	
    	BusProblemInformation information = BusProblemInformation.getBusProblemInformation();
    	int capacidadMaxima = information.getCantidadMaximaPasajeros();
    	
    	List<BusStop> paradas = linea.getParadas();
    	
    	int capacidadActual = 0;
    	for (int i = 0; i < paradas.size()-1 ; i++){
    		if (paradas.get(i).getEstado() != EstadoParada.ELIMINADA){
    			capacidadActual -= paradas.get(i).getBajan();
    			capacidadActual += paradas.get(i).getSuben();
    		}
    	}
    	
    	if (capacidadActual <= capacidadMaxima)
    		lineaValida = true;
    	
    	return lineaValida;
    }
    
    /** Splits the genome into n pieces, according to points, which *must* be sorted. 
        pieces.length must be 1 + points.length */
    public void split(int[] points, Object[] pieces)
        {
        int point0, point1;
        point0 = 0; point1 = points[0];
        for(int x=0;x<pieces.length;x++)
            {
            pieces[x] = new Gene[point1-point0];
            System.arraycopy(genome,point0,pieces[x],0,point1-point0);
            point0 = point1;
            if (x >=pieces.length-2)
                point1 = genome.length;
            else point1 = points[x+1];
            }
        }
    
    /** Joins the n pieces and sets the genome to their concatenation.*/
    public void join(Object[] pieces)
        {
        int sum=0;
        for(int x=0;x<pieces.length;x++)
            sum += ((Gene[])(pieces[x])).length;
        
        int runningsum = 0;
        Gene[] newgenome = new Gene[sum];
        for(int x=0;x<pieces.length;x++)
            {
            System.arraycopy(pieces[x], 0, newgenome, runningsum, ((Gene[])(pieces[x])).length);
            runningsum += ((Gene[])(pieces[x])).length;
            }
        // set genome
        genome = newgenome;
        }

    /** Destructively mutates the individual in some default manner.  The default form
        simply randomizes genes to a uniform distribution from the min and max of the gene values. */
    public void defaultMutate(EvolutionState state, int thread)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        
        for(int x=0;x<genome.length;x++)
            {
            if (state.random[thread].nextBoolean(s.mutationProbability(x)))
                {
                if (s.duplicateRetries(x) <= 0)  // a little optimization
                    {
                    genome[x].mutate(state,thread);
                    }
                else    // argh
                    {
                    Gene old = (Gene)(genome[x].clone());
                    for(int retries = 0; retries < s.duplicateRetries(x) + 1; retries++)
                        {
                        genome[x].mutate(state,thread);
                        if (!genome[x].equals(old)) break;
                        else genome[x] = old;  // try again.  Note that we're copying back just in case.
                        }
                        
                    }
                }
            }
        }

    /** Initializes the individual by calling reset(...) on each gene. */
    public void reset(EvolutionState state, int thread)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;

        for(int x=0;x<genome.length;x++)
            {
            // first create the gene if it doesn't exist
            if (genome[x]==null) genome[x] = (Gene)(s.genePrototype.clone());
            // now reset it
            genome[x].reset(state,thread);
            }

        }

    public int hashCode()
        {
        // stolen from GPIndividual.  It's a decent algorithm.
        int hash = this.getClass().hashCode();

        for(int x=0;x<genome.length;x++)
            hash = ( hash << 1 | hash >>> 31 ) ^ genome[x].hashCode();

        return hash;
        }

    public String genotypeToStringForHumans()
        {
        StringBuilder s = new StringBuilder();
        for( int i = 0 ; i < genome.length ; i++ )
            { if (i > 0) s.append(" "); s.append(genome[i].printGeneToStringForHumans()); }
        return s.toString();
        }
        
    public String genotypeToString()
        {
        StringBuilder s = new StringBuilder();
        for( int i = 0 ; i < genome.length ; i++ )
            { s.append(" "); s.append(genome[i].printGeneToString()); }
        return s.toString();
        }

    protected void parseGenotype(final EvolutionState state,
        final LineNumberReader reader) throws IOException
        {
        // read in the next line.  The first item is the number of genes
        String s = reader.readLine();
        DecodeReturn d = new DecodeReturn(s);
        Code.decode( d );
        if (d.type != DecodeReturn.T_INTEGER)  // uh oh
            state.output.fatal("Individual with genome:\n" + s + "\n... does not have an integer at the beginning indicating the genome count.");
        int lll = (int)(d.l);

        genome = new Gene[ lll ];

        GeneVectorSpecies _species = (GeneVectorSpecies) species;
        for( int i = 0 ; i < genome.length ; i++ )
            {
            genome[i] = (Gene)(_species.genePrototype.clone());
            genome[i].readGene(state,reader);
            }
        }

    public boolean equals(Object ind)
        {
        if (ind == null) return false;
        if (!(this.getClass().equals(ind.getClass()))) return false;
        GeneVectorIndividual i = (GeneVectorIndividual)ind;
        if( genome.length != i.genome.length )
            return false;
        for( int j = 0 ; j < genome.length ; j++ )
            if( !(genome[j].equals(i.genome[j])))
                return false;
        return true;
        }

    public Object getGenome()
        { return genome; }
    public void setGenome(Object gen)
        { genome = (Gene[]) gen; }
    public int genomeLength()
        { return genome.length; }

    // clone all the genes
    public void cloneGenes(Object piece)
        {
        Gene[] genes = (Gene[]) piece;
        for(int i = 0 ; i < genes.length; i++)
            {
            if (genes[i] != null) genes[i] = (Gene)(genes[i].clone());
            }
        }
    
    public void writeGenotype(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(genome.length);
        for(int x=0;x<genome.length;x++)
            genome[x].writeGene(state,dataOutput);
        }

    public void setGenomeLength(int len)
        {
        GeneVectorSpecies s = (GeneVectorSpecies) species;
        Gene[] newGenome = new Gene[len];
        System.arraycopy(genome, 0, newGenome, 0, 
            genome.length < newGenome.length ? genome.length : newGenome.length);
        for(int x=genome.length; x< newGenome.length; x++)
            newGenome[x] = (Gene)(s.genePrototype.clone());  // not reset
        genome = newGenome;
        }

    public void readGenotype(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int len = dataInput.readInt();
        if (genome==null || genome.length != len)
            genome = new Gene[len];
        GeneVectorSpecies _species = (GeneVectorSpecies) species;

        for(int x=0;x<genome.length;x++)
            {
            genome[x] = (Gene)(_species.genePrototype.clone());
            genome[x].readGene(state,dataInput);
            }
        }

    }
