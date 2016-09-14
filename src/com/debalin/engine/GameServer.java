package com.debalin.engine;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer implements Runnable {

  private ServerSocket serverSocket;
  private int localServerPort;
  private Controller controller;
  private ArrayList<Socket> serverConnections;

  public static enum NetworkTag {
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
        new Thread(() -> maintainClientReadConnection(serverConnection)).start();
        new Thread(() -> maintainClientWriteConnection(serverConnection)).start();
      }
    }
  }

  private void maintainClientReadConnection(Socket serverConnection) {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(serverConnection.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      System.out.println(in.readUTF());
      controller.getDataFromClient(null);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void maintainClientWriteConnection(Socket serverConnection) {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(serverConnection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        ConcurrentLinkedQueue<GameObject> dataToSend = controller.sendDataFromServer();

        GameObject startObject = new NetworkStartTag();
        out.writeObject(startObject);

        for (GameObject object : dataToSend)
          out.writeObject(object);

        GameObject endObject = new NetworkEndTag();
        out.writeObject(endObject);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private Socket acceptConnections() {
    Socket serverConnection = null;
    try {
      serverConnection = serverSocket.accept();
      System.out.println("Server accepted connection to " + serverConnection.getRemoteSocketAddress() + ".");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return serverConnection;
  }

}
