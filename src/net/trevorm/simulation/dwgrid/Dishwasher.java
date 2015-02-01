/*****************************************************************************
 * 
 *  Dishwasher
 * 
 *  Class modelling a basic dishwasher for dwgrid simulation
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
 * A class to model a Dishwasher
 * 
 * @author trevorm
 *
 */
public class Dishwasher {
	
	protected float turnofffreq = 49.2F;
	protected float turnonfreq  = 49.95F;
	
	protected float minofftime = 300;
	protected float maxofftime = 3600;
	protected float totofftime = 7200;
	protected float retrytime;
	protected int nwaits = 0;
	protected boolean delay = false;
	protected float waittime = 0;
	protected double totdelaytime = 0;
	protected static double maxdelaytime = 1800;
	
	protected double runtime;
	protected boolean heating;
	protected Random rngen;
	public double totruntime;
	protected Programme p;
	protected int minheatload = 1000;

	/**
	 * Constructor for the Dishwasher class
	 * 
	 * @param prog		an array of integers, in pairs
	 *                  each pair represents
	 *                  step time in seconds
	 *                  step power in Watts
	 * @param randpc	float percentage randomisation
	 */
	public Dishwasher(int[] prog, float randpc) {
		
		runtime = 0;
		rngen = new Random();
		p = new Programme(prog, randpc);
		totruntime = p.totalRunTime;
	}
	
	/**
	 * An alternative constructor with no randomisation
	 * @param prog
	 */
	public Dishwasher(int[] prog) {
		this(prog, 0.0F);
	}
	
	/**
	 * Steps through the steps of the Programme object and
	 * calculates the electrical load.  Restarts the 
	 * Programme on completion.
	 * 
	 * This method is overridden in subclasses implementing
	 * Dynamic Demand control regimes
	 * 
	 * @param freq		Grid frequency - used in subclasses
	 * @param dT		step time
	 * @return			electrical load
	 */
	public float runProgramme(double freq, double dT) {
		float load = 0;

		p.stepRunTime += dT;
		runtime += dT;
		if (p.stepRunTime > p.steps[p.stepNumber].stepTime) {
			p.stepNumber++;
			p.stepRunTime = 0;
			if (p.stepNumber > p.numSteps - 1) {
				p.stepNumber = 0;
			}
		}


		load = p.steps[p.stepNumber].power;
		return load;
	}
	
	/**
	 * Allows the progress through a Programme to be initialised
	 * Used to initialise a 'fleet' of dishwashers at different
	 * steps in their programmes
	 * 
	 * @param t		Elapsed runtime required
	 */
	public void setRuntime(double t) {
		float foo = 0;
		int passedtime = 0;
		int step = 0;
		runtime = t;
		for (int i = 0; i < p.numSteps; i++) {
			foo += p.steps[i].stepTime;
			if (t < foo) {
				step = i;
				break;
			}
			passedtime += p.steps[i].stepTime;
		}
		p.stepNumber = step;
		p.stepRunTime = (float) t - passedtime;
	}
	
	/**
	 * set the turn-off frequency for Dynamic Demand regime
	 * 
	 * @param f		turn-off frequency
	 */
	public void setTurnOffFreq(float f) {
		this.turnofffreq = f;
	}
	
	/**
	 * set the turn-on frequency for Dynamic Demand regime
	 * 
	 * @param f		turn-on frequency
	 */
	public void setTurnOnFreq(float f) {
		this.turnonfreq = f;
	}

}
