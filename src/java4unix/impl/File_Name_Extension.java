package java4unix.impl;

import java.util.Collection;

import toools.io.FileUtilities;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;



public class File_Name_Extension extends J4UScript
{

    @Override
    public int runScript(CommandLine cmdLine)
    {
        Collection<String> args = cmdLine.findArguments();

        if (args.isEmpty())
        {
            String s;
            
            while ((s = readUserInput("", ".*")) != null)
            {
                String ext = FileUtilities.getFileNameExtension(s);

                if (ext == null)
                {
                    printNonFatalError("no extension: " + s);
                }
                else
                {
                    printMessage(ext);
                }
            }
        }
        else
        {
            for (String parm : args)
            {
                String ext = FileUtilities.getFileNameExtension(parm);

                if (ext == null)
                {
                    printNonFatalError("no extension: " + parm);
                }
                else
                {
                    printMessage(ext);
                }
            }
        }

        return 0;
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
        // TODO Auto-generated method stub

    }


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Computes the extension of the given filename.";
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".*", false, "any filename"));
		
	}
}
