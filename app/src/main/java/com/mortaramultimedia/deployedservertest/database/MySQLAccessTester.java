package com.mortaramultimedia.deployedservertest.database;

import android.util.Log;

import java.sql.ResultSet;
import java.util.Properties;


public class MySQLAccessTester 
{
	private static String TAG = "MySQLAccessTester";

	/*
	  public static void main(String[] args) throws Exception
	  {
		MySQLAccess dao = new MySQLAccess();
		
		dao.connectToDataBase();
		dao.getAllUsers();
		dao.createRandomNewUser();
		ResultSet testUser = dao.getUser("tyler", true);
		dao.updateCurrentScore("jason");
		
	  }
	*/
	public static int test(Properties dbProps) throws Exception
	{
		Log.d(TAG, "test");

		MySQLAccess dao = new MySQLAccess(dbProps);

		try
		{
			dao.connectToDataBase();
			dao.getAllUsers();
			dao.createRandomNewUser();
			ResultSet testUser = dao.getUser("tyler", true);
			dao.updateCurrentScore("jason");
			return 1;	// success
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;	// failed
		}
	}
} 