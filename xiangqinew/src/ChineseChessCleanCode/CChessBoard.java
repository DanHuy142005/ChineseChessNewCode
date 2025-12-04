package ChineseChessCleanCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CChessBoard {
        static class Move {
                int fromCol;
                int fromRow;
                int toCol;
                int toRow;

                Move(int fromCol, int fromRow, int toCol, int toRow) {
                        this.fromCol = fromCol;
                        this.fromRow = fromRow;
                        this.toCol = toCol;
                        this.toRow = toRow;
                }

                @Override
                public String toString() {
                        return "(" + fromCol + "," + fromRow + ") -> (" + toCol + "," + toRow + ")";
                }
        }

        private boolean isRedTurn = true;
        final static int ranks = 10;
        final static int files = 9;
        private Set<Pieces> pieces = new HashSet<>();
        private boolean gameOver = false;
        private boolean lastMoverRed = false;
        private boolean winByNoMoves = false;

        Set<Pieces> getPieces() {
                return pieces;
        }

        boolean isRedTurn() {
                return isRedTurn;
        }

        boolean isGameOver() {
                return gameOver;
        }

        CChessBoard() {
            for (int i = 0; i < 2; i++) {
              pieces.add(new Pieces(0 + i * 8, 0, true, Rank.ROOK, "rj"));
              pieces.add(new Pieces(1 + i * 6, 0, true, Rank.KNIGHT, "rm"));
	      pieces.add(new Pieces(2 + i * 4, 0, true, Rank.ELEPHENT, "rx"));
	      pieces.add(new Pieces(3 + i * 2, 0, true, Rank.ADVISOR, "rs"));
	      pieces.add(new Pieces(1 + i * 6, 2, true, Rank.CANNON, "rp"));
	      pieces.add(new Pieces(0 + i * 8, 9, false, Rank.ROOK, "bj"));
	      pieces.add(new Pieces(1 + i * 6, 9, false, Rank.KNIGHT, "bm"));
	      pieces.add(new Pieces(2 + i * 4, 9, false, Rank.ELEPHENT, "bx"));
	      pieces.add(new Pieces(3 + i * 2, 9, false, Rank.ADVISOR, "bs"));
	      pieces.add(new Pieces(1 + i * 6, 7, false, Rank.CANNON, "bp"));
	    }
	    pieces.add(new Pieces(4, 0, true, Rank.KING, "rb"));
	    pieces.add(new Pieces(4, 9, false, Rank.KING, "bb"));
            for (int i = 0; i < 5; i++) {
              pieces.add(new Pieces(i * 2, 3, true, Rank.PAWN, "rz"));
              pieces.add(new Pieces(i * 2, 6, false, Rank.PAWN, "bz"));
            }
          }

        CChessBoard(CChessBoard other) {
                this.isRedTurn = other.isRedTurn;
                for (Pieces p : other.pieces) {
                        pieces.add(new Pieces(p.col, p.row, p.isRed, p.rank, p.imgName));
                }
        }

                  Pieces pieceAt(int files, int ranks) {
                    for (Pieces piece : pieces) {
                      if (piece.col == files && piece.row == ranks) {
                        return piece;
                      }
                    }
                    return null;
                  }
                  void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
                          movePieceInternal(fromCol, fromRow, toCol, toRow, true);
                  }

                  private void movePieceInternal(int fromCol, int fromRow, int toCol, int toRow, boolean checkGameOver) {
                          Pieces movingP = pieceAt(fromCol, fromRow);
                          Pieces targetP = pieceAt(toCol, toRow);
                          lastMoverRed = movingP != null && movingP.isRed;
                          pieces.remove(movingP);
                          pieces.remove(targetP);
                          pieces.add(new Pieces(toCol, toRow, movingP.isRed, movingP.rank, movingP.imgName));
                          isRedTurn = !isRedTurn;
                          if (checkGameOver) {
                                  updateGameOverState();
                          }
                  }

                  private void updateGameOverState() {
                          boolean redKingAlive = hasKing(true);
                          boolean blackKingAlive = hasKing(false);
                          if (!redKingAlive || !blackKingAlive) {
                                  gameOver = true;
                                  winByNoMoves = false;
                                  return;
                          }
                          if (getAllValidMoves().isEmpty()) {
                                  gameOver = true;
                                  winByNoMoves = true;
                          }
                  }
		  private boolean outBoard(int col, int row) {
			  return col < 0 || col > 8 || row < 0 || row > 9;
		  }
		  private boolean isStraight(int fromCol,int fromRow, int toCol, int toRow) {
			  return fromCol == toCol || fromRow == toRow; 
		  }
		  private boolean isDiagonal(int fromCol,int fromRow, int toCol, int toRow) {
			  return Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
		  }
		  private int steps(int fromCol, int fromRow, int toCol, int toRow) {
			    if (fromCol == toCol) {               
			        return Math.abs(fromRow - toRow);
			    } else if (fromRow == toRow) {         
			        return Math.abs(fromCol - toCol);
			    } else if (isDiagonal(fromCol, fromRow, toCol, toRow)) { 
			        return Math.abs(fromRow - toRow);
			    }
			    return 0;
			}
		  private boolean KingAndAdvisoroutPlace(int col, int row, boolean isRed) {
			  if (isRed) {
			    return col < 3 || col > 5 || row < 0 || row > 2;
			  } else {
			    return col < 3 || col > 5 || row < 7 || row > 9;
			  }
		  }
		  private boolean Side(int row, boolean isRed) {
			  return isRed ? row <= 4 : row >= 5;
		  }
		  private int numPiecesBetween(int fromCol, int fromRow, int toCol, int toRow) {
			  if(!isStraight(fromCol, fromRow, toCol, toRow) || steps(fromCol, fromRow, toCol, toRow) < 2) {
				  return 0;
			  }
			  int bd = 0, head, des;
			  if(fromCol == toCol) {
				  head = Math.min(fromRow, toRow);
				  des = Math.max(fromRow, toRow);
				  for (int row = head + 1; row < des; row++) {
					  bd += (pieceAt(fromCol, row) == null) ? 0 : 1;
				  }
			  }
			  else {
				  head = Math.min(fromCol, toCol);
				  des = Math.max(fromCol, toCol);
				  for (int col = head + 1; col < des; col++) {
					  bd += (pieceAt(col, fromRow) == null) ? 0 : 1;
				  }
			  }
			  return bd;
		  }
                  private boolean selfKilling(int fromCol, int fromRow, int toCol, int toRow, boolean isRed) {
                          Pieces target = pieceAt(toCol, toRow);
                          return target != null && target.isRed == isRed;
                  }

                  private boolean kingsFacing() {
                          Pieces redKing = null;
                          Pieces blackKing = null;
                          for (Pieces p : pieces) {
                                  if (p.rank == Rank.KING) {
                                          if (p.isRed) {
                                                  redKing = p;
                                          } else {
                                                  blackKing = p;
                                          }
                                  }
                          }
                          if (redKing == null || blackKing == null) {
                                  return false;
                          }
                          if (redKing.col != blackKing.col) {
                                  return false;
                          }
                          int start = Math.min(redKing.row, blackKing.row) + 1;
                          int end = Math.max(redKing.row, blackKing.row);
                          for (int r = start; r < end; r++) {
                                  if (pieceAt(redKing.col, r) != null) {
                                          return false;
                                  }
                          }
                          return true;
                  }

                  boolean hasKing(boolean isRed) {
                          for (Pieces p : pieces) {
                                  if (p.rank == Rank.KING && p.isRed == isRed) {
                                          return true;
                                  }
                          }
                          return false;
                  }
                  private boolean AdvisorMove(int fromCol, int fromRow, int toCol, int toRow, boolean isRed) {
                          if (KingAndAdvisoroutPlace(toCol, toRow, isRed)) {
                                    return false;
                          }
			  return isDiagonal(fromCol, fromRow, toCol, toRow) 
					  && steps(fromCol, fromRow, toCol, toRow) == 1;
		  }
		  private boolean KingMove(int fromCol, int fromRow, int toCol, int toRow, boolean isRed) {
			  if (KingAndAdvisoroutPlace(toCol, toRow, isRed)) { 
				    return false; 
			  }
			  return isStraight(fromCol, fromRow, toCol, toRow) 
					  && steps(fromCol, fromRow, toCol, toRow) == 1;
		  }
		  private boolean KnightMove(int fromCol, int fromRow, int toCol,int toRow) {
			  if (Math.abs(fromCol - toCol) == 1 && Math.abs(fromRow - toRow) == 2) {
				  return pieceAt(fromCol, (fromRow + toRow)/2) == null;
			  }
			  else if(Math.abs(fromCol - toCol) == 2 && Math.abs(fromRow - toRow) == 1) {
				  return pieceAt((fromCol + toCol) / 2, fromRow) == null;
			  }
			  return false;
		  }
		  private boolean selfSide(int row, boolean isRed) {
			  return isRed ? row <= 4 : row >= 5;
		  }
		  private boolean ElephentMove(int fromCol, int fromRow, int toCol, int toRow, boolean isRed) {
			  return selfSide(toRow, isRed) && pieceAt((fromCol + toCol)/2, (fromRow + toRow)/2) == null 
					  && isDiagonal(fromCol, fromRow, toCol, toRow)	&& steps(fromCol, fromRow, toCol, toRow) == 2;
		  }
		  private boolean RookMove(int fromCol, int fromRow, int toCol, int toRow) {
			  return isStraight(fromCol, fromRow, toCol, toRow) 
					  && numPiecesBetween(fromCol, fromRow, toCol, toRow) == 0;
		  }
		  private boolean CannonMove(int fromCol, int fromRow, int toCol, int toRow) {
			  if (pieceAt(toCol, toRow) == null) {
				    return RookMove(fromCol, fromRow, toCol, toRow);
				  }
				  return numPiecesBetween(fromCol, fromRow, toCol, toRow) == 1;
		  }
                  private boolean PawnMove(int fromCol, int fromRow, int toCol, int toRow, boolean isRed) {
                          if(steps(fromCol, fromRow, toCol, toRow) != 1) {
                                  return false;
                          }
                          if (isRed) {
			        if (selfSide(fromRow, true)) {
			            return (toCol == fromCol && toRow == fromRow + 1);
			        } else {
			            if (toCol == fromCol && toRow == fromRow + 1) return true;             
			            if (toRow == fromRow && Math.abs(toCol - fromCol) == 1) return true;   
			            return false;
			        }
			    } else {
			        if (selfSide(fromRow, false)) {
			            return (toCol == fromCol && toRow == fromRow - 1);
			        } else {
			            if (toCol == fromCol && toRow == fromRow - 1) return true;             
			            if (toRow == fromRow && Math.abs(toCol - fromCol) == 1) return true;  
			            return false;
		  }
			    }
                  }
                  boolean validMove(int fromC, int fromR, int toC, int toR) {
                          if(fromC == toC && fromR == toR || outBoard(toC, toR)) {
                                  return false;
                          }
                          Pieces p = pieceAt(fromC, fromR);
			  if (p == null || p.isRed != isRedTurn || selfKilling(fromC, fromR, toC, toR, p.isRed)) {
			    return false;
			  }
			  boolean ok = false;
                          switch (p.rank) {
                            case ADVISOR:
                              ok = AdvisorMove(fromC, fromR, toC, toR, p.isRed);
			      break;
			    case KING: 
			      ok = KingMove(fromC, fromR, toC, toR, p.isRed);
			      break;
			    case ELEPHENT: 
			      ok = ElephentMove(fromC, fromR, toC, toR, p.isRed);
			      break;
			    case KNIGHT: 
			      ok = KnightMove(fromC, fromR, toC, toR);
			      break;
			    case ROOK: 
			      ok = RookMove(fromC, fromR, toC, toR);
			      break;
			    case CANNON: 
			      ok = CannonMove(fromC, fromR, toC, toR);
			      break;
                            case PAWN:
                              ok = PawnMove(fromC, fromR, toC, toR, p.isRed);
                              break;
                          }
                          if (!ok) {
                                  return false;
                          }
                          return !kingFacesAfter(fromC, fromR, toC, toR);
                  }

        private boolean kingFacesAfter(int fromC, int fromR, int toC, int toR) {
                CChessBoard hypothetical = new CChessBoard(this);
                hypothetical.movePieceInternal(fromC, fromR, toC, toR, false);
                return hypothetical.kingsFacing();
        }

        Boolean winnerIsRed() {
                if (!hasKing(true)) {
                        return false;
                }
                if (!hasKing(false)) {
                        return true;
                }
                if (gameOver && winByNoMoves) {
                        return lastMoverRed;
                }
                return null;
        }

        List<Move> getAllValidMoves() {
                if (gameOver) {
                        return new ArrayList<>();
                }
                List<Move> moves = new ArrayList<>();
                for (Pieces p : pieces) {
                        if (p.isRed != isRedTurn) continue;
                        for (int c = 0; c < files; c++) {
                                for (int r = 0; r < ranks; r++) {
                                        if (validMove(p.col, p.row, c, r)) {
                                                moves.add(new Move(p.col, p.row, c, r));
                                        }
                                }
                        }
                }
                return moves;
        }

        double materialScore(boolean forRed) {
                double score = 0;
                for (Pieces p : pieces) {
                        double value = 0;
                        switch (p.rank) {
                                case KING: value = 1000; break;
                                case ROOK: value = 9; break;
                                case CANNON: value = 4.5; break;
                                case KNIGHT: value = 4; break;
                                case ELEPHENT: value = 2.5; break;
                                case ADVISOR: value = 2; break;
                                case PAWN: value = 1.5; break;
                        }
                        score += p.isRed == forRed ? value : -value;
                }
                return score;
        }
        public String toString() {
                String board = "";
		board += " ";
		for (int i = 0; i < 9; i++) {
			board += " " + i;
		}
		board += "\n";
		for (int rank = 0; rank < ranks; rank++) {
			board += rank + "";
			for (int file = 0; file < files; file++) {
				Pieces p = pieceAt(file, rank);
				if(p == null) {
					board += " .";
				} else {
					switch (p.rank) {
			            case ROOK: board += p.isRed ? " R" : " r"; break;
			            case KNIGHT: board += p.isRed ? " N" : " n"; break;
			            case ELEPHENT: board += p.isRed ? " E" : " e"; break;
			            case ADVISOR: board += p.isRed ? " A" : " a"; break;
			            case KING: board += p.isRed ? " K" : " k"; break;
			            case CANNON: board += p.isRed ? " C" : " c"; break;
			            case PAWN: board += p.isRed ? " P" : " p"; break;
		          }
				}
			}
			board += "\n";
		}
		return board;
	}
}
