package ChineseChessCleanCode;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
public class xiangqi {
  static Map<String, Image> keyNameValueImage = new HashMap<String, Image>();
    xiangqi() {
    CChessBoard brd = new CChessBoard();
    System.out.println(brd);
    boolean playerIsRed = askPlayerColor();
    AIDifficulty difficulty = askDifficulty();
    JFrame f = new JFrame("Chinese Chess");
    f.setSize(800, 800);
    f.setLocation(50, 50);
    f.add(new CChessPanel(brd, true, !playerIsRed, difficulty, playerIsRed));
    f.setVisible(true);
  }
  public static void main(String[] args) throws IOException {
          Set<String> imgNames = new HashSet<>(Arrays.asList(
                  "bj", "bm", "bx", "bs", "bb", "bp", "bz",
	          "rj", "rm", "rx", "rs", "rb", "rp", "rz"));
	    for (String imgName : imgNames) {
              File imgFile = new File("./res/" + imgName + ".png");
              keyNameValueImage.put(imgName, ImageIO.read(imgFile).getScaledInstance(CChessPanel.side, CChessPanel.side, Image.SCALE_SMOOTH));
  }
            new xiangqi();
}

  private AIDifficulty askDifficulty() {
          Object[] options = {
                  "Pikafish Elo 1500 (Dễ)",
                  "Pikafish Elo 1900 (Trung bình)",
                  "Pikafish Elo 2300 (Khó)"};
          int choice = JOptionPane.showOptionDialog(null,
                          "Chọn Elo cho AI Pikafish",
                          "Độ khó",
                          JOptionPane.DEFAULT_OPTION,
                          JOptionPane.QUESTION_MESSAGE,
                          null,
                          options,
                          options[1]);
          switch (choice) {
                  case 0: return AIDifficulty.PIKAFISH_1500;
                  case 2: return AIDifficulty.PIKAFISH_2300;
                  default: return AIDifficulty.PIKAFISH_1900;
          }
  }

  private boolean askPlayerColor() {
          Object[] options = {"Đỏ (đi trước)", "Đen"};
          int choice = JOptionPane.showOptionDialog(null,
                          "Bạn muốn chơi quân nào?",
                          "Chọn màu",
                          JOptionPane.DEFAULT_OPTION,
                          JOptionPane.QUESTION_MESSAGE,
                          null,
                          options,
                          options[0]);
          return choice != 1;
  }
}
