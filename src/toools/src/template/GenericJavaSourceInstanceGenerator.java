package toools.src.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import java4unix.impl.J4UScript;

import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.FileFilter;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;
import toools.text.TextUtilities;

public class GenericJavaSourceInstanceGenerator extends J4UScript
{
	public static RegularFile process(RegularFile f, Map<String, String> m)
			throws IOException
	{
		System.out.println(f.getName() + "   " + m);
		String s = new String(f.getContent());
		String outFilename = f.getName();

		for (String srcType : m.keySet())
		{
			String targetType = m.get(srcType);

			if (s.contains(srcType + " extends Integer"))
			{
				s = s.replaceAll(srcType + " extends Integer", "");

				s = s.replaceAll(" " + srcType + " ", " " + targetType + " ");
				s = s.replaceAll("[\\t]" + srcType + " ", "\t" + targetType
						+ " ");
				s = s.replaceAll("[\\n]" + srcType + " ", "\n" + targetType
						+ " ");
				s = s.replaceAll("\\(" + srcType + " ", "(" + targetType + " ");
				s = s.replaceFirst("_" + srcType + "_",
						TextUtilities.capitalizeWord(targetType));

				s = s.replaceAll(
						"<[, ]*>",
						" // this file an instance of generic file \""
								+ f.getName() + "\" where type \"" + srcType
								+ "\" is set to \"" + targetType + "\"");
				
				outFilename = outFilename.replaceFirst("_" + srcType + "_",
						TextUtilities.capitalizeWord(targetType));
			}
		}

		RegularFile outFile = new RegularFile(f.getParent(), outFilename);
		outFile.setWriteable(true);
		outFile.setContent(s.getBytes());
		outFile.setWriteable(false);
		return outFile;

	}

	public static Set<RegularFile> process(RegularFile inputFile,
			List<Instance> li) throws IOException
	{
		int[] value = new int[li.size()];
		int[] size = new int[li.size()];

		for (int i = 0; i < li.size(); ++i)
		{
			size[i] = li.get(i).getDestinationTypes().size();
		}

		Set<RegularFile> files = new HashSet<RegularFile>();

		do
		{
			Map<String, String> m = new HashMap<String, String>();

			for (int i = 0; i < value.length; ++i)
			{
				m.put(li.get(i).getSourceType(), li.get(i)
						.getDestinationTypes().get(value[i]));
			}

			process(inputFile, m);
		} while (MathsUtilities.next(value, size));

		return files;
	}

	public static Set<RegularFile> process(Set<RegularFile> inputFiles,
			List<Instance> li) throws IOException
	{
		Set<RegularFile> files = new HashSet<RegularFile>();

		for (RegularFile f : inputFiles)
		{
			files.addAll(process(f, li));
		}

		return files;
	}

	public static Set<RegularFile> findGenericFiles(Directory d)
	{
		return new HashSet(d.retrieveTree(new FileFilter() {

			@Override
			public boolean accept(AbstractFile f)
			{
				return f instanceof RegularFile
						&& f.getName().matches("(_.+_)+.*\\.java$");
			}

		}, null));
	}

	
	@Override
	protected void declareOptions(
			Collection<OptionSpecification> optionSpecifications)
	{
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
	}

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{
		List<String> args = cmdLine.findArguments();
		Directory f = new Directory(args.get(0));

		List<Instance> l = new ArrayList<Instance>();
		l.add(new Instance("V", "int", "long"));
		l.add(new Instance("E", "int", "String"));

		Set<RegularFile> files = findGenericFiles(f);
		System.out.println(files.size() + " files");

		for (RegularFile outputFile : process(files, l))
		{
			if (outputFile != null)
			{
				System.out.println(outputFile.getName());
			}
			else
			{
				System.err.println("not processed: ");
			}
		}
		return 0;
	}

	@Override
	public String getShortDescription()
	{

		return "generate template file instances, by replacing _T_, <T> and T by the given types";
	}
}
