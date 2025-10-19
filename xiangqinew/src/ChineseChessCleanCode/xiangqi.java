package ChineseChessCleanCode;
public class xiangqi {
	public static void main(String[] args) {
		CChessBoard board = new CChessBoard();
		System.out.println(board);
		board.movePiece(0, 0, 0, 2);
		System.out.println(board);
	}
}

