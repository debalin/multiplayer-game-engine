package com.debalin;

import com.debalin.characters.FallingStair;
import com.debalin.characters.Player;
import com.debalin.characters.SpawnPoint;
import com.debalin.characters.StandingStair;
import com.debalin.engine.MainEngine;
import com.debalin.engine.events.Event;
import com.debalin.engine.events.EventHandler;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.scripting.ScriptManager;
import com.debalin.engine.util.EngineConstants;
import com.debalin.util.Constants;
import processing.core.PVector;

import java.util.LinkedList;
import java.util.List;

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
        break;
      case "STAIR_SPAWN":
        handleStairSpawn(event);
        break;
      case "PLAYER_SPAWN":
        handlePlayerSpawn(event);
        break;
      case "NULL":
        break;
      case "RECORD_START":
        startRecording();
        break;
      case "RECORD_STOP":
        stopRecording();
        break;
      case "RECORD_PLAY":
        playRecording(event);
        break;
      case "PLAYER_DISCONNECT":
        handlePlayerDisconnect(event);
        break;
      case "SCRIPT":
        handleScripts(event);
        break;
    }
  }

  private void handleScripts(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    String scriptFunctionName = (String) eventParameters.get(0);

    if (scriptFunctionName.equals(simpleRaceManager.engine.scriptFunctionName)) {
      simpleRaceManager.engine.bindScriptObjects();
      ScriptManager.loadScript(simpleRaceManager.engine.scriptPath);
      ScriptManager.executeScript(simpleRaceManager.engine.scriptFunctionName);
    }
    else if (scriptFunctionName.equals("handlePlayer")) {
      int key = (Integer) eventParameters.get(1);
      boolean set = (Boolean) eventParameters.get(2);
      Player player;

      if (event.getConnectionID() == simpleRaceManager.getClientConnectionID().intValue())
        player = simpleRaceManager.player;
      else
        player = (Player) simpleRaceManager.otherPlayers.get(event.getConnectionID());

      ScriptManager.bindArgument("player", player);
      ScriptManager.executeScript("handlePlayer", key, set);
    }
  }

  private void handlePlayerDisconnect(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    int connectionID = (Integer) eventParameters.get(0);

    simpleRaceManager.otherPlayers.get(connectionID).setVisible(false);
    simpleRaceManager.otherPlayers.remove(connectionID);
    simpleRaceManager.engine.getEventManager().removePlayer(connectionID);
  }

  private void startRecording() {
    simpleRaceManager.engine.takeSnapshot();
    simpleRaceManager.engine.getEventManager().setRecording(true);
  }

  private void stopRecording() {
    simpleRaceManager.engine.getEventManager().setRecording(false);
  }

  private void playRecording(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    String replaySpeed = (String) eventParameters.get(0);

    switch (replaySpeed) {
      case "NORMAL":
        simpleRaceManager.engine.replaySpeed = MainEngine.ReplaySpeed.NORMAL;
        simpleRaceManager.engine.playRecordedGameObjects(1f);
        break;
      case "SLOW":
        simpleRaceManager.engine.replaySpeed = MainEngine.ReplaySpeed.SLOW;
        simpleRaceManager.engine.playRecordedGameObjects(2f);
        break;
      case "FAST":
        simpleRaceManager.engine.replaySpeed = MainEngine.ReplaySpeed.FAST;
        simpleRaceManager.engine.playRecordedGameObjects(0.5f);
        break;
    }
  }

  private void handlePlayerSpawn(Event event) {
    List<Object> eventParameters = event.getEventParameters();

    if (event.getConnectionID() == simpleRaceManager.getClientConnectionID().intValue()) {
      simpleRaceManager.player = new Player(simpleRaceManager.engine, (SpawnPoint) eventParameters.get(0), simpleRaceManager.fallingStairs, simpleRaceManager.standingStairs, (PVector) eventParameters.get(1));
      simpleRaceManager.playerObjectID = simpleRaceManager.engine.registerGameObject(simpleRaceManager.player, simpleRaceManager.playerObjectID, true);
      simpleRaceManager.player.setConnectionID(simpleRaceManager.getClientConnectionID().intValue());
    } else {
      Player player = new Player(simpleRaceManager.engine, (SpawnPoint) eventParameters.get(0), simpleRaceManager.fallingStairs, simpleRaceManager.standingStairs, (PVector) eventParameters.get(1));
      player.setConnectionID(event.getConnectionID());
      simpleRaceManager.otherPlayers.put(event.getConnectionID(), player);
      System.out.println("New player added, everybody will regenerate.");
      for (GameObject gameobject : simpleRaceManager.otherPlayers.values()) {
        Player otherPlayer = (Player) gameobject;
        otherPlayer.regenerate();
      }
      if (simpleRaceManager.player != null)
        simpleRaceManager.player.regenerate();
      synchronized (simpleRaceManager.fallingStairs) {
        simpleRaceManager.fallingStairs.clear();
      }
      if (simpleRaceManager.fallingStairsObjectID != -1)
        simpleRaceManager.engine.removeGameObjects(simpleRaceManager.fallingStairsObjectID);
      simpleRaceManager.engine.registerGameObject(player, -1, true);
    }
  }

  private void handleStairSpawn(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    long stairID = (Long) eventParameters.get(0);
    PVector color = (PVector) eventParameters.get(2);
    PVector position = (PVector) eventParameters.get(3);

    if (((Boolean) eventParameters.get(1)).booleanValue()) {
      PVector velocity = (PVector) eventParameters.get(4);
      Boolean isDeathStair = (Boolean) eventParameters.get(5);
      FallingStair stair = new FallingStair(simpleRaceManager.engine, color, position, stairID, velocity, isDeathStair);
      synchronized (simpleRaceManager.fallingStairs) {
        simpleRaceManager.fallingStairs.add(stair);
      }
      simpleRaceManager.stairMap.put(stair.getStairID(), stair);
      simpleRaceManager.fallingStairsObjectID = simpleRaceManager.engine.registerGameObject(stair, simpleRaceManager.fallingStairsObjectID, true);
    } else {
      StandingStair stair = new StandingStair(simpleRaceManager.engine, color, position, stairID);
      simpleRaceManager.standingStairs.add(stair);
      simpleRaceManager.stairMap.put(stair.getStairID(), stair);
      simpleRaceManager.standingStairsObjectID = simpleRaceManager.engine.registerGameObject(stair, simpleRaceManager.standingStairsObjectID, true);
    }
  }

  private void handlePlayerCollision(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    long stairID = (Long) eventParameters.get(0);
    int playerConnectionID = (Integer) eventParameters.get(2);
    Player player;

    if (playerConnectionID == simpleRaceManager.getClientConnectionID().intValue())
      player = simpleRaceManager.player;
    else
      player = (Player) simpleRaceManager.otherPlayers.get(playerConnectionID);

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
    } else {
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
    List<Object> eventParameters = event.getEventParameters();
    long stairID = (Long) (eventParameters.get(0));
    Player player;
    int playerConnectionID = (Integer) eventParameters.get(1);

    if (playerConnectionID == simpleRaceManager.getClientConnectionID().intValue())
      player = simpleRaceManager.player;
    else
      player = (Player) simpleRaceManager.otherPlayers.get(playerConnectionID);

    player.score -= 20;
    if (simpleRaceManager.stairMap.containsKey(stairID)) {
      ((FallingStair) simpleRaceManager.stairMap.get(stairID)).setKilledPlayer(true);
    }
    player.regenerate();
  }

  private void handleUserInput(Event event) {
    List<Object> eventParameters = event.getEventParameters();
    int key = (Integer) eventParameters.get(0);
    boolean set = (Boolean) eventParameters.get(1);
    Player player;

    if (event.getConnectionID() == simpleRaceManager.getClientConnectionID().intValue())
      player = simpleRaceManager.player;
    else
      player = (Player) simpleRaceManager.otherPlayers.get(event.getConnectionID());

    if (simpleRaceManager.engine.runScript) {
      List<Object> cascadeEventParameters = new LinkedList<>();
      cascadeEventParameters.add("handlePlayer");
      cascadeEventParameters.addAll(eventParameters);
      Event cascadeEvent = new Event(Constants.EVENT_TYPES.SCRIPT.toString(), cascadeEventParameters, EngineConstants.DEFAULT_TIMELINES.GAME_MILLIS.toString(), simpleRaceManager.getClientConnectionID().intValue(), simpleRaceManager.engine.gameTimelineInMillis.getTime(), true);
      simpleRaceManager.engine.getEventManager().raiseEvent(cascadeEvent, true);
    }
    else {
      player.handleKeypress(key, set);
    }
  }

}
