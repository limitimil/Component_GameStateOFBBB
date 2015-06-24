package com.mygdx.game.android;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
    public SocketClient(final String IP,final int Port){
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

    public String sendMessage(String message,Boolean multiline){
        if (onAir == false){
            System.out.println("connection is not set, restart and try again");
            return "error";
        }
        System.out.println("you are sendding MSG :  "+ message);
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
                    //System.out.println("tmp is:" + tmp);
                    if(tmp.equals("!")) break;
                    result = result + tmp + '\n';
                }
            }else{
                result = in.readLine();
            }
            System.out.println("The data received is : "+result);

        } catch(java.io.IOException e){
            System.out.println("Socket connection problem");
            System.out.println("IOException :" + e.toString());
        }

        
        return result;

    }
    //customized socket function for our game
    public String sendAgentStatus(int agentID,float speedX,float speedY,float shieldX, float shieldY,Boolean multiline){
        return sendMessage(Integer.toString(agentID)+":"+Float.toString(speedX)+","+Float.toString(speedY)+":"+Float.toString(shieldX)+","+Float.toString(shieldY),multiline);
    }
    public String listenMessage(){
        String message = "";
        try{
            String tmp="";
            while( ( tmp = in.readLine() )!=null){
                System.out.println(tmp);
                message = message + tmp;
            }
            System.out.print("got MSG from server" + message);
        }catch(IOException e){

            System.out.println("Socket connection problem");
            System.out.println("IOException :" + e.toString());
        }
        return message;
    }
    public void disconnect(){
        try {
            stdin.close();
            in.close();
            out.close();
            client.close();
        } catch(java.io.IOException e) {
            System.out.println("Socket connection problem");
            System.out.println("IOException :" + e.toString());
        }
    }

    public static void main(String args[]){
        new SocketClient("127.0.0.1",8888);
    }
}
