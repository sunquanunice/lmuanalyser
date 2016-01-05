package java4unix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import toools.io.file.RegularFile;


public class CommandLine
{
    private final List<CommandLineElement> positionalParameters = new ArrayList<CommandLineElement>();

    // this correspond to the $0 in bash scripts
    private RegularFile cmd;

    public List<String> findArguments()
    {
	List<String> args = new ArrayList<String>();

	for (CommandLineElement p : getPositionalParameters())
	{
	    if (!(p instanceof Option))
	    {
		args.add(p.getText());
	    }
	}

	return Collections.unmodifiableList(args);
    }

    public List<Option> findOptions()
    {
	List<Option> options = new ArrayList<Option>();

	for (CommandLineElement p : getPositionalParameters())
	{
	    if (p instanceof Option)
	    {
		options.add((Option) p);
	    }
	}

	return Collections.unmodifiableList(options);
    }

    @Override
    public String toString()
    {

	return getPositionalParameters().toString();
    }

    public Option getOption(OptionSpecification spec)
    {
	if (spec == null)
	    throw new NullPointerException("null option specification");

	for (Option option : findOptions())
	{
	    if (option.getSpecification() == spec)
	    {
		return option;
	    }
	}

	// if the option has not been specified by the user
	Option requestedOption = new Option();
	requestedOption.setSpecification(spec);
	requestedOption.setSpecified(false);
	this.positionalParameters.add(requestedOption);
	return requestedOption;
    }

    public List<CommandLineElement> getPositionalParameters()
    {
	return this.positionalParameters;
    }

    public void setCommand(RegularFile cmd)
    {
	if (!cmd.exists())
	    throw new IllegalArgumentException("script cannot have been invoked via file " + cmd.getPath()
		    + " since it does not exist!");

	this.cmd = cmd;
    }

    public RegularFile getCommand()
    {
	return this.cmd;
    }

    public CommandLine(List<String> args, AbstractShellScript ss) throws UnsupportedOptionException,
	    InvalidOptionValueException
    {
	for (int i = 0; i < args.size(); ++i)
	{
	    String arg = args.get(i);

	    // if this is a specified option that says that the next element is
	    // NOT an option
	    // like in "rm -- -f" if you wqnt to erase a file named "-f"
	    if (arg.equals("--"))
	    {
		// adds the next positional parameter as an argument
		CommandLineElement parm = new CommandLineElement();
		parm.setText(args.get(++i));
		getPositionalParameters().add(parm);
	    }
	    else
	    {
		// this is an option
		if (arg.startsWith("-"))
		{
		    boolean isLong = arg.startsWith("--");
		    int pos = arg.indexOf('=');
		    String name = isLong ? arg.substring(0, pos > 0 ? pos : arg.length()) : arg;
		    OptionSpecification spec = ss.getOptionSpecification(name);

		    // no specifications exist for this parameter, which means
		    // that the option does not exist
		    if (spec == null)
		    {
			throw new UnsupportedOptionException("option \"" + arg
				+ "\" is not supported. Please use \"--help\"");
		    }
		    else
		    {
			Option option = new Option();
			option.setSpecification(spec);
			option.setSpecified(true);

			// if the option requires a value
			if (spec.getValueRegexp() != null)
			{
			    if (isLong)
			    {
				if (pos < 0)
				{
				    throw new InvalidOptionValueException("empty value for option " + name);
				}
				else
				{
				    option.setValue(arg.substring(pos + 1));

				}
			    }
			    else
			    {
				option.setValue(args.get(++i));
			    }
			}

			getPositionalParameters().add(option);
		    }
		}
		// this argument is not an option, it's hence a parameter
		else
		{
		    CommandLineElement parm = new CommandLineElement();
		    parm.setText(arg);
		    getPositionalParameters().add(parm);
		}
	    }
	}

    }

}
