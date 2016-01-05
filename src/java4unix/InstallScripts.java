package java4unix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import toools.ClassContainer;
import toools.Clazz;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class InstallScripts
{
	public static void main(String... args) throws IOException
	{
		// this is to force windows jdk to adopt Cygwin's home

		System.setProperty("user.home", System.getenv("HOME"));

		f(args);
	}

	public static void f(String... args) throws IOException
	{
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));
		boolean verbose = !(arguments.remove("-q") || arguments.remove("--quiet"));

		for (String s : arguments)
		{
			if (s.startsWith("-"))
			{
				System.err.println("Unsupported option: " + s);
				System.exit(1);
			}
		}

		for (String jarFileName : arguments)
		{
			if (verbose)
				System.out.println("Scanning " + jarFileName);
			
			RegularFile jarFile = new RegularFile(jarFileName);
			String expectedApplicationName = jarFile.getName().replaceAll("-.*", "");
			System.out.println("searching scripts for application " + expectedApplicationName);
			Collection<Class<AbstractShellScript>> scriptClasses = Utilities.findScriptClasses(new ClassContainer(jarFile), ".*" + expectedApplicationName + ".*");

			if (scriptClasses.size() > 0)
			{
				for (Class<? extends AbstractShellScript> thisClass : scriptClasses)
				{
					AbstractShellScript script = Clazz.makeInstance(thisClass);

					if (script == null)
					{
						System.err.println("Cannot instantiate script class " + thisClass.getName());
					}
					else
					{
						if (script.getApplicationName().equals(expectedApplicationName))
						{
							Directory installDir = getInstallDir(script.getApplicationName());

							if (!installDir.exists())
								installDir.mkdirs();

							script.setInstallationDirectory(installDir);

							if (verbose)
								System.out.println("creating " + script.getExecutableScript().getPath());

							script.installScript();

							if (System.getProperty("user.name").equals("root"))
							{
								AbstractFile link = AbstractFile.map("/usr/local/bin/" + script.getExecutableScript().getName(), RegularFile.class);
								link.delete();
								Posix.makeSymbolicLink(script.getExecutableScript(), link);
							}
						}
						else
						{
							System.err.println("Script " + script.getClass().getName() + " belongs to application " + script.getApplicationName()
									+ " instead of " + expectedApplicationName + ". Skipping it.");
						}
					}
				}

				Directory binDir = getInstallDir(expectedApplicationName).getChild("bin", Directory.class);
				// setScript(binDir, expectedApplicationName + "-update",
				// "curl -s http://www-sop.inria.fr/members/Luc.Hogie/java4unix/j4uni | sh -s "
				// + expectedApplicationName,
				// verbose);
				// setScript(binDir, expectedApplicationName + "-uninstall",
				// "rm -rf " + binDir.getParent(), verbose);
			}
			else
			{
				System.out.println("No script class was found in classpath entry " + jarFileName);
			}
		}
	}

	public static Directory getInstallDir(String applicationName)
	{
		boolean userIsRoot = System.getProperty("user.name").equals("root");
		String prefix = userIsRoot ? "/usr/local/share/" : Directory.getHomeDirectory().getPath() + "/.";
		return new Directory(prefix + applicationName);
	}

	private static void setScript(Directory binDirectory, String scriptName, String text, boolean verbose) throws IOException
	{
		RegularFile f = binDirectory.getChild(scriptName, RegularFile.class);
		if (verbose)
			System.out.println("creating " + f.getPath());
		f.setContent(text.getBytes());
		Posix.chmod(f, "+x");
	}

}
