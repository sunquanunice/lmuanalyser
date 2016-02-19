package org.lucci.lmu.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.IntStream;

public class JarUnitDependenceManager {
	public static final String SUFFIX = "LMUMUMUMU";
	private Map<String, Boolean> tracked = new HashMap<>();

	public Map<String, Set<String>> getJarDependencies(String srcJarName, String currentDirectory,
			Map<String, Set<String>> dependenciesMap) {
		// First put the source jar file into the tracked map with true
		tracked.put(srcJarName, true);
		// Create a directory in the current directory with the jar name and a
		// SUFFIX
		String newDirectory = createTmpDirectoryPath(srcJarName, currentDirectory);
		new File(newDirectory).mkdir();

		// This set is to stock all the direct dependencies of this jar
		Set<String> dependencies = new HashSet<>();

		// Add all the dependencies from the MANIFEST.MF
		String jarFilePath = currentDirectory + File.separator + srcJarName;
		dependencies.addAll(ManifestMFDependencies.getManifestDependenciesByPath(jarFilePath));
		try {
			// Create a jarFile object
			JarFile jar = new JarFile(new File(jarFilePath));
			Enumeration<JarEntry> enums = jar.entries();
			while (enums.hasMoreElements()) {
				JarEntry jarEntry = enums.nextElement();
				String fileName = jarEntry.getName();
				FileOutputStream out = null;
				InputStream in = null;
				if (fileName.equals("plugin.xml")) {
					// Copy the plugin.xml into the tmp directory
					in = jar.getInputStream(jarEntry);
					String path = newDirectory + "/" + fileName;
					out = new FileOutputStream(path);
					while (in.available() > 0) {
						out.write(in.read());
					}
					out.close();
					in.close();
					// Get all the dependencies from the plugin.xml
					dependencies.addAll(PluginXMLDependencies.getDependenciesByPath(path));
				} else if (fileName.endsWith(".jar")) {
					// Copy the jar into the tmp directory
					in = jar.getInputStream(jarEntry);
					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
					out = new FileOutputStream(newDirectory + "/" + fileName);
					while (in.available() > 0) {
						out.write(in.read());
					}
					out.close();
					in.close();
					if (tracked.get(fileName) == null) {
						getJarDependencies(fileName, newDirectory, dependenciesMap);
					}
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		}
		dependenciesMap.put(srcJarName, dependencies);
		removeDirectory(srcJarName, currentDirectory);
		return dependenciesMap;
	}

	// Create a tmp folder with the given jar file path
	public static String createTmpDirectoryPath(String src, String currentDirectory) {
		return currentDirectory + "/" + src.substring(0, src.length() - 4) + "_" + SUFFIX;
	}
	
	private  void removeDirectory(String src, String currentDirectory) {
		String newDirectory = createTmpDirectoryPath(src, currentDirectory);
		deleteDir(new File(newDirectory));
	}
	
	private  void deleteDir(File dir) {
	      if (dir.isDirectory()) {
	         String[] children = dir.list();
	         IntStream.range(0, children.length).forEach(index -> {
	        	 deleteDir(new File(dir, children[index]));
	         });
	      } 
	    	  dir.delete();
	      
	 }
}
