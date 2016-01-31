package lmuanalyser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lucci.lmu.Model;
import org.lucci.lmu.input.LmuParser;
import org.lucci.lmu.input.ModelException;
import org.lucci.lmu.input.ParseError;
import org.lucci.lmu.output.GraphVizBasedViewFactory;
import org.lucci.lmu.output.WriterException;

public class ResultGenerator {

	public ResultGenerator() {
	}
	
	public void parser(String src) {
		String output = saveFile();
		if(output!= null) {
			String input = getInput(src);
			if(input != null) {
				 LmuParser parser = LmuParser.getParser();
				 try {
					Model model = parser.createModel(input);
					String extension = output.substring(output.lastIndexOf(".") + 1);
					GraphVizBasedViewFactory imgFactory = new GraphVizBasedViewFactory(extension);
					byte[] bytes = imgFactory.writeModel(model);
					FileOutputStream fos = new FileOutputStream(output);
					fos.write(bytes);
					fos.close();
				 } catch (ParseError e) {
					e.printStackTrace();
				} catch (ModelException e) {
					e.printStackTrace();
				} catch (WriterException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private String saveFile() {
		JFrame parentFrame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file name to save");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".png", "png", "png"));
		fileChooser.setFileFilter(new FileNameExtensionFilter(".jpg", "jpg", "jpg"));
		int userSelection = fileChooser.showSaveDialog(parentFrame);
		File fileToSave = fileChooser.getSelectedFile();
		if(fileToSave == null) {
			return null;
		} else {
			return fileToSave.getAbsolutePath() + fileChooser.getFileFilter().getDescription();
		}
	}
	
	private String getInput(String src) {
		if(src.endsWith(".jar") || src.endsWith(".java")) {
			return "load " + src;
		} else if(src.startsWith("loadProject") || src.startsWith("loadFolder")) {
			return src;
		} else {
			return null;
		}
		
	}
}
