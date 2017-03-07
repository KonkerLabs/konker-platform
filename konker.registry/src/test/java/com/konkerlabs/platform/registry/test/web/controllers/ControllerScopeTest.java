package com.konkerlabs.platform.registry.test.web.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ControllerScopeTest {

	@Test
	public void shouldAllControllersBeRequestScope() throws ClassNotFoundException, IOException {

		Class[] classes = getClasses("com.konkerlabs.platform.registry.web.controllers");

		// check if the class scanner is working
		assertTrue(classes.length > 10);

		for (Class clazz : classes) {

			if (clazz.isAnnotationPresent(Controller.class)) {

				Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
				assertNotNull(scopeAnnotation);
				assertTrue(scopeAnnotation.scopeName().equals("request") || scopeAnnotation.value().equals("request"));

			}

		}

	}

	private Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}
