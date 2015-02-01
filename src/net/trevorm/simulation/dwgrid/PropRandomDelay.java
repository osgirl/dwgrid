/*****************************************************************************
 * 
 *  PropRandomDelay
 * 
 *  Class implementing a dynamic demand model where the limit of the randomised
 *  delay is inversely proportional to the internal temperature. 
 *  Part of dwgrid simulation.
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
 * The 'random' delay is scaled proportional to the internal 
 * temperature of the dishwasher.  The closer the machine is 
 * to it's target temperature the shorter the delay.  Since 
 * temperatures are not actually modelled, progress through 
 * a heating cycle is taken as a proxy for internal temperature.
 * 
 * @author trevorm
 *
 */
public class PropRandomDelay extends Dishwasher {
	
	/**
	 * Constructor for PropRandomDelay object. 
	 * maxofftime sets the upper bound for the randomised delay
	 * 
	 * @param prog
	 * @param randpc
	 */
	public PropRandomDelay(int prog[], float randpc) {
		super(prog, randpc);
		turnofffreq = 49.8F;	// Turn off if freq <= 49.8 Hz
		turnonfreq  = 49.95F;	// Turn on when freq >= 49.95 Hz
		maxofftime  = 1200;		// Maximum time for any one delay
								// is 20 minutes
		totdelaytime = 0;
	}
	
	/**
	 * Overrides the base class runProgramme() method
	 * Turns off heating if frequency drops below turnofffreq,
	 * waits for a randomised delay and tests frequency against
	 * turnonfreq.  If frequency < turnonfreq wait again for a 
	 * new randomised delay.
	 * Random delay is scaled inversely proportionally to the 
	 * percentage of the heating cycle completed
	 */
	public float runProgramme(double freq, double dT) {
		float load = 0;
		float pcStepTime = 0;
		
		if (delay) {
			waittime += dT;
			totdelaytime += dT;
			if (totdelaytime >= maxdelaytime) {
				delay = false;
				load = p.steps[p.stepNumber].power;
			} else {
				if (waittime > retrytime) {
					if (freq < turnonfreq) {
						waittime = 0;
						retrytime = rngen.nextFloat() * maxofftime * (1 - pcStepTime);					} else {
						delay = false;
						load = p.steps[p.stepNumber].power;
					}

				}
			}
			
		} else {
			p.stepRunTime += dT;
			runtime += dT;
			if (p.stepRunTime > p.steps[p.stepNumber].stepTime) {
				p.stepNumber++;
				p.stepRunTime = 0;
				if (p.stepNumber > p.numSteps - 1) {
					p.stepNumber = 0;
					totdelaytime = 0;
				}

			}
			
			heating = p.steps[p.stepNumber].power > minheatload;
			if(heating && freq < turnofffreq) {
				delay = true;
				heating = false;
				waittime = 0;
				pcStepTime = p.stepRunTime / (float)p.steps[0].stepTime;
				retrytime = rngen.nextFloat() * maxofftime * (1 - pcStepTime);
				load = p.steps[0].power;
			} else {
				load = p.steps[p.stepNumber].power;
			}
		}
		
		return load;
	}

}
