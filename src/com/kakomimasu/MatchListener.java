package com.kakomimasu;

public interface MatchListener {
  public default void onStart(Classes.Game game) {
  };

  public default void onTurn(Classes.Game game) {
  };

  public default void onEnd(Classes.Game game) {
  };
}