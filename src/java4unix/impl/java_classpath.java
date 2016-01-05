package java4unix.impl;

import java.util.Collection;
import java.util.HashSet;

import toools.ClassContainer;
import toools.ClassPath;
import toools.io.FileUtilities;
import toools.io.file.AbstractFile;
import toools.io.file.RegularFile;
import toools.math.relation.HashRelation;
import toools.math.relation.Relation;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class java_classpath extends J4UScript
{
	
	public static void main(String[] args) throws Throwable
	{
		System.out.println("coucou");
//		new java_classpath().run("-e");
		System.out.println(new java_classpath().getActualConfigurationFile());
	}
	@Override
	public String getShortDescription()
	{
		return "Check classpath";
	}

	@Override
	protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
	{
		optionSpecifications.add(new OptionSpecification("--list-classes", "-c", null, null, "list all classes"));
		optionSpecifications.add(new OptionSpecification("--list-entries", "-e", null, null, "list all entries"));
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
	}

	@Override
	public int runScript(CommandLine cmdLine)
	{
		ClassPath classpath = ClassPath.retrieveClassPath();

		if (isOptionSpecified(cmdLine, "--list-classes"))
		{
			listClasses(classpath);
		}
		else if (isOptionSpecified(cmdLine, "--list-entries"))
		{
			listEntries(classpath);
		}
		else if (cmdLine.findOptions().isEmpty())
		{
			// if no options were given
			checkNonExistingEntries(classpath);
			checkConflictingEntries(classpath);
		}

		return 0;
	}

	private void listEntries(ClassPath classpath)
	{
		for (ClassContainer f : classpath)
		{
			printMessage(f.getFile().getPath());
		}
	}

	private void checkNonExistingEntries(ClassPath classpath)
	{
		for (ClassContainer e : classpath)
		{ 
			AbstractFile f = e.getFile();
			
			if (!f.exists())
			{
				printNonFatalError("entry does not exist: " + f.getPath());
			}
			else if (!f.canRead())
			{
				printNonFatalError("entry is not readable: " + f.getPath());
			}
		}
	}

	/**
	 * Checks if this classpath defines two entries with the same name but different content
	 * @param classpath
	 */
	private void checkConflictingEntries(ClassPath classpath)
	{
		Relation<String, ClassContainer> r = new HashRelation<String, ClassContainer>();

		for (ClassContainer u : classpath)
		{
			// if the entry is a zip or a jar
			if (u.getFile() instanceof RegularFile)
			{
				r.add(u.getFile().getName(), u);
			}
		}

		for (String e : r.getKeys())
		{
			Collection<RegularFile> files = new HashSet<RegularFile>();
			
			for (ClassContainer thisEntry : new HashSet<ClassContainer>(r.getValues(e)))
			{
				files.add((RegularFile) thisEntry.getFile());
			}
			
			if (!FileUtilities.ensureSameFile(files))
			{
				printNonFatalError("Entry " + e + " refers to conflicting files: " + files);
			}
		}

	}

	private void listClasses(ClassPath classpath)
	{
		for (Class<?> c : classpath.listAllClasses())
		{
			printMessage(c.getName());
		}
	}

}
