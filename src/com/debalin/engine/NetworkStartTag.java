package com.debalin.engine;

public class NetworkStartTag extends GameObject {

  public NetworkStartTag() {
    tag = GameServer.NetworkTag.START_TAG;
  }

  public void updatePosition() {}

  public void drawShape() {}

  public boolean isVisible() {
    return false;
  }

}
