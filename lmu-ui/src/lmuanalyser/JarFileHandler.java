package lmuanalyser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class JarFileHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (!(selection instanceof StructuredSelection))
			return null;
		Object selected = ((StructuredSelection) selection).getFirstElement();
		IResource resource = null;
		String path = "";
		if (selected instanceof IFile) {
			 resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			 path = resource.getLocation().toString();
			 new ResultGenerator().parser(path);
		} else if (selected instanceof PlatformObject) {
			 resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			 path = resource.getLocation().toString();
			if (resource.getFileExtension() != null && resource.getFileExtension().equals("jar")) {
				new ResultGenerator().parser(path);
			}
		}
		return null;
	}

}
