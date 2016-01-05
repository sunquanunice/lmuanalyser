package java4unix.impl;

import java.util.Collection;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class Concatene_Lines extends J4UScript
{
    @Override
    protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
    {
        optionSpecifications.add(new OptionSpecification("--separator", "-s", ".*", "", "separator"));
    }



    @Override
    public int runScript(CommandLine cmdLine)
    {
        String sep = getOptionValue(cmdLine, "-s");
        String prev = null;

        while (true)
        {
            String line = readUserInput("", ".*");

            if (line == null)
            {
                break;
            }
            else
            {
                if (prev != null)
                {
                    printMessageWithNoNewLine(sep);
                }

                printMessageWithNoNewLine(line);
                prev = line;
            }
        }

        printMessage("");
        return 0;
    }

    @Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{

	}


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "concatene the given lines";
	}
}
