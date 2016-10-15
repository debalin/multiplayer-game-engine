package com.debalin.engine.game_objects;

import com.debalin.engine.network.GameServer;
import processing.core.PVector;

import java.io.Serializable;

public abstract class GameObject implements Serializable {

  protected PVector color;
  protected PVector position;
  protected PVector size;

  public GameServer.NetworkTag tag = GameServer.NetworkTag.OBJECT;
  public int connectionID;

  public abstract void updatePosition();
  public abstract void drawShape();

  public abstract boolean isVisible();

}
