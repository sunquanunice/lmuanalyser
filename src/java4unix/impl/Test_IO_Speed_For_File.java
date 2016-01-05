package java4unix.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import toools.io.NullOutputStream;
import toools.io.data_transfer.DataTransfer;
import toools.io.data_transfer.DataTransferEvent;
import toools.io.data_transfer.DataTransferListener;

import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;


public class Test_IO_Speed_For_File extends J4UScript
{
    @Override
    protected void declareOptions(Collection<OptionSpecification> optionSpecifications)
    {
    }


	@Override
	protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
	{
		argumentSpecifications.add(new ArgumentSpecification("file",".+", false, "the file to test"));
		
	}
  
    @Override
    public String getShortDescription()
    {
        return "TestIOSpeedForFile";
    }

  
    @Override
    public int runScript(CommandLine cmdLine)
    {
        File file = new File(cmdLine.findArguments().get(0));

        if (file.exists())
        {
            try
            {
                testRead(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return 1;
            }
        }
        else
        {
            try
            {
                testWrite(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return 1;
            }
        }

        return 0;
    }

    private void testWrite(File file) throws FileNotFoundException, IOException
    {
        FileOutputStream fis = new FileOutputStream(file);
        final DataTransfer t = new DataTransfer();
        t.setOutputStream(fis);
        t.setInputStream(new InputStream()
        {

            @Override
            public int read() throws IOException
            {
                return 0;
            }
        });

        t.addDataTransferListener(new DataTransferListener()
        {

            @Override
            public void notificationDelayExpired(DataTransferEvent event)
            {
            }

            @Override
            public void notificationStepProcessed(DataTransferEvent event)
            {
                printMessage(t.getAverageTransferRate() + " byte/s");
            }

            @Override
            public void readIOErrorOccured(DataTransferEvent event)
            {
            }

            @Override
            public void transferStarted(DataTransferEvent event)
            {
            }

            @Override
            public void transferTerminated(DataTransferEvent event)
            {
            }

            @Override
            public void writeIOErrorOccured(DataTransferEvent event)
            {
            }
        });
        
        t.process();
    }

    private void testRead(File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        final DataTransfer t = new DataTransfer();
        t.setInputStream(fis);
        t.setOutputStream(new NullOutputStream());
        t.addDataTransferListener(new DataTransferListener()
        {

            @Override
            public void notificationDelayExpired(DataTransferEvent event)
            {
            }

            @Override
            public void notificationStepProcessed(DataTransferEvent event)
            {
                printMessage(t.getAverageTransferRate() + " byte/s");
            }

            @Override
            public void readIOErrorOccured(DataTransferEvent event)
            {
            }

            @Override
            public void transferStarted(DataTransferEvent event)
            {
            }

            @Override
            public void transferTerminated(DataTransferEvent event)
            {
            }

            @Override
            public void writeIOErrorOccured(DataTransferEvent event)
            {
            }
        });
        
        t.process();
    }

}
