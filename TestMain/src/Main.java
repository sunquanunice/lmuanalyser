

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.IntStream;

public class Main {

	public static final String SUFFIX = "LMUMUMUMU";
	public static Set<String> getJarDependencies(String path) {
		Set<String> dependencies = new HashSet<String>();
	
		JarFile jar;
		try {
			jar = new JarFile(path);
			Manifest manifest = jar.getManifest();
			if (manifest != null) {
				Attributes attrs = manifest.getMainAttributes();
				String bundles = attrs.getValue("Require-Bundle");
				if (bundles != null) {
					String entites = bundles.replaceAll(";bundle-version=\"[^\"]*\"", "")
							.replaceAll(";visibility:=\\w+", "");
					for (String s : entites.split(",")) {
						dependencies.add(s);
					}
				}
				String classPath = attrs.getValue("Bundle-ClassPath");
				if(classPath != null) {
					for(String s : classPath.split(",")) {
						if(!s.equals("bin/") && !s.equals(".")) {
							dependencies.add(s.substring(s.lastIndexOf("/")+1));
							System.out.println("HHH"+ s.substring(0,s.lastIndexOf("/")));
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dependencies;
	}
	
	public static void unzipJar(String src, String currentDirectory) throws IOException {
		
		// Create a directory in the current directory with the jar name and a SUFFIX
		String newDirectory = createTmpDirectoryPath(src, currentDirectory);
		new File(newDirectory).mkdir();
		
		//Create a jarFile object
		JarFile jar = new JarFile(new File(currentDirectory + File.separator + src));
		Enumeration<JarEntry> enums = jar.entries();
		while(enums.hasMoreElements()) {
			JarEntry jarEntry = enums.nextElement();
			String fileName = jarEntry.getName();
			FileOutputStream out = null;
			InputStream in = null;
			if(fileName.equals("plugin.xml")) {
				in = jar.getInputStream(jarEntry);
				out = new FileOutputStream(newDirectory + "/" + fileName);
				while(in.available() > 0) {
					out.write(in.read());
				}
				out.close();
				in.close();
			} else if(fileName.endsWith(".jar")) {
				in = jar.getInputStream(jarEntry);
				fileName = fileName.substring(fileName.lastIndexOf("/")+1);
				out = new FileOutputStream(newDirectory + "/" + fileName);
				while(in.available() > 0) {
					out.write(in.read());
				}
				out.close();
				in.close();
			}
		}
	}
	
	private static String createTmpDirectoryPath(String src, String currentDirectory) {
		return currentDirectory + "/" + src.substring(0, src.length()-4) + "_" + SUFFIX;
	}
	public static void removeDirectory(String src, String currentDirectory) {
		String newDirectory = createTmpDirectoryPath(src, currentDirectory);
		deleteDir(new File(newDirectory));
	}
	
	 public static void deleteDir(File dir) {
	      if (dir.isDirectory()) {
	         String[] children = dir.list();
	         IntStream.range(0, children.length).forEach(index -> {
	        	 deleteDir(new File(dir, children[index]));
	         });
	      } 
	    	  dir.delete();
	      
	 }
	public static void main(String[] args) throws IOException {
		System.out.println("Hello");
		Main main = new Main();
		main.getJarDependencies("resources/lmu-ui.jar").forEach(s -> {
			System.out.println(s);
		});
		//unzipJar("lmu-ui.jar", "resources");
		//removeDirectory("lmu-ui.jar", "resources");
	}
}
