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

	/**
	 * Attempt login with the LoginRequest obj stored in the Model, say from the LoginActivity
	 * @return
	 */
	public static int attemptLogin()
	{
		Log.d(TAG, "attemptLogin via Model with these credentials: " + Model.userLogin);

		int loginSucceeded = 0;

		if (dao != null)
		{
			try
			{
				ResultSet retrievedUsers = dao.getUser(Model.userLogin.getUserName(), Model.userLogin.getPassword(), null, false);

				// we need to know if there were actual rows of data retrieved by the db query in order to know if the login succeeded or not
				int numRows = 0;
				if (retrievedUsers != null)
				{
					retrievedUsers.beforeFirst();
					retrievedUsers.last();
					numRows = retrievedUsers.getRow();
				}

				if (numRows > 0)
				{
					Log.d(TAG, "attemptLogin: success! found this many matching users: " + numRows);
					loginSucceeded = 1;
				}
				else
				{
					Log.d(TAG, "attemptLogin: failed! found this many matching users: " + numRows);
					loginSucceeded = 0;
				}
			}
			catch (SQLException e)
			{
				Log.d(TAG, "attemptLogin: ERROR");
				e.printStackTrace();
			}
		}
		return loginSucceeded;
	}
} 