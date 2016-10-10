package com.debalin.engine;

import processing.core.PVector;

public class NetworkEndTag extends GameObject {

  protected transient PVector color;
  protected transient PVector position;
  protected transient PVector velocity;
  protected transient PVector acceleration;
  protected transient PVector size;

  public NetworkEndTag() {
    tag = GameServer.NetworkTag.END_TAG;
  }

  public void updatePosition() {}

  public void drawShape() {}

  public boolean isVisible() {
    return false;
  }

}
