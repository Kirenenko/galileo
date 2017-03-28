package com.example.motorcontroller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;


// Modified code from this thread - http://stackoverflow.com/a/19541474/5147720
public class UDP_Client 
{
    private AsyncTask<Void, Void, Void> async_cient;
    public String Message;

    @SuppressLint("NewApi")
    public void Send(final String message, final String address, final int port)
    {
        async_cient = new AsyncTask<Void, Void, Void>() 
        {
            @Override
            protected Void doInBackground(Void... params)
            {   
                DatagramSocket ds = null;

                try 
                {
                	InetAddress inetAddress = InetAddress.getByName(address);
                    ds = new DatagramSocket();
                    DatagramPacket dp;                          
                    dp = new DatagramPacket(message.getBytes(), message.length(), inetAddress, port);
                    //ds.setBroadcast(true);
                    ds.send(dp);
                    System.out.println("Message sent: " + message);
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

            protected void onPostExecute(Void result) 
            {
               super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_cient.execute();
    }

}