/*****************************************************************************
 * 
 *  Grid
 * 
 *  Class modelling grid effects including released demand and 
 *  inertia effects and calculating grid frequency for dwgrid simulation
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

/**
 * Class Grid encapsulates parameters and methods necessary for 
 * modelling a power grid.
 *  
 * This class is part of the dishwasher and grid simulation package
 * 
 * @author trevorm
 * 
 * Copyright 2011 Trevor Marshall
 *
 */
public class Grid {
	
	private double I;				// Moment of inertia for the whole grid
	public  double deltaf;			// Frequency delta
	
	private static double H = 4.0;	// Inertial constant, H - the number of full-output
	                                // seconds of energy stored (at nominal frequency)
	
	/**
	 * Constructor for a Grid object
	 * 
	 * Calculates the moment of inertia, I, for the Grid
	 * @param Pgmax	Grid maximum power
	 * @param H		Inertial constant
	 * @param Fnom	Grid nominal frequency
	 */
	public Grid(double Pgmax, double H, double Fnom) {
		// convert Hz to radians per second
		double omega = 2 * Math.PI * Fnom;
		// Calculate I
		I = (2 * Pgmax * H) / (omega * omega);
	}
	
	/**
	 * Method for calculating released demand
	 * 
	 * As grid frequency reduces then rotating machines will
	 * slow down and so consume less power.  The released
	 * power is calculated and treated as additional power
	 * added to the grid
	 * 
	 * @param Pl	load
	 * @param Freq	current frequency
	 * @param Fnom	nominal frequency
	 * @return released power
	 */
	public double getReleasedPower(double Pl, double Freq, double Fnom){
		// The damping constant is assumed to be 1.0 but as
		// more loads are connected to the grid through 
		// electronic controllers then the damping constant
		// may need to be reduced.  1.0 is OK for now.
		double DampingConst = 1.0; 

		return -DampingConst * Pl * ((Freq - Fnom)/Fnom);
	}
	
	/**
	 * method to calculate the new grid frequency
	 * 
	 * @param f		current frequency
	 * @param Ps	accelerating power
	 * @param dT	time step
	 * @return new frequency
	 */
	public double getNewFreq(double f, double Ps, double dT) {
		double omega = 2 * Math.PI * f;
				
		omega = Math.sqrt(omega * omega + ( 2 * Ps * dT)/I );
		return omega / (2 * Math.PI);
	}
	
	/**
	 * Method to get the current surplus of power (which 
	 * can be negative).  This is the power accelerating or
	 * slowing the rotating bodies of the grid
	 * 
	 * @param Pg	Generated power, sum of all generated power on the grid
	 * @param Pr	Released power, power released as a result of frequency change
	 * @param Pl	Load
	 * @return surplus power
	 */
	public double getSurplus(double Pg, double Pr, double Pl){
		return Pg + Pr - Pl;
	}
	
	/**
	 * Main method for stand-alone testing Grid and Generator classes.
	 * This is not part of the normal simulation code.
	 * @param args
	 */
	public static void main(String[] args) {
		// simulate the grid....
		
		double freq = 50.0;					// base frequency
		double Pl = 30000000000.0;			// 30GW load
		double baseload = 30000000000.0;	// 30GW base load
		double dT   = 0.1;					// simulate at 0.1s intervals
		double t    = -200;					// accumulated time - start at -200 s
		
		double Pbase, Psp, Pg, Pr, Ps;		// Power components
		
		// Instantiate a grid with a total capacity of 31GW and running at 50Hz
		Grid grid = new Grid(31000000000.0, H, 50.0);
		
		// Base load generator output - assume that it's flat out.
		// 4% droop, gain of 0.3 ...
		Generator baseg = new Generator(30000000000.0, 52.0, 50.0, 4.0, 0.00667, 30000000000.0);
		
		// Spinning reserve, not initially generating
		// Capacity of 3300MW, that 5 660MW generators aggregated
		// 4% droop
		// gain of 0.3
		Generator sprsv = new Generator(3300000000.0, 50.0, 50.0, 4.0, 0.3); 
	
		for (int i = -2000; i < 40000; i++) {
			t += dT;
			// Power from the base load generation
			Pbase = baseg.getCurrPower(freq, dT);
			// Power from the spinning reserve
			Psp = sprsv.getCurrPower(freq, dT);
			// Total power being generated
			Pg = Pbase + Psp;
			// Released demand
			Pr = grid.getReleasedPower(baseload, freq, 50.0);
			// "accelerating" power
			Ps = Pg + Pr - Pl;
			// Calculate the new frequency
			freq = grid.getNewFreq(freq, Ps, dT);
			
			// Print output for later analysis
			System.out.println(t + " " + freq + " " + (-Ps/1000000) + " " + Pr/1000000 + " " + Psp/1000000
					+ " " + Pbase/1000000 + " " + baseg.Pmax/1000000 + " " + grid.deltaf );
			
			
			// Step change in load
			if (i == 0) {
				Pl = baseload + 1000000000.0; 	// OK, fail Sizewell B...
			}
			
			// After 2 minutes secondary response kicks in...
			if (t >= 120 && t < 2500 ) {
				baseg.setNewPower(baseload + 1000000000.0);
			}
			
			// After ~40 minutes make up lost capacity
			if (t >= 2500 && t < 3600) {
				baseg.setNewPower(baseload + 2000000000.0);
				// reduce the gain even further
				baseg.setGain(0.001);
				baseg.setNewSetpoint(52.1);
			}
			
			// After an hour return to normal balanced state
			if (t >= 3600) {
				baseg.setNewPower(baseload + 1000000000.0);
				baseg.setNewSetpoint(52.0);
			}
			
		}
	}	
}
