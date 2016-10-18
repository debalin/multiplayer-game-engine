package com.debalin.engine.network;

import com.debalin.engine.Controller;
import com.debalin.engine.util.EngineConstants;
import com.debalin.engine.game_objects.GameObject;
import com.debalin.engine.game_objects.NetworkEndTag;
import com.debalin.engine.game_objects.NetworkStartTag;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer implements Runnable {

  private ServerSocket serverSocket;
  private int localServerPort;
  private Controller controller;
  private List<Socket> serverConnections;

  public enum NetworkTag {
    OBJECT, START_TAG, END_TAG
  }

  public GameServer(int localServerPort, Controller controller) {
    this.localServerPort = localServerPort;
    this.controller = controller;
    this.serverConnections = new ArrayList<>();

    try {
      serverSocket = new ServerSocket(this.localServerPort);
      System.out.println("Server started at IP " + serverSocket.getLocalSocketAddress() + ".");
    }
    catch (IOException e) {
      System.out.println("Some error in GameServer.");
      e.printStackTrace();
    }
  }

  public void run() {
    while (true) {
      System.out.println("Looking for connections.");
      Socket serverConnection = acceptConnections();
      if (serverConnection != null) {
        serverConnections.add(serverConnection);
        new Thread(() -> maintainClientReadConnection(serverConnection, serverConnections.size() - 1)).start();
        new Thread(() -> maintainClientWriteConnection(serverConnection, serverConnections.size() - 1)).start();
      }
    }
  }

  private void maintainClientReadConnection(Socket serverConnection, int connectionID) {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(serverConnection.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    Queue<GameObject> gameObjects = new LinkedList<>();

    while (true) {
      while (true) {
        try {
          GameObject gameObject = (GameObject) in.readObject();
          if (gameObject.tag == GameServer.NetworkTag.START_TAG) {
            gameObjects = new ConcurrentLinkedQueue<>();
          } else if (gameObject.tag == GameServer.NetworkTag.OBJECT) {
            gameObjects.add(gameObject);
          } else if (gameObject.tag == GameServer.NetworkTag.END_TAG) {
            break;
          }
        }
        catch (IOException e) {
          if (e.getMessage().equals(EngineConstants.readErrorMessage)) {
            System.out.println("Connection lost with client " + serverConnection.getRemoteSocketAddress() + ", will stop server read thread.");
            return;
          }
        }
        catch (Exception e) {
          System.out.println("Connection lost with client " + serverConnection.getRemoteSocketAddress() + ", will stop server read thread.");
          return;
        }
      }
      if (gameObjects != null && gameObjects.size() > 0) {
        controller.getDataFromClient(gameObjects, connectionID);
      }
      gameObjects = null;
    }
  }

  private void maintainClientWriteConnection(Socket serverConnection, int connectionID) {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(serverConnection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Queue<GameObject> writeQueue = controller.sendDataFromServer(connectionID);

    while (true) {
      try {
        synchronized (writeQueue) {
          GameObject startObject = new NetworkStartTag(connectionID);
          out.writeObject(startObject);

          while (!writeQueue.isEmpty())
            out.writeObject(writeQueue.poll());

          GameObject endObject = new NetworkEndTag();
          out.writeObject(endObject);
          out.reset();

          while (writeQueue.isEmpty())
            writeQueue.wait();
        }
      } catch (IOException e) {
        if (e.getMessage().equals(EngineConstants.writeErrorMessage)) {
          System.out.println("Connection lost with client " + serverConnection.getRemoteSocketAddress() + ", will stop server write thread.");
          return;
        }
      }
      catch (InterruptedException e) {
        System.out.println("Some issue in writeQueue.");
      }
    }
  }

  private Socket acceptConnections() {
    Socket serverConnection = null;
    try {
      serverConnection = serverSocket.accept();
      controller.incrementClientCount();
      System.out.println("Server accepted connection to " + serverConnection.getRemoteSocketAddress() + ".");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return serverConnection;
  }

}
