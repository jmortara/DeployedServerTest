package com.mortaramultimedia.deployedservertest;

import android.util.Log;

/**
 * Created by jason on 11/26/2014.
 */
public class Model
{
	private static final String TAG = "Model";

	public static final String HOST = "wordwolfgame.com";	// WARNING - will connect to any site hosted on jasonmortara.com
	public static final int PORT = 4001;

	public static String incomingMessage = "default";
	public static Boolean connected = false;

	public static void setIncomingMessage( String msg )
	{
		Log.d( TAG, "setIncomingMessage to: " + msg );
		incomingMessage = msg;
	}
}
