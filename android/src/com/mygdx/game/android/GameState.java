package com.mygdx.game.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by L850 on 6/22/2015.
 */
public class GameState {
    public class AgentInfo{
        int ID;
        float []agentPosition= new float[2];
        float []speedVector= new float[2];
        float []shieldOrientationVector = new float[2]; //if the agent is bullet, this colume is invalid
    }
    int myID;
    AgentInfo [][]playerStatus= new AgentInfo[10][]; //the row indicate Hash ID, you should trace colume to find what instance you need.
    AgentInfo [][]bulletStatus= new AgentInfo[10][];
    //setting connection information
    final String myIP="140.113.68.27";
    final int myPort=7777;
    public SocketClient socketclient= null;
    private static class HandlerExtension extends Handler {
        private final WeakReference<GameState> currentActivity;

        public HandlerExtension(GameState activity) {
            currentActivity = new WeakReference<GameState>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            GameState activity = currentActivity.get();
            if (activity != null) {
                activity.updateWorld(message.getData().getString("result"));
            }
        }
    }
    public void updateWorld(String str){
        System.out.println("Our world is "+str+"wait for arranging");
    }
    //Ctor
    public GameState(){
        myID=0;
        socketclient = new SocketClient(myIP,myPort,new HandlerExtension(this));
        while(socketclient.onAir == false);
        socketclient.sendAgentStatus(0,0,0,0,0,true);
    }
}
