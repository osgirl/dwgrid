/*****************************************************************************
 * 
 *  DishwasherLoad
 * 
 *  Class aggregating multiple Dishwasher objects for dwgrid simulation
 * 
 *  Copyright (c) Trevor Marshall 2011
 *  
 *  This file is part of dwgrid.
 *
 *  dwgrid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  dwgrid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with dwgrid.  If not, see <http://www.gnu.org/licenses/>.
 *  
 ****************************************************************************/
package net.trevorm.simulation.dwgrid;

import java.util.Random;

/**
 * A class to aggregate multiple dishwashers as a load on the grid
 * 
 * @author trevorm
 *
 */
public class DishwasherLoad {

	public int numWashers;			// The number of dishwashers active
	protected Dishwasher   ds[];	// some dishwashers..
	protected float	loads[];		// some loads
	protected float pcOnLoad;		// percentage of dishwashers on heating load.
	protected float pcDelay;		// percentage of dishwahsers delayed
	protected double maxtotdelay;	// some measure of the delay...
	protected float pcEco = 40;		// default percentage of dishwashers running an 'Eco' programme
	protected float randPc = 0.2F;	// default randomisation: 0.2 = 20% ie., +10/-10
		
	/**
	 * Constructor for DishwasherLoad class
	 * 
	 * @param initNumWashers	number of Dishwashers in 'fleet'
	 * @param ecoPc				percentage running an 'Eco' programme
	 */
	public DishwasherLoad(int numWashers, float ecoPc) {
		// Set the percentage running the 'eco' programme
		pcEco = ecoPc;
		long seed = 987654321L;		// repeatable seed for Random generator...
		Random rngen = new Random(seed);
		this.numWashers = numWashers;
		ds = new Dishwasher[numWashers];
		loads = new float[numWashers];
		for (int n = 0; n < numWashers; n++) {
			int prog[];
			// Assign "Eco" or Standard programme
			if ((rngen.nextDouble() * 100) < pcEco) {	// Should this one run an 'Eco' programme?
				// Yes, set the programme accordingly
				// "Eco" 50C Programme
				// 	Cold pre-wash
				//	Main wash at up to 50C
				//	Cold rinse
				//  Hot rinse
				//	Drying
				// (Modelled on Electrolux ESL 6115 using data from CurrentCost sensor)
				prog = new int[]{23*60, 100, 
						14 * 60, 2200, 
						28 *60, 100, 
						15 * 60, 2200, 
						15 * 60, 0};
			} else {
				// No, run the 'standard' programme
				// Standard 65C wash programme
				//	Cold pre-wash
				// 	Main wash at up to 65C
				//	2 warm rinses
				// 	1 hot rinse
				//	Drying
				// (Modelled on Electrolux ESL 6115 using data from CurrentCost sensor)
				prog = new int[]{10*60, 100,
			              12*60, 2200,
			              4*60, 100,
			              5*60, 2200,
			              5*60, 100,
			              4*60, 2200,
			              3*60, 100,
			              4*60, 2200,
			              3*60, 100,
			              15*60, 2200,
			              2*60, 100,
			              1*60, 2200,
			              2*60, 100,
			              1*60, 2200,
			              2*60, 100,
			              1*60, 2200,
			              15*60, 0};
			}
			                
			ds[n] = new PropFreqRandomDelay(prog, randPc);
			ds[n].setRuntime(rngen.nextDouble() * ds[n].totruntime);
		}
	}
	
	
	/**
	 * method to calculate the total load of the 'fleet'
	 * 
	 * @param dT		simulation step time
	 * @param freq		grid frequency
	 * @return			total load of 'fleet'
	 */
	public double calcLoad(double dT, double freq) {
		double load = 0;
		int numOnLoad = 0;
		int numDelay  = 0;
		maxtotdelay = 0;
		pcOnLoad = 0F;		// keep track of number of machines on heating load for display
		for (int n = 0; n < numWashers; n++) {
			loads[n] = ds[n].runProgramme(freq, dT);
			load += loads[n];
			if (loads[n] > 200.0) {		// Allow for random element..
				numOnLoad++;
			}
			if (ds[n].totdelaytime > maxtotdelay) {
				maxtotdelay = ds[n].totdelaytime;
			}
			if( ds[n].delay) {
				numDelay++;
			}
		}
		pcOnLoad = 100 * ((float)numOnLoad / (float)numWashers);
		pcDelay  = 100 * ((float)numDelay / (float)numWashers);
		return load;		
	}
	
	/**
	 * set the turn-off frequency of the 'fleet'
	 * @param f		turn-off frequency
	 */
	public void setTurnOffFreq(float f) {
		for (int n = 0; n < numWashers; n++) {
			ds[n].setTurnOffFreq(f);
		}
	}
	
	/**
	 * set the turn-on frequency of the 'fleet'
	 * @param f		turn-on frequency
	 */
	public void setTurnOnFreq(float f) {
		for (int n = 0; n < numWashers; n++) {
			ds[n].setTurnOnFreq(f);
		}
	}
	
	/**
	 * main entry for testing only
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Arbitrary 1000 dishwashers
		int numdws = 1000;
		// Arbitrary 40% using 'eco' programme
		float pcEco = 40;
		// Create a load of dishwashers, default percentage running an 'eco' programme
		DishwasherLoad dl = new DishwasherLoad(numdws, pcEco);
		
		double dT = 0.1;		// Simulate at 10Hz
		double t  = 0;			// initialise time
		double load = 0;		// initialise the load
		double freq = 50.0;		// default 50Hz (so no dynamic demand) 
		
		while (t < 12000) {
			load = dl.calcLoad(dT, freq);
			System.out.println(t + " " + load + " " + dl.pcOnLoad);
			t += dT;
		}
	}
}
