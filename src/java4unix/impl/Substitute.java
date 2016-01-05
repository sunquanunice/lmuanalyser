package java4unix.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class Substitute extends J4UScript
{
    @Override
    public int runScript(CommandLine cmdLine)
    {
        File mapFile = new File(cmdLine.getOption(getOptionSpecification("-map")).getValue());
        Properties props = new Properties();
        
        try
        {
            props.load(new FileInputStream(mapFile));
            
            while (true)
            {
                String line = readUserInput("user: ", ".*");
                
                if (line == null)
                {
                    break;
                }
                else
                {
                    for (Object key : props.keySet())
                    {
                        String value = props.getProperty((String) key);
                        line = line.replaceAll((String) key, value);
                        printMessage(line);
                    }
                }
            }
            

            return 0;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("unable to open file " + mapFile.getAbsolutePath());
        }
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
        specs.add(new OptionSpecification("--map-file", "-m", ".+\\.map", null, "file containing the map"));
    }

  

    public String getShortDescription()
    {
        return "Replace text according to the content of a dictionary";
    }

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		// TODO Auto-generated method stub
		
	}


}
