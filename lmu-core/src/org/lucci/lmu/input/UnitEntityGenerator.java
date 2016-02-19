package org.lucci.lmu.input;

import org.lucci.lmu.Entity;
import org.lucci.lmu.JarEntity;
import org.lucci.lmu.PackageEntity;
import org.lucci.lmu.ProjectEntity;

public class UnitEntityGenerator {
	public enum TYPE_UNIT {
		PROJECT, PACKAGE, JAR
	}

	public UnitEntityGenerator() {
	}

	public Entity getEntity(String unityName, TYPE_UNIT unit) {
		Entity entity = null;
		if (unit.equals(TYPE_UNIT.PROJECT)) {
			entity = new ProjectEntity();
		} else if (unit.equals(TYPE_UNIT.PACKAGE)) {
			entity = new PackageEntity();
		} else if (unit.equals(TYPE_UNIT.JAR)) {
			entity = new JarEntity();
		} else {
			entity = new Entity();
		}
		entity.setName(unityName);
		return entity;
	}

}
