package com.debalin.characters;

import com.debalin.engine.KeypressUser;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;

public class Player extends BaseRectangle implements KeypressUser{

  private boolean LEFT, RIGHT, JUMP;

  public Player(MainEngine engine) {
    super(Constants.PLAYER_COLOR, Constants.PLAYER_INIT_POS, Constants.PLAYER_SIZE, Constants.PLAYER_INIT_VEL, engine);
    LEFT = RIGHT = JUMP = false;
  }

  public void updatePosition() {
    if (LEFT)
      velocity.x = -Constants.PLAYER_MAX_VEL.x;
    else if (RIGHT)
      velocity.x = Constants.PLAYER_MAX_VEL.x;
    else
      velocity.x = 0f;

    position.add(velocity);
  }

  public void handleKeypress(int key, boolean set) {
    switch(key) {
      case 'A':
      case 'a':
        LEFT = set;
        break;
      case 'D':
      case 'd':
        RIGHT = set;
        break;
      case 32:
        if (!JUMP)
          JUMP = true;
    }
  }

}
