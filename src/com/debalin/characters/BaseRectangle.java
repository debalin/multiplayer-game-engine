package com.debalin.characters;

import com.debalin.engine.GameObject;
import com.debalin.engine.MainEngine;
import processing.core.PVector;

public abstract class BaseRectangle extends GameObject{

  protected PVector color;
  protected PVector position;
  protected PVector size;

  protected MainEngine engine;

  public BaseRectangle(PVector color, PVector position, PVector size, MainEngine engine) {
    this.color = new PVector(color.x, color.y, color.z);
    this.position = new PVector(position.x, position.y);
    this.size = new PVector(size.x, size.y);

    this.engine = engine;
  }

  public abstract void updatePosition();

  public void drawShape() {
    engine.pushMatrix();
    engine.fill(color.x, color.y, color.z);
    engine.rect(position.x, position.y, size.x, size.y);
    engine.popMatrix();
  }
}
