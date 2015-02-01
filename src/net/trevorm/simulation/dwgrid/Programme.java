/*****************************************************************************
 * 
 *  Programme
 * 
 *  Class modelling a dishwasher wash programme for dwgrid simulation
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
 * A class to encapsulate a Dishwasher programme
 *
 * @author trevorm
 *
 */
public class Programme {
	public ProgrammeStep[]	steps;
	public int numSteps;
	public int stepNumber;
	public float stepRunTime;
	public int totalRunTime;
	public static Random rgen = new Random(42);
	
	/**
	 * Constructor for a Programme object, creates and array of 
	 * ProgrammeStep objects and calculates some 'whole programme'
	 * attributes.  Randomises the times and powers by +/- 3%
	 * 
	 * @param progsteps		an array of integers, in pairs
	 *                      each pair represents
	 *                      step time in seconds
	 *                      step power in Watts
	 * @param randpc		float percentage randomisation
	 */
	public Programme(int progsteps[], float randpc) {
		numSteps = progsteps.length / 2;	// array of time/power pairs
		steps = new ProgrammeStep[numSteps];
		stepNumber = 0;
		stepRunTime = 0;
		totalRunTime = 0;
		for (int i = 0; i < numSteps; i++) {
			int time = progsteps[i*2] - (int)(progsteps[i*2] * (randpc/2)) + (int)(progsteps[i*2] * randpc * rgen.nextFloat());
			int power = progsteps[(i*2 + 1)] - (int)(progsteps[(i*2 + 1)] * (randpc/2)) + (int)(progsteps[(i*2 + 1)] * randpc * rgen.nextFloat());
			steps[i] = new ProgrammeStep(time, power);
			totalRunTime += steps[i].stepTime;
		}
		
	}
	
	public Programme(int progsteps[]) {
		this(progsteps, 0.0F);
	}
	
	/**
	 * main entry for testing only
	 * @param args
	 */
	public static void main(String[] args) {
		int[] steps = {14*60, 100, 15*60, 2200, 28 * 60, 100, 15*60, 2200, 15*60, 0};
		Programme p = new Programme(steps, 0.05F);
		// Just prove we've got an object and calculated the total run time
		System.out.println("Total run time = " + p.totalRunTime);
		
		p = new Programme(steps);
		System.out.println("Total run time = " + p.totalRunTime);

	}

}
