package org.lucci.lmu.input;

import org.lucci.lmu.Entity;

public class UnitEntityGenerator {
	public  enum TYPE_UNIT { PROJECT, PACKAGE, JAR}
	
	public UnitEntityGenerator() {
	}
	public  Entity getEntity(String unityName, TYPE_UNIT unit) {
		Entity entity = new Entity();
		entity.setName(unityName);
		return entity;
	}
	

}
