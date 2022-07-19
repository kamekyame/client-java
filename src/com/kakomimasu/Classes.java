package com.kakomimasu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Classes {
  public static class MatchReq {
    public String spec;
    public Guest guest;

    public static class Guest {
      public String name;
    }
  }

  public static class MatchRes {
    public String userId;
    public String spec;
    public String gameId;
    public String index;
    public String pic;
  }

  public static class MatchActionReq {
    public Action[] actions;

    public static class Action {
      public int agentId;
      public String type;
      public int x;
      public int y;
    }
  }

  public static class MatchActionRes {
    public long receptionUnixTime;
    public int turn;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WsGameRes {
    public String type;
  }

  public static class WsGameInitialRes extends WsGameRes {
    public Game[] games;
  }

  public static class WsGameUpdateRes extends WsGameRes {
    public Game game;
  }

  public static class Game {
    public Board board;
    public boolean ending;
    public String gameId;
    public String gameName;
    public boolean gaming;
    public Log[] log;
    public int operationTime;
    public Player[] players;
    public String[] reservedUsers;
    public long startedAtUnixTime;
    public Tiled[] tiled;
    public int totalTurn;
    public int transitionTime;
    public int turn;
    public String type;

    public static class Point {
      public int basepoint;
      public int wallpoint;
    }

    public static class Log {
      public Player[] players;

      public static class Player {
        public Point point;
        public Action[] actions;

        public static class Point {
          public int basepoint;
          public int wallpoint;
        }

        public static class Action {
          public int agentId;
          public int type;
          public int x;
          public int y;
          public int res;
        }
      }
    }

    public static class Player {
      public String id;
      public Agent[] agents;
      public Point point;

      public static class Agent {
        public int x;
        public int y;
      }
    }

    public static class Tiled {
      public int type;
      public Integer player;
    }
  }

  public static class Board {
    public String name;
    public int width;
    public int height;
    public int nAgent;
    public int nPlayer;
    public int nTurn;
    public int nSec;
    public int[] points;
  }
}
