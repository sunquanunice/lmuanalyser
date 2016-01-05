package java4unix.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java4unix.ArgumentSpecification;
import java4unix.CommandLine;
import java4unix.OptionSpecification;

import javax.imageio.ImageIO;

import toools.img.Utilities;
import toools.io.file.AbstractFile;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.math.MathsUtilities;
import toools.text.TextUtilities;

public class ImageReencoder extends J4UScript
{

    public void declareOptions(Collection<OptionSpecification> v)
    {
	v.add(new OptionSpecification("--keep", "-k", null, null, "keep the original files"));
	v.add(new OptionSpecification("--simulate", "-s", null, null,
		"set the file size threshold under which a file will not be processed"));
	v.add(new OptionSpecification("--force-reprocess", "-f", null, null,
		"for the (re)process of files already marked process by the presence of .j4u in their name"));

	v.add(new OptionSpecification("--target-resolution", "-r", "[0-9]+([km])?|same|fullHD|vga|gtab3", "same",
		"set the target resolution. 'same' refers to the resolution of the input file."));
	v.add(new OptionSpecification("--files-greater-than", "-t", "[0-9]+[km]", "0",
		"set the file size threshold under which a file will not be processed"));
    }

    @Override
    protected void declareArguments(Collection<ArgumentSpecification> argumentSpecifications)
    {
	argumentSpecifications.add(new ArgumentSpecification("img", ".+", true, "image file"));
    }

    @Override
    public int runScript(CommandLine cmdLine) throws IOException
    {
	for (String inputFilename : cmdLine.findArguments())
	{
	    processArgument(AbstractFile.map(inputFilename, null), cmdLine);
	}

	return 0;
    }

    private void processArgument(AbstractFile f, CommandLine cmdLine) throws IOException
    {
	if (f instanceof Directory)
	{
	    for (AbstractFile c : ((Directory) f).getChildFiles())
	    {
		processArgument(c, cmdLine);
	    }
	}
	else
	{
	    if (f.getName().toLowerCase().endsWith(".jpg"))
	    {
		pr((RegularFile) f, cmdLine);
	    }
	    else
	    {
		printMessage("skipping file " + f.getPath() + " - not a JPG file");
	    }
	}
    }

    private void pr(RegularFile file, CommandLine cmdLine) throws IOException
    {
	int sizeThreshold = TextUtilities.parseFileSize(getOptionValue(cmdLine, "-t"));

	if (file.getName().contains(".big."))
	{
	    printMessage("skipping image " + file.getPath() + " because it has '.big' flag");
	}
	else if (file.getSize() < sizeThreshold)
	{
	    printMessage("skipping image " + file.getPath()
		    + " because its size is lower than the threshold defined by -t");
	}
	else if (file.getName().contains(".j4u.") && !isOptionSpecified(cmdLine, "-f"))
	{
	    printMessage("skipping image " + file.getPath() + " because it was already processed");
	}
	else if (file.getName().contains(".old."))
	{
	    printMessage("skipping image " + file.getPath() + " because it is a backup file");
	}
	else
	{
	    printMessageWithNoNewLine("reading file \"" + file.getPath() + '"');
	    recodeOrScale(file, cmdLine);
	}
    }

    private void recodeOrScale(RegularFile inputFile, CommandLine cmdLine) throws IOException
    {
	BufferedImage inputImage = ImageIO.read(inputFile.createReadingStream());
	int height = inputImage.getHeight();
	int width = inputImage.getWidth();
	int numberOfPixels = height * width;
	String tr = getOptionValue(cmdLine, "--target-resolution");

	if (tr.equals("same"))
	{
	    recode(inputFile, inputImage, cmdLine);
	}
	else
	{
	    double maxResolution = calculate(tr);
	    double ratio = maxResolution / numberOfPixels;

	    // if the image really is too big
	    if (ratio < 0.8)
	    {
		double newheight = height * Math.sqrt(ratio);
		double newwidth = (width * newheight) / height;
		scale(inputFile, inputImage, (int) newwidth, (int) newheight, cmdLine);
	    }
	    else
	    {
		printMessageWithNoNewLine(", recoding, it has " + numberOfPixels + " pixels");
		recode(inputFile, inputImage, cmdLine);
		markProcessed(inputFile);
	    }
	}
    }

    private void markProcessed(RegularFile f) throws IOException
    {
	String currentName = f.getPath();

	if (!currentName.contains(".j4u."))
	{
	    String futureName = currentName.replaceFirst("(?i)\\.jpg$", ".j4u.jpg");
	    f.renameTo(futureName);
	}
    }

    private void recode(RegularFile inputFile, BufferedImage inputImage, CommandLine cmdLine) throws IOException
    {
	saveResultImageToDisk(inputFile, cmdLine, inputImage, inputImage);
    }

    private void scale(RegularFile inputFile, BufferedImage image, int newwidth, int newheight, CommandLine cmdLine)
	    throws IOException
    {
	// scales
	printMessageWithNoNewLine(", downscaling");
	Image resultImage = image.getScaledInstance(newwidth, newheight, Image.SCALE_SMOOTH);

	// save to disk
	saveResultImageToDisk(inputFile, cmdLine, resultImage, image);
    }

    private void saveResultImageToDisk(RegularFile inputFile, CommandLine cmdLine, Image resultImage,
	    BufferedImage image) throws IOException
    {
	BufferedImage destinationImage = Utilities.toBufferedImage(resultImage, image.getColorModel());

	// write to temporary file
	printMessageWithNoNewLine(", encoding");
	RegularFile outFile = RegularFile.createTempFile(inputFile.getParent(), "j4u-", "-tmp.jpg");
	ImageIO.write(destinationImage, "jpg", outFile.createWritingStream());

	double compressionRatio = MathsUtilities.round((double) inputFile.getSize() / (double) outFile.getSize(), 1);

	if (compressionRatio < 1.3)
	{
	    printMessage(", compression " + compressionRatio + " is not worthy. Keeping original file");
	    outFile.delete();
	    markProcessed(inputFile);
	}
	else
	{
	    printMessageWithNoNewLine(", reduced by " + compressionRatio);

	    if (isOptionSpecified(cmdLine, "--simulate"))
	    {
		outFile.delete();
	    }
	    else
	    {
		String inputFileName = inputFile.getPath();
		printMessageWithNoNewLine(", renaming original file to .old");
		inputFile.setExtension("old.jpg");

		try
		{
		    outFile.renameTo(inputFileName);
		    markProcessed(outFile);

		    if (!isOptionSpecified(cmdLine, "--keep"))
		    {
			printMessage(", deleting original file");
			inputFile.delete();
		    }
		}
		catch (Throwable t)
		{
		    printMessage(", renaming failed");
		    inputFile.renameTo(inputFileName);
		    outFile.delete();
		}
	    }
	}
    }

    private int calculate(String s)
    {
	if (s.equals("fullHD"))
	{
	    return 2304000;
	}
	else if (s.equals("vga"))
	{
	    return 307200;
	}
	else if (s.equals("gtab3"))
	{
	    return 1024 * 600;
	}

	else
	{
	    return TextUtilities.parseFileSize(s);
	}
    }

    @Override
    public String getShortDescription()
    {
	return "Reencodes/downscale the given images.";
    }

}
