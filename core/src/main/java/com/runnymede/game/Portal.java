package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Portal {
    private Sprite sprite;
    private Rectangle hitBox;

    public Portal(float centerX, float centerY, Texture texture) {
        sprite = new Sprite(texture);
        float size = 1.0f; // Makes the portal take up roughly 2x2 grid tiles visually
        sprite.setSize(size, size);

        // Offset by half size to ensure the center coordinates are actually in the middle
        sprite.setPosition(centerX - (size / 2f), centerY - (size / 2f));

        hitBox = new Rectangle(sprite.getX(), sprite.getY(), size, size);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Rectangle getHitBox() { return hitBox; }
}
