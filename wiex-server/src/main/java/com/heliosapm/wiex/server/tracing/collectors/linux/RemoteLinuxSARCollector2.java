
package com.heliosapm.wiex.server.tracing.collectors.linux;


import java.io.File;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.server.tracing.ssh.SSHShell;
import com.heliosapm.wiex.server.tracing.ssh.SSHShellFactory;

/**
 * <p>Title: RemoteLinuxSARCollector2</p>
 * <p>Description: Remotely collects SAR Resource Utilization Data for Linux using <code>sar</code> over an SSH connection.
 * Uses the new SSHShell for optimized SSH access.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.3 $
 */

@JMXManagedObject(annotated=true, declared=false)
public class RemoteLinuxSARCollector2 extends LinuxSARCollector {
	
	protected SSHShell shell = null;
	protected Properties shellProperties = new Properties();
	
	/** The shell command to locate the target JVM Process Id */
	protected String pIDLocator = null;
	
	

	



	/**
	 * Instantiates a new RemoteLinuxSARCollector.
	 */
	public RemoteLinuxSARCollector2() {
		super();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.3 $";
		MODULE = "RemoteLinuxSARCollector";
	}			
	
	/**
	 * Command Line Tester
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			long start = System.currentTimeMillis();
			BasicConfigurator.configure();
			RemoteLinuxSARCollector collector = new RemoteLinuxSARCollector();
			log(collector.getVersion());
			collector.setKnownHostsFile(System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "known_hosts");
			collector.setPrivateKeyFile("C:\\keys\\jboss_dsa.key");
			collector.setHostName("10.19.47.119");
			collector.setPort(22);
			collector.setTimeOut(3000);
			collector.setUserName("jboss");
			collector.setPassPhrase("Hello World");
			collector.setDirSizer("JBossHome~/home/jboss");
			collector.setProcessLocator("PPWebServers~java | grep jboss | grep PPWebServers");
			collector.start();
			
			log("Command Output\n\n" + collector.shellCommand("df -k"));
			long elapsed = System.currentTimeMillis() - start;
			log("main:" + elapsed + " ms.");
			collector.collect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Advanced shell commands
	 * @return A shell command output
	 */
	@JMXOperation(description="Executes a shell command", expose=true, name="shell")	
	public String shell(String command) {
		return issueOSCommand(command);
	}			
		
	
	/**
	 * Command line tester logger.
	 * @param message
	 */
	public static void log(Object message) {
		System.out.println(message);
	}
	
	/**
	 * Initializes the SSH constructs and calls super.
	 * @throws Exception
	 */
	@JMXOperation(description="Starts The Service", expose=true, name="start")
	public void start() throws Exception {
		shell = SSHShellFactory.getSSHShell(shellProperties);
		super.start();
	}
	
	@JMXOperation(description="Stops The Service", expose=true, name="stop")
	public void stop() {
		if(shell!=null) shell.close();
		super.stop();
	}
	
	
	
	/**
	 * Collects Linux host performance data from sar through a remote SSH connection.
	 * Synchronized to avoid thread safety issues with the SSH connection.
	 * Acquires JVM's PID.
	 * Collects JVM performance data from sar.
	 * Collects MXBean stats.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects Remote SAR Stats", expose=true, name="collect")
	public void collect() {
		try {
			if(shell==null) {
				shell = SSHShellFactory.getSSHShell(shellProperties);
			}
			if(!shell.isConnected()) {
				shell.init();
			}
			super.collect();
		} catch (Exception e) {
			log.error("Failed To Collect On SSH Connection", e);
		} finally {
			try { 
			} catch (Exception e) {}
		}
	}
	
	
	/**
	 * Issues the given string as an OS command and returns the standard output of the command.
	 * @param command An arbitrary OS shell command.
	 * @return The standard output of the executed command.
	 */
	protected String issueOSCommand(String command) {	
		try {
			return shell.issueCommand(command);
		} catch (Throwable e) {
			if(logErrors) {
				log.error("Failed to issueOSCommand [" + command + "]", e);
			}
			try { shell.close(); } catch (Exception e2) {}
			throw new RuntimeException("Failed to issueOSCommand [" + command + "]", e);
		}
	}
	

	
	/**
	 * One off shell command. Connects to the SSH server, issues the command and disconnects.
	 * @param command The shell command.
	 * @return The output of the shell command.
	 * @throws Exception
	 */
	@JMXOperation(description="Issues a Shell Command", expose=true, name="ShellCommand")
	public String shellCommand(
			@JMXOperationParameter(description="The Shell Command to Execute", name="ShellCommand")String command) 
	throws Exception {
		
		try {
			return issueOSCommand(command);
		} finally {
		
		}
	}
	
	/**
	 * Reads the contents of the target JVM's /proc/status file into a string. 
	 * @return A string representation of the target JVM's /proc/status file.
	 * @throws Exception
	 */
	protected String getProcStatus() throws Exception {
		return shellCommand("cat /proc/" + pid + "/status");
	}
	
	
	/**
	 * Attempts to determine the process ID of the target JVM.
	 * @return A string representation of the JVM's process ID.
	 */
	@JMXOperation(description="Reports the VM's PID", expose=true, name="reportPid")	
	public String readPid() {
		if(pIDLocator==null||pIDLocator.length()<1) {
			pid = -1;
			return null;
		}
		try {
			return issueOSCommand(pIDLocator);
		} catch (Exception e) {
			log.warn("Unable to locate PID:" + e);
			return "";
		}
	}



	/**
	 * The shell command to locate the target JVM Process Id.
	 * @return the pIDLocator
	 */
	@JMXAttribute(description="The shell command to locate the target JVM Process Id.", name="PIDLocator")
	public String getPIDLocator() {
		return pIDLocator;
	}

	/**
	 * Sets the shell command to locate the target JVM Process Id.
	 * @param locator the pIDLocator to set
	 */
	public void setPIDLocator(String locator) {
		pIDLocator = locator;
	}

	/**
	 * @return the shellProperties
	 */
	@JMXAttribute(description="The SSHShell Properties.", name="ShellProperties")
	public Properties getShellProperties() {
		return shellProperties;
	}

	/**
	 * @param shellProperties the shellProperties to set
	 */
	public void setShellProperties(Properties shellProperties) {
		this.shellProperties = shellProperties;
	}

}
