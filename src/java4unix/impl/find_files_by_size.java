package java4unix.impl;

import java.util.Collection;
import java.util.List;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.math.relation.HashRelation;
import toools.math.relation.Relation;

public class find_files_by_size extends J4UScript
{

    @Override
    public int runScript(CommandLine cmdLine)
    {
	Collection<String> args = cmdLine.findArguments();

	Relation<String, RegularFile> r = new HashRelation<String, RegularFile>();
	boolean recursive = isOptionSpecified(cmdLine, "-r");
	String criterion = getOptionValue(cmdLine, "-c");

	List<? extends AbstractFile> fileList = recursive ? Directory.getCurrentDirectory().retrieveTree() : Directory
		.getCurrentDirectory().listRegularFiles();

	for (AbstractFile f : fileList)
	{
	    if (f instanceof RegularFile)
	    {
		r.add(getCriterionValue(f, criterion), (RegularFile) f);
	    }
	}

	Collection<String> sc = args.isEmpty() ? r.getKeys() : args;

	for (String criterionValue : sc)
	{
	    Collection<RegularFile> matchingFiles = r.getValues(criterionValue);

	    if (matchingFiles == null)
	    {
		printWarning("No file match criterion " + criterion + "=" + criterionValue);
	    }
	    else
	    {
		if (!isOptionSpecified(cmdLine, "-d") || matchingFiles.size() > 1)
		{
		    for (RegularFile f : matchingFiles)
		    {
			printMessage(criterionValue + "\t" + f);
		    }
		}
	    }
	}

	return 0;
    }

    private String getCriterionValue(AbstractFile f, String criterion)
    {
	int p = criterion.indexOf('+');

	if (p == -1)
	{
	    return getSingleCriterionValue(f, criterion);
	}
	else
	{
	    return getSingleCriterionValue(f, criterion.substring(0, p)) + "+" + getCriterionValue(f, criterion.substring(p + 1));
	}
    }

    private String getSingleCriterionValue(AbstractFile f, String c)
    {
	if (c.equals("size"))
	{
	    return String.valueOf(f.getSize());
	}
	else if (c.equals("name"))
	{
	    return f.getName();
	}
	else if (c.equals("date"))
	{
	    return String.valueOf(f.getLastModificationDate());
	}
	else
	{
	    throw new IllegalArgumentException("invalid criterion: " + c + ". Allowed values are "
		    + getOptionSpecification("-c").getValueRegexp());
	}

    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
	specs.add(new OptionSpecification("--criterion", "-c", ".+", "name", "set the group criterion"));
	specs.add(new OptionSpecification("--recursive", "-r", null, null, "set the group criterion"));
	specs.add(new OptionSpecification("--show-duplica-only", "-d", null, null,
		"show only the files appearing multiple times"));

    }

    @Override
    public String getShortDescription()
    {
	return "Find files by the given size";
    }

    @Override
    protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
    {

    }
    
    public static void main(String[] args) throws Throwable
    {
	new find_files_by_size().run("-r", "-d", "-c", "name+size");
    }
}
