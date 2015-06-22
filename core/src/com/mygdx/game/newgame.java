package com.mygdx.game;

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

public class newgame extends ApplicationAdapter {
	
	private static final Color BACKGROUND_COLOR = new Color(0.39f,0.58f,0.92f,1.0f);
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture MainActor;
	private Sprite MainSprite;

    private Texture bgTexture;
	private Texture sheild;
	private Sprite sheildSprite;

	float objectposX = 0, objectposY = 0;

	@Override
	public void create () {
		camera = new OrthographicCamera(640,480);
		batch = new SpriteBatch();

		MainActor = new Texture(Gdx.files.internal("MainActor.png"));
		MainActor.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		MainSprite = new Sprite(MainActor);

        bgTexture = new Texture(Gdx.files.internal("bg1920_1080.png"));
        bgTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		sheild = new Texture(Gdx.files.internal("sheild.png"));
		sheild.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		sheildSprite = new Sprite(sheild);

        camera.position.x = objectposX;
        camera.position.y = objectposY;
	}

	@Override
	public void dispose(){
		batch.dispose();
		MainActor.dispose();
        bgTexture.dispose();
		sheild.dispose();
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

		float accelX = Gdx.input.getAccelerometerX();
		float accelY = Gdx.input.getAccelerometerY();
		//float accelZ = Gdx.input.getAccelerometerZ();

		float accellength = (float)Math.sqrt(accelY * accelY + accelX *accelX);

		objectposX += accelY/accellength;
		if(objectposX > 960) objectposX = 960;
		if(objectposX < -960) objectposX = -960;

		objectposY -= accelX/accellength;

		if(objectposY > 540) objectposY = 540;
		if(objectposY < -540) objectposY = -540;

		camera.position.x = objectposX;
		camera.position.y = objectposY;

		batch.begin();
		//float x, y, Ox, Oy;
		//int  wid, hei, scaleX, scaleY, srcX, srcY, srcWidth, srcHeight;
		//boolean flipX, flipY;

		float rotation = (float) (Math.atan2(-accelY, -accelX) / Math.PI * 180);

		//wid = 50; hei = 50; scaleX = 1; scaleY = 1;
		//float rotation = 0f;
		//srcX = 0; srcY = 0; srcWidth = 50; srcHeight = 50; flipX = false; flipY = false;

        batch.draw(bgTexture,-960, -540, 0,0, 1920, 1080, 1, 1, 0.0f, 0, 0, 1920, 1080, false, false );
        //batch.draw(MainActor, objectposX - 25, objectposY - 25, objectposX + 25, objectposY + 25, wid, hei, scaleX, scaleY,
		//		rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);

		MainSprite.setPosition(objectposX - 25, objectposY - 25);
		MainSprite.setRotation(rotation);
		MainSprite.draw(batch);

		//batch.draw(sheild, objectposX - 38 + ((accelY/accellength)*30), objectposY - 24 - ((accelX/accellength)*30),
		//		objectposX + 38 + ((accelY/accellength)*30), objectposY + 24 - ((accelX/accellength)*30), 75, 48, scaleX, scaleY,
		//		0f, srcX, srcY, 75, 48, flipX, flipY);

		sheildSprite.setPosition(objectposX - 38 + ((accelY / accellength) * 30), objectposY - 24 - ((accelX / accellength) * 30));
		sheildSprite.setRotation(rotation);
		sheildSprite.draw(batch);

		batch.end();
	}
	
	int width = 500;
	int height = 500;
	
}
