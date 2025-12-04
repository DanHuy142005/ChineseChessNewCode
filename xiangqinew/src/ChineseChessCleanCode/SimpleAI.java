package ChineseChessCleanCode;

import java.util.Random;

enum AIDifficulty {
        PIKAFISH_1500(1500, 2, 0.30),
        PIKAFISH_1900(1900, 3, 0.18),
        PIKAFISH_2300(2300, 4, 0.05);

        private final int elo;
        private final int searchDepth;
        private final double noise;

        AIDifficulty(int elo, int searchDepth, double noise) {
                this.elo = elo;
                this.searchDepth = searchDepth;
                this.noise = noise;
        }

        int getElo() {
                return elo;
        }

        int getSearchDepth() {
                return searchDepth;
        }

        double getNoise() {
                return noise;
        }
}

public class SimpleAI {
        private final Random random = new Random();
        private final AIDifficulty difficulty;

        public SimpleAI(AIDifficulty difficulty) {
                this.difficulty = difficulty;
        }

        public CChessBoard.Move chooseMove(CChessBoard board, boolean isRed) {
                if (board.isGameOver()) {
                        return null;
                }
                if (board.isRedTurn() != isRed) {
                        return null;
                }
                var moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return null;
                }
                return moves.get(random.nextInt(moves.size()));
        }
}
