package com.mortaramultimedia.deployedservertest;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ServerActivity extends Activity
{
	private static final String TAG = "ServerActivity";

	//message prefixes
	public static final String ECHO 					= "echo:";
	public static final String GET_OPPONENT_PORTS 		= "getOpponentPorts:";
	public static final String MESSAGE_PLAYER_PORT 		= "messagePlayer_port_";
	public static final String SELECT_OPPONENT_PORT 	= "selectOpponent_port_";
	public static final String MESSAGE_OPPONENT 		= "messageOpponent:";
	public static final String SEND_NEW_CURRENT_SCORE	= "sendNewCurrentScore:";

	private ServerTask serverTask;

	private Button connectButton;
	private TextView outgoingText;
	private TextView incomingText;
	private CheckBox connectedCheckBox;
//	private String incomingMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

		// UI refs
		connectButton 		= (Button)   findViewById(R.id.connectButton);
		outgoingText 		= (EditText) findViewById(R.id.outgoingText);
		incomingText 		= (TextView) findViewById(R.id.incomingText);
		connectedCheckBox 	= (CheckBox) findViewById(R.id.connectedCheckBox);

		updateUI();
    }

	public void updateUI()
	{
		connectButton.setClickable( !Model.connected );
		connectedCheckBox.setChecked( Model.connected );
		if ( Model.connected )
		{
			connectButton.setText( "Connected");
		}
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

    public void handleConnectButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleConnectButtonClick");

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
		outgoingText.setText( "" );
	}

    public void handleDoneButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleDoneButtonClick");
		hideSoftKeyboard();
	}

    public void handleEchoButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleEchoButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		serverTask.sendOutgoingMessageWithPrefix( ECHO, msg );
    }

    public void handleGetOpponentsButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleGetOpponentsButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		serverTask.sendOutgoingMessageWithPrefix( GET_OPPONENT_PORTS, null );
    }

    public void handleSelectOpponentButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleSelectOpponentButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		serverTask.sendOutgoingMessageWithPrefix( SELECT_OPPONENT_PORT, msg );
    }

    public void handleMessageOpponentButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleMessageOpponentButtonClick");
		hideSoftKeyboard();
		String msg = outgoingText.getText().toString();
		serverTask.sendOutgoingMessageWithPrefix( MESSAGE_OPPONENT, msg );
    }

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

		serverTask.sendOutgoingMessageWithPrefix( SEND_NEW_CURRENT_SCORE, Model.score.toString() );
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

	/**
	 * ServerTask - AsyncTask which handles server connections and messaging
	 */
	private class ServerTask extends AsyncTask<Void, Integer, Integer>
	{
		private ServerActivity serverActivity;
		private Socket s;
		private PrintWriter s_out;
		private BufferedReader s_in;

		// constructor
		ServerTask( ServerActivity sa )
		{
			Log.d(TAG, "ServerTask constructor");
			this.serverActivity = sa;
		}

		@Override
		protected Integer doInBackground(Void... unused)
		{
			s = new Socket();
			s_out = null;
			s_in = null;

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
					s_out.close();
					try
					{
						s_in.close();
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
					Log.d(TAG, "Connected.");

					// create writer for socket
					try
					{
						if ( s_out == null )
						{
							s_out = new PrintWriter( s.getOutputStream(), true);
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					//Send inirial message to server
					sendOutgoingMessageWithPrefix( ECHO, "Client says hello." );

					//reader for socket
					try
					{
						if (s_in == null)
						{
							s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}



					//Get response from server
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
			if (s_out != null)
			{
				s_out.close();
			}
			if (s_in != null)
			{
				try
				{
					s_in.close();
				}
				catch (IOException e)
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

		protected void sendMessage(String msg)
		{
			Log.d(TAG, "sendMessage: " + s.isConnected() + ", " + s_out.toString());
			if ( s.isConnected() )
			{
				if (s_out != null)
				{
					s_out.println( msg );
				}
			}
		}

		public void sendOutgoingMessageWithPrefix( String prefix, String msg )
		{
			Log.d(TAG, "sendOutgoingMessageWithPrefix: request from user: " + prefix + ", " + msg);

			String completeMessageToServer = "";

			if ( serverTask == null || !Model.connected )
			{
				Log.d(TAG, "sendOutgoingMessageWithPrefix: not connected");
				return;
			}

			/*
				public static final String ECHO 				= "echo:";
				public static final String GET_OPPONENT_PORTS 	= "getOpponentPorts:";
				public static final String MESSAGE_PLAYER_PORT 	= "messagePlayer_port_";
				public static final String SELECT_OPPONENT_PORT = "selectOpponent_port_";
				public static final String MESSAGE_OPPONENT 	= "messageOpponent:";
			 */

			// if connected (switch statement was not allowed by Java compiler for some reason)
			else
			{
				if ( prefix.equals( ECHO ) )
				{
					completeMessageToServer = prefix + msg;
				}
				else if ( prefix.equals( GET_OPPONENT_PORTS ) )
				{
					completeMessageToServer = prefix;
				}
				else if ( prefix.equals( MESSAGE_PLAYER_PORT ) )
				{
					completeMessageToServer = "";							// ex: 'messagePlayer_port_1234:hello other player' //TODO: won't work. need multiple input fields to handle this case
				}
				else if ( prefix.equals( SELECT_OPPONENT_PORT ) )
				{
					completeMessageToServer = prefix + msg + ":";			// ex: 'selectOpponent_port_12345:'
				}
				else if ( prefix.equals( MESSAGE_OPPONENT ) )
				{
					completeMessageToServer = prefix + ":" + msg;			// ex: 'messageOpponent:hello'
				}
				else if ( prefix.equals( SEND_NEW_CURRENT_SCORE ) )
				{
					completeMessageToServer = prefix + msg;			// ex: 'sendNewCurrentScore:12'
				}
				else
				{
					Log.w( TAG, "sendOutgoingMessageWithPrefix: WARNING - unknown prefix. request was: "  + prefix + ", " + msg );
					return;
				}

				Log.d( TAG, "sendOutgoingMessageWithPrefix: completeMessageToServer: " + completeMessageToServer );

				// send the complete assembled message to the server
				if ( s.isConnected() && completeMessageToServer.length() > 0 )
				{
					if (s_out != null)
					{
						s_out.println( completeMessageToServer );
					}
				}
			}

		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			// Log.d(TAG, "onProgressUpdate: " + progress);
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			String str = "onPostExecute: " + result;
			Log.d(TAG, str);
			updateUI();
		}
	} // end inner class

}
