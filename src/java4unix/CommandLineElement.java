package java4unix;

import toools.util.assertion.Assertions;

public class CommandLineElement
{
    private String text;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        Assertions.ensureArgumentIsNotNull(text);
        this.text = text;
    }

    public boolean isSpecified()
    {
        return true;
    }

    @Override
	public String toString()
	{

		return getText();
	}
}
