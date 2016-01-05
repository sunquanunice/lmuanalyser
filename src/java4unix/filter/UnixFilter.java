package java4unix.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import toools.io.file.RegularFile;

public abstract class UnixFilter
{
	private final BlockingQueue queue = new LinkedBlockingQueue();
	private final Process process;
	private boolean error = false;

	public UnixFilter(String cmd, String... args)
	{
		try
		{
			List<String> cmdLine = new ArrayList<String>();
			cmdLine.add(cmd);

			for (String arg : args)
			{
				cmdLine.add(arg);
			}

			process = Runtime.getRuntime().exec(cmdLine.toArray(new String[0]), args);

			new ReadingThread(process.getInputStream(), true);
			new ReadingThread(process.getErrorStream(), false);

			new Thread() {
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							Object o = queue.take();
							// System.out.println("*** received: " + o);
							if (o.getClass() == String.class)
							{
								String line = (String) o;
								line += '\n';
								process.getOutputStream().write(line.getBytes());
								process.getOutputStream().flush();
							}
							else
							{
								break;
							}
						}
						catch (Throwable e)
						{
							error = true;
							throw new IllegalStateException(e);
						}
					}

					try
					{
						// by sending EOT to stdin, the process will close by
						// itself (filters behave like this)
						process.getOutputStream().close();

						// waiting for the program to return
						// int returnCode = process.waitFor();
						// terminated(returnCode);
					}
					catch (Throwable t)
					{
					}
				}
			}.start();
		}
		catch (IOException e1)
		{
			throw new IllegalStateException(e1);
		}
	}

	protected abstract void newLineOnStdout(String l);

	protected abstract void newLineOnStderr(String l);

	public void newLineToStdin(String line)
	{
		// remove trailing space, if any
		if (line.charAt(line.length() - 1) == '\n')
		{
			line = line.substring(0, line.length() - 1);
		}

		try
		{
			// System.out.println("*** new line to stdin: " + line);

			queue.put(line);
		}
		catch (InterruptedException e)
		{
			error = true;
			throw new IllegalStateException(e);
		}
	}

	private class ReadingThread extends Thread
	{
		private final BufferedReader br;
		private final boolean stdout;

		public ReadingThread(InputStream is, boolean stdout)
		{
			br = new BufferedReader(new InputStreamReader(is));
			this.stdout = stdout;
			start();
		}

		@Override
		public void run()
		{
			while (!error)
			{
				try
				{
					String l = br.readLine();

					if (l == null)
					{
						br.close();
						break;
					}
					else
					{
						if (stdout)
						{
							newLineOnStdout(l);
						}
						else
						{
							newLineOnStderr(l);
						}
					}
				}
				catch (IOException e)
				{
					error = true;
					throw new IllegalStateException(e);
				}
			}
		}
	}

	public int endOfTransmission()
	{
		try
		{
			queue.put(new Object());
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public static void main(String[] args) throws IOException
	{
		List<String> result = new ArrayList<String>();
		UnixFilter unixFilter = new UnixFilter("grep", "-i", "root") {

			@Override
			protected void newLineOnStdout(String l)
			{
				System.out.println("received: " + l);
			}

			@Override
			protected void newLineOnStderr(String l)
			{
				System.err.println("received: " + l);
			}
		};

		BufferedReader r = new BufferedReader(new InputStreamReader(new RegularFile("/etc/passwd").createReadingStream()));
		String line;

		while ((line = r.readLine()) != null)
		{
			unixFilter.newLineToStdin(line);
		}

		// send EOT to sdtin, the program terminates
		System.out.println("Terminated with code: " + unixFilter.endOfTransmission());

	}
}
