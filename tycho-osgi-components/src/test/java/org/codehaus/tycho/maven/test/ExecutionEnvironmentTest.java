package org.codehaus.tycho.maven.test;

import junit.framework.TestCase;

import org.codehaus.tycho.UnknownEnvironmentException;
import org.codehaus.tycho.utils.ExecutionEnvironment;
import org.codehaus.tycho.utils.ExecutionEnvironmentUtils;

public class ExecutionEnvironmentTest extends TestCase {

	private ExecutionEnvironment javaSE6Enviroment;
	private ExecutionEnvironment j2SE5Enviroment;
	private ExecutionEnvironment j2SE14Environment;
	private ExecutionEnvironment j2SE13Environment;
	private ExecutionEnvironment j2SE12Environment;
	private ExecutionEnvironment jre11Environment;
	private ExecutionEnvironment cdc11Environment;
	private ExecutionEnvironment cdc10Environment;
	private ExecutionEnvironment osgiMin10Environment;
	private ExecutionEnvironment osgiMin11Environment;
	private ExecutionEnvironment osgiMin12Environment;

	@Override
	protected void setUp() throws Exception {
		javaSE6Enviroment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("JavaSE-1.6");
		j2SE5Enviroment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("J2SE-1.5");
		j2SE14Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("J2SE-1.4");
		j2SE13Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("J2SE-1.3");
		j2SE12Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("J2SE-1.2");
		jre11Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("JRE-1.1");
		cdc11Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("CDC-1.1/Foundation-1.1");
		cdc10Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("CDC-1.0/Foundation-1.0");
		osgiMin10Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("OSGi/Minimum-1.0");
		osgiMin11Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("OSGi/Minimum-1.1");
		osgiMin12Environment = ExecutionEnvironmentUtils
				.getExecutionEnvironment("OSGi/Minimum-1.2");
	}

	public void testNotNull() {
		assertNotNull(javaSE6Enviroment);
		assertNotNull(j2SE5Enviroment);
		assertNotNull(j2SE14Environment);
		assertNotNull(j2SE13Environment);
		assertNotNull(j2SE12Environment);
		assertNotNull(jre11Environment);
		assertNotNull(cdc10Environment);
		assertNotNull(cdc11Environment);
		assertNotNull(osgiMin10Environment);
		assertNotNull(osgiMin11Environment);
		assertNotNull(osgiMin12Environment);
	}

	public void testGetProfileName() {
		assertEquals("JavaSE-1.6", javaSE6Enviroment.getProfileName());
		assertEquals("J2SE-1.5", j2SE5Enviroment.getProfileName());
		assertEquals("J2SE-1.4", j2SE14Environment.getProfileName());
		assertEquals("J2SE-1.3", j2SE13Environment.getProfileName());
		assertEquals("J2SE-1.2", j2SE12Environment.getProfileName());
		assertEquals("JRE-1.1", jre11Environment.getProfileName());
		assertEquals("CDC-1.0/Foundation-1.0", cdc10Environment
				.getProfileName());
		assertEquals("CDC-1.1/Foundation-1.1", cdc11Environment
				.getProfileName());
		assertEquals("OSGi/Minimum-1.0", osgiMin10Environment.getProfileName());
		assertEquals("OSGi/Minimum-1.1", osgiMin11Environment.getProfileName());
		assertEquals("OSGi/Minimum-1.2", osgiMin12Environment.getProfileName());
	}

	public void testCompilerSourceLevel() {
		assertEquals("1.3", osgiMin10Environment.getCompilerSourceLevel());
		assertEquals("1.3", osgiMin11Environment.getCompilerSourceLevel());
		assertEquals("1.3", osgiMin12Environment.getCompilerSourceLevel());
		assertEquals("1.3", cdc10Environment.getCompilerSourceLevel());
		assertEquals("1.3", cdc11Environment.getCompilerSourceLevel());
		assertEquals("1.3", jre11Environment.getCompilerSourceLevel());
		assertEquals("1.3", j2SE12Environment.getCompilerSourceLevel());
		assertEquals("1.3", j2SE13Environment.getCompilerSourceLevel());
		assertEquals("1.3", j2SE14Environment.getCompilerSourceLevel());
		assertEquals("1.5", j2SE5Enviroment.getCompilerSourceLevel());
		assertEquals("1.6", javaSE6Enviroment.getCompilerSourceLevel());
	}

	public void testCompilerTargetLevel() {
		assertEquals("1.1", osgiMin10Environment.getCompilerTargetLevel());
		assertEquals("1.2", osgiMin11Environment.getCompilerTargetLevel());
		assertEquals("1.2", osgiMin12Environment.getCompilerTargetLevel());
		assertEquals("1.1", cdc10Environment.getCompilerTargetLevel());
		assertEquals("1.2", cdc11Environment.getCompilerTargetLevel());
		assertEquals("1.1", jre11Environment.getCompilerTargetLevel());
		assertEquals("1.1", j2SE12Environment.getCompilerTargetLevel());
		assertEquals("1.1", j2SE13Environment.getCompilerTargetLevel());
		assertEquals("1.2", j2SE14Environment.getCompilerTargetLevel());
		assertEquals("1.5", j2SE5Enviroment.getCompilerTargetLevel());
		assertEquals("1.6", javaSE6Enviroment.getCompilerTargetLevel());
	}

	public void testUnknownEnv() {
		try {
			ExecutionEnvironmentUtils.getExecutionEnvironment("foo");
			fail();
		} catch (UnknownEnvironmentException e) {
			// expected
		}
	}

}
