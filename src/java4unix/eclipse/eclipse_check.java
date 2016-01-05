package java4unix.eclipse;

import java.io.IOException;
import java.util.Collection;

import org.xml.sax.SAXException;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import toools.config.ConfigurationException;
import toools.io.file.RegularFile;
import toools.math.relation.HashRelation;
import toools.math.relation.Relation;

public class eclipse_check extends EprScript
{
	@Override
	public String getShortDescription()
	{
		return "check validity";
	}

	@Override
	public int runScript(final CommandLine cmdLine)
			throws ConfigurationException, IOException, SAXException
	{
		Workspace w = getWorkspace(cmdLine);
		checkDuplicateLibs(w);
		checkDuplicateSourceFiles(w.getProjects());
		return 0;
	}

	private void checkDuplicateLibs(Workspace w)
	{
		printMessage("\n*********************************************");
		printMessage("Duplicate libraries (may head to inconsistencies):");
		printMessage("*********************************************\n");

		Relation<String, RegularFile> r = new HashRelation<String, RegularFile>();

		for (Project p : w.getProjects())
		{
			for (RegularFile j : p.getDirectLibDependancies())
			{
				r.add(j.getName(), j);
			}
		}

		for (Collection<RegularFile> c : r.getValues())
		{
			if (c.size() > 1)
			{
				printMessage(c);
			}
		}
	}

	private void checkDuplicateSourceFiles(Collection<Project> projects)
	{
		printMessage("\n*********************************************");
		printMessage("Source files with same name (may head to inconsistencies):");
		printMessage("*********************************************\n");
		Relation<String, RegularFile> r = new HashRelation<String, RegularFile>();

		for (Project p : projects)
		{
			for (RegularFile f : p.getSourceFiles())
			{
				r.add(f.getName(), f);
			}
		}

		for (Collection<RegularFile> c : r.getValues())
		{
			if (c.size() > 1)
			{
				printMessage(c);
			}
		}
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".+", false,
				"name of a project"));

	}

}
