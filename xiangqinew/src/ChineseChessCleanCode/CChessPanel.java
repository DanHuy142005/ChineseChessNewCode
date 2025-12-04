package ChineseChessCleanCode;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class CChessPanel extends JPanel implements MouseListener, MouseMotionListener {
    private CChessBoard brd;
    private final boolean aiEnabled;
    private final boolean aiIsRed;
    private final boolean playerIsRed;
    private final PikafishAI ai;
    private Point fromColRow;
    private Point movingPieceXY;
    private Image movingPieceImage;

    int orgX = 83, orgY = 83;
    static int side = 67;

    CChessPanel(CChessBoard brd, boolean aiEnabled, boolean aiIsRed, AIDifficulty difficulty, boolean playerIsRed) {
        this.brd = brd;
        this.aiEnabled = aiEnabled;
        this.aiIsRed = aiIsRed;
        this.playerIsRed = playerIsRed;
        this.ai = aiEnabled ? new PikafishAI(difficulty) : null;
        setPreferredSize(new Dimension(700, 900));
        addMouseListener(this);
        addMouseMotionListener(this);
        SwingUtilities.invokeLater(this::makeAIMoveIfNeeded);
    }

    private Point xyToColRow(Point xy) {
        int viewCol = (xy.x - orgX + side / 2) / side;
        int viewRow = (xy.y - orgY + side / 2) / side;
        return viewToBoard(viewCol, viewRow);
    }

    private Point boardToView(int col, int row) {
        if (playerIsRed) {
            return new Point(CChessBoard.files - 1 - col, CChessBoard.ranks - 1 - row);
        }
        return new Point(col, row);
    }

    private Point viewToBoard(int viewCol, int viewRow) {
        if (playerIsRed) {
            return new Point(CChessBoard.files - 1 - viewCol, CChessBoard.ranks - 1 - viewRow);
        }
        return new Point(viewCol, viewRow);
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (brd.isGameOver()) {
            clearMovingState();
            return;
        }
        fromColRow = xyToColRow(me.getPoint());
        if (fromColRow.x < 0 || fromColRow.x >= CChessBoard.files || fromColRow.y < 0 || fromColRow.y >= CChessBoard.ranks) {
            clearMovingState();
            return;
        }
        if (brd.isRedTurn() != playerIsRed) {
            clearMovingState();
            return;
        }
        Pieces movingPiece = brd.pieceAt(fromColRow.x, fromColRow.y);
        if (movingPiece == null || movingPiece.isRed != playerIsRed) {
            clearMovingState();
            return;
        }
        if (movingPiece != null) {
            movingPieceImage = xiangqi.keyNameValueImage.get(movingPiece.imgName);
            Point view = boardToView(movingPiece.col, movingPiece.row);
            int drawX = orgX + side * view.x - side / 2;
            int drawY = orgY + side * view.y - side / 2;
            movingPieceXY = new Point(drawX, drawY);
        } else {
            clearMovingState();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (fromColRow != null && brd.isRedTurn() == playerIsRed && !brd.isGameOver()) {
            boolean moverIsRed = brd.isRedTurn();
            Point toColRow = xyToColRow(me.getPoint());
            if (brd.validMove(fromColRow.x, fromColRow.y, toColRow.x, toColRow.y)) {
                brd.movePiece(fromColRow.x, fromColRow.y, toColRow.x, toColRow.y);
                finishMove(moverIsRed);
            }
        }
        clearMovingState();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (movingPieceImage != null) {
            Point mouseTip = me.getPoint();
            movingPieceXY = new Point(mouseTip.x - side / 2, mouseTip.y - side / 2);
            repaint();
        }
    }

    @Override public void mouseClicked(MouseEvent me) {}
    @Override public void mouseEntered(MouseEvent me) {}
    @Override public void mouseExited(MouseEvent me) {}
    @Override public void mouseMoved(MouseEvent me) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGrid(g);

        for (int i = 0; i < 2; i++) {
            drawStarAt(g, 1 + i * 6, 2);
            drawStarAt(g, 1 + i * 6, 7);
            drawHalfStarAt(g, 0, 3, false);
            drawHalfStarAt(g, 0, 6, false);
            drawHalfStarAt(g, 8, 3, true);
            drawHalfStarAt(g, 8, 6, true);
        }
        for (int i = 0; i < 3; i++) {
            drawStarAt(g, 2 + i * 2, 3);
            drawStarAt(g, 2 + i * 2, 6);
        }

        drawPieces(g);

        if (movingPieceImage != null && movingPieceXY != null) {
            g.drawImage(movingPieceImage, movingPieceXY.x, movingPieceXY.y, this);
        }
    }

    private void drawPieces(Graphics g) {
        for (Pieces p : brd.getPieces()) {
            if (fromColRow != null && fromColRow.x == p.col && fromColRow.y == p.row) {
                continue;
            }
            Image img = xiangqi.keyNameValueImage.get(p.imgName);
            Point view = boardToView(p.col, p.row);
            g.drawImage(img,
                    orgX + side * view.x - side / 2,
                    orgY + side * view.y - side / 2,
                    this);
        }
    }

    private void drawGrid(Graphics g) {
        for (int i = 0; i < CChessBoard.files; i++) {
            g.drawLine(orgX + i * side, orgY,
                       orgX + i * side, orgY + 4 * side);
            g.drawLine(orgX + i * side, orgY + 5 * side,
                       orgX + i * side, orgY + 9 * side);
        }
        for (int i = 0; i < CChessBoard.ranks; i++) {
            g.drawLine(orgX,            orgY + i * side,
                       orgX + 8 * side, orgY + i * side);
        }
        for (int i = 0; i < 2; i++) {
            g.drawLine(orgX + 3 * side, orgY + i * 7 * side,
                       orgX + 5 * side, orgY + (2 + i * 7) * side);
            g.drawLine(orgX + 5 * side, orgY + i * 7 * side,
                       orgX + 3 * side, orgY + (2 + i * 7) * side);
            g.drawLine(orgX + 8 * i * side, orgY + 4 * side,
                       orgX + 8 * i * side, orgY + 5 * side);
        }
    }

    private void drawHalfStarAt(Graphics g, int col, int row, boolean left) {
        int gap = side / 9;
        int bar = side / 4;
        int hSign = left ? -1 : 1;
        int tipX = orgX + col * side + hSign * gap;
        for (int i = 0; i < 2; i++) {
            int vSign = -1 + i * 2;
            int tipY = orgY + row * side + vSign * gap;
            g.drawLine(tipX, tipY, tipX + hSign * bar, tipY);
            g.drawLine(tipX, tipY, tipX, tipY + vSign * bar);
        }
    }

    private void drawStarAt(Graphics g, int col, int row) {
        drawHalfStarAt(g, col, row, true);
        drawHalfStarAt(g, col, row, false);
    }

    private void makeAIMoveIfNeeded() {
        if (!aiEnabled || ai == null) {
            return;
        }
        if (brd.isGameOver() || brd.isRedTurn() != aiIsRed) {
            return;
        }
        CChessBoard.Move move = ai.chooseMove(brd, aiIsRed);
        if (move == null) {
            return;
        }
        boolean moverIsRed = brd.isRedTurn();
        brd.movePiece(move.fromCol, move.fromRow, move.toCol, move.toRow);
        finishMove(moverIsRed);
        repaint();
    }

    private void finishMove(boolean moverIsRed) {
        System.out.println(brd);
        repaint();
        if (brd.isGameOver()) {
            announceResult();
        } else if (aiEnabled && moverIsRed == playerIsRed) {
            makeAIMoveIfNeeded();
        }
    }

    private void announceResult() {
        Boolean winnerRed = brd.winnerIsRed();
        String message;
        if (winnerRed == null) {
            message = "Hết nước đi - ván hòa";
        } else if (brd.isCheckmate()) {
            message = (winnerRed ? "Đỏ" : "Đen") + " chiếu hết!";
        } else {
            message = (winnerRed ? "Đỏ" : "Đen") + " thắng!";
        }
        JOptionPane.showMessageDialog(this, message, "Kết thúc ván", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearMovingState() {
        fromColRow = null;
        movingPieceXY = null;
        movingPieceImage = null;
    }
}
