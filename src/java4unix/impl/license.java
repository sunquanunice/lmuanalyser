package java4unix.impl;

import java.util.Collection;

import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class license extends J4UScript
{
	@Override
	protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
	{
		optionSpecifications.add(new OptionSpecification("--action", "-a", "add|remove", null, "add or remove license"));
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		RegularFile licenceFile = new RegularFile("license-header.txt");
		String header = new String(licenceFile.getContent());
		boolean add = getOptionValue(cmdLine, "-a").equals("add");

		for (AbstractFile f : Directory.getCurrentDirectory().retrieveTree())
		{
			if (f.getName().endsWith(".java"))
			{
				RegularFile javaFile = (RegularFile) f;
				String sourceCode = new String(javaFile.getContent());
				boolean containsLicense = sourceCode.startsWith(header);

				if (add && !containsLicense)
				{
					printMessage("Adding license header to " + javaFile.getPath());
					javaFile.setContent((header + sourceCode).getBytes());
				}
				else if (!add && containsLicense)
				{
					printMessage("Removing license header from " + javaFile.getPath());
					javaFile.setContent(sourceCode.substring(header.length()).getBytes());
				}
			}
		}

		return 0;
	}

	@Override
	public String getShortDescription()
	{
		return "Adds/remove the given license to/from the source files in the current directory and its sub-directories. The licence" +
				"is expected to be in a file called license-header.txt";
	}

}
