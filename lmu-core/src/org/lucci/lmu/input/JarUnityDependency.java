package org.lucci.lmu.input;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarUnityDependency {

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
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dependencies;
	}
}
