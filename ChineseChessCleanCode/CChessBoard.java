package ChineseChessCleanCode;

import java.util.HashSet;
import java.util.Set;

public class CChessBoard { 
	final static int ranks = 10;
    final static int files = 9;
	private Set<Pieces> pieces = new HashSet<>();
    CChessBoard() {
		for (int i = 0; i < 2; i++) {
		      pieces.add(new Pieces(0 + i * 8, 0, true, Rank.ROOK));
		      pieces.add(new Pieces(1 + i * 6, 0, true, Rank.KNIGHT));
		      pieces.add(new Pieces(2 + i * 4, 0, true, Rank.ELEPHENT));
		      pieces.add(new Pieces(3 + i * 2, 0, true, Rank.ADVISOR));
		      pieces.add(new Pieces(1 + i * 6, 2, true, Rank.CANNON));
		      pieces.add(new Pieces(0 + i * 8, 9, false, Rank.ROOK));
		      pieces.add(new Pieces(1 + i * 6, 9, false, Rank.KNIGHT));
		      pieces.add(new Pieces(2 + i * 4, 9, false, Rank.ELEPHENT));
		      pieces.add(new Pieces(3 + i * 2, 9, false, Rank.ADVISOR));
		      pieces.add(new Pieces(1 + i * 6, 7, false, Rank.CANNON));
		    }
		    pieces.add(new Pieces(4, 0, true, Rank.KING));
		    pieces.add(new Pieces(4, 9, false, Rank.KING));
		    for (int i = 0; i < 5; i++) {
		      pieces.add(new Pieces(i * 2, 3, true, Rank.PAWN));
		      pieces.add(new Pieces(i * 2, 6, false, Rank.PAWN));
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