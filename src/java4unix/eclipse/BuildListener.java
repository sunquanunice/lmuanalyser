package java4unix.eclipse;

import toools.io.file.AbstractFile;
import toools.io.file.RegularFile;

public interface BuildListener
{
	void fileCreated(RegularFile file);

	void uploading(AbstractFile selfContainedBinariesFile, String destination);

	void svnuploading(String dest);
	void mavenuploading(String dest);
	void message(String msg);
}
