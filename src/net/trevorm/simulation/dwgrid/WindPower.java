/*****************************************************************************
 * 
 *  WindPower
 * 
 *  Class modelling the output of distributed wind generation by reading historical
 *  data from a fill.  Part of dwgrid simulation.
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

import java.io.*;

/**
 * Class to model contribution of Wind Power to the grid
 * 
 * Raw wind data at 5 minute intervals gathered from the 
 * UK National Grid Balancing Mechanism website at 
 * http://wwww.bmreports.com in July 2010 subsequently
 * processed to give interpolated data at 1 minute 
 * intervals.
 * 
 * @author trevorm
 *
 */

public class WindPower {
	File 			dataFile;		// the data file object
	BufferedReader 	dataReader;		// to read from the file
	float			windpower;		// wind power output in MW
	int				stepSec;		// step length in seconds
	
	/**
	 * Minimal constructor
	 */
	public WindPower() {
		
	}
	
	/**
	 * Method to open a file and connect a file reader
	 * 
	 * @param path
	 * @return
	 */
	public int openWindDataFile(String path) {
		dataFile = new File(path);
		if(!dataFile.exists()) {
			return -1;
		}
		if(!dataFile.isFile()){
			return -2;
		}
		if(!dataFile.canRead())
		{
			return -3;
		}
		try {
			dataReader = new BufferedReader(new FileReader(dataFile));
		}
		catch (IOException ie){
			System.err.println("IO Exception: " + ie);
			return -4;
		}
		
		return 0;
	}
	
	/**
	 * Close the file
	 */
	public void closeWindDataFile() {
		try {
			dataReader.close();
		}
		catch (IOException ie) {
			System.err.println("IOException: " + ie);
		}
	}
	
	/**
	 * Read the next time/power pair from the file
	 * @return wind power
	 */
	public float readNextWind() {
		String line;
		try{
			line = dataReader.readLine();
		}
		catch (IOException ie){
			System.err.println("IO Exception: " + ie);
			return -1.0F;
		}
		String[] temp = new String[2];
		String delim  = ",";
		temp = line.split(delim);
		stepSec = Integer.parseInt(temp[0]);
		windpower = Float.parseFloat(temp[1]) * 1000000;	// Wind Power is in MW
		
		return windpower;
		
	}
	
	/**
	 * main method for testing only
	 * @param args
	 */
	public static void main(String[] args){
		// Open a file based on the first argument
		String path = "RawTimeStampedWind2.csv";
		int status = -1;
		float windp;
		int   stepSec;
		
		if (args.length == 1) {
			path = new String(args[0]);
		}
		
		WindPower wp = new WindPower();
		status = wp.openWindDataFile(path);
		if (status < 0) {
			// oops!
			System.exit(status);
		}
		double dT = 0.1;
		// read one...
		windp = wp.readNextWind();
		
		windp = wp.windpower;
		stepSec = wp.stepSec;
		

		
		
	}

}
