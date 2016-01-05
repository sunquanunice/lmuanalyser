package java4unix.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class replace_in_filename extends J4UScript
{


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Replace text in filenames.";
	}
	
    @Override
    protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
    {
    }


	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("orig",".+", false, "pattern to look for"));
		argumentSpecifications.add(new ArgumentSpecification("replacement",".+", false, "pattern to look for"));
		argumentSpecifications.add(new ArgumentSpecification("file",".+", true, "text file"));
	}

    @Override
    public int runScript(CommandLine cmdLine)
    {
    	List<String> args = new ArrayList<String>(cmdLine.findArguments());
    	String regexp = args.remove(0);
    	String replacement = args.remove(0);
    	
    	for (String filename : args)
    	{
    		File f = new File(filename);
    		f.renameTo(new File(f.getParentFile(), f.getName().replaceFirst(regexp, replacement)));
    		printMessage(f.getAbsolutePath());
    	}

    	return 0;
    }
}
