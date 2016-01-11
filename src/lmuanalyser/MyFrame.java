package lmuanalyser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lucci.lmu.Model;
import org.lucci.lmu.input.LmuParser;
import org.lucci.lmu.input.ModelException;
import org.lucci.lmu.input.ParseError;
import org.lucci.lmu.output.GraphVizBasedViewFactory;
import org.lucci.lmu.output.WriterException;

public class MyFrame extends JFrame{
	private static final String title = "Analyse LMU";
	private static Map<String, String> fileTypes = new HashMap<String, String>();
	private LmuParser parser;
	private Model model;
	private JPanel mainPane;
	private JButton outputButton;
	private Image image;
	
	public MyFrame(String src) {
		super(title);
		this.setVisible(true);
		this.setBounds(200, 100, 800, 600);
		initComponents();
		parser(src);
	}
	
	private void initComponents() {
		mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		outputButton = new JButton();/* {
			 public void paintComponent(Graphics g) {
			      super.paintComponent(g);
			      g.drawImage(image, 0, 0, this);
			  }
		};*/
		outputButton.setBackground(Color.WHITE);
		mainPane.add(outputButton, BorderLayout.CENTER);
		this.add(mainPane);
	}
	private void parser(String src) {
		if(src.endsWith(".jar")) { // This selected file is a jar file
			parser = LmuParser.getParser();
			try {
				model = parser.createModel("load " + src);
				GraphVizBasedViewFactory imgFactory = new GraphVizBasedViewFactory("png");
				byte[] bytes = imgFactory.writeModel(model);
				this.image = new ImageIcon(bytes).getImage();
				outputButton.setIcon(new ImageIcon(this.image));
				outputButton.updateUI();
			} catch (ParseError | ModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
