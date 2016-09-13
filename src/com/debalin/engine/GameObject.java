package com.debalin.engine;

import processing.core.PVector;

public abstract class GameObject {

  protected PVector color;
  protected PVector position;
  protected PVector velocity;
  protected PVector acceleration;
  protected PVector size;

  public abstract void updatePosition();
  public abstract void drawShape();

  public abstract boolean isVisible();

}
