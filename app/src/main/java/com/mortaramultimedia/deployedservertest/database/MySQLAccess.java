//see http://www.vogella.com/tutorials/MySQLJava/article.html

package com.mortaramultimedia.deployedservertest.database;

import android.util.Log;

//import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.Date;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

//import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class MySQLAccess 
{
	private static final String TAG = "MYSQLAccess";

	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	// db props
	String JDBCDriver = null;
	String url = null;
	String port = null;
	String dbname = null;
	String user = null;
	String password = null;
	String connectionStr = null;

	/**
	 * MySQLAccess constructor with passed Properties
	 * @param dbProps
	 */
	public MySQLAccess(Properties dbProps)
	{
		Log.d(TAG, "MYSQLAccess: constructor.");
		JDBCDriver 		= dbProps.getProperty("JDBCDriver");
		url 				= dbProps.getProperty("url");
		port 				= dbProps.getProperty("port");
		dbname 			= dbProps.getProperty("dbname");
		user 				= dbProps.getProperty("user");
		password 		= dbProps.getProperty("password");
		connectionStr	= url + ":" + port + "/" + dbname + "?" + "user=" + user + "&password=" + password;
		Log.d(TAG, "MYSQLAccess: constructor. Properties: " + connectionStr);
	}

  public void connectToDataBase() throws Exception
  {
	  Log.d(TAG, "connectToDataBase");
	try
	{
		 Class.forName( JDBCDriver );	// this loads the defined JDBC class by name
		 connection = DriverManager.getConnection( connectionStr );

		 Log.d(TAG, "connectToDataBase: CONNECTION SUCCESSFUL");
	}
	catch (CommunicationsException e)
	{
		Log.w(TAG, "connectToDataBase: CONNECTION FAILED. COMMUNICATIONS LINK FAILURE. POSSIBLE CONNECTION TIMEOUT OR WRONG PORT.");
		throw e;
	}
	catch (SQLException e)
	{
		Log.w(TAG, "connectToDataBase: CONNECTION FAILED. ACCESS DENIED.");
		throw e;
	}
	catch (Exception e)
	{
		Log.w(TAG, "connectToDataBase: ERROR: " + e.getMessage());
		throw e;
	}
  }

  public void getAllUsers() throws Exception 
  {
	  Log.d(TAG, "getAllUsers: user table data:");
	  try
	  {
		  // statements allow to issue SQL queries to the database
		  statement = connection.createStatement();

		  // resultSet gets the result of the SQL query
		  resultSet = statement.executeQuery("SELECT * from users");

		  // resultSet is initialised before the first data set
		  while (resultSet.next())
		  {
			// it is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g., resultSet.getSTring(2);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			Log.d(TAG, "username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
//	        Log.d(TAG, "email: " + email);
//	        Log.d(TAG, "current_score: " + current_score);
//	        Log.d(TAG, "high_score: " + high_score);
//	        Log.d(TAG, "---");
//	        String summary = resultSet.getString("summary");
//	        Date date = resultSet.getDate("datum");
//	        String comment = resultSet.getString("comments");
//	        Log.d(TAG, "Date: " + date);
//	        Log.d(TAG, "Comment: " + comment);
		  }
	  }
	  catch(Exception e)
	  {
		  throw e;
	  }
	  finally
	  {
		  resultSet.close();
	  }
  }
  
  public void createNewUser(String username, String password, String email) throws Exception
  {
	  Log.d(TAG, "createNewUser: " + username);
	  try
	  {
		  // statements allow to issue SQL queries to the database
//	      statement = connection.createStatement();
//	      resultSet = statement.execute("INSERT INTO users (username, password, email, current_score, high_score) VALUES ('" + username + "', 'pass123', 'm@m.com', 0, 0)");

		  // preparedStatements can use variables and are more efficient
		  preparedStatement = connection.prepareStatement("INSERT INTO users (username, password, email, current_score, high_score) VALUES (?, ?, ?, ?, ?)");
		  // "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
		  // parameters start with 1
		  preparedStatement.setString(1, username);
		  preparedStatement.setString(2, password);
		  preparedStatement.setString(3, email);
//	      preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
		  preparedStatement.setInt(4, 0);
		  preparedStatement.setInt(5, 0);
		  preparedStatement.executeUpdate();

//	      preparedStatement = connection.prepareStatement("SELECT user, password, email, current_score, high_score from users");
//	      resultSet = preparedStatement.executeQuery();
//	      writeResultSet(resultSet);

		  // remove again the insert comment
//	      preparedStatement = connection.prepareStatement("delete from FEEDBACK.COMMENTS where myuser= ? ; ");
//	      preparedStatement.setString(1, "Test");
//	      preparedStatement.executeUpdate();

//	      resultSet = statement.executeQuery("select * from FEEDBACK.COMMENTS");
//	      writeMetaData(resultSet);
	  }
	  catch( MySQLIntegrityConstraintViolationException dupeUserException)
	  {
		  Log.d(TAG, "createNewUser: FAILED TO CREATE NEW USER. USERNAME ALREADY EXISTS: " + username);
	  }
	  catch (SQLException e)
	  {
		  throw e;
	  }
	  finally
	  {
		  preparedStatement.close();
	  }
  }
  
  public void createRandomNewUser() throws Exception
  {
	  int rand = (int)(Math.random()*10000);
	  String username = "_deleteme" + rand;
	  Log.d(TAG, "createRandomNewUser: " + username);
	  createNewUser(username, "pass"+rand, "deleteme"+rand+"@gmail.com");
  }
  
  public void updateCurrentScore(String username) throws SQLException
  {
	  Log.d(TAG, "updateHighScore: for: " + username);
	
	ResultSet userRecord = getUser(username, null, null, false);
	
	try
	{
		int existingCurrentScore = userRecord.getInt("current_score");
		int newCurrentScore = existingCurrentScore + 1;
		  preparedStatement = connection.prepareStatement("UPDATE users SET current_score=" + newCurrentScore + " WHERE username='" + user + "';");
//	      preparedStatement.setInt(5, 0);
		  preparedStatement.executeUpdate();

		// verify that score has been written to db
		userRecord = getUser(username, null, null, true);
	}
	catch (SQLException e)
	{
		Log.d(TAG, "updateHighScore: FAILED.");
		throw e;
	}
	finally
	{
		if ( userRecord != null )
		{
			userRecord.close();
		}
	}
	
	/*
	try
	{
		statement = connection.createStatement();

		  // resultSet gets the result of the SQL query
		  resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'");

		  //TODO: there should only be one row. if there is more than one then multiple users have returned with the same username
		  boolean rowLocated = resultSet.first();
		  if ( rowLocated )
		  {
			Log.d(TAG, "updateHighScore: user record located: " + user);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			Log.d(TAG, "username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
		  }
		  else
		  {
			  Exception e = new Exception("FAILED TO GET USER BY USERNAME: " + user);
			  throw e;
		  }
	} 
	catch (Exception e)
	{
		Log.d(TAG, (e);
	}
	finally
	{
		resultSet.close();
	}
	*/
  }

	public ResultSet getUser(String user, String password, String email, Boolean close) throws SQLException
	{
		Log.d(TAG, "getUser:" + user + ", password:" + password + ", email:" + email + ", close:" + close);

		try
		{
			statement = connection.createStatement();

			// resultSet gets the result of the SQL query, and depends on which fields are passed
			if(password == null && email == null)
			{
				resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "';");
			}
			else if(password != null && email == null)
			{
				resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'" + "AND password='" + password + "';");
			}
			else
			{
				resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'" + "AND password='" + password + "'" + "AND email='" + email + "';");
			}

			//TODO: there should only be one row. if there is more than one then multiple users have returned with the same username
			boolean rowLocated = resultSet.first();
			if (rowLocated)
			{
				Log.d(TAG, "getUser: user record FOUND: " + user);
				String dbUsername 	= resultSet.getString("username");
				String dbPassword 	= resultSet.getString("password");
				String dbEmail 		= resultSet.getString("email");
				int dbCurrentScore 	= resultSet.getInt("current_score");
				int dbHighScore 		= resultSet.getInt("high_score");
				Log.d(TAG, "dbUsername:" + dbUsername + ", dbPassword:" + dbPassword + ", dbEmail:" + dbEmail + ", dbCurrentScore:" + dbCurrentScore + ", dbHighScore:" + dbHighScore);
			}
			else
			{
				SQLException e = new SQLException("getUser: FAILED. USERNAME NOT FOUND: " + user);
				throw e;
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, e.getMessage());
		}
		finally
		{
			if (close)
			{
				resultSet.close();
			}
		}

		return resultSet;
	}
  
  private void writeMetaData(ResultSet resultSet) throws SQLException {
	// now get some metadata from the database
	  Log.d(TAG, "The columns in the table are: ");
	  Log.d(TAG, "Table: " + resultSet.getMetaData().getTableName(1));
	for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
		Log.d(TAG, "Column " + i + " " + resultSet.getMetaData().getColumnName(i));
	}
  }


  // you need to close all three to make sure
  private void close() {
//    resultSet.close();
//    close(statement);
//    close(connect);
  }
  
  /*
  private void close(Closeable c) {
	try {
	  if (c != null) {
		c.close();
	  }
	} catch (Exception e) {
	// don't throw now as it might leave following closables in undefined state
	}
  }*/
} 