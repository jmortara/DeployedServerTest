package com.mortaramultimedia.deployedservertest.database;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.mortaramultimedia.deployedservertest.Model;
import com.mortaramultimedia.deployedservertest.ServerActivity;

import java.io.InputStream;
import java.util.Properties;

/**
 * DatabaseTask - AsyncTask which handles server connections and messaging
 * Created by Jason Mortara on 1/24/2016
 ****************************************************************************************/
public class DatabaseAsyncTask extends AsyncTask<Void, Integer, Integer>
{
	// statics
	private static final String TAG = "DatabaseTask";
	private Activity activity = null;

	// constructor
	public DatabaseAsyncTask(Activity activity)
	{
		Log.d(TAG, "DatabaseTask constructor, called from " + activity.getLocalClassName());

		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		Log.d(TAG, "onPreExecute");
		// called on thread init
		readDatabaseProperties();
	}

	private void readDatabaseProperties()
	{
		Log.d(TAG, "readDatabaseProperties");
		try
		{
			Properties dbProps = new Properties();

			InputStream in = activity.getBaseContext().getAssets().open("database.properties");
			dbProps.load(in);

			// store in Model
			Model.databaseProps = dbProps;

			// once props are loaded, test the db with the values defined therein
			Log.d(TAG, "readDatabaseProperties: Properties read.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected Integer doInBackground(Void... unused)
	{
		// need to force wait for debugger to breakpoint in this thread
		if(android.os.Debug.isDebuggerConnected())
		{
			android.os.Debug.waitForDebugger();
		}

		Log.d(TAG, "doInBackground: dbProps? " + Model.databaseProps.toString());
		int testSucceeded = 0;
		if (Model.databaseProps != null)
		{
			Log.d(TAG, "doInBackground: Attempting to test database");
			try
			{
				testSucceeded = MySQLAccessTester.test(Model.databaseProps);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				testSucceeded = 0;
			}
		}
		// update Model with connection status
		return testSucceeded;
	}


	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		 Log.d(TAG, "onProgressUpdate: " + progress);
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		String str = "onPostExecute: " + result;
		Log.d(TAG, str);
	}
} // end class DatabaseTask
