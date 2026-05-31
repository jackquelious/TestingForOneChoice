package com.runnymede.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Manages the text that shows the bullet damage
public class DamageText {

    // CONSTANTS:
    private final float MAX_LIFE_TIME = 1.2f; // Lasts for 0.8 seconds
    private final float BASE_SCALE = 0.021f; // Normal scale for letters
    private final float SPEED = 0.4f; // the speed it moves up


    // CLASS VARIABLES:
    private float x, y; // position
    private String text; // The displayed text
    private Color color; // Color of the text
    private float scale; // How big it should be
    private float despawnTimer; // How long the text will last
    private boolean active; // if it's active

    // Constructor
    public DamageText(float damage, boolean isCrit, boolean isLethal, float startX, float startY) {
        // initalize all variables
        this.x = startX;
        this.y = startY;
        this.despawnTimer = MAX_LIFE_TIME;
        this.active = true;

        // Convert damage to a whole number for cleaner display
        this.text = String.valueOf(Math.round(damage * 10.0) / 10.0);

        // Chooses what color the text should be bassed on if it's a crit and if it kills the enemy
        if (isCrit) {this.color = new Color(Color.YELLOW);} // Yellow for crits
        else if(isLethal) {this.color = new Color(Color.RED);} // Red for finishing blows
        else {this.color = new Color(Color.WHITE);} // White for standard damage

        float damageScale = (damage/10.0f) * 0.0015f; // Calculate the extra size from damage
        this.scale = (BASE_SCALE + damageScale) * (isCrit ? 1.3f : 1.0f); // Gets the total letter scale
    }
    // GETTERS:
    public float getX(){return this.x;}
    public float getY(){return this.y;}
    public float getScale(){return this.scale;}
    public float getDespawnTimer(){return this.despawnTimer;}
    public Color getColor(){return this.color;}
    public boolean getActive(){return this.active;}
    public String getText(){return this.text;}

    // Calculates fading out effect at the end of its life
    public float getAlpha() {return Math.max(0, despawnTimer / MAX_LIFE_TIME);}

    // Updates the text by moving it upwards and decreasing the despawn timer
    public void update(float dt){
        y += SPEED * dt;
        despawnTimer -= dt;
        if(despawnTimer <= 0){
            x = -55;
            y = 55;
            active = false;
        }
    }

    // draw method
    public void draw(SpriteBatch batch, BitmapFont font) {
        font.getData().setScale(this.scale); // sets the scale
        font.setColor(this.color.r, this.color.g, this.color.b, getAlpha()); // sets the color (also handles the fade out)
        font.draw(batch, this.text, this.x, this.y); // draws it
    }

}
