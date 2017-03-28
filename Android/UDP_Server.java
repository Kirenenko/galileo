package com.example.motorcontroller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;


// Modified code from this thread - http://stackoverflow.com/a/19541474/5147720
public class UDP_Server 
{
    private AsyncTask<Void, Void, Void> async;
    private boolean Server_running = true;
    public static String sensorspeed;

    @SuppressLint("NewApi")
    public void runUdpServer(final Handler mHandler, final Runnable mUpdateResults,  final int port) 
    {
        async = new AsyncTask<Void, Void, Void>() 
        {
            @Override
            protected Void doInBackground(Void... params)
            {   
                byte[] lMsg = new byte[8];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;

                try 
                {
                    ds = new DatagramSocket(port);

                    while(Server_running)
                    {
                        ds.receive(dp);

                        String sensorSpeed = new String(lMsg, "UTF-8");
                        System.out.println(sensorSpeed);
                        sensorspeed = sensorSpeed;
                        
                        mHandler.post(mUpdateResults);
                    }
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                } 
                finally 
                {
                    if (ds != null) 
                    {
                        ds.close();
                    }
                }

                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }
    
    

    public void stop_UDP_Server()
    {
    	Server_running = false;
    }
}