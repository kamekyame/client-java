package com.kakomimasu;

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
}
