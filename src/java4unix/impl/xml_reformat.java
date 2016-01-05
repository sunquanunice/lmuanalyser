package java4unix.impl;

import java.io.File;
import java.util.Collection;

import toools.io.FileUtilities;
import toools.text.xml.XMLNode;
import toools.text.xml.XMLUtilities;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class xml_reformat extends J4UScript
{

	@Override
	public int runScript(CommandLine cmdLine) throws Throwable
	{

		for (String arg : cmdLine.findArguments())
		{
			File f = new File(arg);
			String xmlText = new String(FileUtilities.getFileContent(f));
			XMLNode root = XMLUtilities.xml2node(xmlText, false);
			FileUtilities.setFileContent(f, root.toString().getBytes());
		}

		return 0;
	}

	public String getShortDescription()
	{

		return "reformat the given XML";
	}

	public void declareOptions(Collection<OptionSpecification> specs)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("file",".+", true, "XML file"));
		
	}
	
}
