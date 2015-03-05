package org.jake;

import java.io.File;
import java.util.List;

import org.jake.depmanagement.JakeDependencies;
import org.junit.Assert;
import org.junit.Test;

public class JavaSourceParserTest {

	@Test
	public void withOneImport() {
		final JakeDependencies  dependencies = JavaSourceParser.of(
				new File("."), JavaSourceParserTest.class.getResource("with1Import.javasource")).dependencies();
		Assert.assertEquals(1, dependencies.dependenciesDeclaredWith(Project.JAKE_SCOPE).size());
	}

	@Test
	public void with3Imports() {
		final JakeDependencies  dependencies = JavaSourceParser.of(
				new File("."), JavaSourceParserTest.class.getResource("with3Imports.javasource")).dependencies();
		Assert.assertEquals(3, dependencies.dependenciesDeclaredWith(Project.JAKE_SCOPE).size());
	}



	@Test
	public void withoutImport() {
		final JakeDependencies  dependencies = JavaSourceParser.of(
				new File("."), JavaSourceParserTest.class.getResource("withoutImport.javasource")).dependencies();
		Assert.assertEquals(0, dependencies.dependenciesDeclaredWith(Project.JAKE_SCOPE).size());
	}

	@Test
	public void with2ProjectImports() {
		final List<File>  projects = JavaSourceParser.of(
				new File("."), JavaSourceParserTest.class.getResource("with2projectImports.javasource")).projects();
		Assert.assertEquals(2, projects.size());
		Assert.assertEquals("org.jake.foo", projects.get(0).getName());
		Assert.assertEquals("org.jake.bar", projects.get(1).getName());
	}


}
