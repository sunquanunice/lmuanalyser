package java4unix.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import toools.io.Utilities;
import toools.io.file.RegularFile;

public class UnixBinaryFilter
{
    public static int run(final InputStream is, OutputStream stdout, OutputStream stderr, String cmd, String... args)
    {
	try
	{
	    List<String> cmdLine = new ArrayList<String>();
	    cmdLine.add(cmd);

	    for (String arg : args)
	    {
		cmdLine.add(arg);
	    }

	    final Process process = Runtime.getRuntime().exec(cmdLine.toArray(new String[0]), args);

	    new ReadingThread(process.getErrorStream(), stderr);
	    new ReadingThread(process.getInputStream(), stdout);
	    final IOException[] ee = new IOException[1];

	    new Thread() {
		@Override
		public void run()
		{
		    try
		    {
			Utilities.copy(is, process.getOutputStream());

			// by sending EOT to stdin, the process will close by
			// itself (filters behave like this)
			process.getOutputStream().close();
		    }
		    catch (IOException e)
		    {
			ee[0] = e;
		    }
		}
	    }.start();

	    if (ee[0] != null)
	    {
		throw new IOException(ee[0]);
	    }
	    else
	    {
		return process.waitFor();
	    }
	}
	catch (Throwable e)
	{
	    throw new IllegalStateException(e);
	}
    }

    private static class ReadingThread extends Thread
    {
	private final InputStream processReadingStream;
	private final OutputStream destinationStream;

	public ReadingThread(InputStream is, OutputStream stdout)
	{
	    this.processReadingStream = is;
	    this.destinationStream = stdout;
	    start();
	}

	@Override
	public void run()
	{
	    try
	    {
		Utilities.copy(processReadingStream, destinationStream);
	    }
	    catch (IOException e)
	    {
		throw new IllegalStateException(e);
	    }
	}
    }

    public static void main(String[] args) throws IOException
    {
	URL url = new URL("https://www.google.fr/images/srpr/logo11w.png");
	URLConnection connection = url.openConnection();
	InputStream stream = connection.getInputStream();

	OutputStream result = new RegularFile("$HOME/tmp/test").createWritingStream();
	int r = UnixBinaryFilter.run(stream, result, System.err, "convert", "-transpose", "-", "-");
	result.close();

	// send EOT to sdtin, the program terminates
	System.out.println("Terminated with code: " + r);

    }
}
