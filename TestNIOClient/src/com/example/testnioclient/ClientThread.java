package com.example.testnioclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientThread extends Thread
{
	private AtomicBoolean bRunning = new AtomicBoolean(false);
	private Selector mSelector = null;
	private Handler mHandler = null;
	
	private Charset mCharset = Charset.forName("UTF-8");
	
	private int mClientIndex;
	
	private List<String> mSendInfo = new LinkedList<String>();
	private Lock mLock = new ReentrantLock();
	
	public ClientThread(Handler handler, int clientIndex)
	{
		super();
		
		bRunning.set(true);
		mHandler = handler;
		mClientIndex = clientIndex;
	}
	
	public void stopClient()
	{
		if(!bRunning.get())
			return;
		
		bRunning.set(false);
		
		if(mSelector != null)
			mSelector.wakeup();
	}
	
	public void sendMessageToServer(String txt)
	{
		try
		{
			mLock.lock();
			
			mSendInfo.add(txt);
			if(mSelector != null)
				mSelector.wakeup();
		}
		finally
		{
			mLock.unlock();
		}
	}
	
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		if(bRunning.get())
		{
			boolean bConnected = false;
			boolean bServerClosed = false;
			
			InetSocketAddress inetAddress = new InetSocketAddress(GlobalField.SERVER_HOST, GlobalField.SERVER_PORT);
			SocketChannel clientChannel = null;
			
			try
			{
				mSelector = Selector.open();
				
				clientChannel = SocketChannel.open();
				clientChannel.configureBlocking(false);
				
				clientChannel.connect(inetAddress);
				
				clientChannel.register(mSelector, SelectionKey.OP_CONNECT);
				
				while(mSelector.select() >= 0)
				{
					for (SelectionKey selKey : mSelector.selectedKeys())
					{
						mSelector.selectedKeys().remove(selKey);
						
						if(selKey.isConnectable())
						{							
							Message msg = mHandler.obtainMessage(GlobalField.CLIENT_CONNECTED_SUCCESSED);
							msg.arg1 = mClientIndex;
							mHandler.sendMessage(msg);
							
							if(!clientChannel.finishConnect())
							{
								bConnected = false;
								break;
							}
							
							bConnected = true;
							clientChannel.register(mSelector, SelectionKey.OP_READ);
							
							clientChannel.write(mCharset.encode("Hello Server"));
							
//							{
//								ByteBuffer buffer = ByteBuffer.allocate(1024);
//								buffer.clear();
//								buffer.put(mCharset.encode("Hello Server"));
//								clientChannel.write(buffer);
//							}
						}
						
						if(selKey.isReadable())
						{
							StringBuilder content = new StringBuilder();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							boolean bHasError = false;
							
							try
							{
								while(clientChannel.read(buffer) > 0)
								{
									buffer.flip();
									content.append(mCharset.decode(buffer));
									buffer.clear();
								}
								
								selKey.interestOps(SelectionKey.OP_READ);
							}
							catch(IOException ex)
							{
								bHasError = true;
								
								selKey.cancel();
								if(selKey.channel() != null)
									selKey.channel().close();
								
								bServerClosed = true;
								
								break;
							}
							
							if(!bHasError)
							{
								Message msg = mHandler.obtainMessage(GlobalField.CLIENT_READ);
								msg.arg1 = mClientIndex;
								msg.obj = content;
								mHandler.sendMessage(msg);
							}
						}
					}
					
					try
					{
						mLock.lock();
						
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						
						if(bConnected && clientChannel.isConnected() && mSendInfo.size() > 0)
						{
							for (String sendTxt : mSendInfo)
							{
								buffer.clear();
								buffer.put(mCharset.encode(sendTxt));
								buffer.flip();
								clientChannel.write(buffer);
							}
							
						}
					}
					finally
					{
						mLock.unlock();
					}
					
					if(!bRunning.get())
					{
						break;
					}
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				bRunning.set(false);

				if(!bConnected)
				{
					Message msg = mHandler.obtainMessage(GlobalField.CLIENT_CONNECTED_FAILED);
					msg.arg1 = mClientIndex;
					mHandler.sendMessage(msg);
				}
				else
				{
					if(bServerClosed)
					{
						Message msg = mHandler.obtainMessage(GlobalField.CLIENT_SERVER_CLOSED);
						msg.arg1 = mClientIndex;
						mHandler.sendMessage(msg);
					}
					else
					{
						Message msg = mHandler.obtainMessage(GlobalField.CLIENT_CLOSED);
						msg.arg1 = mClientIndex;
						mHandler.sendMessage(msg);
					}
				}
				
				if(mSelector != null)
					try
					{
						mSelector.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
}
