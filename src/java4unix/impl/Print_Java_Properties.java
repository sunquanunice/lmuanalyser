package java4unix.impl;

import java.util.Collection;
import java.util.Properties;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class Print_Java_Properties extends J4UScript
{


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Print Java properties.";
	}
	@Override
    public int runScript(CommandLine cmdLine)
    {
        boolean onlykeys = cmdLine.getOption(getOptionSpecification("-k")).isSpecified();
        Properties p = System.getProperties();

        if (cmdLine.getOption(getOptionSpecification("-p")).isSpecified())
        {
            String key = (String) cmdLine.getOption(getOptionSpecification("-p")).getValue();
            
            if (!p.containsKey(key))
            {
                printFatalError("unexisting key: " + key);
                return 1;
            }
            else
            {
                printMessage((String) p.get(key));
            }
        }
        else
        {
            for (Object o : p.keySet())
            {
                printMessage(o + (onlykeys ? "" : "=" + p.get(o)));
            }
        }

        return 0;
    }


	@Override
    public void declareOptions(Collection<OptionSpecification> specs)
    {
        specs.add(new OptionSpecification("--only-keys", "-k", null, null, "print only available keys"));
        specs.add(new OptionSpecification("--print-value", "-p", ".+", ".+", "print the value of the given key"));
    }
	

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		
	}
	
}
