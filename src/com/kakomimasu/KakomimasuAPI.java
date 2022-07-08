package com.kakomimasu;

public class KakomimasuAPI {
  public enum PlayerType {
    ACCOUNT, GUEST
  }

  public enum MatchType {
    Free, Custom
  }

  private String host;
  private PlayerType playerType = PlayerType.GUEST;
  private String bearerTokenOrName = "guest";
  private String spec;

  private KakomimasuHttpClient httpClient = new KakomimasuHttpClient();

  public KakomimasuAPI() {
    this.host = "https://api.kakomimasu.com";
  }

  public KakomimasuAPI(String host) {
    this.host = host;
  }

  public void SetPlayerWithBearerToken(String bearerToken) {
    this.playerType = PlayerType.ACCOUNT;
    this.bearerTokenOrName = bearerToken;
  }

  public void SetPlayerWithGuest(String guestName) {
    this.playerType = PlayerType.GUEST;
    this.bearerTokenOrName = guestName;
  }

  public void SetSpec(String spec) {
    this.spec = spec;
  }

  public static class MatchBuilder {
    private MatchType type;
    private String gameId;

    public MatchBuilder(MatchType type) {
      this.type = type;
    }

    public MatchBuilder setGameId(String gameId) {
      if (type == MatchType.Free)
        throw new Error("MatchType is Free. when change to Custom, be able to set gameId.");
      this.gameId = gameId;
      return this;
    }
  }

  public void matchWithFree() {

  }
}