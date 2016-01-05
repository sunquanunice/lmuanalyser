package toools.src.template;

import java.util.ArrayList;
import java.util.List;

public class Instance
{
	private final String sourceType;
	public String getSourceType()
	{
		return sourceType;
	}

	public List<String> getDestinationTypes()
	{
		return destinationTypes;
	}

	private final List<String> destinationTypes = new ArrayList<String>();

	public Instance(String st, String... dt)
	{
		this.sourceType = st;

		for (String d : dt)
		{
			destinationTypes.add(d);
		}
	}
}
