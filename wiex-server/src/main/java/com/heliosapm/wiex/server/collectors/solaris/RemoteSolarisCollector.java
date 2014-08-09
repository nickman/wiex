package com.heliosapm.wiex.server.collectors.solaris;



import java.io.ByteArrayOutputStream;

import org.apache.log4j.BasicConfigurator;
import org.helios.jcraft.jsch.ChannelExec;
import org.helios.jcraft.jsch.JSch;
import org.helios.jcraft.jsch.Session;
import org.helios.jcraft.jsch.UserInfo;

import com.heliosapm.wiex.jmx.dynamic.annotation.JMXAttribute;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXManagedObject;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperation;
import com.heliosapm.wiex.jmx.dynamic.annotation.JMXOperationParameter;
import com.heliosapm.wiex.server.collectors.SmartTracer;

/**
 * <p>Title: RemoteSolarisCollector</p>
 * <p>Description: Remotely collects SAR Resource Utilization Data for Solaris using <code>sar</code> and <code>mpstat</code> over an SSH connection.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

@JMXManagedObject(annotated=true, declared=false)
public class RemoteSolarisCollector extends SolarisCollector implements UserInfo {
	
	/**	The absolute name of the SSH Known Hosts File */
	protected String knownHostsFile = null;
	/**	The absolute name of the private key file */
	protected String privateKeyFile = null;
	/**	The host name to connect to */
	protected String hostName = null;
	/**	The port to connect to */
	protected int port = 22;
	/**	The connection timeout in ms. */
	protected int timeOut = 3000;		
	/**	The user name to connect as */
	protected String userName = null;
	/**	The pass phrase for the private key */
	protected String passPhrase = null;
	/** The JSch SSH Provider */
	protected JSch jsch = new JSch();
	/** The shell command to locate the target JVM Process Id */
	protected String pIDLocator = null;
	/** The currnt thread's SSH Session */
	protected ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
	
	

	



	/**
	 * Instantiates a new RemoteLinuxSARCollector.
	 */
	public RemoteSolarisCollector() {
		super();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.2 $";
		MODULE = "RemoteSolarisCollector";
	}	
	
	
	/*
import javax.management.*;
MBeanServerFactory.findMBeanServer(null).each() { agent ->
	println "Agent:${agent.getDefaultDomain()}";
}
	 */
	
	/**
	 * Command Line Tester
	 * @param args
	 */
	public static void main(String args[]) {
			// bprodpaynrac01-vip
			// C:/keys/BW/solaris/id_dsa
			try {			
				
				BasicConfigurator.configure();
				RemoteSolarisCollector collector = new RemoteSolarisCollector();
				collector.setSegmentPrefixElements(new String[] {"Solaris", "bprodpaynrac01-vip"});
				log(collector.getVersion());
				collector.logErrors=true;
				//collector.setKnownHostsFile("C:\\projects3.3\\SBSCollectors\\test\\solaris\\known_hosts");
				collector.setKnownHostsFile("C:\\Documents and Settings\\whitehen\\My Documents\\.ssh\\known_hosts");
				//collector.setKnownHostsFile(args[0]);
				//collector.setPrivateKeyFile("C:\\projects3.3\\SBSCollectors\\test\\solaris\\whitehen_dsa");
				collector.setPrivateKeyFile("C:/keys/BW/solaris/id_dsa");
				//collector.setPrivateKeyFile(args[1]);
				//collector.setHostName("192.168.31.121");
				collector.setHostName("10.116.6.43");
				collector.setInclude("cpu_stat:.*:cpu_stat.*:iowait", SmartTracer.METRIC_TYPE_COUNTER_DELTA_LONG);
				
				collector.setPort(22);
				collector.setTimeOut(25000);				
				collector.setUserName("whitehen");
				collector.setPassPhrase("Hello World");
				collector.localInit();
				for(int i = 0; i < 10; i++) {
					long start = System.currentTimeMillis();
					//collector.start();
					collector.collect();
					//log("Command Output\n\n" + collector.shellCommand("df -k"));
					long elapsed = System.currentTimeMillis() - start;
					log("main:" + elapsed + " ms.");	
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}
	
	protected void localInit() throws Exception {
		jsch.setKnownHosts(knownHostsFile);
		jsch.addIdentity(privateKeyFile, passPhrase);
		
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
		jsch.setKnownHosts(knownHostsFile);
		jsch.addIdentity(privateKeyFile, passPhrase);
		super.start();
	}
	
	
	/**
	 * Collects Solaris host performance data from sar through a remote SSH connection.
	 * Synchronized to avoid thread safety issues with the SSH connection.
	 * Acquires JVM's PID.
	 * Collects JVM performance data from sar.
	 * Collects MXBean stats.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@JMXOperation(description="Collects Remote SAR Stats", expose=true, name="collect")
	public void collect() {
		try {
			log.info("Collecting......");
			threadSession.set(connectSSH());
			super.collect();
		} catch (Exception e) {
			log.error("Failed To Collect On SSH Connection", e);
		} finally {
			try { 
				threadSession.get().disconnect();
				threadSession.set(null);
			} catch (Exception e) {}
		}
	}
	
	
	/**
	 * Connects to the SSH Server.
	 * @throws Exception
	 */
	protected Session connectSSH() throws Exception {		
		Session session=jsch.getSession(userName, hostName, port);
		session.setUserInfo(this);
		session.connect(timeOut);
		return session;
	}
	
	/**s
	 * Issues the given string as an OS command and returns the standard output of the command.
	 * @param command An arbitrary OS shell command.
	 * @return The standard output of the executed command.
	 */
	protected String issueOSCommand(String command) {	
		try {			
			final ChannelExec exec = (ChannelExec)threadSession.get().openChannel("exec");
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
            exec.setCommand(command);
            exec.setOutputStream(out);
            exec.setExtOutputStream(out);
            exec.connect();
			final Thread thread = new Thread() {
                public void run()  {
                    while(!exec.isEOF()) {                        
                        try { Thread.sleep(500L); }
                        catch(Exception e) { 
                        	e.printStackTrace();
                        }
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
            thread.join(timeOut);     
            thread.interrupt();
            if(thread.isAlive()) {
            	throw new Exception("Time Out Error");
            } else {
            	try { exec.disconnect(); } catch (Exception e) {}
            	return out.toString();
            }
		} catch (Throwable e) {
			e.printStackTrace();
			return e.getMessage();
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
		Session session = null;
		try {
			session = connectSSH();
			return issueOSCommand(command);
		} finally {
			try { session.disconnect(); } catch (Exception e) {}		
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
		try {
			return issueOSCommand(pIDLocator);
		} catch (Exception e) {
			log.warn("Unable to locate PID:" + e);
			return "";
		}
	}

	/**
	 * The host name to connect to.
	 * @return the hostName
	 */
	@JMXAttribute(description="The host name to connect to.", name="HostName")
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets the host name to connect to.
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * The port to connect to.
	 * @return the port
	 */
	@JMXAttribute(description="The port to connect to.", name="Port")
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port to connect to.
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * The connection timeout in ms.
	 * @return the timeOut
	 */
	@JMXAttribute(description="The connection timeout in ms.", name="TimeOut")
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * Sets the connection timeout in ms.
	 * @param timeOut the timeOut to set
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	
	/**
	 * The absolute name of the SSH Known Hosts File.
	 * @return the knownHostsFile
	 */
	@JMXAttribute(description="The absolute name of the SSH Known Hosts File.", name="KnownHostsFile")
	public String getKnownHostsFile() {
		return knownHostsFile;
	}

	/**
	 * Sets the absolute name of the SSH Known Hosts File.
	 * @param knownHostsFile the knownHostsFile to set
	 */
	public void setKnownHostsFile(String knownHostsFile) {
		this.knownHostsFile = knownHostsFile;
	}

	/**
	 * The pass phrase for the private key.
	 * Always returns a masked value.
	 * @return the passPhrase
	 */
	@JMXAttribute(description="The pass phrase for the private key.", name="PassPhrase")
	public String getPassPhrase() {
		return passPhrase.replaceAll(".", "*");
	}

	/**
	 * Sets the pass phrase for the private key.
	 * @param passPhrase the passPhrase to set
	 */
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	/**
	 * The absolute name of the private key file.
	 * @return the privateKeyFile
	 */
	@JMXAttribute(description="The absolute name of the private key file.", name="PrivateKeyFile")
	public String getPrivateKeyFile() {
		return privateKeyFile;
	}

	/**
	 * Sets the absolute name of the private key file.
	 * @param privateKeyFile the privateKeyFile to set
	 */
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	/**
	 * The user name to connect as.
	 * @return the userName
	 */
	@JMXAttribute(description="The user name to connect as.", name="UserName")
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name to connect as.
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return The configured pass phrase.
	 * @see org.helios.jcraft.jsch.UserInfo#getPassphrase()
	 */
	public String getPassphrase() {
		return passPhrase;
	}

	/**
	 * No Op.
	 * @return Null.
	 * @see org.helios.jcraft.jsch.UserInfo#getPassword()
	 */
	public String getPassword() {
		return null;
	}

	/**
	 * No Op.
	 * @param arg0
	 * @return Null.
	 * @see org.helios.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
	 */
	public boolean promptPassphrase(String arg0) {
		return false;
	}

	/**
	 * No Op.
	 * @param arg0
	 * @return Null.
	 * @see org.helios.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
	 */
	public boolean promptPassword(String arg0) {
		return false;
	}

	/**
	 * No Op.
	 * @param arg0
	 * @return Null.
	 * @see org.helios.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	public boolean promptYesNo(String arg0) {
		return false;
	}

	/**
	 * No Op.
	 * @param arg0
	 * @see org.helios.jcraft.jsch.UserInfo#showMessage(java.lang.String)
	 */
	public void showMessage(String arg0) {
		
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

}
