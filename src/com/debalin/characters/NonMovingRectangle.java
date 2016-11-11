package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.engine.game_objects.StaticGameObject;
import processing.core.PVector;

public abstract class NonMovingRectangle extends StaticGameObject {

  public NonMovingRectangle(PVector color, PVector position, PVector size, MainEngine engine) {
    this.color = new PVector(color.x, color.y, color.z);
    this.position = new PVector(position.x, position.y);
    this.size = new PVector(size.x, size.y);

    this.engine = engine;
  }

  public void draw() {
    engine.pushMatrix();
    engine.fill(color.x, color.y, color.z);
    engine.noStroke();
    engine.rect(position.x, position.y, size.x, size.y);
    engine.popMatrix();
  }

}
