package com.kakomimasu;

public interface MatchListener {
  public default void onStart(Classes.Game game, int index) {
  };

  public default Classes.MatchActionReq onTurn(Classes.Game game, int index) {
    return new Classes.MatchActionReq();
  };

  public default void onEnd(Classes.Game game, int index) {
  };
}