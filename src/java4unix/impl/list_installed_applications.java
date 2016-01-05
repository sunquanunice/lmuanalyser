package java4unix.impl;

import java.util.Collection;

import toools.io.file.Directory;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import java4unix.Utilities;


public class list_installed_applications extends J4UScript
{
	@Override
	public String getShortDescription()
	{
		return "list java4unix applications on this system";
	}
	@Override
	protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
	{
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{

	}
	@Override
	public int runScript(CommandLine cmdLine)
	{
		for (Directory d : Utilities.findInstalledApplicationDirs())
		{
			printMessage(d.getName().substring(1));
		}

		return 0;
	}
}
