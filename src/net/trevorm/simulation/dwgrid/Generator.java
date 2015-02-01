/*****************************************************************************
 * 
 *  Generator
 * 
 *  Class modelling a Generator for dwgrid simulation
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
// Model a generator with governor gain and droop

public class Generator {
	protected double Pmax;		// Maximum power
	public    double Pdmd;		// Demanded power
	public    double Fsp;		// Frequency set point
	private   double Fnom;		// Nominal (grid) frequency
	private   double droop;		// generator droop
	private   double gain;		// governor gain;
	public    double Pcurr;		// Current power output
	public    double Ptgt;		// Target power output
	
	/**
	 * Constructor for a Generator object
	 * 
	 * @param Pmax	Maximum generator power
	 * @param Fsp	Frequency set-point
	 * @param Fnom	Nominal frequency
	 * @param droop	Percentage droop characteristic, 
	 * @param gain	Governor gain
	 */
	
	public Generator(double Pmax, double Fsp, double Fnom, double droop, double gain){
		// Create a new generator
		this.Pmax = Pmax;
		this.Fsp  = Fsp;
		this.Fnom = Fnom;
		this.droop = droop/100;
		this.gain = gain;
		this.Pdmd = this.Pcurr = 0;
	}
	/**
	 * Constructor for a Generator object which is generating when instantiated
	 *  
	 * @param Pmax	Maximum generator power
	 * @param Fsp	Frequency set-point
	 * @param Fnom	Nominal frequency
	 * @param droop	Percentage droop characteristic, 
	 * @param gain	Governor gain
	 * @param Pinit	Initial power output
	 */
	public Generator(double Pmax, double Fsp, double Fnom, double droop, double gain, double Pinit){
		// Create a new generator
		this.Pmax = Pmax;
		this.Fsp  = Fsp;
		this.Fnom = Fnom;
		this.droop = droop/100;
		this.gain = gain;
		this.Pdmd = this.Pcurr = Pinit;
		
	}
	
	/**
	 * Method to get the target power output using
	 * the droop characteristic.  The target power
	 * is bounded, the upper limit is the maximum 
	 * power output of the generator, the lower limit
	 * is zero.
	 * 
	 * @param 	freq	Current grid frequency in Hz
	 * @return 	Target Power
	 */
	public double getTargetPower(double freq) {
		double Pt;
		Pt = ((Fsp - freq) / (droop * Fnom)) * Pmax;
		if(Pt > Pmax) {
			Pt = Pmax;
		}
		if (Pt < 0) {
			Pt = 0;
		}
		return Pt;
	}
	
	/**
	 * Method to get the current power output of a
	 * Generator object at a given frequency.
	 * 
	 * @param freq	Current grid frequency
	 * @param dT	time step
	 * @return power output
	 */
	public double getCurrPower(double freq, double dT) {
		// calculate this iteration's power output
		double Ptemp;
		Ptgt = getTargetPower(freq);
		Ptemp = Pcurr + (Ptgt - Pcurr) * gain * dT;
		Pcurr = Ptemp;
		return Pcurr;
	}
	
	/**
	 * Method to set a new maximum power output
	 * 
	 * @param Pmax	new maximum power output
	 */
	public void setNewPower(double Pdmd) {
		// set a new power output level
		this.Pmax = Pdmd;
	}
	
	/**
	 * Method to change the generator set-point
	 * 
	 * @param freq 	new set-point frequency
	 */
	public void setNewSetpoint(double freq) {
		this.Fsp = freq;
	}
	
	/**
	 * Method to instantaneously change the current power output
	 * 
	 * @param newPower
	 */
	public void overrideCurrentPower(double newPower){
		this.Pcurr = newPower;
	}
	
	/**
	 * Method to set a new gain value
	 * 
	 * @param newGain
	 */
	public void setGain(double newGain) {
		this.gain = newGain;
	}
	
	/**
	 * Main entry point for stand-alone testing
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		double pout;
		double  t = 0;
		double  dt = 0.5;
		double  freq = 50.0;
		
		// create a generator
		Generator g = new Generator(1320000000.0, 52.0, 50.0, 4.0, 0.3);
		g.setNewPower(1000000000);
		
		// iterate
		for (int i = 1; i < 1000; i++) {
			// get the current power output
			pout = g.getCurrPower(freq, dt);
			t += dt;
			// after 500 passes change the frequency
			if(i ==  500) {
				g.setNewPower(1320000000);
			}
			// print the output for later logging/ graphing / whatever
			// System.out.println("Power out at time " + t + " is " + pout);
			System.out.println(t + " " + pout);
		}
	}

}
