package com.debalin.engine;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Controller {

  public MainEngine engine;

  public void setEngine(MainEngine engine) {
    this.engine = engine;
    initialize();
  }

  public abstract void initialize();

  public abstract void manage();

  public ConcurrentLinkedQueue<GameObject> sendDataFromServer() {
    return null;
  }

  public ConcurrentLinkedQueue<GameObject> sendDataFromClient() {
    return null;
  }

  public void getDataFromServer(ConcurrentLinkedQueue<GameObject> dataFromServer) {}

  public void getDataFromClient(ConcurrentLinkedQueue<GameObject> dataFromClient) {}

}
