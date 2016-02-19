package org.lucci.lmu.input;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lucci.lmu.AssociationRelation;
import org.lucci.lmu.AssociationRelation.TYPE;
import org.lucci.lmu.DependenceRelation;
import org.lucci.lmu.Entity;
import org.lucci.lmu.Model;

public class JavaProjectAnalyser extends ModelFactory {

	@Override
	public Model createModel(byte[] data) throws ParseError, ModelException {
		Model model = new Model();
		// Convert byte[] to String
		String path = new String(data);
		System.out.println("The project path is : " + path);
		String projectName = path.substring(path.lastIndexOf(File.separator)+1);
		System.out.println(projectName);
		Map<String, Set<String>> dependencies = new HashMap<>();
		dependencies = ProjectDependencies.getDependenciesByPath(path, dependencies);
		Set<String> entities = new HashSet<>();
		entities.addAll(dependencies.keySet());
		dependencies.values().forEach(value -> {
			entities.addAll(value);
		});
		entities.forEach(entity -> {
			model.addEntity(UnitEntity.getEntity(entity));
		});
		dependencies.forEach((key, values) -> {
			values.forEach(value -> {
				Entity head = model.getEntity(key);
				Entity tail = model.getEntity(value);
				DependenceRelation relation = new DependenceRelation(tail, head);
				model.addRelation(relation);
			});
			
		});
		return model;
	}
}
