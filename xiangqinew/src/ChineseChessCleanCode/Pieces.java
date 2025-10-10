package ChineseChessCleanCode;

enum Rank {
	ROOK,
	KNIGHT,
	ELEPHENT,
	ADVISOR,
	KING,
	CANNON,
	PAWN
}

public class Pieces {
	int col;
	int row;
	boolean isRed;
	Rank rank;
	public Pieces(int col, int row, boolean isRed, Rank rank) {
		this.col = col;
		this.row = row;
		this.isRed = isRed;
		this.rank = rank;
	}
}
