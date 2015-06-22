package com.mygdx.game.android;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Some variable you might want to referencing:
 * my ID
 * playerStatus
 * BulletStatus
 * Some function you might want to use
 * GameSensor.update(float,float,float,float)
 * getExistPlayer()
 * getExistBullet()
 * get[Player|Bullet][Pos|Speed|Shield]
 */
public class GameState extends Thread{
    public class GameSensor{
        float speedX;
        float speedY;
        float shieldX;
        float shieldY;
        public GameSensor(){
            speedX = 0;
            speedY = 0;
            shieldX = 0;
            shieldY = 0;
        }
        public void update(float speed_x,float speed_y,float shield_x,float shield_y){
            this.speedX = speed_x;
            this.speedY = speed_y;
            this.shieldX = shield_x;
            this.shieldY = shield_y;
        }
    }
    public class AgentInfo{
        int ID=0;
        float []agentPosition= new float[2];
        float []speedVector= new float[2];
        float []shieldOrientationVector = new float[2]; //if the agent is bullet, this colume is invalid
    }
    int myID;
    AgentInfo [][]playerStatus= new AgentInfo[10][10]; //the row indicate Hash ID, you should trace colume to find what instance you need.
    AgentInfo [][]bulletStatus= new AgentInfo[10][10];
    //setting connection information
    final String myIP="140.113.68.27";
    final int myPort=7777;
    public SocketClient socketclient= null;
    //TimerTask and Timer
    Timer timer=null;
    final int periodToSend = 200;
    //game sensor
    GameSensor gameSensor;
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
    private void helperStringToFloat(String str,float[] flo){
        String[] vector = str.split("[,]");
        float[] tmp = {Float.parseFloat(vector[0]),Float.parseFloat(vector[1])};
        flo = tmp.clone();
    }
    private void helperListToStatus(String[] list, AgentInfo[][] Status, boolean isPlayer){
        for(int i=0;i<list.length;i++){
            String agent_str = list[i];
            System.out.println("Now agent_str is: " + agent_str + "endofline");
            if (agent_str.isEmpty()) continue;
            String []agent_info = agent_str.split("[:]"); //[0] is for ID ;[1] is for speed ;[2] is for position; [3] is for shield
            int ID = Integer.valueOf(agent_info[0]);
            if(isPlayer && i==0)myID = ID;
            AgentInfo hashTarget=Status[ID%10][(ID/10) % 10];
            hashTarget = new AgentInfo();
            hashTarget.ID = ID;
            helperStringToFloat(agent_info[1],hashTarget.speedVector);
            helperStringToFloat(agent_info[2],hashTarget.agentPosition);
            if(isPlayer) {
                helperStringToFloat(agent_info[3], hashTarget.shieldOrientationVector);
            }
        }

    }
    private void updateWorld(String str){
        String virtual="1:1,1:1,1:1,1:1,1\n" +
                "2:2,2:2,2:2,2\n" +
                ";\n" +
                "10:1,1:1,1\n" +
                "20:2,2:2,2\n";
        System.out.println("Our world is "+str+"wait for arranging");
        System.out.println("Our virtual world is "+virtual+"wait for arranging");

        //seperate bullet list and agent list
        playerStatus= new AgentInfo[10][10]; //the row indicate Hash ID, you should trace colume to find what instance you need.
        bulletStatus= new AgentInfo[10][10];
        String[] agent_list = str.split("[;]");
        if(agent_list.length != 2){
            System.out.println("Exception! agent list must be 2: "+ agent_list.length);
        }
        String player_str = agent_list[0];
        String bullet_str = agent_list[1];
        String []player_list = player_str.split("[\n]");
        String []bullet_list = bullet_str.split("[\n]");

        helperListToStatus(player_list,playerStatus,true);
        helperListToStatus(bullet_list,bulletStatus,false);
    }
    private List<Integer> getExistAgentID(AgentInfo[][] Status){
        List<Integer> result = new ArrayList<Integer>();
        for(int i=0;i<Status.length;i++){
            for(int j=0;j<Status[i].length;j++){
                if (Status[i][j]!=null){
                    result.add(Status[i][j].ID);
                }
            }
        }
        return result;
    }
    private float[] getAgentSpeed(int Id, AgentInfo[][] Status){
        if (Status[Id%10][Id/10] == null) System.out.println("Exception null Status Block : " + Id);
        return Status[Id%10][Id/10].speedVector;
    }
    private float[] getAgentPos(int Id, AgentInfo[][] Status){
        if (Status[Id%10][Id/10] == null) System.out.println("Exception null Status Block : " + Id);
        return Status[Id%10][Id/10].agentPosition;
    }
    //Ctor
    public GameState(){
        myID=0;
        socketclient = new SocketClient(myIP,myPort);
        gameSensor = new GameSensor();
        while(socketclient.onAir == false){
            try {
                socketclient = new SocketClient(myIP,myPort);
                System.out.println("just wait for 1 sec, make sure socketclient.onAir is " + (socketclient.onAir).toString());
                Thread.sleep(1000);
            }catch(InterruptedException e){
                System.out.println("Exception occured: "+ e.toString());
            }
        }
        updateWorld( socketclient.sendMessage("let start", true));
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWorld(socketclient.sendAgentStatus(myID,gameSensor.speedX,gameSensor.speedY,
                        gameSensor.shieldX,gameSensor.shieldY,true));
            }
        },0,periodToSend);
    }
    public List<Integer> getExistPlayer(){
        return getExistAgentID(playerStatus);
    }
    public List<Integer> getExistBullet(){
        return getExistAgentID(bulletStatus);
    }
    public float[] getPlayerPos(int Id){
        return getAgentPos(Id,playerStatus);
    }
    public float[] getBulletPos(int Id){
        return getAgentPos(Id,bulletStatus);
    }
    public float[] getPlayerSpeed(int Id){
        return getAgentSpeed(Id,playerStatus);
    }
    public float[] getBulletSpeed(int Id){
        return getAgentSpeed(Id,bulletStatus);
    }
    public float[] getPlayerShield(int Id){
        if (playerStatus[Id%10][Id/10] == null) System.out.println("Exception null Status Block : " + Id);
        return playerStatus[Id%10][Id/10].shieldOrientationVector;
    }
}
