package com.mortaramultimedia.deployedservertest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mortaramultimedia.deployedservertest.database.LoginAsyncTask;
import com.mortaramultimedia.deployedservertest.interfaces.IAsyncTaskCompleted;
import com.mortaramultimedia.wordwolf.shared.messages.LoginRequest;

import java.util.ArrayList;
import java.util.List;


/**
 * Login Activity
 * @author jason mortara
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>, IAsyncTaskCompleted
{
	private static final String TAG = "LoginActivity";

	LoginAsyncTask loginTask;

	// UI references.
	private View mLoginFormView;						// main form
	private AutoCompleteTextView mUsernameView;	// username text field
	private AutoCompleteTextView mEmailView;		// email text field
	private EditText mPasswordView;					// password text field
	private Button mSignInView;						// sign in / register button
	private View mProgressView;						// login progress bar/wheel

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// check for an Intent and any params it carries
		Intent intent = getIntent();
		String testValue = intent.getStringExtra("testParam");
		Log.d(TAG, "onCreate found an intent testParam of: " + testValue);

		// assign UI references
		mLoginFormView = (View) 						findViewById(R.id.login_form);
		mUsernameView 	= (AutoCompleteTextView) 	findViewById(R.id.username);
		mEmailView 		= (AutoCompleteTextView) 	findViewById(R.id.email);
		mPasswordView 	= (EditText) 					findViewById(R.id.password);
		mSignInView 	= (Button) 						findViewById(R.id.account_sign_in_or_register_button);
		mProgressView 	= (View) 						findViewById(R.id.login_progress);

		// assign Sign In / Register button behavior
		mSignInView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.d(TAG, "mSignInView clicked");
				attemptLogin();
			}
		});
	}


	/********************************************************
	 * Attempt the Login once the user fields are filled in
	 */
	private void attemptLogin()
	{
		Log.d(TAG, "attemptLogin");

		if (loginTask != null)
		{
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values as locals at the time of the login attempt.
		String username 	= mUsernameView.getText().toString();
		String email 		= mEmailView.getText().toString();
		String password 	= mPasswordView.getText().toString();

		boolean cancel = false;		// cancel?
		View focusView = null;		// the focus shifts depending on user input

		// Check for a valid username, if the user entered one.
		if (!TextUtils.isEmpty(username) && !isUsernameValid(username))
		{
			mPasswordView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email))
		{
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		else if (!isEmailValid(email))
		{
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			// store the login credentials in the Model
			LoginRequest loginRequest = new LoginRequest(1, username, password, email);
			Model.userLogin = loginRequest;

			// Show a progress spinner,
			showProgress(true);

			// and kick off a background task to perform the user login attempt.
			loginTask = new LoginAsyncTask(this, this);

			// workaround for issues with execute() not working properly on AsyncTasks
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else
			{
//				loginTask.execute();
				loginTask.execute((Void) null);
			}
		}
	}


	/*****************************************************
	 * Text Field Validators
	 */
	private boolean isEmailValid(String email)
	{
		return email.contains("@") && email.contains(".");
	}

	private boolean isUsernameValid(String username)
	{
		return username.length() > 2;
	}

		private boolean isPasswordValid(String password)
	{
		return password.length() > 4;
	}


	/********************************************************
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{
		Log.d(TAG, "showProgress");

		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		}
		else
		{
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}


	/*****************************************************
	 * Overrides for the LoaderCallbacks interface methods
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		Log.d(TAG, "onCreateLoader");

		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
	{
		Log.d(TAG, "onLoadFinished");

		List<String> emails = new ArrayList<>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader)
	{
		Log.d(TAG, "onLoaderReset");
	}

	private void addEmailsToAutoComplete(List<String> emailAddressCollection)
	{
		Log.d(TAG, "addEmailsToAutoComplete");

		//Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
		ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}



	private interface ProfileQuery
	{
		String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
		};

		int ADDRESS = 0;
		int IS_PRIMARY = 1;
	}

	/**********************************************************
	 * Overrides for the IAsyncTaskCompleted interface methods
	 */
	@Override
	public void onTaskCompleted()
	{
		// set a result code for the intent to be returned to the activity which called this one
		Intent returnIntent = new Intent();
		int resultCode;

		if(Model.loggedIn)
		{
			resultCode = 1;
			Log.d(TAG, "onTaskCompleted successful with result code: " + resultCode);
		}
		else
		{
			resultCode = 0;
			Log.d(TAG, "onTaskCompleted canceled with result code: " + resultCode);
		}
		setResult(resultCode, returnIntent);

		this.finish();
	}


}

