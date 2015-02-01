/*****************************************************************************
 * 
 *  SingleRandomDelay
 * 
 *  Class implementing a dynamic demand model using a single, random, delay.
 *  Part of dwgrid simulation
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
 * Class implementing a dishwasher turning off for a random
 * delay when the grid frequency drops below a preset, fixed
 * value and turning on again when the delay times out and
 * the grid frequency has risen above a fixed value.
 * 
 * @author trevorm
 *
 */
public class SingleRandomDelay extends Dishwasher {
	
	/**
	 * Constructor for SingleRandomDelay object. 
	 * minofftime sets the minimum delay before frequency is sensed.
	 * maxofftime sets the upper bound for the randomised delay
	 * 
	 * @param prog
	 * @param randpc
	 */
	public SingleRandomDelay(int prog[], float randpc) {
		super(prog);
		turnofffreq = 49.2F;	// Turn off if freq <= 49.2Hz
		turnonfreq  = 49.95F;	// Turn on when freq >= 49.95 Hz
		minofftime  = 300;		// Minimum off time of 300 seconds - ie 5 minutes
		maxofftime  = 7200;		// Maximum time for any one delay
								// is 60 minutes
		totdelaytime = 0;
	}
	
	/**
	 * Overrides the base class runProgramme() method
	 * Turns off heating if frequency drops below turnofffreq,
	 * waits for a randomised delay and then turns heating
	 * back on.
	 */
	public float runProgramme(double freq, double dT) {
		float load = 0;
		if (delay) {
			waittime += dT;
			if (waittime > retrytime) {
				// timed out, no extension of retrytime, 
				delay = false;
				load = p.steps[p.stepNumber].power;
			}
		} else {
			p.stepRunTime += dT;
			runtime += dT;
			if (p.stepRunTime > p.steps[p.stepNumber].stepTime) {
				p.stepNumber++;
				p.stepRunTime = 0;
				if (p.stepNumber > p.numSteps - 1) {
					p.stepNumber = 0;
				}
			}
			
			heating = p.steps[p.stepNumber].power > minheatload;
			if(heating && freq < turnofffreq) {
				delay = true;
				heating = false;
				waittime = 0;
				retrytime = rngen.nextFloat() * maxofftime + minofftime;
				load = p.steps[0].power;
			} else {
				load = p.steps[p.stepNumber].power;
			}
		}
		
		return load;
	}

}
