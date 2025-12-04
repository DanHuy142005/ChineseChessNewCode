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
	String imgName;
	public Pieces(int col, int row, boolean isRed, Rank rank, String imgName) {
		this.col = col;
		this.row = row;
		this.isRed = isRed;
		this.rank = rank;
		this.imgName = imgName;
	}
}
