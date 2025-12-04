package ChineseChessCleanCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

enum AIDifficulty {
        EASY,
        MEDIUM,
        HARD
}

public class SimpleAI {
        private final Random random = new Random();
        private final AIDifficulty difficulty;

        public SimpleAI(AIDifficulty difficulty) {
                this.difficulty = difficulty;
        }

        public CChessBoard.Move chooseMove(CChessBoard board, boolean isRed) {
                if (board.isRedTurn() != isRed) {
                        return null;
                }
                List<CChessBoard.Move> moves = board.getAllValidMoves();
                if (moves.isEmpty()) {
                        return null;
                }
                switch (difficulty) {
                        case EASY:
                                return moves.get(random.nextInt(moves.size()));
                        case MEDIUM:
                                return bestCaptureOrRandom(board, moves, isRed);
                        case HARD:
                        default:
                                return minimaxDepthTwo(board, moves, isRed);
                }
        }

        private CChessBoard.Move bestCaptureOrRandom(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed) {
                double bestGain = -Double.MAX_VALUE;
                List<CChessBoard.Move> bestMoves = new ArrayList<>();
                for (CChessBoard.Move mv : moves) {
                        Pieces target = board.pieceAt(mv.toCol, mv.toRow);
                        if (target == null) {
                                continue;
                        }
                        double value = targetValue(target);
                        if (value > bestGain) {
                                bestGain = value;
                                bestMoves.clear();
                                bestMoves.add(mv);
                        } else if (value == bestGain) {
                                bestMoves.add(mv);
                        }
                }
                if (!bestMoves.isEmpty()) {
                        return bestMoves.get(random.nextInt(bestMoves.size()));
                }
                return moves.get(random.nextInt(moves.size()));
        }

        private CChessBoard.Move minimaxDepthTwo(CChessBoard board, List<CChessBoard.Move> moves, boolean isRed) {
                double bestScore = -Double.MAX_VALUE;
                CChessBoard.Move bestMove = moves.get(0);
                for (CChessBoard.Move mv : moves) {
                        CChessBoard next = new CChessBoard(board);
                        next.movePiece(mv.fromCol, mv.fromRow, mv.toCol, mv.toRow);
                        double replyScore = evaluateForOpponent(next, isRed);
                        if (replyScore > bestScore) {
                                bestScore = replyScore;
                                bestMove = mv;
                        }
                }
                return bestMove;
        }

        private double evaluateForOpponent(CChessBoard boardAfterMove, boolean aiIsRed) {
                List<CChessBoard.Move> replies = boardAfterMove.getAllValidMoves();
                if (replies.isEmpty()) {
                        return boardAfterMove.materialScore(aiIsRed);
                }
                double worstCase = Double.MAX_VALUE;
                for (CChessBoard.Move reply : replies) {
                        CChessBoard afterReply = new CChessBoard(boardAfterMove);
                        afterReply.movePiece(reply.fromCol, reply.fromRow, reply.toCol, reply.toRow);
                        double score = afterReply.materialScore(aiIsRed);
                        if (score < worstCase) {
                                worstCase = score;
                        }
                }
                return worstCase;
        }

        private double targetValue(Pieces p) {
                switch (p.rank) {
                        case KING: return 1000;
                        case ROOK: return 9;
                        case CANNON: return 4.5;
                        case KNIGHT: return 4;
                        case ELEPHENT: return 2.5;
                        case ADVISOR: return 2;
                        case PAWN: return 1.5;
                        default: return 0;
                }
        }
}
