package com.debalin.engine;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Controller {

  public MainEngine engine;

  public void setEngine(MainEngine engine) {
    this.engine = engine;
    initialize();
  }

  public abstract void initialize();

  public abstract void manage();

  public ArrayList<Serializable> sendDataFromServer() {
    return null;
  }

  public ArrayList<Serializable> sendDataFromClient() {
    return null;
  }

  public void getDataFromServer(ArrayList<Serializable> dataFromServer) {}

  public void getDataFromClient(ArrayList<Serializable> dataFromClient) {}

}
