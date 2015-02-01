/*****************************************************************************
 * 
 *  PropFreqRandomDelay
 * 
 *  Class implementing a dynamic demand model where lower frequency threshold
 *  and the limit of the randomised delay are inversely proportional to the
 *  internal temperature of the dishwasher. Part of dwgrid simulation
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
 * delay when the grid frequency drops below a threshold 
 * inversely proportional to the internal temperature
 * and turning on again when the delay times out and
 * the grid frequency has risen above a fixed value.
 * Since temperatures are not actually modelled, the percentage
 * of a heating cycle completed is taken as a proxy for
 * internal temperature.
 * 
 * The 'random' delay is scaled proportional to the internal 
 * temperature of the dishwasher.  The closer the machine is 
 * to it's target temperature the shorter the delay.  
 * 
 * @author trevorm
 *
 */
public class PropFreqRandomDelay extends Dishwasher {
	
	/**
	 * Constructor for PropFreqRandomDelay object. 
	 * maxofftime sets the upper bound for the randomised delay
	 * 
	 * @param prog
	 * @param randpc
	 */
	public PropFreqRandomDelay(int prog[], float randpc) {
		super(prog, randpc);
		turnofffreq = 49.5F;	// Turn off if freq <= 49.8 Hz
		turnonfreq  = 49.95F;	// Turn on when freq >= 49.95 Hz
		minofftime  = 300;		// Minimum delay is 5 minutes
		maxofftime  = 600;		// Maximum time for any one delay
								// is 10 minutes
		totdelaytime = 0;
	}
	
	/**
	 * Overrides the base class runProgramme() method
	 * Turns off heating if frequency drops below a frequency
	 * inversely proportional to the percentage of the heating
	 * cycle completed.  Threshold set between turnofffreq and 
	 * turnofffreq + 0.3Hz - effectively 49.5 to 49.8 Hz.
	 * Waits for a randomised delay and tests frequency against
	 * the same threshold. If frequency < threshold wait again 
	 * for a new randomised delay.
	 * 
	 * Random delay is scaled inversely proportionally to the 
	 * percentage of the heating cycle completed
	 */
	public float runProgramme(double freq, double dT) {
		float load = 0;
		float pcStepTime = 0;
		float propturnoff;

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
			pcStepTime = p.stepRunTime / (float)p.steps[0].stepTime;
			propturnoff  = turnofffreq + (0.1F * (1.0F - pcStepTime));

			if (p.stepRunTime > p.steps[p.stepNumber].stepTime) {
				p.stepNumber++;
				p.stepRunTime = 0;
				if (p.stepNumber > p.numSteps - 1) {
					p.stepNumber = 0;
					totdelaytime = 0;
				}
			}
			
			heating = p.steps[p.stepNumber].power > minheatload;

			if(heating && freq < propturnoff) {
				delay = true;
				heating = false;
				waittime = 0;
				retrytime = rngen.nextFloat() * maxofftime * (1 - pcStepTime);
				load = p.steps[0].power;
			} else {
				load = p.steps[p.stepNumber].power;
			}
		}
		
		return load;
	}

}
