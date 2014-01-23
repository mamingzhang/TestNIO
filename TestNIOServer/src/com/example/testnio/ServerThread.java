package com.example.testnio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Message;

public class ServerThread extends Thread
{

	private AtomicBoolean bRunning = new AtomicBoolean(false);
	private Selector mSelector = null;
	private Handler mHandler = null;
	
	private Charset mCharset = Charset.forName("UTF-8");
	
	private int mClientIndex = 0;
	
	public ServerThread(Handler handler)
	{
		// TODO Auto-generated constructor stub
		bRunning.set(true);
		mHandler = handler;
	}
	
	public void stopThread()
	{
		if(!bRunning.get())
			return;
		
		bRunning.set(false);
		
		if(mSelector != null)
			mSelector.wakeup();
	}
	
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		if(bRunning.get())
		{
			try
			{
				mSelector = Selector.open();
				
				ServerSocketChannel serverChannel = ServerSocketChannel.open();
				InetSocketAddress inetAddress = new InetSocketAddress(GlobalField.SERVER_HOST, GlobalField.SERVER_PORT);
				serverChannel.socket().bind(inetAddress);
				serverChannel.configureBlocking(false);
				
				serverChannel.register(mSelector, SelectionKey.OP_ACCEPT);
				
				{
					Message msg = mHandler.obtainMessage(GlobalField.SERVER_STARTED);
					mHandler.sendMessage(msg);
				}
				
				while(mSelector.select() > 0)
				{
					if(!bRunning.get())
					{
						break;
					}
					
					for(SelectionKey selKey : mSelector.selectedKeys())
					{
						mSelector.selectedKeys().remove(selKey);
						
						if(selKey.isAcceptable())
						{
							++mClientIndex;
							
							SocketChannel clientChannel = serverChannel.accept();
							clientChannel.configureBlocking(false);
							clientChannel.register(mSelector, SelectionKey.OP_READ, mClientIndex);
							
							{
								Message msg = mHandler.obtainMessage(GlobalField.SERVER_CLIENT_ACCEPTED);
								msg.arg1 = mClientIndex;
								mHandler.sendMessage(msg);
							}
							
							serverChannel.register(mSelector, SelectionKey.OP_ACCEPT);
						}
						
						if(selKey.isReadable())
						{
							SocketChannel clientChannel = (SocketChannel) selKey.channel();
							int clientIndex = ((Integer)selKey.attachment()).intValue();
							
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
								
								{
									Message msg = mHandler.obtainMessage(GlobalField.SERVER_CLIENT_CLOSED);
									msg.arg1 = clientIndex;
									mHandler.sendMessage(msg);
								}
							}
							
							if(!bHasError)
							{
								Message msg = mHandler.obtainMessage(GlobalField.SERVER_CLIENT_READ);
								msg.arg1 = clientIndex;
								msg.obj = content;
								mHandler.sendMessage(msg);
							}
						}
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
			finally
			{
				if(mSelector != null && mSelector.isOpen())
				{
					try
					{
						mSelector.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mSelector = null;
				}
				
				Message msg = mHandler.obtainMessage(GlobalField.SERVER_STOPTED);
				mHandler.sendMessage(msg);
			}
		}
	}
}
