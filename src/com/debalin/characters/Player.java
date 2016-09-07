package com.debalin.characters;

import com.debalin.engine.KeypressUser;
import com.debalin.engine.MainEngine;
import com.debalin.util.Constants;

public class Player extends BaseRectangle implements KeypressUser{

  private boolean LEFT, RIGHT, JUMP, ON_GROUND;

  public Player(MainEngine engine) {
    super(Constants.PLAYER_COLOR, Constants.PLAYER_INIT_POS, Constants.PLAYER_SIZE, Constants.PLAYER_INIT_VEL, Constants.PLAYER_INIT_ACC, engine);
    LEFT = RIGHT = JUMP = false;
    ON_GROUND = true;
  }

  public void updatePosition() {
    if (LEFT)
      velocity.x = -Constants.PLAYER_MAX_VEL.x;
    else if (RIGHT)
      velocity.x = Constants.PLAYER_MAX_VEL.x;
    else
      velocity.x = 0f;

    if (JUMP) {
      ON_GROUND = false;
      velocity.y = -Constants.PLAYER_MAX_VEL.y;
      acceleration.y = Constants.PLAYER_MAX_ACC.y;
      JUMP = false;
    }

    velocity.add(acceleration);
    position.add(velocity);

    checkBounds();
  }

  private void checkBounds() {
    if (position.y >= Constants.RESOLUTION.y - Constants.PLAYER_SIZE.y) {
      velocity.y = Constants.PLAYER_INIT_VEL.y;
      acceleration.y = Constants.PLAYER_INIT_ACC.y;
      position.y = Constants.RESOLUTION.y - Constants.PLAYER_SIZE.y;
      ON_GROUND = true;
    }
    if (position.x >= Constants.RESOLUTION.x - Constants.PLAYER_SIZE.x) {
      RIGHT = false;
      position.x = Constants.RESOLUTION.x - Constants.PLAYER_SIZE.x;
    }
    if (position.x <= 0) {
      LEFT = false;
      position.x = 0;
    }
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
        if (ON_GROUND)
          JUMP = true;
    }
  }

}
