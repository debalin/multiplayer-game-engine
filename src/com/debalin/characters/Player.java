package com.debalin.characters;

import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;

public class Player extends BaseRectangle {

  public Player(MainEngine engine) {
      super(Constants.PLAYER_COLOR, Constants.PLAYER_INIT_POS, Constants.PLAYER_SIZE, engine);
  }

  public void updatePosition() {

  }

}
