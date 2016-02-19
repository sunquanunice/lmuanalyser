package org.lucci.lmu.input;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lucci.lmu.DependenceRelation;
import org.lucci.lmu.Entity;
import org.lucci.lmu.Model;

/*
 * Created on Oct 11, 2004
 */

/**
 * @author luc.hogie
 */
public class JarFileDependenceAnalyser extends ModelFactory
{
	@Override
	public Model createModel(byte[] data) throws ParseError
	{
		Model model = new Model();
		String path = new String(data);
		System.out.println("Jar dependence here");
		String jarName = path.substring(path.lastIndexOf(File.separator) + 1);
		String currentDirectory = path.substring(0, path.lastIndexOf(File.separator));
		Map<String, Set<String>> dependencies = new HashMap<>();
		dependencies = new JarUnitDependenceManager().getJarDependencies(jarName, currentDirectory, dependencies);
		Set<String> entities = new HashSet<>();
		entities.addAll(dependencies.keySet());
		dependencies.values().forEach(value -> {
			entities.addAll(value);
		});
		entities.forEach(entity -> {
			UnitEntityGenerator generator = new UnitEntityGenerator();
			if(entity.endsWith(".jar")) {
				model.addEntity(generator.getEntity(entity, UnitEntityGenerator.TYPE_UNIT.JAR));
			} else {
					model.addEntity(generator.getEntity(entity, UnitEntityGenerator.TYPE_UNIT.PACKAGE));
			}
		});
		dependencies.forEach((key, values) -> {
			values.forEach(value -> {
				Entity head = model.getEntity(key);
				Entity tail = model.getEntity(value);
				DependenceRelation relation = new DependenceRelation(tail, head);
				model.addRelation(relation);
			});
		});
		/*
		Entity headEntity = new Entity();
		headEntity.setName(jarName);
		model.addEntity(headEntity);
		Set<String> dependencies = JarUnitDependency.getJarDependencies(path);
		dependencies.forEach(dependence -> {
			Entity entity = new Entity();
			entity.setName(dependence);
			model.addEntity(entity);
			DependenceRelation relation = new DependenceRelation(entity, headEntity);
			relation.setType(TYPE.COMPOSITION);
			model.addRelation(relation);
		});
*/
		return model;
	}
}