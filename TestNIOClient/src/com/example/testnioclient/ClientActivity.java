package com.example.testnioclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ClientActivity extends Activity implements OnClickListener,
		TextWatcher, OnEditorActionListener
{
	private SparseArray<ClientThread> mClientThreadAry = new SparseArray<ClientThread>();
	private int mClientCount = 0;

	private EditText mEditText = null;
	private TextView mTextView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_main);

		findViewById(R.id.startClient).setOnClickListener(this);
		findViewById(R.id.stopClient).setOnClickListener(this);
		findViewById(R.id.sendToServer).setOnClickListener(this);
		findViewById(R.id.stopClient).setEnabled(false);
		findViewById(R.id.editTxt).setEnabled(false);
		findViewById(R.id.sendToServer).setEnabled(false);

		mEditText = (EditText) findViewById(R.id.editTxt);
		mEditText.addTextChangedListener(this);
		mEditText.setOnEditorActionListener(this);
		mEditText.setEnabled(false);

		mTextView = (TextView) findViewById(R.id.clientInfo);
	}

	private void appendTextToServerInfo(String appendText)
	{
		StringBuilder builder = new StringBuilder();
		String info = mTextView.getText().toString();
		if (TextUtils.isEmpty(info))
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
			case GlobalField.CLIENT_CLOSED:
				appendTextToServerInfo("Client Closed : " + msg.arg1);
				mClientThreadAry.remove(msg.arg1);
				if (mClientThreadAry.size() == 0)
				{
					findViewById(R.id.stopClient).setEnabled(false);
					findViewById(R.id.sendToServer).setEnabled(false);
					mEditText.setText("");
					mEditText.setEnabled(false);
				}
				break;
			case GlobalField.CLIENT_READ:
				appendTextToServerInfo("Client Read(" + msg.arg1 + ") : "
						+ msg.obj);
				break;
			case GlobalField.CLIENT_SERVER_CLOSED:
				appendTextToServerInfo("Client Server Closed : " + msg.arg1);
				mClientThreadAry.clear();
				findViewById(R.id.stopClient).setEnabled(false);
				findViewById(R.id.sendToServer).setEnabled(false);
				mEditText.setText("");
				mEditText.setEnabled(false);
				break;
			case GlobalField.CLIENT_CONNECTED_FAILED:
				appendTextToServerInfo("Client Connect Failed : " + msg.arg1);
				mClientThreadAry.remove(msg.arg1);
				if (mClientThreadAry.size() == 0)
				{
					findViewById(R.id.stopClient).setEnabled(false);
					findViewById(R.id.sendToServer).setEnabled(false);
					mEditText.setText("");
					mEditText.setEnabled(false);
				}
				break;
			case GlobalField.CLIENT_CONNECTED_SUCCESSED:
				appendTextToServerInfo("Client Connect Success : " + msg.arg1);
				findViewById(R.id.stopClient).setEnabled(true);
				mEditText.setEnabled(true);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.startClient:
		{
			ClientThread clientThread = new ClientThread(mHandler,
					++mClientCount);
			mClientThreadAry.put(mClientCount, clientThread);
			clientThread.start();
			break;
		}
		case R.id.stopClient:
		{
			if (mClientThreadAry.size() > 0)
			{
				int key = mClientThreadAry.keyAt(0);
				ClientThread clientThread = mClientThreadAry.get(key);
				clientThread.stopClient();
				mClientThreadAry.remove(key);
			}

			if (mClientThreadAry.size() == 0)
			{
				findViewById(R.id.stopClient).setEnabled(false);
				findViewById(R.id.sendToServer).setEnabled(false);
				mEditText.setText("");
				mEditText.setEnabled(false);
			}
			break;
		}
		case R.id.sendToServer:
		{
			String input = mEditText.getText().toString();
			if (!TextUtils.isEmpty(input))
			{
				for (int index = 0; index < mClientThreadAry.size(); index++)
				{
					ClientThread clientThread = mClientThreadAry.valueAt(index);
					clientThread.sendMessageToServer(input);
				}
			}

			mEditText.setText("");
			findViewById(R.id.sendToServer).setEnabled(false);
			break;
		}
		}
	}

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
		String input = mEditText.getText().toString();
		if (TextUtils.isEmpty(input))
		{
			findViewById(R.id.sendToServer).setEnabled(false);
		}
		else
		{
			findViewById(R.id.sendToServer).setEnabled(true);
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		// TODO Auto-generated method stub
		String input = mEditText.getText().toString();
		if (TextUtils.isEmpty(input))
		{
			findViewById(R.id.sendToServer).setEnabled(false);
		}
		else
		{
			findViewById(R.id.sendToServer).setEnabled(true);
		}
	}

}
