package com.mortaramultimedia.deployedservertest.interfaces;

/**
 * Created by Jason Mortara on 1/25/2016.
 */
public interface IAsyncTaskCompleted
{
	/**
	 * Called when the Async task has completed, typically during onPostExecute()
	 */
	void onTaskCompleted();
}
