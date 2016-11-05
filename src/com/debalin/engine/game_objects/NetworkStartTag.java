package com.debalin.engine.game_objects;

import com.debalin.engine.network.GameServer;

public class NetworkStartTag extends UtilityGameObject {

  public NetworkStartTag(int connectionID) {
    tag = GameServer.NetworkTag.START_TAG;
    this.connectionID = connectionID;
  }

  public void update() {}
  public void draw() {}

}
