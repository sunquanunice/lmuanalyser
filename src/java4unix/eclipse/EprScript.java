package java4unix.eclipse;

import java.io.IOException;
import java.util.Collection;

import org.xml.sax.SAXException;

import java4unix.CommandLine;
import java4unix.OptionSpecification;
import java4unix.impl.J4UScript;
import toools.io.file.Directory;

public abstract class EprScript extends J4UScript
{

	@Override
	public void declareOptions(Collection<OptionSpecification> specs)
	{
		Directory defaultEclipseWorkspace = new Directory(
				Directory.getHomeDirectory(), "Documents/workspace");
		specs.add(new OptionSpecification("--workspace", "-w", ".+",
				defaultEclipseWorkspace.getPath(), "Path to the workspace"));
	}

	public final Workspace getWorkspace(CommandLine cmdLine) throws IOException, SAXException
	{
		return new Workspace(new Directory(getOptionValue(cmdLine,
				"--workspace")));
	}

}
