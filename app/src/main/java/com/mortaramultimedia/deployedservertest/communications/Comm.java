package com.mortaramultimedia.deployedservertest.communications;

import android.util.Log;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Comm - Communications Singleton
 * Created by jason mortaraon 1/30/2016.
 */
public class Comm
{
	private static final String TAG = "Comm";

	private static ObjectInputStream  inStream  = null;
	private static ObjectOutputStream outStream = null;

	private static Comm instance = new Comm();

	public static Comm getInstance()
	{
		return instance;
	}

	private Comm()
	{
	}

	public static ObjectInputStream in()
	{
		return inStream;
	}

	public static void setIn(ObjectInputStream in)
	{
		if (inStream == null)
		{
			inStream = in;
		}
		else Log.w(TAG, "IGNORING REQUEST TO CREATE NEW GLOBAL ObjectInputStream FOR SERVER COMMUNICATIONS");
	}

	public static ObjectOutputStream out()
	{
		return outStream;
	}

	public static void setOut(ObjectOutputStream out)
	{
		if (outStream == null)
		{
			outStream = out;
		}
		else Log.w(TAG, "IGNORING REQUEST TO CREATE NEW GLOBAL ObjectOutputStream FOR SERVER COMMUNICATIONS");
	}
}
