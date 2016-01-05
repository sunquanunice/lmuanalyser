package java4unix.eclipse;

import java.io.IOException;
import java.util.Collection;

import org.xml.sax.SAXException;

import toools.config.ConfigurationException;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;


public class eclipse_list_classes extends EprScript
{

	@Override
	public String getShortDescription()
	{
		return "list project classes";
	}

	@Override
	public int runScript(final CommandLine cmdLine) throws ConfigurationException, IOException, SAXException
	{
		String projectName = cmdLine.findArguments().get(0);
		Workspace w = getWorkspace(cmdLine);

		Project p = w.findProject(projectName);

		for (Class<?> c : p.getClasses())
		{
			printMessage(c.getName());
		}

		return 0;
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".+", false, "name of a project"));

	}

}
