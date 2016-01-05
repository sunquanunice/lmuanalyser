package java4unix.eclipse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;

import toools.io.file.Directory;
import toools.io.file.RegularFile;

public class Workspace
{
	private final Directory location;
	private final Set<Project> projects = new HashSet<Project>();

	public static boolean isWorkspace(Directory directory)
	{
		return new Directory(directory, ".metadata").exists();
	}

	public Workspace(Directory workspaceDirectory) throws IOException,
			SAXException
	{
		if (!isWorkspace(workspaceDirectory))
			throw new IllegalArgumentException(
					"directory does not seem to be a valid Eclipse workspace: "
							+ workspaceDirectory.getPath());

		this.location = workspaceDirectory;

		if (!getEclipseMetaDataDirectory().exists())
			throw new IllegalArgumentException(
					"are you sure this is a valid workspace? no .metadata was found in it");
		scanProjects();
	}

	public void scanProjects() throws IOException, SAXException
	{
		this.projects.clear();

		for (Directory projectDir : getLocation().listDirectories())
		{
			if (new RegularFile(projectDir, ".project").exists()
					&& new RegularFile(projectDir, ".java4unix").exists()
					&& new RegularFile(projectDir, ".classpath").exists())
			{
				this.projects.add(new Project(this, projectDir));
			}
		}
	}

	private Directory getEclipseMetaDataDirectory()
	{
		return new Directory(getLocation(), ".metadata");
	}

	public Directory getLocation()
	{
		return this.location;
	}

	public Set<Project> getProjects()
	{
		return projects;
	}

	public Project findProject(String projectName) throws IOException,
			SAXException
	{
		for (Project p : getProjects())
		{
			if (p.getName().equals(projectName))
			{
				return p;
			}
		}

		Project p = new Project(this, new Directory(location, projectName));
		projects.add(p);
		return p;
	}

}
