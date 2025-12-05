package ChineseChessCleanCode;

import java.util.List;
import java.util.Random;

class PikafishAI {
        private final Random rng = new Random();
        private final AIDifficulty difficulty;

        PikafishAI(AIDifficulty difficulty) {
                this.difficulty = difficulty;
        }

        CChessBoard.Move chooseMove(CChessBoard board, boolean isRed) {
                if (board.isGameOver() || board.isRedTurn() != isRed) {
                        return null;
                }
                List<CChessBoard.Move> moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return null;
                }
                int searchDepth = difficulty.getSearchDepth();
                double alpha = -Double.MAX_VALUE;
                double beta = Double.MAX_VALUE;
                CChessBoard.Move best = null;
                double bestScore = -Double.MAX_VALUE;
                for (CChessBoard.Move move : moves) {
                        CChessBoard next = new CChessBoard(board);
                        next.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
                        double score = minimax(next, searchDepth - 1, isRed, alpha, beta);
                        if (score > bestScore || best == null) {
                                bestScore = score;
                                best = move;
                        }
                        alpha = Math.max(alpha, score);
                }
                if (best == null) {
                        return moves.get(rng.nextInt(moves.size()));
                }
                return best;
        }

        private double minimax(CChessBoard board, int depth, boolean aiIsRed, double alpha, double beta) {
                if (depth == 0 || board.isGameOver()) {
                        return evaluate(board, aiIsRed);
                }
                List<CChessBoard.Move> moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return evaluate(board, aiIsRed);
                }
                boolean maximizing = board.isRedTurn() == aiIsRed;
                double best = maximizing ? -Double.MAX_VALUE : Double.MAX_VALUE;
                for (CChessBoard.Move mv : moves) {
                        CChessBoard next = new CChessBoard(board);
                        next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                        double score = minimax(next, depth - 1, aiIsRed, alpha, beta);
                        if (maximizing) {
                                best = Math.max(best, score);
                                alpha = Math.max(alpha, best);
                        } else {
                                best = Math.min(best, score);
                                beta = Math.min(beta, best);
                        }
                        if (beta <= alpha) {
                                break;
                        }
                }
                return best;
        }

        private double evaluate(CChessBoard board, boolean aiIsRed) {
                if (board.isGameOver()) {
                        Boolean winner = board.winnerIsRed();
                        if (winner == null) return 0;
                        return winner == aiIsRed ? 10000 : -10000;
                }
                double score = board.materialScore(aiIsRed);
                if (board.isInCheck(!aiIsRed)) {
                        score += 0.75;
                }
                if (board.isInCheck(aiIsRed)) {
                        score -= 0.75;
                }
                // Add light bonus for having the move
                if (board.isRedTurn() == aiIsRed) {
                        score += 0.15;
                }
                // Slight randomness to avoid deterministic play at lower levels
                score += rng.nextGaussian() * difficulty.getNoise();
                return score;
        }
}
