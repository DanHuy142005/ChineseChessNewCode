package ChineseChessCleanCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

enum AIDifficulty {
        EASY(500, 2, 0.6, 2.3),
        MEDIUM(1200, 4, 0.15, 1.0),
        HARD(1600, 5, 0.02, 0.25),
        EXTREME(2000, 6, 0.0, 0.15);

        private final int elo;
        private final int maxDepth;
        private final double evalNoise;
        private final double topMoveRange;

        AIDifficulty(int elo, int maxDepth, double evalNoise, double topMoveRange) {
                this.elo = elo;
                this.maxDepth = maxDepth;
                this.evalNoise = evalNoise;
                this.topMoveRange = topMoveRange;
        }

        int getElo() {
                return elo;
        }

        int getMaxDepth() {
                return maxDepth;
        }

        double getEvalNoise() {
                return evalNoise;
        }

        double getTopMoveRange() {
                return topMoveRange;
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
                List<CChessBoard.Move> moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return null;
                }
                if (difficulty == AIDifficulty.EASY) {
                        return pickNoSearch(board, moves, isRed);
                }
                return searchMove(board, moves, isRed);
        }

        private CChessBoard.Move pickNoSearch(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed) {
                double best = Double.NEGATIVE_INFINITY;
                Map<CChessBoard.Move, Double> scores = new HashMap<>();
                for (CChessBoard.Move mv : moves) {
                        CChessBoard next = new CChessBoard(board);
                        next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                        double score = evaluate(next, isRed);
                        score += noise(difficulty.getEvalNoise());
                        scores.put(mv, score);
                        if (score > best) {
                                best = score;
                        }
                }
                List<CChessBoard.Move> candidates = new ArrayList<>();
                for (Map.Entry<CChessBoard.Move, Double> e : scores.entrySet()) {
                        if (e.getValue() >= best - difficulty.getTopMoveRange()) {
                                candidates.add(e.getKey());
                        }
                }
                if (candidates.isEmpty()) {
                                return moves.get(random.nextInt(moves.size()));
                }
                return candidates.get(random.nextInt(candidates.size()));
        }

        private CChessBoard.Move searchMove(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed) {
                long deadline = System.nanoTime() + 3_000_000_000L;
                double[] lastScores = null;
                List<CChessBoard.Move> moveOrder = new ArrayList<>(moves);
                for (int depth = 1; depth <= difficulty.getMaxDepth(); depth++) {
                        TimeControl timeControl = new TimeControl(deadline);
                        double[] scores = new double[moveOrder.size()];
                        boolean completed = true;
                        for (int i = 0; i < moveOrder.size(); i++) {
                                CChessBoard.Move mv = moveOrder.get(i);
                                CChessBoard next = new CChessBoard(board);
                                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                                boolean maximizing = next.isRedTurn() == isRed;
                                double score = alphaBeta(next, depth - 1, -1e9, 1e9, maximizing, isRed, timeControl);
                                if (timeControl.timedOut) {
                                        completed = false;
                                        break;
                                }
                                scores[i] = score;
                        }
                        if (completed) {
                                lastScores = scores;
                        } else {
                                break;
                        }
                }
                if (lastScores == null) {
                        return moves.get(random.nextInt(moves.size()));
                }
                double best = Double.NEGATIVE_INFINITY;
                for (double s : lastScores) {
                        if (s > best) best = s;
                }
                List<CChessBoard.Move> candidates = new ArrayList<>();
                for (int i = 0; i < moveOrder.size(); i++) {
                        if (lastScores[i] >= best - difficulty.getTopMoveRange()) {
                                candidates.add(moveOrder.get(i));
                        }
                }
                if (difficulty != AIDifficulty.EASY) {
                        List<CChessBoard.Move> filtered = new ArrayList<>();
                        for (CChessBoard.Move mv : candidates) {
                                if (!isLosingCapture(board, mv)) {
                                        filtered.add(mv);
                                }
                        }
                        if (!filtered.isEmpty()) {
                                candidates = filtered;
                        }
                }
                if (candidates.isEmpty()) {
                        return moves.get(random.nextInt(moves.size()));
                }
                return candidates.get(random.nextInt(candidates.size()));
        }

        private double alphaBeta(CChessBoard board, int depth, double alpha, double beta, boolean maximizingPlayer, boolean aiIsRed, TimeControl timeControl) {
                if (timeControl.isTimeout()) {
                        return 0.0;
                }
                if (depth == 0 || board.isGameOver()) {
                        return evaluate(board, aiIsRed);
                }
                List<CChessBoard.Move> moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return evaluate(board, aiIsRed);
                }
                if (maximizingPlayer) {
                        double value = -1e9;
                        for (CChessBoard.Move mv : moves) {
                                CChessBoard next = new CChessBoard(board);
                                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                                value = Math.max(value, alphaBeta(next, depth - 1, alpha, beta, next.isRedTurn() == aiIsRed, aiIsRed, timeControl));
                                alpha = Math.max(alpha, value);
                                if (beta <= alpha || timeControl.timedOut) break;
                        }
                        return value;
                } else {
                        double value = 1e9;
                        for (CChessBoard.Move mv : moves) {
                                CChessBoard next = new CChessBoard(board);
                                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                                value = Math.min(value, alphaBeta(next, depth - 1, alpha, beta, next.isRedTurn() == aiIsRed, aiIsRed, timeControl));
                                beta = Math.min(beta, value);
                                if (beta <= alpha || timeControl.timedOut) break;
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
                double score = board.materialScore(aiIsRed);
                for (Pieces p : board.getPieces()) {
                        if (p.rank == Rank.PAWN && ((p.isRed && p.row > 4) || (!p.isRed && p.row < 5))) {
                                score += p.isRed == aiIsRed ? 0.2 : -0.2;
                        }
                        if (p.rank != Rank.KING && p.rank != Rank.PAWN) {
                                if ((p.isRed && p.row == 0) || (!p.isRed && p.row == 9)) {
                                        score += p.isRed == aiIsRed ? -0.1 : 0.1;
                                }
                        }
                        if (p.col >= 3 && p.col <= 5 && p.row >= 3 && p.row <= 6) {
                                score += p.isRed == aiIsRed ? 0.05 : -0.05;
                        }
                        if (p.rank == Rank.KING && board.isInCheck(p.isRed)) {
                                score += p.isRed == aiIsRed ? -0.5 : 0.5;
                        }
                }
                score += noise(difficulty.getEvalNoise());
                return score;
        }

        private double pieceValue(Pieces p) {
                switch (p.rank) {
                        case KING: return 1000;
                        case ROOK: return 9;
                        case CANNON: return 4.5;
                        case KNIGHT: return 4;
                        case ELEPHENT: return 2.5;
                        case ADVISOR: return 2;
                        case PAWN: return ((p.isRed && p.row > 4) || (!p.isRed && p.row < 5)) ? 2.0 : 1.5;
                        default: return 0;
                }
        }

        private boolean isLosingCapture(CChessBoard board, CChessBoard.Move mv) {
                Pieces attacker = board.pieceAt(mv.fromCol, mv.fromRow);
                Pieces target = board.pieceAt(mv.toCol, mv.toRow);
                if (attacker == null || target == null) {
                        return false;
                }
                double attackerValue = pieceValue(attacker);
                double targetValue = pieceValue(target);
                if (attackerValue <= targetValue) {
                        return false;
                }
                CChessBoard next = new CChessBoard(board);
                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                List<CChessBoard.Move> replies = next.getAllValidMoves();
                for (CChessBoard.Move rep : replies) {
                        if (rep.toCol == mv.toCol && rep.toRow == mv.toRow) {
                                return true;
                        }
                }
                return false;
        }

        private double noise(double range) {
                if (range == 0.0) {
                        return 0.0;
                }
                return (random.nextDouble() * 2.0 - 1.0) * range;
        }

        private static class TimeControl {
                final long deadlineNanos;
                boolean timedOut;

                TimeControl(long deadlineNanos) {
                        this.deadlineNanos = deadlineNanos;
                        this.timedOut = false;
                }

                boolean isTimeout() {
                        if (timedOut) {
                                return true;
                        }
                        if (System.nanoTime() > deadlineNanos) {
                                timedOut = true;
                                return true;
                        }
                        return false;
                }
        }
}
