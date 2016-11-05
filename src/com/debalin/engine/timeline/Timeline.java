package com.debalin.engine.timeline;

import com.debalin.engine.game_objects.DynamicGameObject;
import processing.core.PApplet;

import java.util.concurrent.atomic.AtomicLong;

public class Timeline extends DynamicGameObject {

  private double anchor;
  private double ticSize;
  private AtomicLong time;
  private TimelineIterationTypes timelineIterationType;
  private PApplet engineRef;

  public enum TimelineIterationTypes {
    REAL, LOOP
  }

  public Timeline(long anchor, int ticSize, TimelineIterationTypes timelineIterationType, PApplet engineRef) {
    visible = true;
    this.anchor = anchor;
    this.time = new AtomicLong(anchor);
    this.ticSize = ticSize;
    this.timelineIterationType = timelineIterationType;
    this.engineRef = engineRef;
    this.update();
  }

  public void update() {
    switch (timelineIterationType) {
      case REAL:
        time.set((long)((System.nanoTime() - anchor) / ticSize));
        break;
      case LOOP:
        time.set((long)((engineRef.frameCount - anchor) / ticSize));
    }
  }

  public void draw() {}

  public long getTime() {
    return time.get();
  }
}
