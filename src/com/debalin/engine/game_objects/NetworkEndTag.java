package com.debalin.engine.game_objects;

import com.debalin.engine.network.GameServer;

public class NetworkEndTag extends UtilityGameObject {

  public NetworkEndTag() {
    tag = GameServer.NetworkTag.END_TAG;
  }

  public void update() {}
  public void draw() {}

}
