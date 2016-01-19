package lmuanalyser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class JavaPackageHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (!(selection instanceof StructuredSelection))
			return null;
		Object selected = ((StructuredSelection) selection).getFirstElement();
		IResource resource = null;
		String path = "";
		if (selected instanceof IFolder) {
			 resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			 path = resource.getLocation().toString();
			 MyFrame myFrame = new MyFrame("loadFolder " + path);
		} else if (selected instanceof PlatformObject) {
			resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			 path = resource.getLocation().toString();
			 //Verify if the folder is in the src or not
			
			 /*String binPath = "";
			 if(path.endsWith("src")) {
				 binPath = path.substring(0, path.length()-3);
			 } else {
				 int index = path.indexOf("/src/");
				 if(index == -1) {
					 
				 }
			 }*/
				MyFrame myFrame = new MyFrame("loadFolder " + path);
		}
		return null;
	}

}
