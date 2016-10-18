package com.debalin.engine.network;

import com.debalin.engine.Controller;
import com.debalin.engine.util.EngineConstants;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.game_objects.NetworkEndTag;
import com.debalin.engine.game_objects.NetworkStartTag;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class GameClient implements Runnable {

  private Controller controller;
  private Socket clientConnection;
  private int remoteServerPort;
  private String remoteServerAddress;

  public GameClient(String remoteServerAddress, int remoteServerPort, Controller controller) {
    this.controller = controller;
    this.remoteServerPort = remoteServerPort;
    this.remoteServerAddress = remoteServerAddress;
  }

  public void run() {
    System.out.println("Trying to connect to server at " + remoteServerAddress + ":" + remoteServerPort + ".");
    connectToServer();
    new Thread(() -> maintainServerReadConnection()).start();
    new Thread(() -> maintainServerWriteConnection()).start();
  }

  private void maintainServerReadConnection() {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(clientConnection.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    Queue<GameObject> gameObjects = new LinkedList<>();

    int connectionID = -1;
    while (true) {
      while (true) {
        try {
          GameObject gameObject;
          if (EngineConstants.stringProtocol)
            gameObject = GameObjectAndStringConverter.convertStringToGameObject((String)in.readObject(), controller);
          else
            gameObject = (GameObject) in.readObject();
          if (gameObject.tag == GameServer.NetworkTag.START_TAG) {
            connectionID = gameObject.connectionID;
            gameObjects = new LinkedList<>();
          } else if (gameObject.tag == GameServer.NetworkTag.OBJECT) {
            gameObjects.add(gameObject);
          } else if (gameObject.tag == GameServer.NetworkTag.END_TAG) {
            break;
          }
        }
        catch (IOException e) {
          if (e.getMessage().equals(EngineConstants.readErrorMessage)) {
            System.out.println("Connection lost with server, will stop client read thread.");
            return;
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          System.out.println("Connection lost with server, will stop client read thread.");
          return;
        }
      }
      if (gameObjects != null && gameObjects.size() > 0) {
        controller.getDataFromServer(gameObjects, connectionID);
      }
      gameObjects = null;
    }
  }

  private void maintainServerWriteConnection() {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(clientConnection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        Queue<GameObject> dataToSend = controller.sendDataFromClient();

        if (dataToSend == null)
          continue;
        GameObject startObject = new NetworkStartTag(-1);
        if (EngineConstants.stringProtocol)
          out.writeObject(GameObjectAndStringConverter.convertGameObjectToString(startObject));
        else
          out.writeObject(startObject);

        for (GameObject object : dataToSend) {
          if (EngineConstants.stringProtocol)
            out.writeObject(GameObjectAndStringConverter.convertGameObjectToString(object));
          else
            out.writeObject(object);
        }

        GameObject endObject = new NetworkEndTag();
        if (EngineConstants.stringProtocol)
          out.writeObject(GameObjectAndStringConverter.convertGameObjectToString(endObject));
        else
          out.writeObject(endObject);
        out.reset();
        Thread.sleep(1);
      } catch (IOException e) {
        if (e.getMessage().equals(EngineConstants.writeErrorMessage)) {
          System.out.println("Connection lost with server, will stop client write thread.");
          return;
        }
      }
      catch (InterruptedException e) {}
    }
  }

  private void connectToServer() {
    try {
      clientConnection = new Socket(remoteServerAddress, remoteServerPort);
      System.out.println("Client accepted connection to " + clientConnection.getRemoteSocketAddress() + ".");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
