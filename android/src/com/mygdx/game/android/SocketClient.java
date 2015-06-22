package com.mygdx.game.android;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

/*Instructions, Tips
*SocketClient.onAir : local variable to check whether the connection is linked
* if you want peroidically chat with server, here is some keyword for you "TimerTask" "Timer"
*
* */
public class SocketClient{
//    private String address = "140.113.68.27";
    /*make this class more packaged, you can customize your IP and port with 2 parameter of constructor*/
    //private String address = "140.113.65.65";  //  server IP
    //private int port = 7777;  //  server port
    BufferedReader stdin;
    BufferedReader in;
    PrintWriter out;
    Socket client;
    Boolean onAir = false;
    private Handler threadHandler = null;
    //periodically send
    public SocketClient(final String IP,final int Port,Handler handler){
        threadHandler = handler;
        client = new Socket();
        System.out.println("Start to connect to Python Server");
        new Thread(new Runnable(){
            @Override
            public void run(){

                InetSocketAddress isa = new InetSocketAddress(IP, Port);
                try{
                    client.connect(isa, 10000);
                    onAir = true;
                } catch(java.io.IOException e){
                    System.out.println("Socket connection problem?");
                    System.out.println("IOException :" + e.toString());
                    onAir = false;
                }
                //buffer initializing
                try {
                    stdin = new BufferedReader(new InputStreamReader(System.in));
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out = new PrintWriter(client.getOutputStream(), true);
                }catch (IOException e){
                    System.out.println("Socket connection problem");
                    System.out.println("IOException :" + e.toString());
                }

            }
        }).start();
    }
    public String sendMessage(final String message,final Boolean multiline){
        if (onAir == false){
            System.out.println("connection is not set, restart and try again");
            return "error";
        }
            System.out.println("you are sendding MSG :  "+ message);
        new Thread(new Runnable(){
            @Override
            public void run(){
                String result = "";
                try{

                    System.out.println("-- socket sending --");

                    if(message == null || message.isEmpty())
                        System.out.println("no message");
                    else{
                        out.write(message+"\r\n");  //  println()會多一個\n在尾巴  改用print 或write
                        out.flush();
                        System.out.println("Successfully send the message");
                    }
                    if (multiline) {
                        String tmp = null;
                        while ((tmp = in.readLine()) != null) {
                            System.out.println("tmp is:" + tmp);
                            result = result + tmp;
                        }
                    }else{
                        result = in.readLine();
                    }
                    System.out.println("The data received is : "+result);

                } catch(java.io.IOException e){
                    System.out.println("Socket connection problem");
                    System.out.println("IOException :" + e.toString());
                }

                Bundle msgBundle = new Bundle();
                msgBundle.putString("result", result);
                //msgBundle.putInt("result", 123);
                Message msg = new Message();
                msg.setData(msgBundle);
                threadHandler.sendMessage(msg);
            }
        }).start();

        return "You have called sendMessage function";

    }
    //customized socket function for our game
    public String sendAgentStatus(int agentID,float speedX,float speedY,float shieldX, float shieldY,Boolean multiline){
            return sendMessage(Integer.toString(agentID)+":"+Float.toString(speedX)+","+Float.toString(speedY)+":"+Float.toString(shieldX)+","+Float.toString(shieldY),multiline);
    }
    public void disconnect(){
        try {
            stdin.close();
            in.close();
            out.close();
            client.close();
            onAir = false;
        } catch(java.io.IOException e) {
            System.out.println("Socket connection problem");
            System.out.println("IOException :" + e.toString());
        }
    }

}
