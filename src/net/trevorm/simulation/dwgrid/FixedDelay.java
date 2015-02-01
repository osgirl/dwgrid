/*****************************************************************************
 * 
 *  FixedDelay
 * 
 *  Class implementing a Fixed Delay model of dynamic demand. 
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
 * Class implementing a dishwasher turning off for a fixed
 * delay when the grid frequency drops below a preset, fixed
 * value and turning on again when the delay times out and
 * the grid frequency has risen above a fixed value.
 * 
 * @author trevorm
 *
 */
public class FixedDelay extends Dishwasher {
	
	
	/**
	 * Constructor for FixedDelay object. 
	 * minofftime sets the delay before frequency is sensed.
	 * 
	 * @param prog
	 * @param randpc
	 */
	public FixedDelay(int prog[], float randpc) {
		super(prog, randpc);
		minofftime  = 1200;		// Turn off for 1200 seconds - ie 20 minutes
		totdelaytime = 0;

	}
	
	/**
	 * Overrides the base class runProgramme() method
	 * Turns off heating if frequency drops below turnofffreq,
	 * waits for minofftime and tests frequency against
	 * turnonfreq.  If frequency < turnonfreq wait again.
	 */
	public float runProgramme(double freq, double dT) {
		float load = 0;
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
						retrytime = minofftime;
					} else {
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
				//retrytime = rngen.nextFloat() * maxofftime;
				retrytime = minofftime;
				load = p.steps[0].power;
			} else {
				load = p.steps[p.stepNumber].power;
			}
		}

		return load;
	}

}
