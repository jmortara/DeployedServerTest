package com.mortaramultimedia.deployedservertest;

import android.util.Log;

import com.mortaramultimedia.wordwolf.shared.messages.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;




/**
 * Created by Jason Mortara on 11/26/2014.
 */
public class Model
{
	private static final String TAG = "Model";

	public static Properties databaseProps = null;
	public static LoginRequest userLogin = null;
	public static String opponentUsername = null;
	public static String incomingMessage = null;
	public static SelectOpponentRequest selectOpponentRequest = null;
	public static GameBoard gameBoard = null;

	public static final String HOST = "wordwolfgame.com";	// WARNING - will connect to any site hosted on jasonmortara.com
	public static final int PORT = 4001;

	public static Boolean connected 	= false;
	public static Boolean connectedToDatabase 	= false;
//	public static Boolean dbTestOK 	= false;
	public static Boolean loggedIn 	= false;
	public static Integer score 		= 0;

}
