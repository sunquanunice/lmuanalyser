package lmuanalyser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class MyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (!(selection instanceof StructuredSelection))
			return null;
		Object selected = ((StructuredSelection) selection).getFirstElement();
		// assert(selected instanceof IFile);
		// new MyLMUFrameView(selected);

		if (selected instanceof IFile) {
			IResource resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			String path = resource.getLocation().toString();
			MyFrame myFrame = new MyFrame(path);
		} else if(selected instanceof IProject) {
			IResource resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			String path = resource.getLocation().toString();
			MyFrame myFrame = new MyFrame("loadProject " + path);
		} else if(selected instanceof IFolder) {
			IResource resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			String path = resource.getLocation().toString();
			MyFrame myFrame = new MyFrame("loadFolder " + path);
		}

		if (selected instanceof IFolder || selected instanceof PlatformObject) {
			IResource resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			IPath path = resource.getLocation();

			/*
			 * //Get the classes names in the folder String spath =
			 * path.toString(); try {
			 * Files.walk(Paths.get(spath)).forEach(filePath -> { if
			 * (Files.isRegularFile(filePath)) { System.err.println(filePath); }
			 * }); } catch (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			
		}
		return null;
	}

}
