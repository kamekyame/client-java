import java.net.URL;
import java.util.ArrayList;

import com.kakomimasu.*;
import com.kakomimasu.Classes.*;
import com.kakomimasu.Classes.MatchActionReq.Action.ActionType;

public class App {
    public static void main(String[] args) throws Exception {
        KakomimasuAPI client = new KakomimasuAPI(new URL("http://localhost:8880"));
        // KakomimasuAPI client = new KakomimasuAPI();
        client.setPlayerWithGuest("java-sample");

        var matchListener = new MatchListener() {
            private final int[][] around = new int[][] { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0,
                    -1 }, { 0, 1 }, { 1, -1 },
                    { 1, 0 }, { 1, 1 } };

            @Override
            public void onStart(Game game, int index) {
                System.out.println(String.format("ゲーム開始(%s)", game.gameId));
            }

            @Override
            public MatchActionReq onTurn(Game game, int index) {
                System.out.println(String.format("ターン %d/%d", game.turn, game.totalTurn));

                var actions = new Classes.MatchActionReq.Action[game.board.nAgent];

                for (int i = 0; i < game.players[index].agents.length; i++) {
                    var agent = game.players[index].agents[i];
                    actions[i] = new Classes.MatchActionReq.Action();
                    actions[i].agentId = i;
                    if (agent.x == -1) {
                        actions[i].type = ActionType.PUT;
                        actions[i].x = (int) (Math.random() * game.board.width);
                        actions[i].y = (int) (Math.random() * game.board.height);
                    } else {
                        ArrayList<int[]> nextList = new ArrayList<>();
                        for (int[] dp : around) {
                            var x = agent.x + dp[0];
                            var y = agent.y + dp[1];

                            if (x < 0 || x >= game.board.width || y < 0 || y >= game.board.height)
                                continue;
                            var idx = y * game.board.width + x;
                            var tiled = game.tiled[idx];
                            if (tiled.player != null && tiled.player == index)
                                continue;

                            nextList.add(dp);
                        }
                        int[] next = { 0, 0 };
                        if (nextList.size() > 0) {
                            next = nextList.get((int) (Math.random() * nextList.size()));
                        }
                        actions[i].x = agent.x + next[0];
                        actions[i].y = agent.y + next[1];
                        int nextIdx = actions[i].y * game.board.width + actions[i].x;
                        if (game.tiled[nextIdx].type == 1 && game.tiled[nextIdx].player != index) {
                            actions[i].type = ActionType.REMOVE;
                        } else {
                            actions[i].type = ActionType.MOVE;
                        }
                    }
                }

                // for (var action : actions) {
                // System.out.println(action.agentId + "(" + action.type + ") : " + action.x
                // +
                // "," + action.y);
                // }

                var req = new Classes.MatchActionReq();
                req.actions = actions;
                return req;
            }

            @Override
            public void onEnd(Game game, int index) {
                System.out.println("ゲーム終了");
            }

        };
        // client.matchInFree(matchListener);
        client.connectMatchInVsAI("none", matchListener);
    }
}
