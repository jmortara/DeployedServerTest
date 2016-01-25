package com.mortaramultimedia.deployedservertest.database;

import android.util.Log;

import com.mortaramultimedia.deployedservertest.Model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import messages.LoginMessage;


public class MySQLAccessTester 
{
	private static final String TAG = "MySQLAccessTester";

	private static MySQLAccess dao;
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
		Log.d(TAG, "test: attempting to connect to DB and run a few operations...");

		dao = new MySQLAccess(dbProps);

		try
		{
			dao.connectToDataBase();
			dao.getAllUsers();
			dao.createRandomNewUser();
			ResultSet testUser = dao.getUser("tyler", "1234monkey", "tysclark@gmail.com", true);
			dao.updateCurrentScore("jason");
			return 1;	// success
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;	// failed
		}
	}

	public static int attemptLogin()
	{
		Log.d(TAG, "attemptLogin -- HARDCODED VALUES");

		int loginSucceeded = 0;

		if (dao != null)
		{
			try
			{
				dao.getUser(Model.userLogin.getUserName(), Model.userLogin.getPassword(), null, false);
				Log.d(TAG, "attemptLogin: success!");
				loginSucceeded = 1;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return loginSucceeded;
	}
} 