package java4unix;

import toools.util.assertion.Assertions;

public class ParameterSpecification
{
    private String regexp;
    private String description;
    
    public ParameterSpecification(String valueRegexp, String description)
    {
        Assertions.ensure(description != null && description.trim().length() > 0, "a description must be provided for all parameters");
        this.regexp = valueRegexp;
        this.description = description;
    }
    
    

    public String getValueRegexp()
    {
        return this.regexp;
    }


    public String getDescription()
    {
        return this.description;
    }
  
}
