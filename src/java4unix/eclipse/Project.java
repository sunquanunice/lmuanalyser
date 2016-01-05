package java4unix.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.xml.sax.SAXException;

import toools.Clazz;
import toools.Version;
import toools.Version.Level;
import toools.collections.Collections;
import toools.extern.Proces;
import toools.io.FileUtilities;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.FileFilter;
import toools.io.file.RegularFile;
import toools.text.xml.XMLNode;
import toools.text.xml.XMLUtilities;

public class Project
{
	private final Workspace workspace;
	private final Directory directory;
	private final Directory sourceLocation;
	private final Directory classesLocation;
	private final Set<Project> projectDependancies = new HashSet<Project>();
	private final Set<RegularFile> libDependancies = new HashSet<RegularFile>();
	private boolean sourceIsPublic;
	private final String scpDestination;
	private final String svnDestination;
	private final String mavenRepository;
	private Version version;
	private final RegularFile versionFile;
	private final RegularFile changesFile;
	private final Collection<RegularFile> manual = new ArrayList<RegularFile>();

	protected Project(Workspace w, Directory baseDirectory) throws IOException, SAXException
	{
		if (baseDirectory == null)
			throw new NullPointerException("null base directory");

		if (!baseDirectory.exists())
			throw new IllegalArgumentException(baseDirectory.getPath() + " does not exist");

		this.workspace = w;
		this.directory = baseDirectory;
		w.getProjects().add(this);
		System.out.println("scanning " + baseDirectory.getPath());
		if (!getEclipseClasspathFile().exists())
			throw new IllegalArgumentException("no .classpath found at " + baseDirectory.getPath());

		Properties config = new Properties();
		InputStream r = new RegularFile(getDirectory(), ".java4unix").createReadingStream();
		config.load(r);
		this.sourceIsPublic = config.get("source_is_public") == null || config.get("source_is_public").toString().isEmpty() ? false : Boolean
				.valueOf((String) config.get("source_is_public"));
		this.scpDestination = config.get("scp_destination") == null || config.get("scp_destination").toString().isEmpty() ? null : (String) config
				.get("scp_destination");
		this.svnDestination = config.get("svn_repository") == null || config.get("svn_repository").toString().isEmpty() ? null : (String) config
				.get("svn_repository");

		this.mavenRepository = config.get("maven_repository") == null || config.get("maven_repository").toString().isEmpty() ? null : (String) config
				.get("maven_repository");

		for (int i = 1;; ++i)
		{
			String s = (String) config.get("manual" + (i == 1 ? "" : i));

			if (s == null)
			{
				break;
			}
			else
			{
				if (!s.isEmpty())
				{
					this.manual.add(new RegularFile(s));
				}
			}
		}

		r.close();

		String xml = new String(getEclipseClasspathFile().getContent());
		XMLNode root = XMLUtilities.xml2node(xml, false);
		List<XMLNode> nodes = root.findChildren("classpathentry", false);
		this.sourceLocation = new Directory(getDirectory(), nodes.get(0).getAttributes().get("path"));
		changesFile = new RegularFile(sourceLocation, "CHANGES.txt");

		this.classesLocation = new Directory(getDirectory(), nodes.get(nodes.size() - 1).getAttributes().get("path"));

		for (int i = 1; i < nodes.size() - 1; ++i)
		{
			XMLNode node = nodes.get(i);
			String kind = node.getAttributes().get("kind");

			if (kind.equals("src"))
			{
				// remove starting "/"
				String projectName = node.getAttributes().get("path").substring(1);
				Project project = getWorkspace().findProject(projectName);

				if (project == null)
				{
					throw new IllegalStateException("when scanning: " + getName() + " project: " + getName() + ": project not found: " + projectName);
				}

				projectDependancies.add(project);
			}
			else if (kind.equals("lib"))
			{
				String libName = node.getAttributes().get("path");
				RegularFile libFile = new RegularFile(getDirectory(), libName);

				if (!libFile.exists())
				{
					libFile = new RegularFile(getWorkspace().getLocation(), libName);
				}

				if (!libFile.exists())
				{
					libFile = new RegularFile(getDirectory(), libName);
				}

				if (!libFile.exists())
				{
					libFile = new RegularFile(libName);
				}

				if (!libFile.exists())
				{
					throw new IllegalStateException("project: " + getName() + ": file not found " + libFile.getPath());
				}

				libDependancies.add(libFile);
			}
		}

		versionFile = new RegularFile(sourceLocation, getName() + "-version.txt");

		// versionOnDisk = computeCurrentVersionBasedOnMostRecentFile();
		// versionOnDisk = new String(getVersionFile().getContent()).trim();

		// versionOfTheWebFile = new RegularFile(getDirectory(),
		// "version-on-the-web.txt");

		// if (versionOfTheWebFile.exists())
		{

			version = new Version();

			if (versionFile.exists())
			{
				version.set(new String(versionFile.getContent()).trim());
			}
			else
			{
				version.set("0.0.0");
			}
		}
	}

	public Version getVersion()
	{
		return version;
	}

	public boolean isSourceIsPublic()
	{
		return sourceIsPublic;
	}

	public void setSourceIsPublic(boolean sourceIsPublic)
	{
		this.sourceIsPublic = sourceIsPublic;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public Directory getDirectory()
	{
		return directory;
	}

	public RegularFile getVersionFile()
	{
		return versionFile;
	}

	public String getDeclaredVersion()
	{
		RegularFile file = getVersionFile();

		if (file.exists())
		{
			try
			{
				return new String(file.getContent());
			}
			catch (IOException e)
			{
				throw new IllegalStateException();
			}
		}
		else
		{
			return null;
		}
	}

	public String getName()
	{
		return directory.getName();
	}

	public Directory getSourceDirectory()
	{
		return this.sourceLocation;
	}

	public Directory getClassesDirectory()
	{
		return this.classesLocation;
	}

	public Collection<Project> getDirectProjectDependancies()
	{
		return projectDependancies;
	}

	public Set<Project> computeIndirectProjectDependancies()
	{
		Set<Project> r = new HashSet<Project>();
		List<Project> queue = new ArrayList<Project>();
		queue.addAll(getDirectProjectDependancies());
		Set<Project> visitedProject = new HashSet<Project>();

		while (!queue.isEmpty())
		{
			Project thisProject = queue.remove(0);

			if (!visitedProject.contains(thisProject))
			{
				visitedProject.add(thisProject);
				r.add(thisProject);
				queue.addAll(thisProject.getDirectProjectDependancies());
			}
		}

		return Collections.difference(r, getDirectProjectDependancies());
	}

	public Set<RegularFile> computeIndirectLibDependancies()
	{
		Set<RegularFile> r = new HashSet<RegularFile>();

		for (Project p : getDirectAndIndirectProjectDependancies())
		{
			r.addAll(p.getDirectLibDependancies());
		}

		return Collections.difference(r, getDirectLibDependancies());
	}

	public Set<RegularFile> getDirectLibDependancies()
	{
		return libDependancies;
	}

	public Workspace getWorkspace()
	{
		return this.workspace;
	}

	public Directory getBuildDirectory()
	{
		Directory projectBuildDir = new Directory(getDirectory(), "build");

		if (!projectBuildDir.exists())
		{
			projectBuildDir.mkdirs();
		}

		return projectBuildDir;
	}

	public RegularFile getEclipseClasspathFile()
	{
		return new RegularFile(getDirectory(), ".classpath");
	}

	public RegularFile getJAR()
	{
		return new RegularFile(getBuildDirectory(), getName() + "-" + getVersion() + ".jar");
	}

	public RegularFile getSourceZIPFile()
	{
		return new RegularFile(getBuildDirectory(), getName() + "-" + getVersion() + ".src.zip");
	}

	public void createJARFile() throws IOException
	{

		Collection<RegularFile> tempSourceFiles = new ArrayList<RegularFile>();

		if (sourceIsPublic)
		{
			for (RegularFile javaFile : (Collection<RegularFile>) (Collection) getSourceDirectory().retrieveTree(
					new FileFilter.RegexFilter(".*\\.java$"), null))
			{
				if (!javaFile.exists())
					throw new IllegalStateException();

				String relativeName = javaFile.getNameRelativeTo(getSourceDirectory());
				RegularFile dest = new RegularFile(getClassesDirectory(), relativeName);
				tempSourceFiles.add(dest);
				javaFile.copyTo(dest, true);
			}
		}
		else
		{
			for (RegularFile javaFile : (Collection<RegularFile>) (Collection) getClassesDirectory().retrieveTree(
					new FileFilter.RegexFilter(".*\\.java$"), null))
			{
				javaFile.delete();
			}
		}

		getVersionFile().setContentAsUTF8(getVersion().toString());

		getVersionFile().copyTo(new RegularFile(getClassesDirectory(), getVersionFile().getName()), true);
		FileUtilities.zip(getJAR(), getClassesDirectory(), null);

		for (RegularFile tmpFile : tempSourceFiles)
		{
			tmpFile.delete();
		}
	}

	public void createSourceArchive() throws IOException
	{
		getVersionFile().setContent(getVersion().toString().getBytes());

		FileUtilities.zip(getSourceZIPFile(), getSourceDirectory(), new FileFilter() {
			@Override
			public boolean accept(AbstractFile s)
			{
				return !(s instanceof Directory) && !s.getPath().contains(".svn") && !s.getName().endsWith(".class");
			}
		});

		getSourceZIPFile();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Project && ((Project) obj).getDirectory().equals(getDirectory());
	}

	@Override
	public int hashCode()
	{
		return directory.hashCode();
	}

	public Collection<RegularFile> getJavaFiles()
	{
		return (Collection<RegularFile>) (Collection) getSourceDirectory().retrieveTree(new FileFilter() {
			@Override
			public boolean accept(AbstractFile pathname)
			{
				return pathname.getName().endsWith(".java");
			}
		}, null);
	}

	public Collection<RegularFile> getClassFiles()
	{
		return (Collection<RegularFile>) (Collection) getClassesDirectory().retrieveTree(new FileFilter() {
			@Override
			public boolean accept(AbstractFile pathname)
			{
				return pathname.getName().endsWith(".class");
			}
		}, null);
	}

	public Collection<RegularFile> getSourceFiles()
	{
		return (Collection<RegularFile>) (Collection) getSourceDirectory().retrieveTree(new FileFilter() {

			@Override
			public boolean accept(AbstractFile s)
			{
				return !(s instanceof Directory) && !s.getPath().contains(".svn");
			}
		}, null);
	}

	public Collection<RegularFile> getResourceFiles()
	{
		return Collections.difference(getSourceFiles(), getJavaFiles(), getClassFiles());
	}

	public int getNumberOfSourceLines()
	{
		int n = 0;

		for (RegularFile f : getJavaFiles())
		{
			try
			{
				for (String line : Arrays.asList(new String(f.getContent()).split("\n")))
				{
					if (!line.trim().isEmpty())
					{
						++n;
					}
				}
			}
			catch (IOException ex)
			{
				throw new IllegalStateException("IO error");
			}
		}

		return n;
	}

	public Set<Project> getDirectAndIndirectProjectDependancies()
	{
		return Collections.union(getDirectProjectDependancies(), computeIndirectProjectDependancies());
	}

	public Set<RegularFile> getDirectAndIndirectLibDependancies()
	{
		return Collections.union(getDirectLibDependancies(), computeIndirectLibDependancies());
	}

	public int computeNumberOfLinesAlsoInProjectDependancies()
	{
		int n = 0;

		for (Project p : getDirectProjectDependancies())
		{
			n += p.getNumberOfSourceLines();
		}

		return n + getNumberOfSourceLines();
	}

	public void createBigJar(BuildListener listener) throws IOException
	{
		Map<String, byte[]> map = new HashMap<String, byte[]>();

		for (Project thisProject : getDirectAndIndirectProjectDependancies())
		{
			//if (!thisProject.getJAR().exists())
			{
				thisProject.createJARFile();
			}

			System.out.println("putall " + thisProject.getJAR());
			map.putAll(FileUtilities.unzip(thisProject.getJAR()));
		}

		for (RegularFile lib : getDirectAndIndirectLibDependancies())
		{
			System.out.println("putall " + lib);
			map.putAll(FileUtilities.unzip(lib));
		}

		if (!getJAR().exists())
		{
			createJARFile();
		}

		System.out.println("putall " + getJAR());
		map.putAll(FileUtilities.unzip(getJAR()));

		// removing all META-INF information
		Iterator<Entry<String, byte[]>> i = map.entrySet().iterator();

		while (i.hasNext())
		{
			if (i.next().getKey().startsWith("META-INF/"))
			{
				i.remove();
			}
		}

		RegularFile bigzip = getBigJAR();
		FileUtilities.zip(bigzip, map);
		listener.fileCreated(bigzip);
	}

	public RegularFile getBigJAR()
	{
		return new RegularFile(Directory.getSystemTempDirectory(), getName() + "-big-" + getVersion() + ".jar");
	}

	public String getSCPDestination()
	{
		return this.scpDestination;
	}

	public String getSVNDestination()
	{
		return this.svnDestination;
	}

	public void release(BuildListener l, boolean putOnTheWeb, Level upgrade) throws IOException, SAXException
	{
		if (upgrade != null)
		{
			version.upgrade(upgrade);
		}
		System.out.println(getBigJAR());

		l.message("creating javadoc");
		createJavadoc();
		if (!getBigJAR().exists())
		{
			l.message("creating big jar");
			createBigJar(l);
		}

		if (!putOnTheWeb)
			return;

		if (svnDestination != null)
		{
			String src = getSVNDestination() + "/trunk/";
			String dest = getSVNDestination() + "/tags/" + getName() + "-" + getVersion();
			l.svnuploading(dest);

			Proces.exec("svn", "copy", "-rHEAD", src, dest, "--message", "\"This is release " + getVersion() + "\"");
		}

		if (scpDestination == null)
		{
			l.message("missing scp_destination");
		}
		else if (mavenRepository == null)
		{
			l.message("missing maven repository");
		}
		else
		{
			// uploads the big jar
			{
				l.uploading(getBigJAR(), getSCPDestination());
				Proces.exec("rsync", getBigJAR().getPath(), getSCPDestination() + "/releases/" + getJAR().getName());
			}

			// uploads manuals, if any
			{
				for (RegularFile f : getManualFiles())
				{
					l.uploading(f, getSCPDestination());
					Proces.exec("rsync", f.getPath(), getSCPDestination() + "/" + f.getName());
				}
			}

			// uploads javadoc
			{
				l.message("uploading javadoc");
				l.uploading(getJavadocDirectory(), getSCPDestination());
				Proces.exec("rsync", "-a", getJavadocDirectory().getPath() + "/", getSCPDestination() + "/javadoc");
			}

			// update maven repository
			{
				l.mavenuploading(mavenRepository);
				String mavenServer = mavenRepository.replaceAll(":.*", "");
				String repositoryDir = mavenRepository.replaceAll(".*:", "");
				String dir = repositoryDir + "/" + getName() + "/" + getName() + "/" + version;

				// create the directory on the maven repository
				Proces.exec("ssh", mavenServer, "mkdir", "-p", dir);

				String xml = "<project>\n\t<modelVersion>4.0.0</modelVersion>\n\t<groupId>" + getName() + "</groupId>\n\t<artifactId>" + getName()
						+ "</artifactId>\n\t<version>" + version + "</version>\n</project>";

				// creates the pom file in the repository
				Proces.exec("ssh", xml.getBytes(), mavenServer, "cat", ">", dir + "/" + getName() + "-" + version + ".pom");

				// uploads the big JAR
				Proces.exec("scp", getBigJAR().getPath(), mavenServer + ":" + dir + "/" + getName() + "-" + version + ".jar");
			}

			{
				String scpServer = scpDestination.replaceAll(":.*", "");
				String scpDir = scpDestination.replaceAll(".*:", "");

				// maven users will update patch correcting releases
				Proces.exec("ssh", version.toString().replaceFirst("\\.[0-9]*$", "").trim().getBytes(), scpServer, "cat", ">" + scpDir
						+ "/releases/maven-public-version.txt");
			}

			// timestamps the "CHANGES" file
			Calendar rightNow = Calendar.getInstance();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
			String formattedDate = df.format(rightNow.getTime());
			changesFile.append(("\n\nVersion " + getVersion() + "\treleased on " + formattedDate + "\n\n").getBytes());

			l.uploading(changesFile, getSCPDestination());
			Proces.exec("scp", changesFile.getPath(), getSCPDestination());

			if (putOnTheWeb)
			{
				versionFile.setContent(version.toString().getBytes());
			}

			// uploads last version information
			l.uploading(getVersionFile(), getSCPDestination());
			Proces.exec("scp", getVersionFile().getPath(), getSCPDestination() + "/releases/last-version.txt");
}
	}

	public Directory getJavadocDirectory()
	{
		return new Directory(getDirectory(), "javadoc");
	}

	public List<Class<?>> getClasses()
	{
		List<Class<?>> l = new ArrayList<Class<?>>();

		for (RegularFile sf : (Collection<RegularFile>) (Collection) getSourceDirectory().retrieveTree(new FileFilter.RegexFilter("\\.java$"), null))
		{
			String classname = sf.getName().substring(getSourceDirectory().getPath().length() + 1).replace('\\', '.');
			l.add(Clazz.findClass(classname));
		}

		return l;
	}

	public void createJavadoc()
	{
		Proces.exec("bash", "-c", "javadoc -d " + getJavadocDirectory().getPath() + " $(find " + getSourceDirectory().getPath()
				+ "  -type f -name '*.java')");
	}

	private Collection<RegularFile> getManualFiles()
	{
		return this.manual;
	}

	public int computeNumberOfFileAlsoInProjectDependancies()
	{
		int n = 0;

		for (Project p : getDirectProjectDependancies())
		{
			n += p.getJavaFiles().size();
		}

		return n + getJavaFiles().size();
	}

	public String getMavenRepository()
	{
		return mavenRepository;
	}

}
