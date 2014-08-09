/**
 * 
 */
package com.heliosapm.wiex.server.tracing.ssh;

import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.server.helpers.BeanHelper;

/**
 * A factory for creating new SSHShells.
 * @author WhiteheN
 * @TODO: Support for URL based knownHosts and privateKey.
 */
public class SSHShellFactory {
	protected static Logger log = Logger.getLogger(SSHShellFactory.class);
	protected Map<String, SSHShell> shells = new ConcurrentHashMap<String, SSHShell>();
	
	//=======================================================
	//		Property Constants
	//=======================================================
	public static final String HOST_NAME = "sbs.collectors.ssh.shell.hostname";
	public static final String USER_NAME = "sbs.collectors.ssh.shell.username";
	public static final String PASSWORD = "sbs.collectors.ssh.shell.password";
	public static final String PASSPHRASE = "sbs.collectors.ssh.shell.passphrase";
	public static final String KNOWN_HOSTS = "sbs.collectors.ssh.shell.knownhosts";
	public static final String PORT = "sbs.collectors.ssh.shell.port";
	public static final String PRIVATE_KEY = "sbs.collectors.ssh.shell.privatekey";
	//=============================================
	//       Stream tokens & formatting
	//=============================================	
	public static final String COMMAND_SUFFIX = "sbs.collectors.ssh.shell.command.suffix";
	public static final String END_OF_LINE = "sbs.collectors.ssh.shell.command.eol";
	public static final String CUSTOM_SHELL = "sbs.collectors.ssh.shell.command.customshell";
	public static final String CUSTOM_SHELL_ENABLED = "sbs.collectors.ssh.shell.command.customshellenabled";
	public static final String FAIL_ON_INCOMPLETE = "sbs.collectors.ssh.shell.command.failonincomplete";
	
	//=============================================
	//       SSH transport details
	//=============================================	
	public static final String CONNECT_TIMEOUT = "sbs.collectors.ssh.shell.timeout.connect";
	public static final String REQUEST_TIMEOUT = "sbs.collectors.ssh.shell.timeout.request";
	public static final String REQUEST_END_TIMEOUT = "sbs.collectors.ssh.shell.timeout.requestend";
	public static final String WAIT_CYCLE = "sbs.collectors.ssh.shell.timeout.waitcycle";
	public static final String READ_BUFFER_SIZE = "sbs.collectors.ssh.shell.readbuffersize";
	public static final String SOCKET_FACTORY_NAME = "sbs.collectors.ssh.shell.socketfactoryname";
	//=============================================
	//       SSH through a Proxy Options
	//=============================================
	public static final String PROXY_CLASS_NAME = "sbs.collectors.ssh.shell.proxy.classname";
	public static final String PROXY_HOST = "sbs.collectors.ssh.shell.proxy.host";
	public static final String PROXY_PORT = "sbs.collectors.ssh.shell.proxy.port";	
	//=============================================
	//       JCraft Constructs
	//=============================================
	public static final String THREAD_GROUP_NAME = "sbs.collectors.ssh.shell.threadgroup.name";
	public static final String THREAD_GROUP_DAEMON = "sbs.collectors.ssh.shell.threadgroup.daemon";
	
	//=======================================================
	//		Factory Methods
	//=======================================================
	
	
	public static SSHShell getSSHShell(Properties p) throws Exception {
		try {
			SSHShell shell = new SSHShell();
			applyProperty(HOST_NAME, "HostName", p, shell);
			applyProperty(USER_NAME, "UserName", p, shell);
			applyProperty(PASSWORD, "Password", p, shell);
			applyProperty(PASSPHRASE, "PassPhrase", p, shell);
			applyProperty(KNOWN_HOSTS, "KnownHostsFile", p, shell);
			applyProperty(PASSPHRASE, "PassPhrase", p, shell);
			applyProperty(PORT, "Port", p, shell);
			applyProperty(PRIVATE_KEY, "PrivateKey", p, shell);
			applyProperty(COMMAND_SUFFIX, "CommandSuffix", p, shell);
			applyProperty(END_OF_LINE, "EndOfLine", p, shell);
			applyProperty(REQUEST_TIMEOUT, "RequestTimeOut", p, shell);
			applyProperty(REQUEST_END_TIMEOUT, "RequestEndTimeOut", p, shell);
			applyProperty(WAIT_CYCLE, "WaitCycle", p, shell);
			applyProperty(READ_BUFFER_SIZE, "ReadBufferSize", p, shell);
			applyProperty(SOCKET_FACTORY_NAME, "SocketFactoryName", p, shell);
			applyProperty(PROXY_CLASS_NAME, "ProxyClassName", p, shell);
			applyProperty(PROXY_HOST, "ProxyHost", p, shell);
			applyProperty(PROXY_PORT, "ProxyPort", p, shell);
			applyProperty(THREAD_GROUP_NAME, "ThreadGroupName", p, shell);
			applyProperty(THREAD_GROUP_DAEMON, "ThreadGroupDaemon", p, shell);
			applyProperty(CUSTOM_SHELL, "CustomShell", p, shell);
			applyProperty(CUSTOM_SHELL_ENABLED, "CustomShellEnabled", p, shell);
			applyProperty(FAIL_ON_INCOMPLETE, "FailOnIncomplete", p, shell);
			
			shell.init();
			return shell;
		} catch (Exception e) {
			log.error("Failed to initialize shell", e);
			throw new Exception("Failed to initialize shell", e);
		}
	}
	
	public static SSHShell getSSHShell(URL url) {
		return null;
	}
	
	public static SSHShell getSSHShell(String...args) {
		return null;
	}
	
	/**
	 * Consitionally applies an attribute to the shell.
	 * @param name The property name.
	 * @param attribute The name of the attribute to set.
	 * @param props The supplied properties.
	 * @param shell The shell to set the attribute on.
	 */
	protected static void applyProperty(String name, String attribute, Properties props, SSHShell shell) {
		String value = props.getProperty(name);		
		if(value!=null) {
			try {
				BeanHelper.setAttribute(attribute, value, shell);
			} catch (Exception e) {
				log.warn("Attribute set for [" + attribute + "] failed for value [" + value + "]");
			}
		}
	}
	
	
}

