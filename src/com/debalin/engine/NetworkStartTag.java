package com.debalin.engine;

public class NetworkStartTag extends GameObject {

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
