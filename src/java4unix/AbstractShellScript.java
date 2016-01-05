package java4unix;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import toools.ClassContainer;
import toools.ClassPath;
import toools.Clazz;
import toools.ExceptionUtilities;
import toools.config.Configuration;
import toools.gui.Utilities;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.src.Source;
import toools.text.TextUtilities;
import toools.util.assertion.Assertions;

public abstract class AbstractShellScript extends Application
{
	private double verbosity = 0.5;
	private Collection<OptionSpecification> optionSpecifications = new ArrayList<OptionSpecification>();
	private List<ArgumentSpecification> argumentSpecifications = new ArrayList<ArgumentSpecification>();
	private Configuration configuration;
	private Directory baseDirectory = new Directory(Directory.getHomeDirectory(), '.' + getApplicationName());
	private Directory installationDirectory;

	public Directory getInstallationDirectory()
	{
		return installationDirectory;
	}

	public void setInstallationDirectory(Directory installationDirectory)
	{
		if (installationDirectory == null)
			throw new NullPointerException();

		if (!installationDirectory.exists())
			throw new IllegalArgumentException("not found: " + installationDirectory);

		this.installationDirectory = installationDirectory;
	}

	public void setBaseDirectory(Directory baseDirectory)
	{
		this.baseDirectory = baseDirectory;
	}

	public AbstractShellScript()
	{
		this.optionSpecifications.add(new OptionSpecification("--help", "-h", null, null, "print the help message"));
		this.optionSpecifications.add(new OptionSpecification("--longhelp", null, null, null, "print the help message, showing additional utility options"));
		this.optionSpecifications.add(new OptionSpecification("--version", null, null, null, "print version and copyright information"));
		this.optionSpecifications.add(new OptionSpecification("--verbosity", null, "(0\\.[0-9]+)|1", "0.5",
				"set the verbosity level, which must be comprised between 0 and 1"));
		this.optionSpecifications.add(new OptionSpecification("--Xgui", null, null, null, "use a window for user interaction, instead of stdin/stdout/stderr"));
		this.optionSpecifications.add(new OptionSpecification("--Xprint-configuration", null, null, null, "print configuration file as is"));
		this.optionSpecifications.add(new OptionSpecification("--Xgenerate-default-configuration", null, null, null, "generates a default configuration file"));
		this.optionSpecifications.add(new OptionSpecification("--Xprint-source", null, null, null, "print the script source code, if available"));
		this.optionSpecifications.add(new OptionSpecification("--Xprint-classpath", null, null, null, "print the classpath"));
		this.optionSpecifications.add(new OptionSpecification("--Xprint-source-information", null, null, null, "print information about the script code"));
		this.optionSpecifications.add(new OptionSpecification("--Xdebug", null, null, null, "print the full java stack when an error occurs"));
		declareOptions(this.optionSpecifications);
		declareArguments(this.argumentSpecifications);
		checkProgrammerData();
	}

	public Unix getUnix()
	{
		return Unix.getLocalUnix();
	}

	public final String getUsage()
	{
		String s = getName() + " [OPTIONS]";

		for (ArgumentSpecification as : this.argumentSpecifications)
		{
			s += ' ' + as.getAbbrv();
		}

		if (!argumentSpecifications.isEmpty())
		{
			s += "\n\nWhere:";

			for (ArgumentSpecification as : this.argumentSpecifications)
			{
				s += "\n\t" + as.getAbbrv() + '\t' + as.getDescription();
			}
		}

		return s;
	}

	synchronized protected final boolean isOptionSpecified(CommandLine cmdLine, String optionName)
	{
		return cmdLine.getOption(getOptionSpecification(optionName)).isSpecified();
	}

	protected final String getOptionValue(CommandLine cmdLine, String optionName)
	{
		OptionSpecification spec = getOptionSpecification(optionName);

		if (spec == null)
			throw new IllegalArgumentException("option does not exist: " + optionName);

		return cmdLine.getOption(spec).getValue();
	}

	/**
	 * Computes the name of the script out of the name of the class For example,
	 * "lucci.cmdline.MyScript" becomes "MyScript".
	 * 
	 * @return
	 */
	public final String getName()
	{
		String s = getClass().getName();
		s = s.substring(s.lastIndexOf('.') + 1);
		s = getApplicationName() + '-' + s;
		s = s.replace('_', '-');
		s = s.toLowerCase();
		return s;
	}

	public final String getHelp(boolean printUtilityOptions)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Usage: ");
		buf.append(getUsage());
		buf.append("\n\nDescription:\n\t");
		buf.append(getShortDescription());
		buf.append("\n\nOptions:");

		for (OptionSpecification spec : getOptionSpecifications())
		{
			if (!spec.getLongName().startsWith("--X") || (spec.getLongName().startsWith("--X") && printUtilityOptions))
			{
				buf.append("\n\t");
				buf.append(spec.getLongName());

				if (spec.getValueRegexp() != null)
				{
					buf.append("=" + spec.getValueRegexp());
				}

				if (spec.getShortName() != null)
				{
					buf.append("\n\t ");
					buf.append(spec.getShortName());

					if (spec.getValueRegexp() != null)
					{
						buf.append(" " + spec.getValueRegexp());
					}
				}

				buf.append("\n\t\t");
				buf.append(spec.getDescription());

				if (spec.getDefaultValue() != null)
				{
					buf.append("\n\t\tDefault value is \"");
					buf.append(spec.getDefaultValue());
					buf.append("\".");
				}
			}
		}

		buf.append("\n\n");

		buf.append("Note: arguments are ordered, options are not.\n");

		if (getActualConfigurationFile() != null && getActualConfigurationFile().exists())
		{
			buf.append("Configuration file: " + getActualConfigurationFile().getPath() + " (" + getActualConfigurationFile().getSize() + " bytes)");
		}

		return buf.toString().replace("\t", "    ");
	}

	private List<ArgumentSpecification> getArgumentSpecifications()
	{
		return argumentSpecifications;
	}

	/**
	 * This is an utility method used by executable classes to run a script
	 * without having to go to the command line. It is NOT used in the project.
	 */
	public final int run(String... args) throws Throwable
	{
		return run(null, Arrays.asList(args));
	}

	public final int run(RegularFile cmd, List<String> args) throws Throwable
	{
		CommandLine cmdLine = new CommandLine(args, this);

		if (cmd != null)
		{
			cmdLine.setCommand(cmd);
			setInstallationDirectory(cmd.getParent());
			readConfiguration(cmdLine);
		}

		int errorCode = 0;

		if (cmdLine.getOption(getOptionSpecification("--Xgui")).isSpecified())
		{
			this.frame = new JFrame(getName());
			this.textArea = new JTextArea();
			this.textArea.setEditable(false);
			this.frame.setContentPane(new JScrollPane(this.textArea));
			this.frame.setSize(800, 600);
			this.frame.setVisible(true);
		}

		if (cmdLine.getOption(getOptionSpecification("--help")).isSpecified())
		{
			printMessage(getHelp(false));
		}
		else if (cmdLine.getOption(getOptionSpecification("--longhelp")).isSpecified())
		{
			printMessage(getHelp(true));
		}
		else if (cmdLine.getOption(getOptionSpecification("--version")).isSpecified())
		{
			printMessage(getName() + " v" + getVersion());
			printMessage("Copyright(C) " + getYear() + " " + getAuthor());
			printMessage("License: " + getLicence().getName());
		}
		else if (cmdLine.getOption(getOptionSpecification("--Xprint-configuration")).isSpecified())
		{
			RegularFile file = getActualConfigurationFile();

			if (file.exists())
			{
				try
				{
					printMessage(file.getPath() + ":\n" + new String(file.getContent()));
				}
				catch (IOException e)
				{
					printFatalError("Cannot read configuration file " + file.getPath());
				}
			}
			else
			{
				printMessage("Configuration file " + file.getPath() + " does not exist");
			}
		}
		else if (cmdLine.getOption(getOptionSpecification("--Xgenerate-default-configuration")).isSpecified())
		{
			if (!getUserConfigurationFile().exists())
			{
				String s = "";

				for (OptionSpecification os : getOptionSpecifications())
				{
					if (os.getDefaultValue() != null)
					{
						s += "# " + os.getDescription() + "\n";
						s += "# default value is: " + os.getDefaultValue() + "\n";
						s += os.getLongName().substring(2) + "=" + os.getDefaultValue() + "\n\n";
					}
				}

				getUserConfigurationFile().setContent(s.getBytes());
				printMessage("Configuration file created: " + getUserConfigurationFile().getPath());
			}
			else
			{
				printWarning("configuration file " + getUserConfigurationFile().getPath() + " already exists.");
			}
		}
		else if (cmdLine.getOption(getOptionSpecification("--Xprint-source")).isSpecified())
		{
			String text = Source.getClassSourceCode(getClass());

			if (text == null)
			{
				printWarning("the source for class " + getClass().getName() + " is not available");
			}
			else
			{
				printMessage(text);
			}
		}
		else if (cmdLine.getOption(getOptionSpecification("--Xprint-classpath")).isSpecified())
		{
			for (ClassContainer f : ClassPath.retrieveClassPath())
			{
				printMessage(TextUtilities.flushLeft(f.getFile().getPath() + " ", 70, '.') + " " + f.getFile().getSize() + " bytes");
			}
		}
		else if (cmdLine.getOption(getOptionSpecification("--Xprint-source-information")).isSpecified())
		{
			printMessage("Script implementation is in java class : " + getClass().getName());
			ClassPath urls = Clazz.findClassContainer(getClass().getName(), ClassPath.retrieveClassPath());

			if (!urls.isEmpty())
			{
				printMessage("This class is in directory: " + urls.get(0).getFile().getPath());
			}
		}
		else
		{
			ensureMandatoryStuffAreSpecified(cmdLine);
			setVerbosity(Double.valueOf(cmdLine.getOption(getOptionSpecification("--verbosity")).getValue()));

			errorCode = runScript(cmdLine);
		}

		if (this.frame != null)
		{
			this.frame.setTitle("Terminated - " + this.frame.getTitle());
			final Thread thread = Thread.currentThread();
			this.frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e)
				{
					thread.resume();
				}
			});
			thread.suspend();
			this.frame.dispose();
		}

		return errorCode;
	}

	private boolean isRunAsRoot()
	{
		return getUser().getName().equals("root");
	}

	private User getUser()
	{
		return User.getUserByName(System.getProperty("user.name"));
	}

	private void readConfiguration(CommandLine cmdLine) throws InvalidOptionValueException, UnsupportedOptionException, IOException
	{
		Configuration config = loadConfiguration();

		for (String k : config.getKeys())
		{
			OptionSpecification os = getOptionSpecification("--" + k);

			if (os == null)
			{
				throw new UnsupportedOptionException("invalid option in config file: " + k);
			}
			else
			{
				Option option = cmdLine.getOption(os);

				if (!option.isSpecified())
				{
					option.setSpecified(true);

					// if the option requires a value
					if (os.getValueRegexp() != null)
					{
						option.setValue(config.getValue(k));
					}
				}
			}
		}

	}

	abstract protected void declareOptions(Collection<OptionSpecification> optionSpecifications);

	abstract protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications);

	public final Directory getBinDirectory()
	{
		return new Directory(getInstallationDirectory(), "bin");
	}

	public final Directory getJarsDirectory()
	{
		return new Directory(getInstallationDirectory(), "lib");
	}

	public final Directory getBaseDirectory()
	{
		return this.baseDirectory;
	}

	public final RegularFile getExecutableScript()
	{
		return new RegularFile(getBinDirectory(), getName());
	}

	public final void installScript() throws IOException
	{
		RegularFile executable = getExecutableScript();

		if (!executable.getParent().exists())
		{
			executable.getParent().mkdirs();
		}

		executable.setContent(getScriptText().getBytes());
		Posix.chmod(executable, "+x");
	}

	public String getScriptText()
	{
		String t = "";
		t += "#!/bin/sh\n";
		t += "\n# this script was generated by Java4unix.\n\n";
		t += "export CLASSPATH=\"$JAVA4UNIX_PRIORITARY_PATH" + System.getProperty("path.separator") + "$(find \"" + this.baseDirectory.getPath()
				+ "\" -name '*.jar' | tr '\\n' '" + System.getProperty("path.separator") + "')\"\n";
		t += "java `cat $0.jvmargs 2>/dev/null` " + getVMOptionsAsText() + " " + RunScript.class.getName() + " " + getClass().getName() + " $0 \"$@\"";
		return t;
	}

	protected final String getVMOptionsAsText()
	{
		return TextUtilities.concatene(getVMOptions(), " ");
	}

	public abstract int runScript(CommandLine cmdLine) throws Throwable;

	private void checkProgrammerData()
	{
		Assertions.ensure(getAuthor() != null && !getAuthor().trim().isEmpty(), "author not set: " + getAuthor());
		Assertions.ensure(getShortDescription() != null && !getShortDescription().trim().isEmpty(), "description not set: " + getShortDescription());
		Assertions.ensure(getVersion() != null && !getVersion().trim().isEmpty(), "invalid version: " + getVersion());
		Assertions.ensure(getYear() != null && !getYear().trim().isEmpty(), "invalid date: " + getYear());
		Assertions.ensure(getLicence() != null, "licence not set: " + getLicence());
		Assertions.ensure(getApplicationName() != null, "application name not set");

		Collection<String> options = new HashSet<String>();

		for (OptionSpecification spec : getOptionSpecifications())
		{
			if (spec.getLongName() != null)
			{
				Assertions.ensure(!options.contains(spec.getLongName()), spec.getLongName() + " is already used");
				options.add(spec.getLongName());
			}

			if (spec.getShortName() != null)
			{
				Assertions.ensure(!options.contains(spec.getShortName()), spec.getShortName() + " is already used");
				options.add(spec.getShortName());
			}
		}
	}

	public final void ensureMandatoryStuffAreSpecified(CommandLine cmdLine) throws InvalidOptionValueException
	{
		for (OptionSpecification spec : getOptionSpecifications())
		{
			// the option waits a value and no default value if available
			if (spec.getValueRegexp() != null && spec.getDefaultValue() == null)
			{
				Option o = cmdLine.getOption(spec);

				if (!o.isSpecified())
				{
					throw new InvalidOptionValueException("option " + spec.getLongName() + " must be specified with value matching " + spec.getValueRegexp());
				}
				else if (o.getValue() == null)
				{
					throw new InvalidOptionValueException("option " + spec.getLongName() + " must be specified a value matching " + spec.getValueRegexp());
				}
			}
		}
	}

	public final OptionSpecification getOptionSpecification(String name)
	{
		for (OptionSpecification thisOptionSpecification : getOptionSpecifications())
		{
			if (thisOptionSpecification.getLongName().equals(name)
					|| (thisOptionSpecification.getShortName() != null && thisOptionSpecification.getShortName().equals(name)))
			{
				return thisOptionSpecification;
			}
		}

		throw new IllegalArgumentException("non existing option: " + name);
	}

	private JFrame frame;

	private JTextArea textArea;

	protected void printMessage(PrintStream os, Object msg, double importance, boolean addnewline)
	{
		if (!(0 <= importance && importance <= 1))
			throw new IllegalArgumentException("message importance should be comprised between 0 and 1.");

		// if the msg is important enough to be printed
		if (importance >= 1 - getVerbosity())
		{
			if (frame == null)
			{
				os.print(msg + (addnewline ? "\n" : ""));
			}
			else
			{
				this.textArea.append(msg + (addnewline ? "\n" : ""));
				this.textArea.setCaretPosition(textArea.getText().length());
			}
		}
	}

	private String lastcancellablePrint;

	protected void cancelLastPrint()
	{
		if (lastcancellablePrint == null)
		{
			printWarning("Messaging error: unable to cancel last print (none found)");
		}
		else
		{
			printMessageWithNoNewLine(TextUtilities.repeat(String.valueOf((char) 0x08), lastcancellablePrint.length()));
		}
	}

	protected final void printMessageWithNoNewLine(Object msg)
	{
		printMessage(System.out, msg, 0.5, false);
		this.lastcancellablePrint = msg.toString();
	}

	protected final void printMessage(PrintStream os, Object msg)
	{
		printMessage(os, msg, 0.5, true);
	}

	protected final void printMessage(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.out, m == null ? m : m.toString(), 0.5, true);
		}
	}

	protected final void printWarning(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Warning: " + m, 0.75, true);
		}
	}

	protected final void printDebugMessage(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.out, "#Debug: " + m, 0.75, true);

		}
	}

	protected final void printNonFatalError(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Error: " + m, 0.8, true);
		}
	}

	protected final void printFatalError(Object... msg)
	{
		for (Object m : msg)
		{
			printMessage(System.err, "Fatal error: " + m, 1, true);
		}
	}

	protected final void printExceptionStrackTrace(Exception e)
	{
		printMessage(System.err, "Java Exception: " + ExceptionUtilities.toString(e), 1, true);
	}

	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	protected final String readUserInput(String invitation, String regexp)
	{
		if (this.frame == null)
		{
			try
			{
				String line = null;

				while (true)
				{
					if (invitation != null)
					{
						printMessageWithNoNewLine(invitation);
					}

					line = stdin.readLine();

					if (regexp == null || line == null || line.matches(regexp))
					{
						return line;
					}
					else
					{
						printNonFatalError("Input error, the string must match " + regexp + "\n");
					}
				}
			}
			catch (IOException e)
			{
				printFatalError("Error while reading user input");
				return null;
			}
		}
		else
		{
			final String[] currentLine = new String[1];
			final Thread thread = Thread.currentThread();
			KeyListener listener = new KeyAdapter() {
				public void keyTyped(KeyEvent e)
				{
					if (e.getKeyChar() == '\n')
					{
						textArea.setCaretPosition(textArea.getCaretPosition() - 1);
						currentLine[0] = Utilities.getCurrentLine(textArea);
						thread.resume();
					}
				}

			};

			this.textArea.addKeyListener(listener);
			this.textArea.setEditable(true);
			thread.suspend();
			this.textArea.setEditable(false);
			this.textArea.removeKeyListener(listener);
			return currentLine[0];
		}
	}

	public final double getVerbosity()
	{
		return verbosity;
	}

	public final void setVerbosity(double verbosity)
	{
		Assertions.ensure(0 <= verbosity && verbosity <= 1, "script verbosity should be comprised between 0 and 1.");
		this.verbosity = verbosity;
	}

	public final Collection<OptionSpecification> getOptionSpecifications()
	{
		return optionSpecifications;
	}

	public final Directory getHomeDirectory()
	{
		return Directory.getHomeDirectory();
	}

	public final int select(List<String> list)
	{
		int n = -1;

		while (n < 0 || n >= list.size())
		{
			printMessage("\n");

			for (int i = 0; i < list.size(); ++i)
			{
				printMessage((i + 1) + ") " + list.get(i));
			}

			printMessage("\n");
			n = Integer.parseInt(readUserInput("Your choice: ", "[0-9]+")) - 1;
		}

		return n;
	}

	public final Directory getDataDirectory()
	{
		return new Directory(getBaseDirectory(), "data");
	}

	public final RegularFile getDefaultConfigurationFile()
	{
		// if the application is not run via a shell script
		// there is no installation directory
		if (getInstallationDirectory() == null)
		{
			return null;
		}
		else
		{
			return new RegularFile(getInstallationDirectory(), getName() + ".conf");
		}
	}

	public final RegularFile getUserConfigurationFile()
	{
		return new RegularFile(getBaseDirectory(), getName() + ".conf");
	}

	public final RegularFile getActualConfigurationFile()
	{
		RegularFile userConfigFile = getUserConfigurationFile();

		if (userConfigFile != null && userConfigFile.exists())
		{
			return userConfigFile;
		}
		else
		{
			RegularFile defaultConfigFile = getDefaultConfigurationFile();
			return defaultConfigFile != null && defaultConfigFile.exists() ? defaultConfigFile : userConfigFile;
		}
	}

	public RegularFile getDataFile(String name)
	{
		RegularFile f = new RegularFile(getDataDirectory(), name);

		if (!f.getParent().exists())
			f.getParent().mkdirs();

		return f;
	}

	public String getLastPrint()
	{
		return lastcancellablePrint;
	}

	private Configuration loadConfiguration() throws IOException
	{
		RegularFile configFile = getActualConfigurationFile();

		if (configFile.exists())
		{
			return Configuration.readFromFile(getActualConfigurationFile());
		}
		else
		{
			return new Configuration();
		}

	}

	public void saveConfiguration(String key, String value) throws IOException
	{
		Configuration config = loadConfiguration();
		config.remove(key);
		config.add(key, value);

		RegularFile configFile = getUserConfigurationFile();

		if (!configFile.getParent().exists())
		{
			configFile.getParent().mkdirs();
		}

		configuration.saveToFile(configFile);
	}

}
