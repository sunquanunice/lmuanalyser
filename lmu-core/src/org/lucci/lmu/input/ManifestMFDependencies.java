package org.lucci.lmu.input;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestMFDependencies {

	public static Set<String> getManifestDependenciesByPath(String path) {
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
						if(!s.equals("bin/") && !s.equals(".")&&s.endsWith(".jar")) {
							dependencies.add(s.substring(s.lastIndexOf("/")+1));
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dependencies;
	}
	
}
