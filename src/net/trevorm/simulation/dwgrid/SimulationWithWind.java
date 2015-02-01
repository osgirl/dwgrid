/*****************************************************************************
 * 
 *  SimulationWithWind
 * 
 *  Driver class including wind power data for dwgrid simulation
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Class to drive the simulation
 * @author trevorm
 *
 */
public class SimulationWithWind {
	/**
	 * Driver for simulation of Grid, Generators and some loads...
	 * @param args
	 */
	public static void main(String[] args) {
		// simulate the grid....

		// Simulation parameters
		final double H = 4;					// Inertial constant
		double baseload = 29020000000.0;	// fixed part of load
		double basegen  = 29700000000.0;	// base generating capacity
		double rsvgen   = 3300000000.0;		// spinning reserve capacity
		double Pdw      = 1320000000.0;		// Power used by dishwasher part of the load
		// (initial value only...)


		int    numdws   = 1000;				// number of dishwashers simulated
		int    dwmult   = 1280;             // dishwasher multiplier
		float  pcEco    = 40.0F;			// percentage of dishwashers running an 'eco' programme
		float  turnofffreq = 49.8F;			// turn off frequency
		float  turnonfreq  = 49.95F;		// turn on frequency

		float  fnom     = 50.0F;			// Grid nominal frequency
		double droop    = 4.0;				// Generator droop
		double gain     = 0.0067;				// Generator controller gain

		double freq;		// grid actual frequency

		double Pl;			// Power in the load
		double Pbase;		// base generator output
		double Psp;			// Spinning reserve output
		double Pw;			// Power from wind
		double Pr;			// Released power
		double Ps;			// Power accelerating the generator
		double Pg;			// Total generated power

		//double dT   = 0.1;	// simulate at 0.1s intervals
		double dT = 1.0;
		double t    = 0;	// Start time

		freq = fnom;
		int status;
		WindPower wp = new WindPower();
		status = wp.openWindDataFile("InterpolatedWindData.csv");
		if(status < 0) {
			System.err.println("Problem opening wind data file, status = " + status);
			System.exit(status);
		}
		Pw = wp.readNextWind();

		DishwasherLoad dl = new DishwasherLoad(numdws, pcEco);
		dl.setTurnOffFreq(turnofffreq);
		dl.setTurnOnFreq(turnonfreq);

		// Create a grid
		Grid grid = new Grid((basegen + Pdw + 1000000000)	, H, fnom);

		// Base load generator output - assume that it's flat out.
		// Frequency setpoint (zero load) of 52Hz
		// 4% droop, gain of 0.0067 ...
		Generator baseg = new Generator(basegen, 52.0, fnom, droop, gain, basegen);

		// Spinning reserve, not initially generating
		// Capacity set by rsvgen parameter
		// 4% droop
		// gain of 0.3
		// Frequency setpoint (zero load) of 50Hz
		Generator sprsv = new Generator(rsvgen, 50.0, fnom, droop, 0.3);
		try {
			FileOutputStream out = new FileOutputStream("results.dat");
			PrintStream p = new PrintStream(out);

			p.println("H = " + H);
			p.println("Base Generation (GW) = " + basegen/1000000000.0);
			p.println("Spinning Reserve (GW)= " + rsvgen/1000000000.0);
			p.println("Number of dishwashers = " + numdws * 1000);
			p.println("Percentage running 'Eco' programme = " + pcEco);

			p.println("Time (s), Frequency (Hz), Ps (MW), Pr (MW), Psp (MW), Pbase (MW), Pdw (MW), %Dw heating" );

			for (int i = 0; i < 324000; i++) {
				t += dT;

				// Calculate the load due to dishwashers
				Pdw = dl.calcLoad(dT, freq) * dwmult;

				// Calculate the total load
				Pl = baseload + Pdw;

				// Calculate power from wind
				Pw = wp.windpower;
				if (t > wp.stepSec) {
					Pw = wp.readNextWind();
				}

				// Power from the base load generation
				Pbase = baseg.getCurrPower(freq, dT);
				// Pbase = basegen;
				// Power from the spinning reserve
				Psp = sprsv.getCurrPower(freq, dT);
				// Total power being generated
				Pg = Pbase + Psp;
				// Released demand
				Pr = grid.getReleasedPower(baseload, freq, fnom);
				// "accelerating" power
				Ps = Pg + Pr + Pw - Pl;
				// Calculate the new frequency
				freq = grid.getNewFreq(freq, Ps, dT);

				p.println(t/3600 + ", " + freq + ", " + (-Ps/1000000) + ", " + Pr/1000000 + ", " + Psp/1000000
						+ ", " + Pbase/1000000 + ", " + Pdw/1000000 + ", " + dl.pcOnLoad + ", " + baseg.Pmax/1000000 + ", " + grid.deltaf + ", " + dl.maxtotdelay + ", " + dl.pcDelay + ", " + Pw/1000000);





			}
		}
		catch (IOException ie) {
			System.err.println("IOException: results.dat " + ie);
		}
	}
}
