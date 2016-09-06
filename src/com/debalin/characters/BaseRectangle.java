package com.debalin.characters;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class BaseRectangle {

  protected PVector color;
  protected PVector position;
  protected PVector size;

  protected PApplet parent;

  public BaseRectangle(PVector color, PVector position, PVector size, PApplet parent) {
    this.color = new PVector(color.x, color.y, color.z);
    this.position = new PVector(position.x, position.y);
    this.size = new PVector(size.x, size.y);

    this.parent = parent;
  }

  protected void drawShape() {
    parent.pushMatrix();
    parent.fill(color.x, color.y, color.z);
    parent.rect(position.x, position.y, size.x, size.y);
    parent.popMatrix();
  }
}
