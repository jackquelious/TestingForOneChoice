package com.runnymede.game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.*;

public class MainGame implements ApplicationListener{
    SpriteBatch spriteBatch;
    ArrayList<Enemy> enemies;

    public void create(){
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
    }

    public void resize(int width, int height) {
    }

    public void render() {
        input();
        logic();
        draw();
    }


    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
        spriteBatch.dispose();
    }

    public void input(){

    }

    public void logic(){

    }

    public void draw(){

    }
}
