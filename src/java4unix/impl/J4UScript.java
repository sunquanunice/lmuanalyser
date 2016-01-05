package java4unix.impl;

import java4unix.AbstractShellScript;
import java4unix.License;

public abstract class J4UScript extends AbstractShellScript
{
	@Override
	public final String getAuthor()
	{
		return "Luc Hogie, Aur�lien Lancin, Benoit Ferrero (CNRS/INRIA/Universt� de Nice Sophia Antipolis)";
	}

	@Override
	public final String getApplicationName()
	{
		return "java4unix";
	}

	@Override
	public final License getLicence()
	{
		return License.GPL;
	}

	@Override
	public final String getYear()
	{
		return "2008-2011";
	}

}
