package com.debalin.engine;

import processing.core.PVector;

import java.io.Serializable;

public abstract class GameObject implements Serializable {

  protected PVector color;
  protected PVector position;
  protected PVector velocity;
  protected PVector acceleration;
  protected PVector size;

  public GameServer.NetworkTag tag = GameServer.NetworkTag.OBJECT;

  public abstract void updatePosition();
  public abstract void drawShape();

  public abstract boolean isVisible();

}
