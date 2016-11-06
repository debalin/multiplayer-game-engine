package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.StandingStair;
import com.debalin.engine.events.Event;
import com.debalin.engine.events.EventHandler;
import com.debalin.util.Constants;

import java.util.List;

/**
 * Created by debalin on 05-11-2016.
 */
public class GameEventHandler implements EventHandler {

  SimpleRaceManager simpleRaceManager;

  public GameEventHandler(SimpleRaceManager simpleRaceManager) {
    this.simpleRaceManager = simpleRaceManager;
  }

  public void onEvent(Event event) {
    switch (event.getEventType()) {
      case "USER_INPUT":
        handleUserInput(event);
        break;
      case "PLAYER_DEATH":
        handlePlayerDeath(event);
        break;
      case "PLAYER_COLLISION":
        handlePlayerCollision(event);
    }
  }

  private void handlePlayerCollision(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    long stairID = (Long) eventParameters.get(0);
    Player player = simpleRaceManager.player;

    if (((Boolean) eventParameters.get(1)).booleanValue()) {
      FallingStair stair = (FallingStair) simpleRaceManager.stairMap.get(stairID);
      player.getPosition().y = (stair).getPosition().y - player.getSize().y - 3;
      player.LEFT = false;
      player.RIGHT = false;
      player.getVelocity().y = stair.getVelocity().y;
      player.getAcceleration().y = Constants.PLAYER_INIT_ACC.y;
      player.changeState(Player.States.ON_STAIR);
      player.collidedStair = stair;
      player.score += 10;
    }
    else {
      StandingStair stair = (StandingStair) simpleRaceManager.stairMap.get(stairID);
      player.getPosition().y = (stair).getPosition().y - player.getSize().y - 3;
      player.LEFT = false;
      player.RIGHT = false;
      player.getVelocity().y = 0;
      player.getAcceleration().y = Constants.PLAYER_INIT_ACC.y;
      player.changeState(Player.States.ON_STAIR);
      player.collidedStair = stair;
      player.score += 7;
    }
  }

  private void handlePlayerDeath(Event event) {
    long stairID = (Long)(event.getEventParameters().get(0));
    simpleRaceManager.player.score -= 20;
    if (simpleRaceManager.stairMap.containsKey(stairID)) {
      ((FallingStair) simpleRaceManager.stairMap.get(stairID)).setKilledPlayer(true);
    }
    simpleRaceManager.player.regenerate();
  }

  private void handleUserInput(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    simpleRaceManager.player.handleKeypress((Integer)eventParameters.get(0), (Boolean)eventParameters.get(1));
  }

}
