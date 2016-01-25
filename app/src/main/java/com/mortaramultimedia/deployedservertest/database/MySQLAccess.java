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
		url 			= dbProps.getProperty("url");
		port 			= dbProps.getProperty("port");
		dbname 			= dbProps.getProperty("dbname");
		user 			= dbProps.getProperty("user");
		password 		= dbProps.getProperty("password");
		connectionStr	= url + ":" + port + "/" + dbname + "?" + "user=" + user + "&password=" + password;
		Log.d(TAG, "MYSQLAccess: constructor. Properties: " + connectionStr);
	}

  public void connectToDataBase() throws Exception
  {
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
	  System.out.println("getAllUsers: user table data:");
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
			System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
//	        System.out.println("email: " + email);
//	        System.out.println("current_score: " + current_score);
//	        System.out.println("high_score: " + high_score);
//	        System.out.println("---");
//	        String summary = resultSet.getString("summary");
//	        Date date = resultSet.getDate("datum");
//	        String comment = resultSet.getString("comments");
//	        System.out.println("Date: " + date);
//	        System.out.println("Comment: " + comment);
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
	  System.out.println("createNewUser: " + username);
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
		  System.out.println("createNewUser: FAILED TO CREATE NEW USER. USERNAME ALREADY EXISTS: " + username);
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
	  System.out.println("createRandomNewUser: " + username);
	  createNewUser(username, "pass"+rand, "deleteme"+rand+"@gmail.com");
  }
  
  public void updateCurrentScore(String user) throws SQLException
  {
	System.out.println("updateHighScore: for: " + user);
	
	ResultSet userRecord = getUser( user, false );
	
	try
	{
		int existingCurrentScore = userRecord.getInt("current_score");
		int newCurrentScore = existingCurrentScore + 1;
		  preparedStatement = connection.prepareStatement("UPDATE users SET current_score=" + newCurrentScore + " WHERE username='" + user + "';");
//	      preparedStatement.setInt(5, 0);
		  preparedStatement.executeUpdate();

		  // check result
		  getUser( user, true );
	}
	catch (SQLException e)
	{
		System.out.println("updateHighScore: FAILED.");
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
			System.out.println("updateHighScore: user record located: " + user);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
		  }
		  else
		  {
			  Exception e = new Exception("FAILED TO GET USER BY USERNAME: " + user);
			  throw e;
		  }
	} 
	catch (Exception e)
	{
		System.out.println(e);
	}
	finally
	{
		resultSet.close();
	}
	*/
  }
  
  public ResultSet getUser(String user, Boolean close) throws SQLException
  {
	try
	{
		statement = connection.createStatement();

		  // resultSet gets the result of the SQL query
		  resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'");

		  //TODO: there should only be one row. if there is more than one then multiple users have returned with the same username
		  boolean rowLocated = resultSet.first();
		  if ( rowLocated )
		  {
			System.out.println("getUser: user record located: " + user);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
		  }
		  else
		  {
			  Exception e = new Exception("getUser: FAILED. USERNAME NOT FOUND: " + user);
			  throw e;
		  }
	} 
	catch (Exception e)
	{
		System.out.println(e);
	}
	finally
	{
		if ( close )
		{
			resultSet.close();
		}
	}
	
	return resultSet;
  }
  
  private void writeMetaData(ResultSet resultSet) throws SQLException {
	// now get some metadata from the database
	System.out.println("The columns in the table are: ");
	System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
	for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
	  System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
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