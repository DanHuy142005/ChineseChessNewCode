package ChineseChessCleanCode;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
public class xiangqi {
  static Map<String, Image> keyNameValueImage = new HashMap<String, Image>();
  xiangqi() {
    CChessBoard brd = new CChessBoard();
    System.out.println(brd);
    JFrame f = new JFrame("Chinese Chess");
    f.setSize(800, 800);
    f.setLocation(50, 50);
    f.add(new CChessPanel(brd));
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
}
