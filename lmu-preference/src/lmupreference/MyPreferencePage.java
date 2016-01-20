package lmupreference;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	 public MyPreferencePage() {
		    super(GRID);

		  }

		  public void createFieldEditors() {
		    addField(new DirectoryFieldEditor("PATH", "&Diagram export to:",
		        getFieldEditorParent()));

		    addField(new RadioGroupFieldEditor("CHOICE",
		        "Export format", 1,
		        new String[][] { { "&PDF", "choice1" },
		            { "&PNG", "choice2" } }, getFieldEditorParent()));
		   /* addField(new StringFieldEditor("MySTRING1", "A &text preference:",
		        getFieldEditorParent()));
		    addField(new StringFieldEditor("MySTRING2", "A &text preference:",
		        getFieldEditorParent()));*/
		  }

		  @Override
		  public void init(IWorkbench workbench) {
		    setPreferenceStore(Activator.getDefault().getPreferenceStore());
		    setDescription("LMU Analyser Configuration");
		  }
		
	
}
