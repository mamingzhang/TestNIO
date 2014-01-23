package com.example.testnio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ServerActivity extends Activity implements OnClickListener,
		OnEditorActionListener, TextWatcher
{
	private ServerThread mServerThread = null;
	private EditText mEditText = null;
	private TextView mTextView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_main);

		findViewById(R.id.startServer).setOnClickListener(this);
		findViewById(R.id.stopServer).setOnClickListener(this);
		findViewById(R.id.sendToClient).setOnClickListener(this);
		findViewById(R.id.stopServer).setEnabled(false);
		findViewById(R.id.sendToClient).setEnabled(false);

		mEditText = (EditText) findViewById(R.id.editTxt);
		mEditText.addTextChangedListener(this);
		mEditText.setOnEditorActionListener(this);
		mEditText.setEnabled(false);
		
		mTextView = (TextView) findViewById(R.id.serverInfo);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.startServer:
		{
			if (mServerThread != null)
			{
				mServerThread.stopThread();
				mServerThread = null;
			}

			mServerThread = new ServerThread(mHandler);
			mServerThread.start();

			findViewById(R.id.startServer).setEnabled(false);
			findViewById(R.id.stopServer).setEnabled(true);
			mEditText.setEnabled(true);
			break;
		}
		case R.id.stopServer:
		{
			if (mServerThread != null)
			{
				mServerThread.stopThread();
				mServerThread = null;
			}
			findViewById(R.id.startServer).setEnabled(true);
			findViewById(R.id.stopServer).setEnabled(false);
			break;
		}
		case R.id.sendToClient:
		{
			break;
		}
		}
	}

	private void appendTextToServerInfo(String appendText)
	{
		StringBuilder builder = new StringBuilder();
		String info = mTextView.getText().toString();
		if(TextUtils.isEmpty(info))
		{
			builder.append(appendText);
		}
		else
		{
			builder.append(appendText).append("\n").append(info);
		}
		mTextView.setText(builder);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
			case GlobalField.SERVER_STARTED:
				appendTextToServerInfo("Server Started Success");
				break;
			case GlobalField.SERVER_STOPTED:
				appendTextToServerInfo("Server Stopped");
				break;
			case GlobalField.SERVER_CLIENT_ACCEPTED:
				appendTextToServerInfo("Accept Client : "+msg.arg1);
				break;
			case GlobalField.SERVER_CLIENT_READ:
				appendTextToServerInfo("Read Client("+msg.arg1+") : "+msg.obj);
				break;
			case GlobalField.SERVER_CLIENT_CLOSED:
				appendTextToServerInfo("Client Closed : "+msg.arg1);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s)
	{
		// TODO Auto-generated method stub

	}

}
