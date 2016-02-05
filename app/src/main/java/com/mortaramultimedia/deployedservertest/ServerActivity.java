package com.mortaramultimedia.deployedservertest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.mortaramultimedia.deployedservertest.communications.Comm;
import com.mortaramultimedia.deployedservertest.database.LoginAsyncTask;
import com.mortaramultimedia.deployedservertest.database.DatabaseAsyncTask;
import com.mortaramultimedia.deployedservertest.interfaces.IAsyncTaskCompleted;
import com.mortaramultimedia.wordwolf.shared.constants.Constants;
import com.mortaramultimedia.wordwolf.shared.messages.ConnectToDatabaseResponse;
import com.mortaramultimedia.wordwolf.shared.messages.GetPlayerListRequest;
import com.mortaramultimedia.wordwolf.shared.messages.GetPlayerListResponse;
import com.mortaramultimedia.wordwolf.shared.messages.LoginRequest;
import com.mortaramultimedia.wordwolf.shared.messages.LoginResponse;
import com.mortaramultimedia.wordwolf.shared.messages.OpponentBoundMessage;
import com.mortaramultimedia.wordwolf.shared.messages.SelectOpponentRequest;
import com.mortaramultimedia.wordwolf.shared.messages.SelectOpponentResponse;
import com.mortaramultimedia.wordwolf.shared.messages.SimpleMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import messages.LoginMessage;


public class ServerActivity extends Activity implements IAsyncTaskCompleted
{
	private static final String TAG = "ServerActivity";

	private ServerTask serverTask;				// inner async task which handles in/out socket streams
	private DatabaseAsyncTask databaseTask;	// external async task
	private LoginAsyncTask loginTask;			// external async task

	private Button connectButton;
	private Button testDatabaseButton;
	private Button loginButton;
	private TextView userNameText;
	private TextView outgoingText;
	private TextView incomingText;
	private CheckBox connectedCheckBox;
	private CheckBox databaseCheckBox;
	private CheckBox loginCheckBox;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

		// UI refs
		connectButton 		= (Button)   findViewById(R.id.connectButton);
		testDatabaseButton= (Button)   findViewById(R.id.testDatabaseButton);
		loginButton 		= (Button)   findViewById(R.id.loginButton);
		userNameText		= (TextView) findViewById(R.id.userNameText);
		outgoingText 		= (EditText) findViewById(R.id.outgoingText);
		incomingText 		= (TextView) findViewById(R.id.incomingText);
		connectedCheckBox = (CheckBox) findViewById(R.id.connectedCheckBox);
		databaseCheckBox 	= (CheckBox) findViewById(R.id.databaseCheckBox);
		loginCheckBox 		= (CheckBox) findViewById(R.id.loginCheckBox);

		updateUI();
	}

	public void updateUI()
	{
		Log.d(TAG, "updateUI");

		// Connection UI
		connectButton.setClickable(!Model.connected);
		connectedCheckBox.setChecked(Model.connected);
		if (Model.connected)
		{
			connectButton.setText("Server\nOK");
		}

		// DB Test UI
		testDatabaseButton.setClickable(!Model.connectedToDatabase);
		databaseCheckBox.setChecked(Model.connectedToDatabase);
		if (Model.connectedToDatabase)
		{
			testDatabaseButton.setText("DB\nOK");
		}

		// Login UI
		loginButton.setClickable(!Model.loggedIn);
		loginCheckBox.setChecked(Model.loggedIn);
		if (Model.loggedIn)
		{
			loginButton.setText("Logged\nIn");
		}

		// Login info
		if (Model.userLogin != null)
		{
			String username = Model.userLogin.getUserName();
			if (username != null)
			{
				userNameText.setText(username);
			}
		}

		// Messages
//		if(Model.incomingMessage != null)
//		{
			incomingText.setText(Model.incomingMessage);
//		}
	}

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/

	/**
	 * Connect to the Word Wolf Server
	 * @param view
	 * @throws IOException
	 */
	public void handleConnectButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleConnectButtonClick");

		if (Model.connected)
		{
			Log.d(TAG, "handleConnectButtonClick: already connected!");
			return;
		}
		// Start network tasks separate from the main UI thread
		if ( serverTask == null && !Model.connected )
		{
			serverTask = new ServerTask( this );
			serverTask.execute();
		}
		else
		{
			Log.d(TAG, "handleConnectButtonClick: not connected");
		}
	}

	public void handleOutgoingTextClick(View view)
	{
		Log.d(TAG, "handleOutgoingTextClick");
		clearOutgoingText();
	}

	private void clearOutgoingText()
	{
		Log.d(TAG, "clearOutgoingText");
		outgoingText.setText("");
	}

	public void handleDoneButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleDoneButtonClick");
		hideSoftKeyboard();
	}

	public void handleMessageButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleMessageButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		SimpleMessage msgObj = new SimpleMessage(msg, true);
//		serverTask.sendOutgoingMessageWithPrefix(ECHO, msg);
		serverTask.sendOutgoingObject(msgObj);
	}

	public void handleEchoButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleEchoButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		SimpleMessage msgObj = new SimpleMessage(msg, false);
//		serverTask.sendOutgoingMessageWithPrefix(ECHO, msg);
		serverTask.sendOutgoingObject(msgObj);
	}

	public void handleSetUsernameButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleSetUsernameButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		//serverTask.sendOutgoingMessageWithPrefix(SET_USERNAME, msg);
		//TODO add serverTask.sendOutgoingObject(SetUsernameRequest);
	}

	public void handleGetUsernameButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleGetUsernameButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		//serverTask.sendOutgoingMessageWithPrefix(GET_USERNAME, null);
		//TODO add serverTask.sendOutgoingObject(GetUsernameRequest);
	}

	public void handleGetAllPlayersButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleGetAllPlayersButtonClick");
		hideSoftKeyboard();
		GetPlayerListRequest getPlayerListRequest = new GetPlayerListRequest(GetPlayerListRequest.REQUEST_TYPE_ALL_PLAYERS);
		serverTask.sendOutgoingObject(getPlayerListRequest);
	}


/*
	public void handleGetOpponentsButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleGetOpponentsButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		//serverTask.sendOutgoingMessageWithPrefix( GET_OPPONENT_PORTS, null );
		serverTask.sendOutgoingMessageWithPrefix( GET_OPPONENT_USERNAMES, null );
	}
*/

	public void handleSelectOpponentButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleSelectOpponentButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		//serverTask.sendOutgoingMessageWithPrefix( SELECT_OPPONENT_PORT, msg );
//		serverTask.sendOutgoingMessageWithPrefix(SELECT_OPPONENT_USERNAME, msg);	// deprecated system
		SelectOpponentRequest request = new SelectOpponentRequest(Model.userLogin.getUserName(), msg);
		serverTask.sendOutgoingObject(request);
	}

	public void handleMessageOpponentButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleMessageOpponentButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		//serverTask.sendOutgoingMessageWithPrefix( GET_OPPONENT_USERNAMES, null );	// deprecated system
		try
		{
			OpponentBoundMessage msgObj = new OpponentBoundMessage(msg, false);	// this constructor assumes the server handles figuring out who is the opponent for the msg destination
			serverTask.sendOutgoingObject(msgObj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void handleTestDatabaseButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleTestDatabaseButtonClick");
		databaseTask = new DatabaseAsyncTask(this, this);

		// workaround for issues with execute() not working properly on AsyncTasks
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			databaseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			databaseTask.execute();
		}
	}

	/**
	 * Login Button handler - launches LoginActivity
	 * @param view
	 * @throws IOException
	 */
	public void handleLoginButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleTestDatabaseButtonClick");

		// create an Intent, with optional additional params
		Context thisContext = ServerActivity.this;
		Intent intent = new Intent(thisContext, LoginActivity.class);
		intent.putExtra("testParam", "testValue");								//optional params

		// start the activity
		startActivityForResult(intent, 1);		//TODO: note that in order for this class' onActivityResult to be called when the LoginActivity has completed, the requestCode here must be > 0
	}

	// original login button handler -- attempted login with hardcoded credentials
/*	public void handleLoginButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleTestDatabaseButtonClick");

		// next try a login	//TODO get this from input fields ************************************
		LoginMessage newLogin = new LoginMessage(1, "test1", "test1pass", "test1@wordwolfgame.com");    //TODO: HARDCODED
		Model.userLogin = newLogin;

		loginTask = new LoginAsyncTask(this, this);

		// workaround for issues with execute() not working properly on AsyncTasks
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			loginTask.execute();
		}
	}*/

	public void handleSendNewScoreOfButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleSendNewScoreOfButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		Integer newScore;
		try
		{
			newScore = Integer.parseInt( msg );
		}
		catch ( Error e )
		{
			// just substitute an incremented score if the conversion was invalid
			newScore = Model.score + 1;
		}

		Model.score = newScore;

		//serverTask.sendOutgoingMessageWithPrefix( SEND_NEW_CURRENT_SCORE, Model.score.toString() );	// deprecated
		//TODO: add new Score update via writeObject()
	}

	public void handleLoginTestButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleLoginTestButtonClick: USING HARDCODE VALUES, NOT INPUT FIELD DATA");
		hideSoftKeyboard();

		LoginRequest testLoginRequest = new LoginRequest(2, "test2", "test2pass", "test2@wordwolfgame.com");
		Model.userLogin = testLoginRequest;
		serverTask.sendOutgoingObject(testLoginRequest);
	}

	private void hideSoftKeyboard()
	{
		Log.d(TAG, "hideSoftKeyboard");
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(outgoingText.getWindowToken(), 0);
	}

	public void handleClearButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleClearButtonClick");
		clearOutgoingText();
	}

	/*
	public void handleIncomingMessage( String msg )
	{
		Log.d(TAG, "handleIncomingMessage: " + msg);
		incomingText.setText( "TEST" );
	}
	*/

	/****************************************************************************************
	 * ServerTask - AsyncTask which handles server connections and messaging
	 ****************************************************************************************/
	private class ServerTask extends AsyncTask<Void, Integer, Integer>
	{
		private ServerActivity serverActivity;
		private Socket s;
//		private PrintWriter s_out;
//		private BufferedReader s_in;
		private ObjectOutputStream s_objOut;
		private ObjectInputStream s_objIn;

		// constructor
		ServerTask( ServerActivity sa )
		{
			Log.d(TAG, "ServerTask constructor");
			this.serverActivity = sa;
		}

		@Override
		protected Integer doInBackground(Void... unused)
		{
			// need to force wait for debugger to breakpoint in this thread
			if(android.os.Debug.isDebuggerConnected())
			{
				android.os.Debug.waitForDebugger();
			}

			s = new Socket();
//			s_out = null;
//			s_in = null;

			try
			{
				Log.d(TAG, "Attempting to connect to " + Model.HOST + " " + Model.PORT);
				try
				{
					s.connect(new InetSocketAddress( Model.HOST, Model.PORT ));
				}
				//Host not found
				catch (UnknownHostException e)
				{
					System.err.println("Don't know about host : " + Model.HOST);
					System.exit(1);
//					s_out.close();
					try
					{
						s_objOut.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					try
					{
//						s_in.close();
						s_objIn.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					try
					{
						s.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				catch (SecurityException e)
				{
					Log.d(TAG, "SecurityException: " + e.getMessage());
					e.printStackTrace();
				}
				catch (IOException e)
				{
					Log.d(TAG, "IOException: " + e.getMessage());
					e.printStackTrace();
				}

				// update Model with connection status
				Model.connected = s.isConnected();

				if ( s.isConnected() )
				{
					Log.d(TAG, "Connected to wwss.");

					// create writer for socket
					try
					{
//						if ( s_out == null )
//						{
//							s_out = new PrintWriter( s.getOutputStream(), true);
//						}
						if ( s_objOut == null )
						{
							s_objOut = new ObjectOutputStream(s.getOutputStream());
							Comm.setOut(s_objOut);
							Log.d(TAG, "Created ObjectOutputStream.");
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					//Send initial message to server
//					sendOutgoingMessageWithPrefix( ECHO, "Client says hello." );
					SimpleMessage msgObj = new SimpleMessage(Constants.HELLO_SERVER, true);
					sendOutgoingObject(msgObj);

					//reader for socket
					try
					{
//						if (s_in == null)
//						{
//							s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//						}
						if (s_objIn == null)
						{
							s_objIn = new ObjectInputStream(s.getInputStream());
							Comm.setIn(s_objIn);	// create reference for other classes to use
							Log.d(TAG, "Created ObjectInputStream.");
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}



					//Get response from server
/*
					String response;
					if (s_in != null)
					{
						try
						{
							while ((response = s_in.readLine()) != null)
							{
								Log.d(TAG, "Server message: " + response );

								Model.setIncomingMessage( response );

								// UI updates, which require this special method to run from this thread
								ServerActivity.this.runOnUiThread(new Runnable()
								{
									public void run()
									{
										updateUI();
										incomingText.setText( Model.incomingMessage );
									}
								});
							}
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						return 2;
					}
*/

					// obj response
					Object responseObj;
					if(s_objIn != null)
					{
						try
						{
							while ((responseObj = s_objIn.readObject()) != null)
							{
								Log.d(TAG, "Server response obj: " + responseObj );
								//Model.setIncomingMessageObj(responseObj);
								//TODO: FILL IN RESPONSE HANDLING
								handleIncomingObject(responseObj);
							}
						}
						catch(IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
					}

				}
				else
				{
					Log.d(TAG, "Not connected.");
				}

			}
			catch (Error e)
			{
				e.printStackTrace();
			}



			// other stuff? close?
			Log.d(TAG, "Continuing...");
//			if (s_out != null)
//			{
//				s_out.close();
//			}
			if (s_objOut != null)
			{
				try
				{
					s_objOut.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

//			if (s_in != null)
//			{
//				try
//				{
//					s_in.close();
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
			if (s_objIn != null)
			{
				try
				{
					s_objIn.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				s.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return 1;
		}

		/**
		 * Handle all incoming object types.
		 * @param obj
		 */
		private void handleIncomingObject(Object obj)
		{
			Log.d(TAG, "handleIncomingObject: " + obj);

			/**
			 * If receiving a SimpleMessage, log the message. If echo was requested, send it back to the client as well.
			 */
			if(obj instanceof SimpleMessage)
			{
				handleSimpleMessage(((SimpleMessage) obj));
			}
			/**
			 * If receiving a ConnectToDatabaseResponse, if it is a successful one, that means we can then attempt a login or create account.
			 */
			if(obj instanceof ConnectToDatabaseResponse)
			{
				handleConnectToDatabaseResponse(((ConnectToDatabaseResponse) obj));
			}
			/**
			 * If receiving a LoginResponse...
			 */
			else if(obj instanceof LoginResponse)
			{
				handleLoginResponse(((LoginResponse) obj));
			}
			/**
			 * If receiving a GetPlayerListResponse
			 */
			else if(obj instanceof GetPlayerListResponse)
			{
				handleGetPlayerListResponse(((GetPlayerListResponse) obj));
			}
			/**
			 * If receiving a SelectOpponentRequest, which is a request from another player to become opponents.
			 */
			else if(obj instanceof SelectOpponentRequest)
			{
				handleRequestToBecomeOpponent(((SelectOpponentRequest) obj));
			}
			/**
			 * If receiving a SelectOpponentResponse, which is a response to a request to become another player's opponent.
			 */
			else if(obj instanceof SelectOpponentResponse)
			{
				handleSelectOpponentResponse(((SelectOpponentResponse) obj));
			}
			/**
			 * If receiving an OpponentBoundMessage, which is a message to this client from this player's opponent.
			 */
			else if(obj instanceof OpponentBoundMessage)
			{
				handleMessageFromOpponent(((OpponentBoundMessage) obj));
			}
			/**
			 * If receiving a CreateNewAccountResponse...
			 */
			/*else if(obj instanceof CreateNewAccountResponse)
			{
				handleCreateNewAccountResponse(((CreateNewAccountResponse) obj), out);
			}*/
			/**
			 * If receiving a CreateGameResponse...
			 */
			/*else if(obj instanceof CreateGameResponse)
			{
				handleCreateGameResponse(((CreateGameResponse) obj), out);
			}*/


		}

		//TODO: use sendOutgoingObject instead
		protected void sendMessage(String msg)
		{
			Log.d(TAG, "sendMessage: " + s.isConnected() + ", " + msg);
//			Log.d(TAG, "sendMessage: " + s.isConnected() + ", " + s_out.toString());
//			if ( s.isConnected() )
//			{
//				if (s_out != null)
//				{
//					s_out.println( msg );
//				}
//			}
		}

		public void sendOutgoingObject(Object obj)
		{
			Log.d(TAG, "sendOutgoingObject: " + obj);

			if ( serverTask == null || !Model.connected )
			{
				Log.d(TAG, "sendOutgoingObject: WARNING: not connected. Ignoring.");
				return;
			}

			// send it to the server
			if ( s.isConnected() && obj != null )
			{
				if (s_objOut != null)
				{
					try
					{
						s_objOut.writeObject(obj);
						s_objOut.flush();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		/*
		public void sendOutgoingMessageWithPrefix( String prefix, String msg )
		{
			Log.d(TAG, "sendOutgoingMessageWithPrefix: Response from user: " + prefix + ", " + msg);

			String completeMessageToServer = "";

			if ( serverTask == null || !Model.connected )
			{
				Log.d(TAG, "sendOutgoingMessageWithPrefix: not connected");
				return;
			}

			// if connected (switch statement was not allowed by Java compiler for some reason)
			else
			{
				if ( prefix.equals( ECHO ) )
				{
					completeMessageToServer = prefix + msg;
				}
				else if ( prefix.equals( SET_USERNAME ) )
				{
					completeMessageToServer = prefix + msg;
				}
				else if ( prefix.equals( GET_USERNAME ) )
				{
					completeMessageToServer = prefix;
				}
				else if ( prefix.equals( GET_OPPONENT_USERNAMES ) )
				{
					completeMessageToServer = prefix;
				}
				else if ( prefix.equals( SELECT_OPPONENT_USERNAME ) )
				{
					completeMessageToServer = prefix + msg;
				}

				else if ( prefix.equals( GET_OPPONENT_PORTS ) )
				{
					completeMessageToServer = prefix;
				}
				else if ( prefix.equals( MESSAGE_PLAYER_PORT ) )			//TODO: won't work. need multiple input fields to handle this case
				{
					completeMessageToServer = "";							// ex: 'messagePlayer_port_1234:hello other player'
				}
				else if ( prefix.equals( SELECT_OPPONENT_PORT ) )
				{
					completeMessageToServer = prefix + msg + ":";			// ex: 'selectOpponent_port_12345:'
				}

				else if ( prefix.equals( MESSAGE_OPPONENT ) )
				{
					completeMessageToServer = prefix + msg;					// ex: 'messageOpponent:hello'
				}
				else if ( prefix.equals( SEND_NEW_CURRENT_SCORE ) )
				{
					completeMessageToServer = prefix + msg;					// ex: 'sendNewCurrentScore:12'
				}
				else
				{
					Log.w( TAG, "sendOutgoingMessageWithPrefix: WARNING - unknown prefix. request was: "  + prefix + ", " + msg );
					return;
				}

				Log.d( TAG, "sendOutgoingMessageWithPrefix: completeMessageToServer: " + completeMessageToServer );

				// send the complete assembled message to the server
//				if ( s.isConnected() && completeMessageToServer.length() > 0 )
//				{
//					if (s_out != null)
//					{
//						s_out.println( completeMessageToServer );
//					}
//				}
			}

		}
		*/

		private void handleSimpleMessage(SimpleMessage msgObj)
		{
			Log.d(TAG, "handleSimpleMessage: " + msgObj.getMsg());
			publishObject(msgObj);
		}

		private void handleConnectToDatabaseResponse(ConnectToDatabaseResponse response)
		{
			Log.d(TAG, "handleConnectToDatabaseResponse: " + response);
			Model.connectedToDatabase = response.getSuccess();
			publishObject(response);
		}

		private void handleLoginResponse(LoginResponse response)
		{
			Log.d(TAG, "handleLoginResponse: " + response);
			publishObject(response);
		}

		private void handleGetPlayerListResponse(GetPlayerListResponse response)
		{
			Log.d(TAG, "handleGetPlayerListResponse: " + response);
			publishObject(response);
			Log.d(TAG, "handleGetPlayerListResponse: player list: " + response.getPlayersCopy());
		}

		private void handleRequestToBecomeOpponent(SelectOpponentRequest request)
		{
			Log.d(TAG, "handleRequestToBecomeOpponent: " + request);
			publishObject(request);
			Log.d(TAG, "handleRequestToBecomeOpponent: YOU HAVE BEEN OFFERED TO BECOME AN OPPONENT OF: " + request.getSourceUsername());



			// TODO: WE ARE AUTOMATICALLY ACCEPTING THE REQUEST HERE
			Log.d(TAG, "handleRequestToBecomeOpponent: Model.connected: " + Model.connected);
			Log.d(TAG, "handleRequestToBecomeOpponent: Model.loggedIn: " + Model.loggedIn);
			Log.d(TAG, "handleRequestToBecomeOpponent: Model.userLogin.getUserName(): " + Model.userLogin.getUserName());
			try
			{
				if (Model.connected && /*Model.loggedIn &&*/ Model.userLogin.getUserName() != null)
				{
					Log.d(TAG, "handleRequestToBecomeOpponent: accepting opponent request: " + Model.userLogin.getUserName());
					SelectOpponentResponse response = new SelectOpponentResponse(true, Model.userLogin.getUserName(), request.getSourceUsername());
					response.setRequestAccepted(true);	//TODO: roll into response obj params
					sendOutgoingObject(response);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		private void handleSelectOpponentResponse(SelectOpponentResponse response)
		{
			Log.d(TAG, "handleSelectOpponentResponse: " + response);
			publishObject(response);
			if(response.getRequestAccepted() == true)
			{
				Log.d(TAG, "handleRequestToBecomeOpponent: REQUEST ACCEPTED! from: " + response.getSourceUsername());
			}
			else
			{
				Log.d(TAG, "handleRequestToBecomeOpponent: REQUEST REJECTED! from: " + response.getSourceUsername());
			}
		}

		private void handleMessageFromOpponent(OpponentBoundMessage msgObj)
		{
			Log.d(TAG, "handleMessageFromOpponent: " + msgObj);
			publishObject(msgObj);
		}

		/**
		 * Store the most recent incoming message Object as a String in the Model, and use the AsyncTasks's onProgressUpdate to display it in the test UI.
		 * This effectively displays incoming messages in the UI while the ServerTask thread is running.
		 * @param obj
		 */
		private void publishObject(Object obj)
		{
			Model.incomingMessage = obj.toString();
			publishProgress(1);
		}


		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			 Log.d(TAG, "onProgressUpdate");
			updateUI();
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			String str = "onPostExecute: " + result;
			Log.d(TAG, str);
			updateUI();
		}
	} // end inner class ServerTask

	/**
	 * Handle the result of the activity launched from this one.
	 * TODO - THE REQUEST CODES ARE NOT WORKING CORRECTLY... FAILED LOGINS ON SERVER SIDE RESULT IN REQUESTCODE 1 HERE
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode == 1)	// see the note in the startActivityForResult above
		{
			Log.d(TAG, "onActivityResult: LOGIN SUCCESS returned from LoginActivity. requestCode: " + requestCode);
			Model.loggedIn = true;
		}
		else
		{
			Log.d(TAG, "onActivityResult: LOGIN FAILURE returned from LoginActivity. requestCode: " + requestCode);
			Model.loggedIn = false;
		}
		updateUI();
	}

	/**
	 * IAsyncTaskCompleted overrides
	 * TODO = still called?
	 */
	@Override
	public void onTaskCompleted()
	{
		Log.d(TAG, "onTaskCompleted");
		updateUI();
	}


}
