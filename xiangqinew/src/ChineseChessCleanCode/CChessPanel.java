package ChineseChessCleanCode;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class CChessPanel extends JPanel implements MouseListener, MouseMotionListener {
    private CChessBoard brd;
    private Point fromColRow;
    private Point movingPieceXY;
    private Image movingPieceImage;

    int orgX = 83, orgY = 83;
    static int side = 67;

    CChessPanel(CChessBoard brd) {
        this.brd = brd;
        setPreferredSize(new Dimension(700, 900));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    CChessPanel() {
        setPreferredSize(new Dimension(700, 900));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private Point xyToColRow(Point xy) {
        return new Point((xy.x - orgX + side / 2) / side,
                         (xy.y - orgY + side / 2) / side);
    }

    @Override
    public void mousePressed(MouseEvent me) {
        fromColRow = xyToColRow(me.getPoint());
        Pieces movingPiece = brd.pieceAt(fromColRow.x, fromColRow.y);
        if (movingPiece != null) {
            movingPieceImage = xiangqi.keyNameValueImage.get(movingPiece.imgName);
            int drawX = orgX + side * movingPiece.col - side / 2;
            int drawY = orgY + side * movingPiece.row - side / 2;
            movingPieceXY = new Point(drawX, drawY);
        } else {
            movingPieceImage = null;
            movingPieceXY = null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (fromColRow != null) {
            Point toColRow = xyToColRow(me.getPoint());
            if (brd.validMove(fromColRow.x, fromColRow.y, toColRow.x, toColRow.y)) {
                brd.movePiece(fromColRow.x, fromColRow.y, toColRow.x, toColRow.y);
                System.out.println(brd);
            }
        }
        fromColRow = null;
        movingPieceXY = null;
        movingPieceImage = null;
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
            g.drawImage(img,
                    orgX + side * p.col - side / 2,
                    orgY + side * p.row - side / 2,
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
}
