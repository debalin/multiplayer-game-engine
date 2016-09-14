package com.debalin.engine;

public class NetworkEndTag extends GameObject {

  public NetworkEndTag() {
    tag = GameServer.NetworkTag.END_TAG;
  }

  public void updatePosition() {}

  public void drawShape() {}

  public boolean isVisible() {
    return false;
  }

}
