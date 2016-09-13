package com.debalin.engine;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class GameServer implements Runnable {

  private ServerSocket serverSocket;
  private int localServerPort;
  private Controller controller;
  private ArrayList<Socket> serverConnections;

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
    DataInputStream in;

    try {
      in = new DataInputStream(serverConnection.getInputStream());
      System.out.println(in.readUTF());
      controller.getDataFromClient(null);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void maintainClientWriteConnection(Socket serverConnection) {
    DataOutputStream out;

    try {
      ArrayList<Serializable> dataToSend = controller.sendDataFromServer();
      out = new DataOutputStream(serverConnection.getOutputStream());
      out.writeUTF("Writing stuff from server to client (" + serverConnection.getRemoteSocketAddress() + ").");
    }
    catch (IOException e) {
      e.printStackTrace();
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
