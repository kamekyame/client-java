package com.kakomimasu;

public class KakomimasuAPI {
  public enum PlayerType {
    ACCOUNT, GUEST
  }

  public enum MatchType {
    Free, Custom
  }

  private PlayerType playerType = PlayerType.GUEST;
  private String bearerTokenOrName = "guest";
  private String spec;

  private KakomimasuHttpClient httpClient;

  public KakomimasuAPI() {
    this.httpClient = new KakomimasuHttpClient("https://api.kakomimasu.com");
  }

  public KakomimasuAPI(String host) {
    this.httpClient = new KakomimasuHttpClient(host);
  }

  public void setPlayerWithBearerToken(String bearerToken) {
    this.playerType = PlayerType.ACCOUNT;
    this.bearerTokenOrName = bearerToken;
  }

  public void setPlayerWithGuest(String guestName) {
    this.playerType = PlayerType.GUEST;
    this.bearerTokenOrName = guestName;
  }

  public void setSpec(String spec) {
    this.spec = spec;
  }

  public void matchWithFree() {
    var req = new Classes.MatchReq();
    req.spec = this.spec;
    if (this.playerType == PlayerType.GUEST) {
      req.guest = new Classes.MatchReq.Guest();
      req.guest.name = this.bearerTokenOrName;
    }
    var res = this.httpClient.match(req);
    System.out.println(res);

    this.httpClient.connectWebSocket();
  }
}
