package lmuanalyser;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class MyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection  = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if(!(selection instanceof StructuredSelection)) return null;
		Object selected = ((StructuredSelection) selection).getFirstElement();
//		assert(selected instanceof IFile);
		JOptionPane.showMessageDialog(null, "Hellow:"+selected.getClass());
		return null;
	}

	
}
