package java4unix.impl;

import java.util.Collection;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

public class Echo extends J4UScript
{

	@Override
	public int runScript(CommandLine cmdLine)
	{
		while (true)
		{
			String s = readUserInput(null, ".*");

			if (s != null)
			{
				printMessage(s);
			}
		}
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("arg", ".*", true, "any text"));
	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "echo the given text";
	}
}
