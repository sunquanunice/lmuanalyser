package java4unix.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;

import toools.collections.Collections;
import toools.text.TextUtilities;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;



public class Get_Ip_Addresses extends J4UScript
{

    @Override
    public int runScript(CommandLine cmdLine)
    {
        try
        {
            Collection<NetworkInterface> e = Collections.convertEnumerationToCollection(NetworkInterface.getNetworkInterfaces());
            printMessage("Found "  + e.size() + " network interface(s):");

            for (NetworkInterface i : e)
            {
                printMessage("\nName:             " + i.getDisplayName());
                
                if (i.getHardwareAddress() != null)
                {
                    printMessage("Hardware address: " + TextUtilities.toHex(i.getHardwareAddress(), "-").toUpperCase());
                }
                
                for (InetAddress address : Collections.convertEnumerationToCollection(i.getInetAddresses()))
                {
                    printMessage("IP address:       " + address.getHostAddress());
                    
                    if (!address.getHostName().equals(address.getHostAddress()))
                    {
                        printMessage("Hostname:         " + address.getHostName());
                    }
                }
            }

            return 0;
        }
        catch (SocketException ex)
        {
            printNonFatalError("Cannot list the network interfaces");
            return 1;
        }
    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
    }


	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		
		
	}


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "List the IP addresses associated to this computer";
	}
}
