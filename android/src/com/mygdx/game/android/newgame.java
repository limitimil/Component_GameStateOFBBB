package com.mygdx.game.android;

import java.util.*;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class newgame extends ApplicationAdapter {

    private static final Color BACKGROUND_COLOR = new Color(0.39f,0.58f,0.92f,1.0f);
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture MainActor;
    private Sprite MainSprite;

    private Texture bgTexture;

    private Texture shield;
    private Sprite shieldSprite;

    private Texture []bullet = new Texture[50];
    private Sprite []bulletSprite_old = new Sprite[50];
    private int []bulletbounce_old = new int [50];
    //game world information
    GameState gameState_Copy = null;
    Hashtable playerSprite = new Hashtable();
    Hashtable bulletSprite = new Hashtable();
    Hashtable bulletbounce = new Hashtable();

    float mainposX = 0, mainposY = 0;
    float sheildposX, sheildposY;
    float accelX, accelY, accellength;

    //bulletGroup bullets = new bulletGroup();

    float []bulletposX = new float[50];
    float []bulletposY = new float[50];
    float []bulletaccelX = new float[50];
    float []bulletaccelY = new float[50];

    //limin: test gameState
    GameState gameState=null;
    @Override
    public void create () {
        camera = new OrthographicCamera(640,480);
        batch = new SpriteBatch();

        bgTexture = new Texture(Gdx.files.internal("bg1920_1080.png"));
        bgTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        MainActor = new Texture(Gdx.files.internal("MainActor"+ MathUtils.random(1, 4) +".png"));
        MainActor.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        MainSprite = new Sprite(MainActor);

        shield = new Texture(Gdx.files.internal("sheild" + MathUtils.random(1, 3) + ".png"));
        shield.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        shieldSprite = new Sprite(shield);


        camera.position.x = 0;
        camera.position.y = 0;
        //new Archi
        gameState = new GameState();
        gameState_Copy = new GameState(gameState,false);
        List<Integer> playerList = gameState_Copy.getExistPlayer();
        for(Integer playerID: playerList){ //playerSprite.get(key) returns an array contains two Sprite, first is for MainActor, the second is for sheild
            Texture player_t = new Texture(Gdx.files.internal("MainActor" + MathUtils.random(1, 4) + ".png"));
            player_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
            Texture sheild_t= new Texture(Gdx.files.internal("sheild"+MathUtils.random(1, 3)+".png"));
            sheild_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
            Sprite player = new Sprite(player_t);
            Sprite sheild = new Sprite(sheild_t);

            playerSprite.put(playerID,new Sprite[]{player,sheild});
        }
        List<Integer> bulletList = gameState_Copy.getExistBullet();
        for(Integer bulletID: bulletList){
            Texture bullet_t = new Texture(Gdx.files.internal("bullet"+ MathUtils.random(1, 3) +".png"));
            bullet_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
            Sprite bullet = new Sprite(bullet_t);
            bulletSprite.put(bulletID,bullet);
        }
        Gdx.graphics.setContinuousRendering(false);//不再自动调用render()方法
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Gdx.graphics.requestRendering();

            }
        },0,1000);
    }

    @Override
    public void dispose(){
        batch.dispose();
        MainActor.dispose();
        bgTexture.dispose();
        shield.dispose();
        //bullets.dispose();
        for(int i=0;i<10;i++) bullet[i].dispose();
    }
    private float helperSpeedToLength(float[] speed){
        return (float)Math.sqrt(speed[1] * speed[1]+ speed[0] * speed[0]);
    }
    @Override
    public void render () {
        Gdx.gl.glClearColor(BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        accelX = Gdx.input.getAccelerometerX();
        accelY = Gdx.input.getAccelerometerY();
        //New Archi
        if(gameState.isUpdate){
            gameState_Copy = new GameState(gameState,false);
        }else{
            //do nothing
        }
        List<Integer> playerList = gameState_Copy.getExistPlayer();
        for(Integer playerID: playerList){ //playerSprite.get(key) returns an array contains two Sprite, first is for MainActor, the second is for sheild
            if(!playerSprite.containsKey(playerID)){
                Texture player_t = new Texture(Gdx.files.internal("MainActor" + MathUtils.random(1, 4) + ".png"));
                player_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
                Texture sheild_t= new Texture(Gdx.files.internal("sheild"+MathUtils.random(1, 3)+".png"));
                sheild_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
                Sprite player = new Sprite(player_t);
                Sprite sheild = new Sprite(sheild_t);

                playerSprite.put(playerID,new Sprite[]{new Sprite(player),new Sprite(sheild)});


            }
            float[] Position = gameState_Copy.getPlayerPos(playerID);
            float[] Speed = gameState_Copy.getPlayerSpeed(playerID);
            float[] Shield = gameState_Copy.getPlayerShield(playerID);
            if(playerID == gameState_Copy.myID){
                Shield[0] = Speed[0] = Gdx.input.getAccelerometerY();
                Shield[1] = Speed[1] = -Gdx.input.getAccelerometerX();
            }
            float speedLength = helperSpeedToLength(Speed);
            //rotation can be infered by speedVector
            //float rotation = (float) (Math.atan2(-Speed[0], Speed[1]) / Math.PI * 180);
            //sheilpos can be infered by new position and shieldOrientationVector.
            //sheildposX = mainposX + ((accelY / accellength) * 30);
            //sheildposY = mainposY - ((accelX / accellength) * 30);

            Position[0] = Position[0] + Speed[0]/speedLength;
            if(Position[0] > 960) Position[0] = 960;
            if(Position[0] < -960) Position[0] = -960;

            Position[1] = Position[1]+ Speed[1]/speedLength; // Position[1] - (-accelX)/accellength
            if(Position[1] > 540) Position[1] = 540;
            if(Position[1] < -540) Position[1] = -540;

            if(playerID == gameState_Copy.myID){

                camera.position.x = Position[0];
                camera.position.y = Position[1];
            }

            gameState_Copy.storePlayerStatus(gameState_Copy.new AgentInfo(playerID,Position,Speed,Shield));
        }
        List<Integer> bulletList = gameState_Copy.getExistBullet();
        float[] mainPlayerPos = gameState_Copy.getPlayerPos(gameState_Copy.myID);
        System.out.println("check render mainPlayerPos :"+mainPlayerPos[0]+","+mainPlayerPos[1]);
        float[] mainPlayerSpeed = gameState_Copy.getPlayerSpeed(gameState_Copy.myID);
        float[] mainPlayerShield = gameState_Copy.getPlayerShield(gameState_Copy.myID);

        for(Integer bulletID: bulletList){
            if(!bulletSprite.containsKey(bulletID)){
                Texture bullet_t = new Texture(Gdx.files.internal("bullet"+ MathUtils.random(1, 3) +".png"));
                bullet_t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
                Sprite bullet = new Sprite(bullet_t);
                bulletSprite.put(bulletID,bullet);
            }
            float[] Position = gameState_Copy.getBulletPos(bulletID);
            float[] Speed = gameState_Copy.getBulletSpeed(bulletID);



            float disX = (Position[0]) - (mainPlayerPos[0]);
            float disY = (Position[1]) - (mainPlayerPos[1]);
            float dis = (float)Math.sqrt(disX * disX + disY * disY);
            if(dis < 50 && dis > 45 && bulletbounce.get(bulletID) != null ){
                float product = (mainPlayerSpeed[0] / helperSpeedToLength(mainPlayerSpeed)) * - Speed[0] +
                        (mainPlayerSpeed[1] / helperSpeedToLength(mainPlayerSpeed)) * -Speed[1];
                float angle = (float)(Math.acos(product)/ Math.PI * 180);
                if(angle < 60) {
                    product = (disX/dis) * Speed[0] + (disY/dis) * Speed[1];
                    bulletbounce.put(bulletID,50);
                    Speed[0] = Speed[0] - 2* product * (disX/dis);
                    Speed[1] = Speed[1] - 2* product * (disY/dis);
                }
            }

            if(dis <= 30)android.os.Process.killProcess(android.os.Process.myPid());

            Position[0] += Speed[0]*2;
            Position[1] += Speed[1]*2;
            if(bulletbounce.get(bulletID) != null) {
                if((int)bulletbounce.put(bulletID,(int)bulletbounce.get(bulletID)-1) == 0 ){
                    bulletbounce.remove(bulletID);
                }
            }
            if(Position[0] > 960 || Position[0] < -960 || Position[1] > 540 || Position[0] < -540) {
                //what should i do?
            }
            gameState_Copy.storeBulletStatus(gameState_Copy.new AgentInfo(bulletID,Position,Speed,new float[]{0,0}));
        }
        gameState.gameSensor.update(mainPlayerSpeed[0],mainPlayerSpeed[1],mainPlayerShield[0],mainPlayerShield[1]);
        System.out.println("check render mainPlayerSpeed :"+mainPlayerSpeed[0]+","+mainPlayerSpeed[1]);
        drawing();
    }

    public void regenerate(int i){
        bullet[i] = new Texture(Gdx.files.internal("bullet" + MathUtils.random(1, 3) + ".png"));
        bullet[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        bulletSprite_old[i] = new Sprite(bullet[i]);
        bulletbounce_old[i] = 0;

        int startline = MathUtils.random(1, 4);
        switch(startline){
            case 1:
                bulletposX[i] = -959;
                bulletposY[i] = MathUtils.random(-539, 539);
                break;
            case 2:
                bulletposX[i] = MathUtils.random(-959, 959);
                bulletposY[i] = 539;
                break;
            case 3:
                bulletposX[i] = 959;
                bulletposY[i] = MathUtils.random(-539, 539);
                break;
            case 4:
                bulletposX[i] = MathUtils.random(-959, 959);
                bulletposY[i] = -539;
                break;
            default:
        }

        float bulletdisX = mainposX - bulletposX[i];
        float bulletdisY = mainposY - bulletposY[i];

        bulletaccelX[i] = bulletdisX / (float)Math.sqrt(bulletdisY * bulletdisY + bulletdisX *bulletdisX);
        bulletaccelY[i] = bulletdisY / (float)Math.sqrt(bulletdisY * bulletdisY + bulletdisX *bulletdisX);
    }

    public void drawing(){
        batch.begin();

        //New Archi

        batch.draw(bgTexture, -960, -540, 0, 0, 1920, 1080, 1, 1, 0.0f, 0, 0, 1920, 1080, false, false);
        List<Integer> playerList = gameState_Copy.getExistPlayer();
        for(Integer playerID: playerList){ //playerSprite.get(key) returns an array contains two Sprite, first is for MainActor, the second is for sheild
            try {
                float[] Position = gameState_Copy.getPlayerPos(playerID);
                float[] Speed = gameState_Copy.getPlayerSpeed(playerID);
                float[] Shield = gameState_Copy.getPlayerShield(playerID);
                float rotation = (float) (Math.atan2(-Speed[0], Speed[1]) / Math.PI * 180);
                Sprite[] playerSpriteBox = (Sprite[]) playerSprite.get(playerID);
                playerSpriteBox[0].setPosition(Position[0] - 25, Position[1] - 25);
                playerSpriteBox[0].setRotation(rotation);
                playerSpriteBox[0].draw(batch);

                playerSpriteBox[1].setPosition(
                        Position[0] + ((Shield[0] / helperSpeedToLength(Speed)) * 30) - 40,
                        Position[1] + ((Shield[1] / helperSpeedToLength(Speed)) * 30) - 20
                );
                playerSpriteBox[1].setRotation(rotation);
                playerSpriteBox[1].draw(batch);
            }catch(Exception e){
                System.out.print("Exception in drawing : "+e);
                System.out.println("when this id " + playerID);
            }
        }

        List<Integer> bulletList = gameState_Copy.getExistBullet();
        for(Integer bulletID: bulletList){

            float[] Position = gameState_Copy.getBulletPos(bulletID);
            Sprite bulletSpriteBox = (Sprite)bulletSprite.get(bulletID);
            bulletSpriteBox.setPosition(Position[0] - 5, Position[1] - 5);
            bulletSpriteBox.draw(batch);

        }
        batch.end();
    }
}
