package ChineseChessCleanCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

enum AIDifficulty {
    EASY(500, 2, 0.6, 2.3, 100_000_000L, 300_000_000L),
    MEDIUM(1200, 3, 0.15, 1.0, 400_000_000L, 800_000_000L),
    HARD(1600, 4, 0.0, 0.28, 1_000_000_000L, 2_000_000_000L),
    EXTREME(2000, 5, 0.0, 0.2, 1_500_000_000L, 3_000_000_000L);

    private final int elo;
    private final int maxDepth;
    private final double evalNoise;
    private final double topMoveRange;
    private final long minThinkTimeNanos;
    private final long maxThinkTimeNanos;

    AIDifficulty(int elo, int maxDepth, double evalNoise, double topMoveRange, long minThinkTimeNanos, long maxThinkTimeNanos) {
        this.elo = elo;
        this.maxDepth = maxDepth;
        this.evalNoise = evalNoise;
        this.topMoveRange = topMoveRange;
        this.minThinkTimeNanos = minThinkTimeNanos;
        this.maxThinkTimeNanos = maxThinkTimeNanos;
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

    long getMinThinkTimeNanos() {
        return minThinkTimeNanos;
    }

    long getMaxThinkTimeNanos() {
        return maxThinkTimeNanos;
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
        if (moves == null || moves.isEmpty()) {
            return null;
        }
        long now = System.nanoTime();
        long span = difficulty.getMinThinkTimeNanos() + (long) ((difficulty.getMaxThinkTimeNanos() - difficulty.getMinThinkTimeNanos()) * random.nextDouble());
        long deadline = now + span;
        if (difficulty == AIDifficulty.EASY) {
            return pickEasy(board, moves, isRed, deadline);
        }
        return searchMove(board, moves, isRed, deadline);
    }

    private CChessBoard.Move pickEasy(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed, long deadline) {
        double best = Double.NEGATIVE_INFINITY;
        Map<CChessBoard.Move, Double> scores = new HashMap<>();
        for (CChessBoard.Move mv : moves) {
            CChessBoard next = new CChessBoard(board);
            next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
            double score = evaluate(next, isRed);
            if (isLosingCapture(board, mv)) {
                score -= 0.3;
            }
            score += noise(difficulty.getEvalNoise());
            scores.put(mv, score);
            if (score > best) {
                best = score;
            }
            if (System.nanoTime() > deadline) {
                break;
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

    private CChessBoard.Move searchMove(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed, long deadline) {
        List<CChessBoard.Move> moveOrder = new ArrayList<>(moves);
        double[] lastScores = null;
        int deepestCompleted = 0;
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
                score += centralCannonBonus(board, mv, isRed);
                scores[i] = score;
            }
            if (completed) {
                lastScores = scores;
                deepestCompleted = depth;
                reorder(moveOrder, scores);
            } else {
                break;
            }
        }
        if (lastScores == null) {
            return moves.get(random.nextInt(moves.size()));
        }
        double best = Double.NEGATIVE_INFINITY;
        for (double s : lastScores) {
            if (s > best) {
                best = s;
            }
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
            if (!filtered.isEmpty() && (difficulty == AIDifficulty.HARD || difficulty == AIDifficulty.EXTREME)) {
                candidates = filtered;
            } else if (!filtered.isEmpty() && difficulty == AIDifficulty.MEDIUM) {
                if (random.nextDouble() < 0.8) {
                    candidates = filtered;
                }
            }
        }
        if (candidates.isEmpty()) {
            return moves.get(random.nextInt(moves.size()));
        }
        if (deepestCompleted > 1) {
            Collections.shuffle(candidates, random);
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
        if (moves == null || moves.isEmpty()) {
            return evaluate(board, aiIsRed);
        }
        if (maximizingPlayer) {
            double value = -1e9;
            for (CChessBoard.Move mv : moves) {
                CChessBoard next = new CChessBoard(board);
                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                double child = alphaBeta(next, depth - 1, alpha, beta, next.isRedTurn() == aiIsRed, aiIsRed, timeControl);
                if (timeControl.timedOut) {
                    break;
                }
                if (child > value) {
                    value = child;
                }
                if (value > alpha) {
                    alpha = value;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return value;
        } else {
            double value = 1e9;
            for (CChessBoard.Move mv : moves) {
                CChessBoard next = new CChessBoard(board);
                next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                double child = alphaBeta(next, depth - 1, alpha, beta, next.isRedTurn() == aiIsRed, aiIsRed, timeControl);
                if (timeControl.timedOut) {
                    break;
                }
                if (child < value) {
                    value = child;
                }
                if (value < beta) {
                    beta = value;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return value;
        }
    }

    private double evaluate(CChessBoard board, boolean aiIsRed) {
        if (board.isGameOver()) {
            Boolean winner = board.winnerIsRed();
            if (winner == null) {
                return 0.0;
            }
            return winner == aiIsRed ? 10000.0 : -10000.0;
        }
        double score = board.materialScore(aiIsRed);
        for (Pieces p : board.getPieces()) {
            double factor = p.isRed == aiIsRed ? 1.0 : -1.0;
            if (p.rank == Rank.PAWN) {
                if ((p.isRed && p.row > 4) || (!p.isRed && p.row < 5)) {
                    score += factor * 0.3;
                }
                if (p.col >= 3 && p.col <= 5) {
                    score += factor * 0.1;
                }
            }
            if (p.rank != Rank.KING && p.rank != Rank.PAWN) {
                if ((p.isRed && p.row == 0) || (!p.isRed && p.row == 9)) {
                    score -= factor * 0.2;
                }
            }
            if (p.col == 0 || p.col == 8) {
                score -= factor * 0.05;
            }
            if (p.rank == Rank.KING) {
                if (!inPalace(p)) {
                    score -= factor * 0.5;
                }
                if (board.isInCheck(p.isRed)) {
                    score -= factor * 0.7;
                }
            }
        }
        score += noise(difficulty.getEvalNoise());
        return score;
    }

    private boolean inPalace(Pieces k) {
        if (k.isRed) {
            return k.col >= 3 && k.col <= 5 && k.row >= 0 && k.row <= 2;
        }
        return k.col >= 3 && k.col <= 5 && k.row >= 7 && k.row <= 9;
    }

    private double pieceValue(Pieces p) {
        switch (p.rank) {
            case KING:
                return 1000;
            case ROOK:
                return 9;
            case CANNON:
                return 4.5;
            case KNIGHT:
                return 4;
            case ELEPHENT:
                return 2.5;
            case ADVISOR:
                return 2;
            case PAWN:
                return ((p.isRed && p.row > 4) || (!p.isRed && p.row < 5)) ? 2.0 : 1.5;
            default:
                return 0.0;
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
        if (attackerValue <= targetValue + 0.1) {
            return false;
        }
        CChessBoard next = new CChessBoard(board);
        next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
        List<CChessBoard.Move> replies = next.getAllValidMoves();
        for (CChessBoard.Move rep : replies) {
            if (rep.toCol == mv.toCol && rep.toRow == mv.toRow) {
                Pieces responder = next.pieceAt(rep.fromCol, rep.fromRow);
                if (responder != null && pieceValue(responder) >= targetValue - 0.1) {
                    return true;
                }
            }
        }
        return false;
    }

    private double centralCannonBonus(CChessBoard board, CChessBoard.Move mv, boolean aiIsRed) {
        if (difficulty == AIDifficulty.EASY) {
            return 0.0;
        }
        Pieces moving = board.pieceAt(mv.fromCol, mv.fromRow);
        if (moving == null || moving.rank != Rank.KNIGHT) {
            return 0.0;
        }
        int dir = aiIsRed ? 1 : -1;
        int pawnRow = findCentralPawnRow(board, aiIsRed);
        if (pawnRow == -1) {
            return 0.0;
        }
        int cannonRow = pawnRow - dir;
        Pieces cannon = board.pieceAt(4, cannonRow);
        if (cannon == null || cannon.rank != Rank.CANNON || cannon.isRed != aiIsRed) {
            return 0.0;
        }
        boolean before = knightAttacksSquare(mv.fromCol, mv.fromRow, 4, pawnRow);
        boolean after = knightAttacksSquare(mv.toCol, mv.toRow, 4, pawnRow);
        if (!before && after) {
            return 0.6;
        }
        return 0.0;
    }

    private int findCentralPawnRow(CChessBoard board, boolean isRed) {
        for (Pieces p : board.getPieces()) {
            if (p.rank == Rank.PAWN && p.isRed == isRed && p.col == 4) {
                if ((isRed && p.row >= 3) || (!isRed && p.row <= 6)) {
                    return p.row;
                }
            }
        }
        return -1;
    }

    private boolean knightAttacksSquare(int kCol, int kRow, int targetCol, int targetRow) {
        int[][] deltas = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
        for (int[] d : deltas) {
            if (kCol + d[0] == targetCol && kRow + d[1] == targetRow) {
                return true;
            }
        }
        return false;
    }

    private void reorder(List<CChessBoard.Move> moves, double[] scores) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            idx.add(i);
        }
        idx.sort((a, b) -> Double.compare(scores[b], scores[a]));
        List<CChessBoard.Move> reordered = new ArrayList<>();
        for (int i : idx) {
            reordered.add(moves.get(i));
        }
        moves.clear();
        moves.addAll(reordered);
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
