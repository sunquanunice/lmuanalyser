package java4unix.eclipse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.xml.sax.SAXException;

import toools.Version.Level;
import toools.io.file.AbstractFile;
import toools.io.file.RegularFile;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

/**
 * 
 * @author lhogie
 * 
 *         The releasing of Java project is already possible by the use of
 *         software like Ant, Maven ,Ivy. Unfortunately, those cumbersome tools
 *         impose too many constraints on the structure of the project. In order
 *         to be given the possibility to quickly generate releases, we have
 *         developed a small releasing tool for Eclipse that allow us, in one
 *         command line to: - generate the jar (which source or note, depending
 *         on licencing) of a given project - generate the self-contained ZIP
 *         files, directly utilizable by the end-user. - tag the SVN repository
 *         - upload the last version of the software on the website - do all
 *         these operations recursively, within all depending projects.
 * 
 *         Next work include the developement of a minimal Eclipse plugin which
 *         allow the developer to perform a project release through a simple
 *         right-click.
 */

public class eclipse_release extends EprScript
{
	public static void main(String[] args) throws Throwable
	{
		new eclipse_release().run("-w", "/Users/lhogie/dev/src/", "java4unix");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws IOException, SAXException
	{
		Workspace workspace = getWorkspace(cmdLine);
		List<String> args = cmdLine.findArguments();

		String projectName = args.get(0);

		String s = getOptionValue(cmdLine, "-u");
		Level upgradeLevel = s.equals("overwrite") ? null : Level.valueOf(Level.class, s);

		printMessage("Project " + projectName);
		Project project = workspace.findProject(projectName);

		if (project == null)
		{
			printNonFatalError("Project cannot be found: " + projectName);
		}
		else
		{
			project.release(new BuildListener() {

				@Override
				public void fileCreated(RegularFile file)
				{
					printMessage("creating " + file.getPath());
				}

				@Override
				public void uploading(AbstractFile file, String dest)
				{
					printMessage("SCP transfer " + file.getName() + " => " + dest);
				}

				@Override
				public void svnuploading(String dest)
				{
					printMessage("creating tag on SVN at: " + dest);

				}

				@Override
				public void mavenuploading(String dest)
				{
					printMessage("copying to Maven repository at " + dest);
				}

				@Override
				public void message(String msg)
				{
					printMessage(msg);
				}
			}, !isOptionSpecified(cmdLine, "--noupload"), upgradeLevel);
		}

		return 0;
	}

	@Override
	public void declareOptions(Collection<OptionSpecification> specs)
	{
		super.declareOptions(specs);
		specs.add(new OptionSpecification("--nosvn", null, "Do not tag the SVN repository"));
		specs.add(new OptionSpecification("--noupload", null, "Do not touch the website"));
		specs.add(new OptionSpecification("--upgrade-level", "-u", "major|minor|revision|overwrite", null, "upgrade"));
	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification(".+", false, "name of a project"));
	}

	@Override
	public String getShortDescription()
	{
		return getName();
	}

}
