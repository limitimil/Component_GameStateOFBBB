package com.mygdx.game.android;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Some variable you might want to referencing:
 * my ID
 * playerStatus
 * BulletStatus
 *
 * Some function you might want to use:
 * GameSensor.update(float,float,float,float)
 * getExistPlayer()
 * getExistBullet()
 * get[Player|Bullet][Pos|Speed|Shield]
 *
 * here is some Limitation:
 * ID provide by Server can't be much than 100 because of playerStatus and bulletStatus's initialization
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
        public AgentInfo(int id,float[] pos,float[] speed,float[] shield){
            ID= id;
            System.arraycopy(pos,0,agentPosition,0,pos.length);
            System.arraycopy(speed,0,speedVector,0,speed.length);
            System.arraycopy(shield,0,shieldOrientationVector,0,shield.length);
        }
        public AgentInfo(){

        }
    }
    int myID;
    Hashtable playerStatus= new Hashtable(); //the row indicate Hash ID, you should trace colume to find what instance you need.
    Hashtable bulletStatus= new Hashtable(); //the row indicate Hash ID, you should trace colume to find what instance you need.

    //setting connection information
    final String myIP="140.113.68.27";
    final int myPort=7777;
    public SocketClient socketclient= null;
    //TimerTask and Timer
    Timer timer=null;
    final int periodToSend = 25;
    //game sensor
    GameSensor gameSensor;
    //is update by server or not
    boolean isUpdate;
    private void helperStringToFloat(String str,float[] flo){
        String[] vector = str.split("[,]");
        flo[0] = Float.parseFloat(vector[0]);
        flo[1] = Float.parseFloat(vector[1]);
    }
    private void helperListToStatus(String[] list, Hashtable Status, boolean isPlayer){
        try {
            for (int i = 0; i < list.length; i++) {
                String agent_str = list[i];
                if (agent_str.isEmpty()) continue;
                String[] agent_info = agent_str.split("[:]"); //[0] is for ID ;[1] is for speed ;[2] is for position; [3] is for shield
                int ID = Integer.valueOf(agent_info[0]);
                if (isPlayer && i == 0) myID = ID;
                AgentInfo Target = new AgentInfo();
                Target.ID = ID;
                helperStringToFloat(agent_info[1], Target.speedVector);
                helperStringToFloat(agent_info[2], Target.agentPosition);
                if (isPlayer) {
                    helperStringToFloat(agent_info[3], Target.shieldOrientationVector);
                }
                Status.put(ID, Target);
            }
        }catch (Exception e){
            System.out.println("Exception in helperListToStatus : "+ e.toString());
            return;
        }

    }
    private boolean updateWorld(String str){
        /*String virtual="1:1,1:1,1:1,1:1,1\n" +
                "2:2,2:20,20:2,2\n" +
                ";\n" +
                "10:1,1:30,30\n" +
                "20:2,2:40,40\n";*/
        //System.out.println("Our world is "+str+"wait for arranging");
        //System.out.println("Our virtual world is "+virtual+"wait for arranging");

        //seperate bullet list and agent list
        Hashtable playerStatus_tmp= new Hashtable();
        Hashtable bulletStatus_tmp= new Hashtable();

        String[] agent_list = str.split("[;]");
        if(agent_list.length != 2){
            System.out.println("Exception! agent list must be 2: "+ agent_list.length);
            return false;
        }
        String player_str = agent_list[0];
        String bullet_str = agent_list[1];
        String []player_list = player_str.split("[\n]");
        String []bullet_list = bullet_str.split("[\n]");

        helperListToStatus(player_list,playerStatus_tmp,true);
        helperListToStatus(bullet_list,bulletStatus_tmp,false);

        playerStatus = playerStatus_tmp;
        bulletStatus = bulletStatus_tmp;
        isUpdate = true;
        return true;
    }
    private List<Integer> getExistAgentID(Hashtable Status){
        Enumeration idList = Status.keys();
        List<Integer> result = new ArrayList<Integer>();
        while(idList.hasMoreElements()){
            int tmp=(int ) idList.nextElement();
            result.add(tmp);
            //System.out.println("check getExistAgentID loop: "+tmp);
        }
        //System.out.println("check getExistAgentID return :" + Status.toString());
        return result;
    }
    private float[] getAgentSpeed(int Id, Hashtable Status){
        try {
            return ((AgentInfo) Status.get(Id)).speedVector;
        }catch(Exception e){
            System.out.println("Exception in get Agent Speed :"+e);
            return new float[] {(float)0.0,(float)0.0};
        }
    }
    private float[] getAgentPos(int Id, Hashtable Status){
        try {
            return ((AgentInfo) Status.get(Id)).agentPosition;
        }catch(Exception e){
            System.out.println("Exception in get Agent Position :"+e);
            return new float[] {(float)0.0,(float)0.0};
        }
    }
    private void storeAgentStatus(Hashtable Status,AgentInfo newStatus){
        Status.put(newStatus.ID, new AgentInfo(newStatus.ID,newStatus.agentPosition,newStatus.speedVector,newStatus.shieldOrientationVector));
    }
    //Ctor
    public GameState(GameState statusToCopy, boolean setIsUpdate){
        //this copy constructor return a object with all data copied except for socket initialize and timertask, thus , updateWorld will never be called.
        this.myID = statusToCopy.myID;
        this.playerStatus = (Hashtable)statusToCopy.playerStatus.clone();
        this.bulletStatus = (Hashtable)statusToCopy.bulletStatus.clone();
        statusToCopy.isUpdate = setIsUpdate;
    }
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
        while(!updateWorld( socketclient.sendMessage("let start", true)));
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateWorld(socketclient.sendAgentStatus(myID,gameSensor.speedX,gameSensor.speedY,
                                gameSensor.shieldX,gameSensor.shieldY,true));
                    }
                }).start();

            }
        },0,periodToSend);
    }
    public List<Integer> getExistPlayer(){
        //System.out.println("check getExistPlayerReturn :" + getExistAgentID(playerStatus));
        return getExistAgentID(playerStatus);
    }
    public List<Integer> getExistBullet(){
        return getExistAgentID(bulletStatus);
    }
    public float[] getPlayerPos(int Id){
        //System.out.println("get Player " + Id +" position : "+getAgentPos(Id,playerStatus)[0]+","+getAgentPos(Id,playerStatus)[1]);
        return getAgentPos(Id, playerStatus);
    }
    public float[] getBulletPos(int Id){
        return getAgentPos(Id,bulletStatus);
    }
    public float[] getPlayerSpeed(int Id){
        return getAgentSpeed(Id, playerStatus);
    }
    public float[] getBulletSpeed(int Id){
        return getAgentSpeed(Id,bulletStatus);
    }
    public float[] getPlayerShield(int Id){
        try {
            return ((AgentInfo) playerStatus.get(Id)).shieldOrientationVector;
        }catch(Exception e){
            System.out.println("Exception in get Agent Shield :"+e);
            return new float[] {(float)0.0,(float)0.0};
        }
    }
    public void storePlayerStatus(AgentInfo status){storeAgentStatus(playerStatus,status);}
    public void storeBulletStatus(AgentInfo status){storeAgentStatus(bulletStatus,status);}
}
