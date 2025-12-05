package ChineseChessCleanCode;

import java.util.Random;

enum AIDifficulty {
        EASY(1200, 2, 0.30),
        MEDIUM(1600, 3, 0.18),
        HARD(1800, 4, 0.08),
        EXTREME(2000, 5, 0.02);

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
                switch (difficulty) {
                        case EASY:
                                return moves.get(random.nextInt(moves.size()));
                        case MEDIUM:
                                return bestMaterialGainMove(board, moves, isRed);
                        case HARD:
                        case EXTREME:
                                return minimaxMove(board, moves, isRed, difficulty.getSearchDepth());
                        default:
                                return moves.get(random.nextInt(moves.size()));
                }
        }

        private CChessBoard.Move bestMaterialGainMove(CChessBoard board, java.util.List<CChessBoard.Move> moves, boolean isRed) {
                double bestScore = Double.NEGATIVE_INFINITY;
                CChessBoard.Move bestMove = null;
                for (CChessBoard.Move move : moves) {
                        CChessBoard copy = new CChessBoard(board);
                        copy.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
                        double score = copy.materialScore(isRed);
                        score += random.nextGaussian() * difficulty.getNoise();
                        if (score > bestScore) {
                                bestScore = score;
                                bestMove = move;
                        }
                }
                return bestMove != null ? bestMove : moves.get(random.nextInt(moves.size()));
        }

        private CChessBoard.Move minimaxMove(CChessBoard board, java.util.List<CChessBoard.Move> moves, boolean isRed, int depth) {
                double bestScore = Double.NEGATIVE_INFINITY;
                CChessBoard.Move bestMove = null;
                for (CChessBoard.Move move : moves) {
                        CChessBoard copy = new CChessBoard(board);
                        copy.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
                        double score = minimax(copy, depth - 1, isRed, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                        score += random.nextGaussian() * difficulty.getNoise();
                        if (score > bestScore) {
                                bestScore = score;
                                bestMove = move;
                        }
                }
                return bestMove != null ? bestMove : moves.get(random.nextInt(moves.size()));
        }

        private double minimax(CChessBoard board, int depth, boolean aiIsRed, double alpha, double beta) {
                if (depth == 0 || board.isGameOver()) {
                        return evaluate(board, aiIsRed);
                }
                var moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return evaluate(board, aiIsRed);
                }
                boolean maximizing = board.isRedTurn() == aiIsRed;
                if (maximizing) {
                        double value = Double.NEGATIVE_INFINITY;
                        for (CChessBoard.Move move : moves) {
                                CChessBoard copy = new CChessBoard(board);
                                copy.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
                                value = Math.max(value, minimax(copy, depth - 1, aiIsRed, alpha, beta));
                                alpha = Math.max(alpha, value);
                                if (alpha >= beta) break;
                        }
                        return value;
                } else {
                        double value = Double.POSITIVE_INFINITY;
                        for (CChessBoard.Move move : moves) {
                                CChessBoard copy = new CChessBoard(board);
                                copy.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
                                value = Math.min(value, minimax(copy, depth - 1, aiIsRed, alpha, beta));
                                beta = Math.min(beta, value);
                                if (alpha >= beta) break;
                        }
                        return value;
                }
        }

        private double evaluate(CChessBoard board, boolean aiIsRed) {
                if (board.isGameOver()) {
                        Boolean winner = board.winnerIsRed();
                        if (winner == null) return 0;
                        return winner == aiIsRed ? 10000 : -10000;
                }
                return board.materialScore(aiIsRed) + (board.isRedTurn() == aiIsRed ? 0.05 : 0);
        }
}
