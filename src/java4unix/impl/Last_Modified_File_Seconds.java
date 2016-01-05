package java4unix.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;



public class Last_Modified_File_Seconds extends J4UScript
{


	@Override
	public String getShortDescription()
	{
		// TODO Auto-generated method stub
		return "Comptes the date of the more recent file.";
	}
    public int runScript(CommandLine cmdLine)
    {
        File lastModified = null;
        String line;

        while ((line = readUserInput(null, ".*")) != null)
        {
            File f = new File(line);

            if (lastModified == null)
            {
                lastModified = f;
            }
            else
            {
                if (f.lastModified() > lastModified.lastModified())
                {
                    lastModified = f;
                }
            }
        }

        if (lastModified != null)
        {
            printMessage(getDateRepresentation(lastModified.lastModified()));

            if (cmdLine.getOption(getOptionSpecification("--printFileName")).isSpecified())
            {
                printMessage("\t" + lastModified.getAbsolutePath());
            }

            return 0;
        }
        else
        {
            return 1;
        }
    }

	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{

	}
    private String getDateRepresentation(long millis)
    {
        // Calendar cal = Calendar.getInstance();
        // cal.setTimeInMillis(millis);
        // int year = cal.get(Calendar.YEAR);
        // int month = cal.get(Calendar.MONTH) + 1;
        // int day = cal.get(Calendar.DAY_OF_MONTH);
        // int hour = cal.get(Calendar.HOUR_OF_DAY);
        // int minute = cal.get(Calendar.MINUTE);
        // int second = cal.get(Calendar.SECOND);
        // // project-2008-01-03.15'45'33
        // String s = year + "-" + month + "-" + day + "." + hour + "'" + minute
        // + "'" + second;

        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date date = new Date(millis);
        return dateFormat.format(date);

    }

    public void declareOptions(Collection<OptionSpecification> specs)
    {
        specs.add(new OptionSpecification("--printFileName", "-n", null, null, "Print the name of the newest file"));
    }

}
