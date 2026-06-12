package com.runnymede.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.apache.tools.ant.Main;

public class UpgradeText {
    // CONSTANTS:
    private final float MAX_LIFE_TIME = 1.6f; // Lasts for 1.6 seconds
    private final float BASE_SCALE = 0.028f; // Normal scale for letters
    private final float SPEED = 0.38f; // the speed it moves up


    // CLASS VARIABLES:
    private float x, y; // position
    private String text; // The displayed text
    private Color color; // Color of the text
    private float scale; // How big it should be
    private float despawnTimer; // How long the text will last
    private boolean active; // if it's active

    public UpgradeText(float x, float y, String message, Color color){
        this.x = x;
        this.y = y;
        this.text = message;
        this.scale = BASE_SCALE;
        this.color = color;
        this.despawnTimer = MAX_LIFE_TIME;
        this. active = true;
    }

    // GETTERS:
    public float getX(){return this.x;}
    public float getY(){return this.y;}
    public float getScale(){return this.scale;}
    public float getDespawnTimer(){return this.despawnTimer;}
    public Color getColor(){return this.color;}
    public boolean getActive(){return this.active;}
    public String getText(){return this.text;}

    // Calculates the visibility of the text
    public float getAlpha() {return Math.max(0, despawnTimer / MAX_LIFE_TIME);}

    // Method that moves the text up and
    public void update(float dt, MainGame game){
        y += SPEED * dt;
        despawnTimer -= dt;
        if(despawnTimer <= 0){
            x = -55;
            y = 55;
            active = false;
            game.removeUpgradeText(this);
        }
    }

    // draw method
    public void draw(SpriteBatch batch, BitmapFont font) {
        font.getData().setScale(this.scale); // sets the scale
        font.setColor(this.color.r, this.color.g, this.color.b, getAlpha()); // sets the color (also handles the fade out)
        font.draw(batch, this.text, this.x, this.y); // draws itddddd
    }
}
