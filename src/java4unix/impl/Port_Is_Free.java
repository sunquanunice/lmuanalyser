package java4unix.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class Port_Is_Free extends J4UScript
{


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Checks if the given port is free";
	}
    public int runScript(CommandLine cmdLine)
    {
        for (String portT : cmdLine.findArguments())
        {
            int port = Integer.valueOf(portT);

            try
            {
                ServerSocket s = new ServerSocket(port);

                try
                {
                    s.close();
                }
                catch (IOException e)
                {
                }

                printMessage("port " + port + " is free");
            }
            catch (IOException e)
            {
                printMessage("port " + port + " is allocated");
            }
        }
        
        return 0;
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
 
    }

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("port",".+", true, "port number"));
		
	}
}
