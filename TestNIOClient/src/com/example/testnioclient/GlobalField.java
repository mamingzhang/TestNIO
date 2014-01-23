package com.example.testnioclient;

public class GlobalField
{

	public static final String SERVER_HOST = "127.0.0.1";
	
	public static final short SERVER_PORT = 5000;
	
	public static final int SERVER_STARTED = 1;
	public static final int SERVER_STOPTED = 2;
	public static final int SERVER_CLIENT_ACCEPTED = 3;
	public static final int SERVER_CLIENT_READ = 4;
	public static final int SERVER_CLIENT_CLOSED = 5;
	
	public static final int CLIENT_CONNECTED_SUCCESSED = 6;
	public static final int CLIENT_CONNECTED_FAILED = 7;
	public static final int CLIENT_CLOSED = 8;
	public static final int CLIENT_READ = 9;
	public static final int CLIENT_SERVER_CLOSED = 10;
}
