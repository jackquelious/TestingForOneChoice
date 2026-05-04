package com.runnymede.game;

public class Enemy {
    private float xPos;
    private float yPos;
    private float speed;
    private int health;
    private int damage;
    private Texture texture;
    private Sprite sprite;
    private boolean alive;

    public Enemy(float xPos, float yPos, float speed, int health, int damage, Texture texture) {
        super();
        this.xPos = xPos;
        this.yPos = yPos;
        this.speed = speed;
        this.health = health;
        this.damage = damage;
        this.texture = texture;
        this.sprite = new Sprite(texture);
        this.alive = true;

    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public float getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public Texture getTexture() {
        return texture;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            remove();
        }
    }

    public void spawn(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        alive = true;
    }   

    public void setPosition(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        alive = false;
    }

    public void remove() {
        // Implementation for removing the enemy
        xPos = -100; // Move off-screen or set a flag for removal
        yPos = -100;
        health = 0; // Ensure health is set to 0 to indicate removal
    }

    public float getAngleToPoint(float targetX, float targetY) {
        float deltaX = targetX - xPos;
        float deltaY = targetY - yPos;
        float resultingAngle = (float) Math.atan2(deltaY, deltaX);
        return resultingAngle;
    }

    public void moveTowardsPoint(float targetX, float targetY) {
        float angle = getAngleToPoint(targetX, targetY);
        xPos += speed * Math.cos(angle);
        yPos += speed * Math.sin(angle);
    }

    public void draw(SpriteBatch batch) {
        sprite.setPosition(xPos, yPos);
        sprite.draw(batch);
    }

}