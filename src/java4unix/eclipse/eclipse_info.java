package java4unix.eclipse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xml.sax.SAXException;

import toools.config.ConfigurationException;
import toools.io.FileUtilities;
import toools.io.file.RegularFile;
import toools.text.TextUtilities;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;

public class eclipse_info extends EprScript
{
	public static void main(String[] args) throws Throwable
	{
		new eclipse_info().run("-w", "~/dev/src");
	}

	private int lineLenght = 35;

	@Override
	public String getShortDescription()
	{
		return "draw project dependencies";
	}

	@Override
	public int runScript(final CommandLine cmdLine)
			throws ConfigurationException, IOException, SAXException
	{
		Map<String, Project> ps = new HashMap<String, Project>();
		Workspace w = getWorkspace(cmdLine);

		for (Project p : w.getProjects())
		{
			ps.put(p.getName(), p);
		}

		Collection<String> projectNames = cmdLine.findArguments();

		if (projectNames.isEmpty())
		{
			printMessage("Workspace " + w.getLocation().getPath()
					+ " include the projects:");

			for (Project thisProject : ps.values())
			{
				printMessage("\t- " + thisProject.getName());
			}
		}
		else
		{
			for (String name : projectNames)
			{
				Project p = ps.get(name);

				if (p == null)
				{
					printNonFatalError("Project cannot be found: " + name);
				}
				else
				{
					printSeparator("Locations on the local drive");
					printProperty("Version on disk", p.getVersion().toString());
					printProperty("Project location", p.getDirectory()
							.getPath());
					printProperty("Sources directory", p.getSourceDirectory()
							.getPath());
					printProperty("Classes directory", p.getClassesDirectory()
							.getPath());
					printProperty("Source archive", p.getSourceZIPFile()
							+ (p.getSourceZIPFile().exists() ? ""
									: " (not found)"));
					printProperty("Binaries archive", p.getJAR()
							+ (p.getJAR().exists() ? ""
									: " (not found)"));
					// (p.getSelfContainedBinariesFile().exists()
					// ?
					// ""
					// :
					// " (not found)"));

					printSeparator("Release info");


					printProperty("Source is public",
							p.isSourceIsPublic() ? "yes" : "no");
					printProperty("SCP destination", p.getSCPDestination());
					printProperty("Maven repository", p.getMavenRepository());
					printProperty(
							"SVN destination",
							p.getSVNDestination() == null ? "-" : p
									.getSVNDestination());

					printSeparator("Statistics");
					printProperty(
							"Number of source files",
							+p.getJavaFiles().size()
									+ " files ("
									+ p.computeNumberOfFileAlsoInProjectDependancies()
									+ " when including all projects)");
					printProperty(
							"Number of source lines",
							p.getNumberOfSourceLines()
									+ " lines ("
									+ p.computeNumberOfLinesAlsoInProjectDependancies()
									+ " when including all projets");
					printProperty(
							"Number of resource files",
							p.getResourceFiles().size()
									+ " files of type "
									+ new HashSet<String>(
											FileUtilities
													.computeExtensions(new ArrayList<RegularFile>(
															p.getResourceFiles()))));

					printSeparator("Dependancies");

					for (RegularFile lib : p.getDirectLibDependancies())
					{
						printProperty("Jar dependancy", lib.getName());
					}

					for (RegularFile lib : p.computeIndirectLibDependancies())
					{
						printProperty("Indirect JAR dependancy", lib.getName());
					}

					for (Project dp : p.getDirectProjectDependancies())
					{
						printProperty("Project dependancy", dp.getName());
					}

					for (Project dp : p.computeIndirectProjectDependancies())
					{
						printProperty("Indirect Project dependancy",
								dp.getName());
					}
				}
			}
		}

		return 0;
	}

	@Override
	protected void declareArguments(
			Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".+", false,
				"name of a project"));

	}

	private void printProperty(String string, String name)
	{
		printMessage(TextUtilities.flushLeft(string + ":", lineLenght, ' ')
				+ name);
	}

	private void printSeparator(String title)
	{
		printMessage("");
		printMessage(TextUtilities.repeat("-", this.lineLenght));
		printMessage(title.toUpperCase() + ":");
		printMessage(TextUtilities.repeat("-", this.lineLenght));
	}
	
	

}
