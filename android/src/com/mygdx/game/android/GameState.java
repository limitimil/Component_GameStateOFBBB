package com.mygdx.game.android;

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
    AgentInfo []playerStatus= new AgentInfo[100];
    AgentInfo []bulletStatus= new AgentInfo[100];
    
}
