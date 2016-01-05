package java4unix.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import toools.text.TextUtilities;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

public class check_servers_reacheability extends J4UScript
{
    @Override
    public String getShortDescription()
    {
	return "Checks if the given servers are accessible.";
    }

    public int runScript(CommandLine cmdLine)
    {
	final List<String> serverNames = new ArrayList<String>(cmdLine.findArguments());
	final int timeoutMs = (int) (Double.valueOf(getOptionValue(cmdLine, "--timeout")) * 1000d);
	final int port = Integer.valueOf(getOptionValue(cmdLine, "--port"));

	for (int serverIndex = 0; serverIndex < serverNames.size(); ++serverIndex)
	{
	    final String serverName = serverNames.get(serverIndex);
	    final int row = serverIndex;

	    new Thread() {
		@Override
		public void run()
		{
		    printMessage(TextUtilities.flushLeft(serverName, 20, ' ') + "\t"
			    + (serverIsAlive(serverNames.get(row), port, timeoutMs) ? "yes" : "no"));
		}
	    }.run();
	}

	return 0;
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
	getOptionSpecifications().add(new OptionSpecification("--port", "-p", ".+", "22", "The port to check to"));
	getOptionSpecifications().add(
		new OptionSpecification("--timeout", "-t", ".+", "1", "The acceptable wait (in second)"));
    }

    @Override
    protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
    {
	argumentSpecifications.add(new ArgumentSpecification("port", ".+", true, "port number"));
    }

    public static void main(String[] args) throws Throwable
    {
	new check_servers_reacheability().run("mijote", "musclotte", "srv-mascotte", "srv-aoste",
		"www-sop", "kona", "mauka");
    }

    private boolean serverIsAlive(String serverName, int port, int timeout)
    {
	try
	{
	    Socket s = new Socket();
	    SocketAddress a = new InetSocketAddress(serverName, port);
	    s.connect(a, timeout);

	    try
	    {
		s.close();
	    }
	    catch (IOException e)
	    {
	    }

	    return true;
	}
	catch (IOException e)
	{
	    return false;
	}
    }
}
