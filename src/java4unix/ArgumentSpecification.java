package java4unix;

public class ArgumentSpecification extends ParameterSpecification
{
	private final boolean multiple;
	private final String abbrv;

	public String getAbbrv()
	{
		return abbrv;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public ArgumentSpecification(String regexp, boolean multiple, String description)
	{
		this("arg", regexp, multiple, description);
	}

		
	public ArgumentSpecification(String abbrv, String regexp, boolean multiple, String description)
	{
		super(regexp, description);
		
		if (abbrv == null)
			throw new NullPointerException();
		
		abbrv = abbrv.trim();

		if (abbrv.isEmpty())
			throw new IllegalArgumentException();

		this.abbrv = abbrv;
		this.multiple = multiple;
	}
}
