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
	public static final String ECHO = "echo:";
	public static final String MESSAGE_PLAYER_PORT = "messagePlayer_port_";

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

    public void handleSendButtonClick(View view) throws IOException
	{
		Log.d(TAG, "handleSendButtonClick");

		hideSoftKeyboard();

		if ( serverTask != null && Model.connected )
		{
			String msg = outgoingText.getText().toString();
			Log.d(TAG, "handleSendButtonClick: entered outgoing text is: " + msg);
			serverTask.sendMessage( msg );
		}
		else
		{
			Log.d(TAG, "handleSendButtonClick: not connected" );
		}

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
					sendMessage( "echo:Client says hello." );

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
