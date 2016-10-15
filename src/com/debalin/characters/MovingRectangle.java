package com.debalin.characters;

import com.debalin.engine.game_objects.DynamicGameObject;
import com.debalin.engine.MainEngine;
import processing.core.PVector;

public abstract class MovingRectangle extends DynamicGameObject {

  transient public MainEngine engine;

  public PVector getPosition() {
    return position;
  }

  public PVector getSize() {
    return size;
  }

  public MovingRectangle(PVector color, PVector position, PVector size, PVector velocity, PVector acceleration, MainEngine engine) {
    this.color = new PVector(color.x, color.y, color.z);
    this.position = new PVector(position.x, position.y);
    this.size = new PVector(size.x, size.y);
    this.velocity = new PVector(velocity.x, velocity.y);
    this.acceleration = new PVector(acceleration.x, acceleration.y);

    this.engine = engine;
  }

  public abstract void updatePosition();

  public void drawShape() {
    engine.pushMatrix();
    engine.fill(color.x, color.y, color.z);
    engine.noStroke();
    engine.rect(position.x, position.y, size.x, size.y);
    engine.popMatrix();
  }
}
