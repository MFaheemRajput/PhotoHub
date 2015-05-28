// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.faheem.wifidirect;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.faheemm.photohub.CameraActivity;
import com.example.faheemm.photohub.CameraFragment;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_ADDRESS = "go_host";
	public static final String EXTRAS_PORT = "go_port";
	public static final String EXTRAS_REQUEST_TYPE = "request_type";

	public FileTransferService(String name) {
		super(name);
	}

	public FileTransferService() {
		super("FileTransferService");
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		Context context = getApplicationContext();
		if (intent.getAction().equals(ACTION_SEND_FILE)) {
			String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
			String host = intent.getExtras().getString(EXTRAS_ADDRESS);
			Socket socket = new Socket();
			int port = intent.getExtras().getInt(EXTRAS_PORT);
			int requestType=intent.getExtras().getInt(EXTRAS_REQUEST_TYPE);

			try {
				Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

				Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());

				ContentResolver cr = context.getContentResolver();
				InputStream is = null;
				try {
					is = cr.openInputStream(Uri.parse(fileUri));
				} catch (FileNotFoundException e) {
					Log.d(WiFiDirectActivity.TAG, e.toString());
				}
				DataOutputStream stream =new DataOutputStream(socket.getOutputStream()) ;
				stream.writeInt(requestType);
				if(requestType== CameraActivity.POST_REQUEST){

					DeviceDetailFragment.copyFile(is, stream);
				}else if(requestType== CameraActivity.GET_REQUEST){

					DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
					int files=dataInputStream.readInt();
//                    dataInputStream.close();
                    Log.d(WiFiDirectActivity.TAG, "FilesOndServer:"+files);
					for (int i=0;i<files;i++){
//                        dataInputStream=new DataInputStream(socket.getInputStream());
						File file= CameraFragment.getOutputMediaFile(CameraFragment.MEDIA_TYPE_IMAGE);
//						DeviceDetailFragment.copyFile(dataInputStream, new FileOutputStream(file));
                        long fileLength=dataInputStream.readLong();
                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        for(int j = 0; j < fileLength; j++) fileOutputStream.write(dataInputStream.read());

                        fileOutputStream.close();
                        fileOutputStream.flush();
					}

				}

				Log.d(WiFiDirectActivity.TAG, "Client: Data written");
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
						} catch (IOException e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}

		}
	}
}
