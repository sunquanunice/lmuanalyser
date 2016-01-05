package java4unix.impl;

import java.util.Collection;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

import toools.ClassContainer;
import toools.ClassPath;
import toools.UnitTests;


public class unit_tests extends J4UScript
{
 
    @Override
    protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
    {
	// TODO Auto-generated method stub

    }

    @Override
    public int runScript(CommandLine cmdLine) throws Throwable
    {
	for (String s : cmdLine.findArguments())
	{
	    for (ClassContainer cc : ClassPath.retrieveClassPath().getContainer(s))
	    {
		UnitTests.test(cc);
	    }
	}
	
	return 0;
    }

    @Override
    public String getShortDescription()
    {
	return "Runs the unit tests for Grph";
    }

    @Override
    protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
    {
    }

 
}
