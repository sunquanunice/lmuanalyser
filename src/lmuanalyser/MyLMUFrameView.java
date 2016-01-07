package lmuanalyser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MyLMUFrameView extends JFrame {
	private static String title = "My LMU";
	private Object src;
	public MyLMUFrameView(Object selected) {	
		super(title);
		src = selected;
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 100, 800, 600);
		setVisible(true);
		addComponents();
	}
	public MyLMUFrameView(String s) {
		
	}
	private void addComponents() {
		JPanel upperPanel = new JPanel();
		JTextArea t1 = new JTextArea("Hello");
		t1.setColumns(WIDTH);
		t1.setRows((int)(HEIGHT * 0.5));
		t1.setText("Hello" + src.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		upperPanel.add(t1);
		add(upperPanel, BorderLayout.NORTH);
		revalidate();
		
	}
}
