/*****************************************************************************
 * 
 *  ProgrammeStep
 * 
 *  Class abstracting a single step in a dishwasher wash programme
 *  for dwgrid simulation
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
 * ProgrammeStep
 * 
 * A class to model individual programme steps, each step
 * consists of a step time and step power level.  
 * ProgrammeStep objects are combined to form a Programme
 * 
 * @author trevorm
 *
 */
public class ProgrammeStep {
	public int stepTime;		// step time in seconds
	public int power;			// power level in Watts
	/**
	 * Constructor for a ProgrammeStep object
	 * @param time	step time in seconds
	 * @param power power level in Watts
	 */
	public ProgrammeStep (int time, int power) {
		stepTime = time;
		this.power = power;
	}

}
