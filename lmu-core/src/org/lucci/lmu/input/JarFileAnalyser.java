package org.lucci.lmu.input;

import java.io.File;
import java.util.Set;

import org.lucci.lmu.AssociationRelation;
import org.lucci.lmu.Entity;
import org.lucci.lmu.Model;
import org.lucci.lmu.AssociationRelation.TYPE;

/*
 * Created on Oct 11, 2004
 */

/**
 * @author luc.hogie
 */
public class JarFileAnalyser extends ModelFactory
{
	@Override
	public Model createModel(byte[] data) throws ParseError
	{
		Model model = new Model();
		String path = new String(data);
		String jarName = path.substring(path.lastIndexOf(File.separator) + 1);
		Entity headEntity = new Entity();
		headEntity.setName(jarName);
		model.addEntity(headEntity);
		Set<String> dependencies = JarUnityDependency.getJarDependencies(path);
		dependencies.forEach(dependence -> {
			Entity entity = new Entity();
			entity.setName(dependence);
			model.addEntity(entity);
			AssociationRelation relation = new AssociationRelation(entity, headEntity);
			relation.setType(TYPE.COMPOSITION);
			model.addRelation(relation);
		});

		return model;
	}
}