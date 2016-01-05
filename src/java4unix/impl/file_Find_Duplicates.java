package java4unix.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import toools.io.FileUtilities;
import toools.io.file.Directory;
import toools.io.file.FileFilter;
import toools.io.file.RegularFile;
import toools.math.HashMatrix;
import toools.math.relation.HashRelation;
import toools.math.relation.Relation;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class file_Find_Duplicates extends J4UScript
{
	public static void main(String[] args) throws Throwable
	{
		new file_Find_Duplicates().run("/Users/lhogie/souvenirs/unclassified", "-c");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws IOException
	{
		Directory aDir = new Directory(cmdLine.findArguments().get(0));
		printMessage("Scanning " + aDir.getPath() + "...");
		Set<RegularFile> files = new HashSet<RegularFile>((Set) aDir.retrieveTree(new FileFilter.RegularFileFilter(), null));
		printMessage(files.size() + " files found");

		Relation<String, RegularFile> nameMap = new HashRelation<String, RegularFile>();
		Relation<Long, RegularFile> sizeMap = new HashRelation<Long, RegularFile>();

		printMessage("Classifying names and sizes...");
		for (RegularFile thisFile : files)
		{
			nameMap.add(thisFile.getName().toLowerCase(), thisFile);
			sizeMap.add(thisFile.getSize(), thisFile);
		}

		printMessage("Classifying by content...");
		Set<Set<RegularFile>> contentMap = classifyByContent(sizeMap.getValues());

		if (isOptionSpecified(cmdLine, "-n"))
		{
			print(nameMap, "name");
		}

		if (isOptionSpecified(cmdLine, "-s"))
		{
			print(sizeMap, "size");
		}

		if (isOptionSpecified(cmdLine, "-c"))
		{
			printMessage("Files with the same content:");

			for (Set<RegularFile> setOfDuplicates : contentMap)
			{
				List<RegularFile> listOfDuplicates = new ArrayList<RegularFile>(setOfDuplicates);
				FileUtilities.sortByAbsolutePath(listOfDuplicates);

				// if there are duplicates
				if (listOfDuplicates.size() > 1)
				{
					// if duplicates must be deleted
					if (isOptionSpecified(cmdLine, "-d"))
					{
						printMessage("");

						// retaining the first file
						Iterator<RegularFile> i = listOfDuplicates.iterator();
						RegularFile f = i.next();
						printMessage("Retaining " + f.getPathRelativeToCurrentDir());
						
						// deleting all the others
						while (i.hasNext())
						{
							RegularFile duplicate = i.next();
							printMessage("Deleting " + duplicate.getPathRelativeToCurrentDir());
							duplicate.delete();
						}	
					}
					else
					{
						printMessage("");
						
						for (RegularFile f : listOfDuplicates)
						{
							printMessage(f.getPathRelativeToCurrentDir());
						}
					}
				}
			}
		}

		return 0;
	}

	private Set<Set<RegularFile>> classifyByContent(Collection<Collection<RegularFile>> filesWithSameSize) throws IOException
	{
		Relation<String, RegularFile> contentMap = new HashRelation<String, RegularFile>();

		for (Collection<RegularFile> f : filesWithSameSize)
		{
			if (f.size() > 1)
			{
				for (RegularFile a : f)
				{
					contentMap.add(String.valueOf(Arrays.hashCode(a.getContent())), a);
					// contentMap.add(TextUtilities.toHex(FileUtilities.computeMD5(a),
					// ""), a);
				}
			}
		}

		Set<Set<RegularFile>> s = new HashSet<Set<RegularFile>>();

		for (Collection<RegularFile> c : contentMap.getValues())
		{
			s.add(new HashSet<RegularFile>(c));
		}

		return s;
	}

	private Set<Set<RegularFile>> classifyByContent2(Collection<Collection<RegularFile>> filesWithSameSize) throws IOException
	{
		Set<Set<RegularFile>> contentMap = new HashSet<Set<RegularFile>>();

		for (Collection<RegularFile> files : filesWithSameSize)
		{
			if (files.isEmpty())
			{
				throw new IllegalStateException();
			}
			else if (files.size() == 1)
			{
				// contentMap.add(new HashSet<File>(files));
			}
			else
			{
				List<RegularFile> list = new ArrayList<RegularFile>(files);
				final HashMatrix<RegularFile, RegularFile, Boolean> m = new HashMatrix<RegularFile, RegularFile, Boolean>();
				Collections.sort(list, new Comparator<RegularFile>()
				{

					@Override
					public int compare(RegularFile a, RegularFile b)
					{
						try
						{
							int c = RegularFile.compareFileContentsLexicographically(a, b);
							m.set(a, b, c == 0);
							m.set(b, a, c == 0);
							return c;
						}
						catch (IOException e)
						{
							throw new IllegalStateException(e.getMessage());
						}
					}
				});

				Set<RegularFile> current = new HashSet<RegularFile>();
				current.add(list.get(0));
				contentMap.add(current);

				for (int i = 1; i < list.size(); ++i)
				{
					if (m.get(list.get(i - 1), list.get(i)))
					{
						current.add(list.get(i));
					}
					else
					{
						current = new HashSet<RegularFile>();
						current.add(list.get(i));
						contentMap.add(current);
					}
				}
			}
		}

		return contentMap;
	}

	private void print(Relation r, String msg)
	{
		printMessage("Files with the same " + msg + ":");

		for (Object name : r.getKeys())
		{
			Collection<RegularFile> matchingFiles = r.getValues(name);

			if (matchingFiles.size() > 1)
			{
				for (RegularFile thisFile : matchingFiles)
				{
					printMessage("\t\t" + thisFile.getPath());
				}
				printMessage("");
			}
		}

	}

	@Override
	public void declareOptions(Collection<OptionSpecification> specs)
	{
		specs.add(new OptionSpecification("--name", "-n", null, null, "search files with same name"));
		specs.add(new OptionSpecification("--size", "-s", null, null, "search files with same size"));
		specs.add(new OptionSpecification("--content", "-c", null, null, "search files with same content"));
		specs.add(new OptionSpecification("--delete", "-d", null, null, "delete duplicates"));
		specs.add(new OptionSpecification("--method", "-m", "md5|hash|full", "hash", "method for comparing file contents"));
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".+", false, "a directory"));

	}

	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Find files with same name and/or same size.";
	}
}
