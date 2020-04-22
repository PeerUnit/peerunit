/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.util;

import java.util.StringTokenizer;

/**
 * 
 * @author Jeremy Masson
 *
 */
public class Util
{
	private static int iColor;
	
	
	
	public static boolean areInSameNet(String ip1, String ip2)
	{
		int index1=ip1.lastIndexOf(".");
		int index2=ip2.lastIndexOf(".");
		if((index1<5) || (index2<5))
			return false;
		String ip1Net=ip1.substring(0,index1-1);		
		String ip2Net=ip2.substring(0,index2-1);
		return ip1Net.equals(ip2Net);
	}
	
	/**
	 * This method verify if a ip addresses is valid.
	 * 
	 * @param ip the ip addresses
	 * @return a <code>true</code> if the addresses is valid, <code>false</code> else. 
	 */
	public static boolean isAValidIP(String ip) 
	{
		StringTokenizer tokenizer=new StringTokenizer(ip,".");
		if(tokenizer.countTokens()!=4)
		{
			return false;
		}
		boolean nullIP=true;
		while(tokenizer.hasMoreTokens())
		{
			String part=tokenizer.nextToken();
			try
			{
				int value=Integer.parseInt(part);
				if(value!=0)
				{
					nullIP=false;
				}
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return (!nullIP);
	}
	
	/**
	 * Initiate the color number to 1.
	 */
	public static void initColor()
	{
		iColor = 0;
	}
	
	/**
	 * return a string color 
	 * @param i color number 
	 * @return white by default
	 */
	public static String getColor()
	{
		if(iColor++ > 20)
		{
			initColor();
		}
		switch(iColor)
		{
		case 1:
			return "green";
		case 2:
			return "red";
		case 3:
			return "blue";
		case 4:
			return "yellow";
		case 5:
			return "hotpink";
		case 6:
			return "indigo";
		case 7:
			return "khaki";
		case 8:
			return "limegreen";
		case 9:
			return "lightcyan4";
		case 10:
			return "khaki1";
		case 11:
			return "lightcyan";
		case 12:
			return "lightsalmon";
		case 13:
			return "hotpink3";
		case 14:
			return "olivedrab1";
		case 15:
			return "peachpuff2";
		case 16:
			return "sienna3";
		case 17:
			return "violet";
		case 18:
			return "whitesmoke";
		case 19:
			return "yellowgreen";
		case 20:
			return "dodgerblue4";
		}
		return "white";
	}
}
