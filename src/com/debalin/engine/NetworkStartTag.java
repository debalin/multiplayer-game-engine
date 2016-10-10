package com.debalin.engine;

import processing.core.PVector;

public class NetworkStartTag extends GameObject {

  protected transient PVector color;
  protected transient PVector position;
  protected transient PVector velocity;
  protected transient PVector acceleration;
  protected transient PVector size;

  public NetworkStartTag(int connectionID) {
    tag = GameServer.NetworkTag.START_TAG;
    this.connectionID = connectionID;
  }

  public void updatePosition() {}

  public void drawShape() {}

  public boolean isVisible() {
    return false;
  }

}
