package java4unix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import toools.ClassContainer;
import toools.ClassPath;
import toools.io.JavaResource;


public abstract class Application
{
	public String getVersion()
	{
		JavaResource r = new JavaResource("/" + getApplicationName() + "-version.txt");

		try
		{
			return new String(r.getByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public abstract String getApplicationName();

	public abstract String getAuthor();

	public abstract License getLicence();

	public abstract String getYear();

	public abstract String getShortDescription();

	protected List<String> getVMOptions()
	{
		return new ArrayList<String>();
	}

	public ClassContainer getClasspathEntry()
	{
		for (ClassContainer f : ClassPath.retrieveClassPath())
		{
			if (f.getFile().getName().startsWith(getApplicationName() + "-"))
			{
				return f;
			}
		}

		return null;
	}
	
}
