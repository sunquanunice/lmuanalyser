package java4unix;


public class Option extends CommandLineElement
{
    private OptionSpecification specs;
    private String value;
    private boolean specified;

    public OptionSpecification getSpecification()
    {
        return this.specs;
    }

    protected void setSpecification(OptionSpecification s)
    {
        if (s == null)
        	throw new NullPointerException();

        this.specs = s;
    }

    public String getValue()
    {
        if (this.value == null)
        {
            return getSpecification().getDefaultValue();
        }
        else
        {
            return this.value;
        }
    }

    protected void setValue(String v) throws InvalidOptionValueException
    {
        if (v == null)
        	throw new NullPointerException();

        if (getSpecification().getValueRegexp() == null)
        {
            throw new InvalidOptionValueException("option '" + getSpecification().getLongName() + "' does not accept any value");
        }
        else
        {
            if (v.matches(getSpecification().getValueRegexp()))
            {
                this.value = v;
            }
            else
            {
                throw new InvalidOptionValueException("value '" + v + "' for option '" + getSpecification().getLongName() + "' does not match '" + getSpecification().getValueRegexp() + "'");
            }
        }
    }

    public String getText()
    {
        if (getValue() == null)
        {
            return getSpecification().getLongName();
        }
        else
        {
            return getSpecification().getLongName() + "=" + getValue();
        }
    }

    public boolean isSpecified()
    {
        return this.specified;
    }

    public void setSpecified(boolean specified)
    {
        this.specified = specified;
    }

}
