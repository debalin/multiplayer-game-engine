package com.debalin.engine.game_objects;

import com.debalin.engine.network.GameServer;
import processing.core.PVector;

import java.io.Serializable;

public abstract class GameObject implements Serializable {

  protected PVector color;
  protected PVector position;
  protected PVector size;
  protected boolean visible;

  public GameServer.NetworkTag tag = GameServer.NetworkTag.OBJECT;
  public int connectionID;

  public PVector getPosition() { return position; }
  public PVector getSize() {
    return size;
  }
  public PVector getColor() { return color; }
  public int getConnectionID() { return connectionID; }

  public void setColor(PVector color) {
    this.color = color;
  }
  public void setPosition(PVector position) {
    this.position = position;
  }
  public void setSize(PVector size) {
    this.size = size;
  }
  public void setVisible(boolean visible) {
    this.visible = visible;
  }
  public void setConnectionID(int connectionID) {
    this.connectionID = connectionID;
  }

  public abstract void update();
  public abstract void draw();

  public boolean isVisible() {
    return visible;
  }

}
