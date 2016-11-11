package com.debalin.engine.events;

import com.debalin.engine.MainEngine;
import com.debalin.engine.timeline.Timeline;
import com.debalin.engine.util.EngineConstants;

import java.util.*;

public class EventManager implements Runnable {

  Map<String, EventPriorities> eventTypePriorities;
  Map<String, Timeline> timelines;
  MainEngine engine;
  Map<String, PriorityQueue<OrderedEvent>> eventQueues;
  Map<String, PriorityQueue<OrderedEvent>> eventQueuesBackup;
  Map<String, PriorityQueue<OrderedEvent>> recordedEventQueues;

  Map<Integer, Queue<Event>> fromServerWriteQueues;
  Queue<Event> fromClientWriteQueue;
  Queue<Event> startupEvents;

  public Timeline replayTimelineInFrames;
  public float replayStartFrame;

  public boolean recording = false;
  public boolean playingRecording = false;

  public void setRecording(boolean recording) {
    if (recording) {
      replayStartFrame = engine.gameTimelineInFrames.getTime();
    }
    this.recording = recording;
  }

  private class OrderedEvent {
    public Event event;
    public float score;

    public OrderedEvent(Event event, float score) {
      this.event = event;
      this.score = score;
    }
  }

  private class EventComparator implements Comparator<OrderedEvent> {
    public int compare(OrderedEvent event1, OrderedEvent event2) {
      if (event1.score < event2.score) {
        return -1;
      } else if (event1.score > event2.score) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public enum EventPriorities {
    HIGH, MED, LOW
  }

  public Queue<Event> getWriteQueue(int connectionID) {
    if (connectionID == -1) {
      return fromClientWriteQueue;
    } else {
      if (fromServerWriteQueues.get(connectionID) == null) {
        Queue<Event> fromServerWriteQueue = new LinkedList<>();
        synchronized (fromServerWriteQueues) {
          fromServerWriteQueues.put(connectionID, fromServerWriteQueue);
          synchronized (fromServerWriteQueue) {
            synchronized (startupEvents) {
              fromServerWriteQueue.addAll(startupEvents);
            }
          }
        }
      }
      return fromServerWriteQueues.get(connectionID);
    }
  }

  public EventManager(MainEngine engine) {
    fromServerWriteQueues = new HashMap<>();
    fromClientWriteQueue = new LinkedList<>();
    startupEvents = new LinkedList<>();
    eventTypePriorities = new HashMap<>();
    timelines = new HashMap<>();
    eventQueues = new HashMap<>();
    eventQueuesBackup = new HashMap<>();
    recordedEventQueues = new HashMap<>();
    this.engine = engine;
    getDefaultEventTypes();
  }

  private void getDefaultEventTypes() {
    for (EngineConstants.DEFAULT_EVENT_TYPES defaultEventType : EngineConstants.DEFAULT_EVENT_TYPES.values()) {
      eventTypePriorities.put(defaultEventType.toString(), EventPriorities.HIGH);
    }
  }

  public void registerEventType(String eventType, EventPriorities defaultEventPriority) {
    eventTypePriorities.put(eventType, defaultEventPriority);
  }

  public void registerTimeline(Timeline timeline, String timelineName) {
    timelines.put(timelineName, timeline);
    eventQueues.put(timelineName, new PriorityQueue<>(new EventComparator()));
    recordedEventQueues.put(timelineName, new PriorityQueue<>(new EventComparator()));
  }

  public void raiseEvent(Event event, boolean toBroadcast) {
    if (playingRecording)
      return;

    if (MainEngine.serverMode) {
      broadcastEvent(event, -1);
      return;
    }

    Timeline timeline = timelines.get(event.getTimelineName());
    if (timeline == null) {
      System.out.println("Timeline not registered, please do it first.");
      return;
    }
    String eventType = event.getEventType();
    EventPriorities eventDefaultPriority = eventTypePriorities.get(eventType);

    if (eventDefaultPriority == null) {
      System.out.println("Event type does not exist, will default to low.");
      eventDefaultPriority = EventPriorities.LOW;
    }

    synchronized (eventQueues) {
      switch (eventDefaultPriority) {
        case HIGH:
          eventQueues.get(event.getTimelineName()).add(new OrderedEvent(event, event.getTime() - 100));
          break;
        case MED:
          eventQueues.get(event.getTimelineName()).add(new OrderedEvent(event, event.getTime() - 50));
          break;
        case LOW:
          eventQueues.get(event.getTimelineName()).add(new OrderedEvent(event, event.getTime()));
          break;
      }
    }

    if (toBroadcast) {
      synchronized (fromClientWriteQueue) {
        fromClientWriteQueue.add(event);
        fromClientWriteQueue.notify();
      }
    }
  }

  public void handleEvents() {
    try {
      Iterator it;
      synchronized (eventQueues) {
        it = eventQueues.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<String, PriorityQueue<OrderedEvent>> pair = (Map.Entry) it.next();
          String timelineName = pair.getKey();
          Queue<OrderedEvent> timelineQueue = pair.getValue();
          while (!timelineQueue.isEmpty()) {
            if (playingRecording) {
              OrderedEvent orderedEvent = timelineQueue.peek();
//              System.out.println("Frame difference: " + (orderedEvent.event.frame - replayStartFrame) + ", current replay time: " + replayTimelineInFrames.getTime() + ".");
              if (orderedEvent.event.frame - replayStartFrame <= replayTimelineInFrames.getTime() - 1) {
                orderedEvent = timelineQueue.poll();
                MainEngine.controller.getEventHandler().onEvent(orderedEvent.event);
//                System.out.println("Handling recorded event " + orderedEvent.event.getEventType() + ".");
              }
              else {
                break;
              }
              if (timelineQueue.isEmpty()) {
                finishPlayingRecordedEvents();
                break;
              }
            } else {
              OrderedEvent orderedEvent = timelineQueue.poll();
              if (recording) {
                recordEvent(orderedEvent, timelineName);
              }
              MainEngine.controller.getEventHandler().onEvent(orderedEvent.event);
            }
          }
        }
      }
    } catch (Exception ex) {
    }
  }

  public void finishPlayingRecordedEvents() {
    playingRecording = false;
    eventQueues.clear();
    for (String timelineName : recordedEventQueues.keySet()) {
      recordedEventQueues.get(timelineName).clear();
    }
    for (String timelineName : eventQueuesBackup.keySet()) {
      eventQueues.put(timelineName, eventQueuesBackup.get(timelineName));
    }
    engine.stopPlayingRecordedGameObjects();
  }

  public void playRecordedEvents(float frameTicSize) {
    synchronized (eventQueues) {
      playingRecording = true;
      eventQueuesBackup.clear();
      System.out.println("Backing up event queue.");
      for (String timelineName : eventQueues.keySet()) {
        eventQueuesBackup.put(timelineName, eventQueues.get(timelineName));
      }
      eventQueues.clear();
      for (String timelineName : recordedEventQueues.keySet()) {
        eventQueues.put(timelineName, recordedEventQueues.get(timelineName));
      }
      replayTimelineInFrames = new Timeline(engine.gameTimelineInFrames, frameTicSize, Timeline.TimelineIterationTypes.LOOP, engine);
      engine.registerGameObject(replayTimelineInFrames, -1, true);
    }
  }

  private void recordEvent(OrderedEvent orderedEvent, String timelineName) {
    orderedEvent.event.frame = engine.gameTimelineInFrames.getTime();
    recordedEventQueues.get(timelineName).add(orderedEvent);
  }

  public void run() {
    System.out.println("Starting event handling.");
    handleEvents();
  }

  public void broadcastEvent(Event event, int connectionID) {
    if (event.isBackup()) {
      synchronized (startupEvents) {
        startupEvents.add(event);
      }
    }
    synchronized (fromServerWriteQueues) {
      Iterator it = fromServerWriteQueues.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Queue<Event>> pair = (Map.Entry) it.next();
        if (pair.getKey() == connectionID)
          continue;
        else {
          Queue<Event> fromServerWriteQueue = pair.getValue();
          synchronized (fromServerWriteQueue) {
            fromServerWriteQueue.add(event);
            fromServerWriteQueue.notify();
          }
        }
      }
    }
  }

}
