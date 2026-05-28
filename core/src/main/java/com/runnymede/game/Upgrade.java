package com.runnymede.game;

// Imports
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.Random;

// Objects of this class will upgrade player stats upon collection
public class Upgrade {
    // CLASS VARIABLES:
    Rectangle hitBox; // The hit box of the upgrade

    // The texture and sprite of the object
    Texture texture;
    Sprite sprite;

    float despawnTimer; // Tracks when the upgrade should despawn

    String type; // The type of the object = what stat it increases

    public Upgrade(float centerX, float centerY, float despawnTimer){
        // Creates and configures a trophy sprite
        texture = new Texture("trophyTexture.png");
        sprite = new Sprite(texture);
        sprite.setSize(0.4f, 0.4f);
        sprite.setPosition(centerX - (sprite.getWidth()/2.0f), centerY - (sprite.getHeight()/2.0f));

        hitBox = new Rectangle(centerX - (sprite.getWidth()/2.0f), centerY - (sprite.getHeight()/2.0f), sprite.getWidth(), sprite.getHeight()); // Makes a hitBox around the upgrade

        this.despawnTimer = despawnTimer; // Initializes despawnTimer;

        randomizeType(); // Randomizes the type of upgrade on startup
    }

    // Getter Methods:
    public Rectangle getHitBox(){return  hitBox;}
    public String getType(){return type;}
    public float getXPos(){return sprite.getX();}
    public float getYPos(){return sprite.getY();}
    public float getCenterXPos(){return getXPos() + (sprite.getWidth() / 2.0f);}
    public float getCenterYPos(){return getYPos() + (sprite.getHeight() / 2.0f);}

    // SETTERS:
    public void setPos(float x, float y){sprite.setPosition(x, y);}

    public void setCenterPos(float centerX, float centerY){
        float xPos = centerX - (sprite.getWidth() / 2.0f);
        float yPos = centerY - (sprite.getHeight() / 2.0f);
        sprite.setPosition(xPos, yPos);
    }

    // Randomizes the effect that the upgrade will have
    public void randomizeType(){
        Random rand = new Random(); // Creates an instance of the random class
        int num = rand.nextInt(12); // Generates a num (0 - 6)
        String result = "none"; // Initializes the result variable

        // Based on the random number, the type will be different things
        if(num <= 2) result = "damage";
        else if (num <= 4) result = "health";
        else if (num == 5) result = "bulletSize";
        else if (num == 6) result = "bulletSpeed";
        else if (num == 7) result = "bulletBounces";
        else if (num == 8) result = "critChance";
        else if (num == 9) result = "speed";
        else if (num == 10) result = "lifeSteal";
        else if (num == 11) result = "pierce";

        type = result; // Sets the type
    }

    // Runs upon collision with the player
    // Increases a random stat based on the type
    public void collect(Player player){
        // Increases stat based on what type the upgrade was
        if(type.equals("damage"))player.setDamage(player.getDamage() + 1); // Adds one to damage
        if(type.equals("health"))player.setMaxHealth(player.getMaxHealth() + 1); // Adds one maxHealth
        if(type.equals("bulletSize"))player.setBulletSize(player.getBulletSize() * 1.1f); // Multiplies bullet size by 1.1
        if(type.equals("bulletSpeed"))player.setBulletSpeed(player.getBulletSpeed() * 1.1f); // multiplies bullet speed
        if(type.equals("bulletBounces"))player.setBulletBounces(player.getBulletBounces() + 1); // Increases bullet bounces
        if(type.equals("critChance")){ // Increases crit chance to a max of 100%
            player.setCritChance(Math.min(1f, player.getCritChance() + 0.05f));
        }
        if(type.equals("speed")) player.setSpeed(player.getSpeed() * 1.05f); // Increases speed by 5%
        if(type.equals("lifeSteal")) player.setLifeSteal(Math.min(1f, player.getLifeSteal() + 0.05f)); // Increases lifeSteal to a max of 100%
        if(type.equals("pierce")) player.setPierce(player.getPierce() + 1); // Incerases pierce

        despawn();
    }

    public void draw(SpriteBatch batch){sprite.draw(batch);}

    public void despawn(){
        sprite.setPosition(-20, -20);
        type = "none";
    }

    public boolean tick(float deltaTime){
        despawnTimer -= deltaTime;
        if(despawnTimer <= 0){
            despawn();
            return true;
        } else return false;
    }
}
