package java4unix;

import java.util.Collection;
import java.util.HashSet;

import toools.ClassContainer;
import toools.Clazz;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;


public class Utilities
{
	public static Collection<Class<AbstractShellScript>> findScriptClasses(
			ClassContainer cc, String re)
	{
		Collection<Class<AbstractShellScript>> scriptClasses = new HashSet<Class<AbstractShellScript>>();

		for (Class<?> thisClass : cc.listAllClasses(re))
		{
			if (!Clazz.isAbstract(thisClass)
					&& AbstractShellScript.class.isAssignableFrom(thisClass))
			{
				scriptClasses.add((Class<AbstractShellScript>) thisClass);
			}
		}

		return scriptClasses;
	}

	public static Collection<Directory> findInstalledApplicationDirs()
	{
		Collection<Directory> s = new HashSet<Directory>();

		for (AbstractFile c : Directory.getHomeDirectory()
				.findChildFilesWhoseTheNameMatches("^\\..*$"))
		{
			if (c instanceof Directory)
			{
				Directory d = (Directory) c;

				if (new Directory(d, "bin").exists()
						&& new Directory(d, "lib").exists())
				{
					s.add(d);
				}
			}
		}

		return s;
	}

}
