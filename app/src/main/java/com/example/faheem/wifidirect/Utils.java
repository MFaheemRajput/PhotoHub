package com.example.faheem.wifidirect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.util.Log;

public class Utils {

	private final static String p2pInt = "p2p0";

	public static String getIPFromMac(String MAC) {
		/*
		 * method modified from:
		 * 
		 * http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
		 * 
		 * */
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {

				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4) {
					// Basic sanity check
					String device = splitted[5];
					if (device.matches(".*" +p2pInt+ ".*")){
						String mac = splitted[3];
						if (mac.matches(MAC)) {
							return splitted[0];
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public static String getLocalIPAddress() {
		/*
		 * modified from:
		 * 
		 * http://thinkandroid.wordpress.com/2010/03/27/incorporating-socket-programming-into-your-applications/
		 * 
		 * */
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//
//					String iface = intf.getName();
//					if (iface.matches(".*" + p2pInt + ".*")) {
//						if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
//							return getDottedDecimalIP(inetAddress.getAddress());
//						}
//					}
//				}
//			}
//		} catch (SocketException ex) {
//
//		} catch (NullPointerException ex) {
//
//		}

		//Loop through all the network interface devices
		try{
			for (Enumeration<NetworkInterface> enumeration = NetworkInterface
					.getNetworkInterfaces(); enumeration.hasMoreElements();) {
				NetworkInterface networkInterface = enumeration.nextElement();
				//Loop through all the ip addresses of the network interface devices
				for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumerationIpAddr.nextElement();
					//Filter out loopback address and other irrelevant ip addresses
					if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
						//Print the device ip address in to the text view
						return inetAddress.getHostAddress();
					}
				}
			}
		}catch (SocketException ex){

		}catch (NullPointerException ex){

		}


		return null;
	}

	private static String getDottedDecimalIP(byte[] ipAddr) {
		/*
		 * ripped from:
		 * 
		 * http://stackoverflow.com/questions/10053385/how-to-get-each-devices-ip-address-in-wifi-direct-scenario
		 * 
		 * */
		String ipAddrStr = "";
		for (int i=0; i<ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i]&0xFF;
		}
		return ipAddrStr;
	}
}
