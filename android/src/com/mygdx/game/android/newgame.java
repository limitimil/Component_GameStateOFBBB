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
	private Sprite []bulletSprite = new Sprite[50];
	private int []bulletbounce = new int [50];

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

		for(int i=0;i<50;i++) regenerate(i);

        camera.position.x = mainposX;
        camera.position.y = mainposY;

        gameState = new GameState();
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
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

		accelX = Gdx.input.getAccelerometerX();
		accelY = Gdx.input.getAccelerometerY();
		//float accelZ = Gdx.input.getAccelerometerZ();
		accellength = (float)Math.sqrt(accelY * accelY + accelX *accelX);

		float rotation = (float) (Math.atan2(-accelY, -accelX) / Math.PI * 180);

		mainposX += accelY/accellength;
		if(mainposX > 960) mainposX = 960;
		if(mainposX < -960) mainposX = -960;

		mainposY -= accelX/accellength;
		if(mainposY > 540) mainposY = 540;
		if(mainposY < -540) mainposY = -540;

		camera.position.x = mainposX;
		camera.position.y = mainposY;

		sheildposX = mainposX + ((accelY / accellength) * 30);
		sheildposY = mainposY - ((accelX / accellength) * 30);

		for(int i=0;i<50;i++){
			float disX = (bulletposX[i]) - (mainposX);
			float disY = (bulletposY[i]) - (mainposY);
			float dis = (float)Math.sqrt(disX * disX + disY * disY);
			if(dis < 50 && dis > 45 && bulletbounce[i] == 0){
				float product = (accelY / accellength) * -bulletaccelX[i] + (-accelX / accellength) * -bulletaccelY[i];
				float angle = (float)(Math.acos(product)/ Math.PI * 180);
				if(angle < 60) {
					product = (disX/dis) * bulletaccelX[i] + (disY/dis) * bulletaccelY[i];
					bulletbounce[i] = 50;
					bulletaccelX[i] = bulletaccelX[i] - 2* product * (disX/dis);
					bulletaccelY[i] = bulletaccelY[i] - 2* product * (disY/dis);
				}
			}

			if(dis <= 30)android.os.Process.killProcess(android.os.Process.myPid());

			bulletposX[i] += bulletaccelX[i]*2;
			bulletposY[i] += bulletaccelY[i]*2;
			if(bulletbounce[i] > 0) bulletbounce[i] = bulletbounce[i] - 1;
			if(bulletposX[i] > 960 || bulletposX[i] < -960 || bulletposY[i] > 540 || bulletposY[i] < -540) regenerate(i); //regenerate
		}
		drawing(rotation);

	}

	public void regenerate(int i){
		bullet[i] = new Texture(Gdx.files.internal("bullet" + MathUtils.random(1, 3) + ".png"));
		bullet[i].setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		bulletSprite[i] = new Sprite(bullet[i]);
		bulletbounce[i] = 0;

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

	public void drawing(float rotation){
		batch.begin();

		batch.draw(bgTexture, -960, -540, 0, 0, 1920, 1080, 1, 1, 0.0f, 0, 0, 1920, 1080, false, false);

		MainSprite.setPosition(mainposX - 25, mainposY - 25);
		MainSprite.setRotation(rotation);
		MainSprite.draw(batch);

		shieldSprite.setPosition(sheildposX - 40, sheildposY - 20);
		shieldSprite.setRotation(rotation);
		shieldSprite.draw(batch);

		for(int i=0;i<50;i++){
			bulletSprite[i].setPosition(bulletposX[i] - 5, bulletposY[i] - 5);
			bulletSprite[i].draw(batch);
		}

		batch.end();
	}
}
