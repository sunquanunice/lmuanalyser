package java4unix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.Clazz;
import toools.io.file.RegularFile;



public class RunScript
{
    public static void main(String[] args)
    {
    	// this is to force windows jdk to adopt Cygwin's home
    	System.setProperty("user.home", System.getenv("HOME"));

        List<String> arguments = new ArrayList<String>(Arrays.asList(args));

        if (arguments.size() == 0)
        {
            System.err.println("Usage: java " + RunScript.class.getName() + " [scriptClass|scriptName] [arg1] [arg2] ... [argN]");
            System.exit(1);
        }
        else
        {
            String scriptClassName = arguments.remove(0);
            Class<AbstractShellScript> clazz = (Class<AbstractShellScript>) Clazz.findClassOrFail(scriptClassName);

            if (clazz == null)
            {
                System.err.println("Cannot find class " + scriptClassName);
            }
            else
            {
                AbstractShellScript script = Clazz.makeInstance(clazz);

                if (script == null)
                {
                    System.err.println("Cannot instantiate class " + scriptClassName);
                    System.exit(1);
                }
                else
                {
                    try
                    {
                        int returnCode = script.run(new RegularFile(arguments.remove(0)), arguments);
                        System.exit(returnCode);
                    }
                    catch (Throwable t)
                    {
                    	// if there is no message
                    	if (arguments.contains("--Xdebug") || t.getMessage() == null || t.getMessage().trim().isEmpty())
                    	{
                    		t.printStackTrace();
                    	}
                    	else
                    	{
                    		System.err.println("Failure! " + t.getMessage());
                    	}
                    	
                    	System.exit(1);
                    }
                }
            }
        }
    }
}
