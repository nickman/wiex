/**
 * 
 */
package com.heliosapm.wiex.server.tracing.ssh;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StreamTokenizer;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.helios.jcraft.jsch.Channel;
import org.helios.jcraft.jsch.JSch;
import org.helios.jcraft.jsch.Session;
import org.helios.jcraft.jsch.UserInfo;


/**
 * SSH Shell client.
 * This class is intended to streamline the *nix SSH data collection by keeping 
 * the shell session open instead of re-opening a new SSH session on every call.
 * @author WhiteheN
 * TODO: Capture stats: total timeouts. (need to trap different exceptions) Expose specific averages as attributes.
 * TODO: Optionally expose as MBean.
 * TODO: Capture error stream.
 * TODO: synchronization
 * TODO: Shell Factory
 * TODO: Implement proxy and socket factory options.
 * TODO: String non-ascii option.
 * TODO: Decay Compensation: watch Incomplete responses, adjust waitCycle and requestEndTimeOut
 * TODO: timeout values of 0 mean no timeout.
 */
public class SSHShell implements UserInfo {
	protected Logger log = null;
	protected StringBuilder userMessages = new StringBuilder();
	protected boolean connected = false;
	protected boolean customShellEnabled = true;
	protected boolean failOnIncomplete = true;
	
	//=============================================
	//       SSH Authentication and Net
	//=============================================		
	protected String hostName = null;
	protected String userName = null;
	protected String password = null;
	protected String passPhrase = null;
	protected String knownHostsFile = null;
	protected int port = 22;
	protected String privateKey = null;
	
	
	//=============================================
	//       Stream tokens & formatting
	//=============================================	
	protected String prompt = null;
	protected String commandSuffix = "\n";
	protected String endOfLine = "\r\n";
	protected String customShell = null;
	
	
	
	//=============================================
	//       SSH transport details
	//=============================================	
	protected int connectTimeOut = 3000;
	protected long requestTimeOut = 1000;
	protected long requestEndTimeOut = 500;
	protected long waitCycle = 100;
	protected int readBufferSize = 8092;
	protected byte[] readBuffer = null;
	protected StringBuilder resultBuffer = new StringBuilder();
	protected String socketFactoryClassName = null;
	
	//=============================================
	//       SSH through a Proxy Options
	//=============================================	
	protected String proxyClassName = null;
	protected String proxyHost = null;
	protected int proxyPort = -1;
	
	//=============================================
	//       JCraft Constructs
	//=============================================
	protected JSch jsch=new JSch();
	protected Session session=null;
	protected Channel channel=null;
	protected String threadGroupName = null;
	protected ThreadGroup threadGroup = null;
	protected boolean threadGroupDaemon = true;
	
	//=============================================
	//       Input and Output Stream Handlers
	//=============================================
	
    // Output Handler
    protected PipedOutputStream pipeOut = null;
    protected PipedInputStream sshOutput = null;
    // Input Command Handler
    protected PipedInputStream commandStream = null;
    protected PipedOutputStream sshInput = null;
    protected OutputStreamWriter commander = null;

	//=============================================
	//       Shell Stats.
	//=============================================
    protected long connectElapsedTime = 0;
    protected long connectTime = 0;
	protected long totalBytesIn = 0;
	protected long totalBytesOut = 0;
	protected long totalRequestElapsedTime = 0;
	protected long totalRequests = 0;
	protected long totalErrors = 0;
	protected long totalTimeOuts = 0;
	protected long totalWaitCycles = 0;
	protected long totalWaitCycleTime = 0;
	protected long totalIncompleteResponses = 0;
	protected long disconnects = 0;
	
	
	
	

	/**
	 * Creates a new SSHShell.
	 */
	public SSHShell() {
		log = Logger.getLogger(getClass());
	}
	
	/**
	 * @throws Exception
	 */
	public void init() throws Exception {
		try {
			// Reset Stats
			resetStats();
			// Re-Assign Logger
			log = Logger.getLogger(userName + "." + hostName + "." + port);
			// Conditionally set private key and passphrase
			if(privateKey!=null) {
				jsch.addIdentity(privateKey, passPhrase);
			}
			// Conditionally set knownHosts
			if(knownHostsFile!=null) {
				jsch.setKnownHosts(knownHostsFile);
			}			
			// Conditionally set ThreadGroup
			if(threadGroupName!=null) {
				threadGroup = new ThreadGroup(threadGroupName);
				threadGroup.setDaemon(threadGroupDaemon);
			}
			long start = System.currentTimeMillis();
			// Initiate Session and Channel
			session=jsch.getSession(userName, hostName, port);
			session.setUserInfo(this);
			session.connect(connectTimeOut);
			session.setTimeout((int)requestTimeOut);
			if(threadGroup!=null) {
				channel=session.openChannel("shell", threadGroup);
			} else {
				channel=session.openChannel("shell");
			}
				
			// Initialize Shell Output Handler
			pipeOut = new PipedOutputStream();
			sshOutput = new PipedInputStream(pipeOut);
			// Initialize Shell Input Handler
			commandStream = new PipedInputStream();
			sshInput = new PipedOutputStream(commandStream);
			commander = new OutputStreamWriter(sshInput);
			// Configure shell streams
		    channel.setInputStream(commandStream);
		    channel.setOutputStream(pipeOut);
			// Create sized read buffer
		    readBuffer = new byte[readBufferSize];		    
			// Connect the channel
		    channel.connect();
		    // Initialize the shell prompt
		    initializeShell();
		    connectTime = System.currentTimeMillis();
		    connectElapsedTime = connectTime-start;
		    connected=true;
		} catch (Exception e) {
			close();
			log.error("Failed to initialized SSHShell", e);
			throw new Exception("Failed to initialized SSHShell", e);
		}
	}
	
	/**
	 * Post connect procedure to determine the prompt signature.
	 * @throws Exception
	 */
	protected void initializeShell() throws Exception {
		resultBuffer.setLength(0);
		sshWait(sshOutput, requestTimeOut, waitCycle, true);
		while(sshOutput.available()>0) {
			int size = sshOutput.read(readBuffer, 0, readBuffer.length);
			if(size==-1) break;
			resultBuffer.append(new String(readBuffer, 0, size));
			totalBytesOut += size;
			sshWait(sshOutput, requestEndTimeOut*5, waitCycle, false);
		}
		resultBuffer.reverse().toString().split("\\s+");
		String[] loginFragments = resultBuffer.reverse().toString().split("\\s+");	      
		prompt = loginFragments[loginFragments.length-1];
		log.info("\n\tPrompt is [" + prompt + "]");
	}
	
	/**
	 * @param message
	 * @return
	 * @see org.helios.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
	 */
	public boolean promptPassphrase(String message) {
		return false;
	}

	/**
	 * Resets collected performance stats. 
	 */
	public void resetStats() {
		totalBytesIn = 0;
		totalBytesOut = 0;
		totalRequests = 0;
		totalRequestElapsedTime = 0;	
		totalErrors = 0;
		totalTimeOuts = 0;		
		totalWaitCycles = 0;
		totalWaitCycleTime = 0;
		totalIncompleteResponses = 0;
	}
	
	/**
	 * Prints the current shell's performance stats.
	 * @return
	 */
	public String printStats() {
		StringBuilder buff = new StringBuilder("Shell Stats For ");
		buff.append(log.getName()).append("\n==================================");
		buff.append("\n\tTotal Requests:").append(totalRequests);
		buff.append("\n\tTotal Request Elapsed Time:").append(totalRequestElapsedTime);
		buff.append("\n\tAverage Request Time:").append(getAverageRequestStat(totalRequestElapsedTime));
		buff.append("\n\tAverage Response Size:").append(getAverageRequestStat(totalBytesOut));
		buff.append("\n\tTotal Bytes In:").append(totalBytesIn);
		buff.append("\n\tTotal Bytes Out:").append(totalBytesOut);		
		buff.append("\n\tConnect Elapsed Time:").append(connectElapsedTime);
		buff.append("\n\tTotal Connected Time:").append(System.currentTimeMillis() - connectTime);
		buff.append("\n\tTotal Wait Cycles:").append(totalWaitCycles);
		buff.append("\n\tTotal Wait Cycle Time:").append(totalWaitCycleTime);
		buff.append("\n\tAverage Request Wait Cycle Time:").append(getAverageRequestStat(totalWaitCycleTime));
		buff.append("\n\tAverage Request Wait Cycle Count:").append(getAverageRequestStat(totalWaitCycles));
		buff.append("\n\tTotal Request Errors:").append(totalErrors);
		buff.append("\n\tTotal Incomplete Responses:").append(totalIncompleteResponses);
		buff.append("\n");
		return buff.toString();
	}
	
	
	/**
	 * Returns the average request elapsed time.
	 * @return
	 */
	public long getAverageRequestStat(long total) {
		try {
			double avg = (double)total/(double)totalRequests;
			return (long)avg;
		} catch (Exception e) {
			return 0;
		}
	}
	
	
	
	/**
	 * Closes the Session and Channel.
	 */
	public void close() {
		if(connected) {
			connected=false;
			disconnects++;
		}		
		try { channel.disconnect(); } catch (Exception e) {}		
		try { session.disconnect(); } catch (Exception e) {}
		try { sshOutput.close(); } catch (Exception e) {}
		try { sshInput.close(); } catch (Exception e) {}
		try { commander.close(); } catch (Exception e) {}
		
		long sessionTime = System.currentTimeMillis() - connectTime;
		log.debug("Shell disconnected after session time of " + sessionTime + " ms.");
	}
	
	/**
	 * Issues a shell command and returns the output.
	 * @param command The shell command to execute.
	 * @return The result of the shell command.
	 * @throws Exception
	 */
	public String issueCommandX(String command) throws Exception {
		if(customShell!=null && customShellEnabled) {
			command = customShell.replace("##COMMAND##", command);
		}
		try {
			if(!connected) {
				close();				
				throw new Exception("Channel Found Disconnected. Expected");
			}
			totalRequests++;
			long start = System.currentTimeMillis();
			resultBuffer.setLength(0);
			String actualCommand = command + commandSuffix; 
			commander.write(actualCommand);
			totalBytesIn += actualCommand.getBytes().length;
			String commandToken = command + endOfLine;
			commander.flush();
			pipeOut.flush();
			sshWait(sshOutput, requestTimeOut, waitCycle, true);
			pipeOut.flush();
			while(sshOutput.available()>0) {
				int size = sshOutput.read(readBuffer, 0, readBuffer.length);		    	  
				if(size==-1) break;
				resultBuffer.append(new String(readBuffer, 0, size));
				totalBytesOut += size;
				pipeOut.flush();
				sshWait(sshOutput, requestEndTimeOut, waitCycle, false);
			}
			int index = resultBuffer.indexOf(commandToken);
			if(index != -1) {
				resultBuffer.delete(index, commandToken.length());
			}
			index = resultBuffer.indexOf(prompt);
			if(index != -1) {
				resultBuffer.delete(index, index+prompt.length());
			} else {				 
				totalIncompleteResponses++;
				if(failOnIncomplete) {
					log.error("\n\n\tIncomplete Output for command [" + command + "]\n\n==============================================\n" + resultBuffer + "\n==============================================\n\n\n" );
					throw new Exception("Response Was Incomplete for command [" + command + "]");
				}
			}
			totalRequestElapsedTime += System.currentTimeMillis()-start;
			return resultBuffer.toString();
		} catch (Exception e) {
			totalErrors++;
			log.error("Failed to process command", e);
			throw new Exception("Failed to process command", e);
		}
	}
	
	/**
	 * Issues a shell command and returns the output.
	 * @param command The shell command to execute.
	 * @return The result of the shell command.
	 * @throws Exception
	 */
	public String issueCommand(String command) throws Exception {
		if(customShell!=null && customShellEnabled) {
			command = customShell.replace("##COMMAND##", command);
		}
		try {
			if(!connected) {
				close();				
				throw new Exception("Channel Found Disconnected. Expected");
			}
			totalRequests++;
			long start = System.currentTimeMillis();
			resultBuffer.setLength(0);
			String actualCommand = command + commandSuffix; 
			commander.write(actualCommand);
			totalBytesIn += actualCommand.getBytes().length;
			String commandToken = command + endOfLine;
			commander.flush();
//			InputStreamReader isr = new InputStreamReader(sshOutput); 
//			StreamTokenizer streamTokenizer = new StreamTokenizer(isr);
			
			pipeOut.flush();
			// Wait for shell response to become available
			sshWait(sshOutput, requestTimeOut, waitCycle, true);
			
			
			// in this version, we will wait for the prompt to show up in the output
			// until the socket request times out.
			
			
//			while(sshOutput.available()>0) {
//				int size = sshOutput.read(readBuffer, 0, readBuffer.length);		    	  
//				if(size==-1) break;
//				resultBuffer.append(new String(readBuffer, 0, size));
//				totalBytesOut += size;
//				pipeOut.flush();
//				sshWait(sshOutput, requestEndTimeOut, waitCycle, false);
//			}
			int index = -1;
			long startRead = System.currentTimeMillis();
			while(true) {				
				int size = sshOutput.read(readBuffer, 0, readBuffer.length);		    	  
				if(size>0) {
					totalBytesOut += size;
					resultBuffer.append(new String(readBuffer, 0, size));
					if(resultBuffer.indexOf(prompt) != -1) {
						break;
					}
				} else {
					Thread.sleep(waitCycle);					
					pipeOut.flush();					
					if(sshOutput.available()<1 && System.currentTimeMillis()-startRead > requestTimeOut) {
						break;
					}					
				}							
			}
			
			
			
			index = resultBuffer.indexOf(commandToken);
			if(index != -1) {
				resultBuffer.delete(index, commandToken.length());
			}
			index = resultBuffer.indexOf(prompt);
			if(index != -1) {
				resultBuffer.delete(index, index+prompt.length());
			} else {				 
				totalIncompleteResponses++;
				if(failOnIncomplete) {
					log.error("\n\n\tIncomplete Output for command [" + command + "]\n\n==============================================\n" + resultBuffer + "\n==============================================\n\n\n" );
					throw new Exception("Response Was Incomplete for command [" + command + "]");
				}
			}
			totalRequestElapsedTime += System.currentTimeMillis()-start;
			return resultBuffer.toString();
		} catch (Exception e) {
			totalErrors++;
			log.error("Failed to process command", e);
			throw new Exception("Failed to process command", e);
		}
	}
	
/*
    // Output Handler
    protected PipedOutputStream pipeOut = null;
    protected PipedInputStream sshOutput = null;
    // Input Command Handler
    protected PipedInputStream commandStream = null;
    protected PipedOutputStream sshInput = null;
    protected OutputStreamWriter commander = null;

 */	
	
	
	/**
	 * Returns true if the shell is connected.
	 * Returns false if it is not.
	 * @return
	 */
	public boolean isConnected() {
		return connected; 
	}
	

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log("SSHShell Test");
		BasicConfigurator.configure();
		SSHShell shell = null;
		// Manual Set Up
//		shell = new SSHShell();
//		log("Configuring Shell");
//		shell.setWaitCycle(10);
//		shell.setRequestEndTimeOut(100);
//		shell.setUserName("jboss");
//		shell.setHostName("PAR1TNET64");
//		shell.setPrivateKey("C://SBSMonitorServer//server//default//keys//collector_dsa");
//		shell.setKnownHostsFile("C:\\Documents and Settings\\whitehen\\.ssh\\known_hosts");
//		shell.setPassPhrase("Hello World");
//		shell.setThreadGroupName("SSHShell Test");
		// Factory Setup
		Properties p = new Properties();
		p.put(SSHShellFactory.WAIT_CYCLE, "20");
		p.put(SSHShellFactory.REQUEST_END_TIMEOUT, "120");
		p.put(SSHShellFactory.USER_NAME, "jboss");
		p.put(SSHShellFactory.HOST_NAME, "PAR1TNET64");
		p.put(SSHShellFactory.PRIVATE_KEY, "C://SBSMonitorServer//server//default//keys//collector_dsa");
		p.put(SSHShellFactory.KNOWN_HOSTS, "C:\\Documents and Settings\\whitehen\\.ssh\\known_hosts");
		p.put(SSHShellFactory.PASSPHRASE, "Hello World");
		p.put(SSHShellFactory.THREAD_GROUP_NAME, "SSHShell Test");
		p.put(SSHShellFactory.THREAD_GROUP_DAEMON, "false");
		p.put(SSHShellFactory.CUSTOM_SHELL, "/bin/bash -c \"##COMMAND##\"");
		p.put(SSHShellFactory.CUSTOM_SHELL_ENABLED, "true");
		p.put(SSHShellFactory.FAIL_ON_INCOMPLETE, "false");
		
		try {
			try {
				FileOutputStream fosp = new FileOutputStream("C:\\projects3.3\\SBSCollectors\\conf\\ssh\\ssh-factory.properties");
				FileOutputStream fosx = new FileOutputStream("C:\\projects3.3\\SBSCollectors\\conf\\ssh\\ssh-factory.xml");
				p.storeToXML(fosx, "Sample SSHFactory Properties");
				p.store(fosp, "Sample SSHFactory Properties");
				fosx.flush(); fosx.close();
				fosp.flush(); fosp.close();
			} catch (Exception e) {
				log("Failed to save props:" + e);
			}
			shell = SSHShellFactory.getSSHShell(p);
			log("Initializing Shell");
			
			//shell.init();
			log("Shell Initialized. Prompt:[" + shell.getPrompt() + "]");
			log("Shell Connected:" + shell.isConnected());

			
			for(int i = 0; i < 1; i++) {
				log(shell.issueCommand("ls -l"));
				log("============================================================");
				log(shell.issueCommand("set"));
				log("============================================================");
				log(shell.issueCommand("ps -ef"));
				log("============================================================");
				//log(shell.issueCommand("sleep 20"));
				//Thread.sleep(1000);
			}
			log(shell.printStats());
		} catch (Exception e) {
			log("Shell Initialization Error");
			e.printStackTrace();
		} finally {
			try { shell.close(); } catch (Exception e) {}
		}
		
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}
	
	
	

	/**
	 * Method to implement a configurable pause time to wait for more IO to become available
	 * on an input stream.
	 * @param inputStream The input stream to test and wait on.  
	 * @param timeOut The total period in ms. that can elapsed before the wait expires.
	 * @param waitCycle The micro wait period between looping tests on the inpout stream.
	 * @param exc If true, the method will throw a timeout exception if no IO becomes 
	 * available before the timeout period. If false, the method will return when 
	 * the timeout period expires.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws RuntimeException
	 */
	protected void sshWait(InputStream inputStream, long timeOut,
			long waitCycle, boolean exc) throws IOException,
			InterruptedException, RuntimeException {
		long start = System.currentTimeMillis();
		while(inputStream.available()<1) {
			  Thread.sleep(waitCycle);
			  totalWaitCycles++;
			  totalWaitCycleTime+= waitCycle;
			  if(System.currentTimeMillis()-start > timeOut) {
				  if(exc) {
					  totalTimeOuts++;
					  throw new RuntimeException("Request Timed Out");
				  } else {
					  break;
				  }
			  }
		  }
	}
	
	

	
	//=======================================
	//        UserInfo Implementation
	//=======================================
	
	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}



	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}



	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}



	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}



	/**
	 * @return the passPhrase
	 */
	public String getPassPhrase() {
		return passPhrase;
	}



	/**
	 * @param passPhrase the passPhrase to set
	 */
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}



	/**
	 * @return the knownHostsFile
	 */
	public String getKnownHostsFile() {
		return knownHostsFile;
	}



	/**
	 * @param knownHostsFile the knownHostsFile to set
	 */
	public void setKnownHostsFile(String knownHostsFile) {
		this.knownHostsFile = knownHostsFile;
	}



	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}



	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}



	/**
	 * @return the privateKey
	 */
	public String getPrivateKey() {
		return privateKey;
	}



	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}



	/**
	 * @return the commandSuffix
	 */
	public String getCommandSuffix() {
		return commandSuffix;
	}



	/**
	 * @param commandSuffix the commandSuffix to set
	 */
	public void setCommandSuffix(String commandSuffix) {
		this.commandSuffix = commandSuffix;
	}



	/**
	 * @return the endOfLine
	 */
	public String getEndOfLine() {
		return endOfLine;
	}



	/**
	 * @param endOfLine the endOfLine to set
	 */
	public void setEndOfLine(String endOfLine) {
		this.endOfLine = endOfLine;
	}



	/**
	 * @return the connectTimeOut
	 */
	public int getConnectTimeOut() {
		return connectTimeOut;
	}



	/**
	 * @param connectTimeOut the connectTimeOut to set
	 */
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}



	/**
	 * @return the requestTimeOut
	 */
	public long getRequestTimeOut() {
		return requestTimeOut;
	}



	/**
	 * @param requestTimeOut the requestTimeOut to set
	 */
	public void setRequestTimeOut(long requestTimeOut) {
		this.requestTimeOut = requestTimeOut;
	}



	/**
	 * @return the waitCycle
	 */
	public long getWaitCycle() {
		return waitCycle;
	}



	/**
	 * @param waitCycle the waitCycle to set
	 */
	public void setWaitCycle(long waitCycle) {
		this.waitCycle = waitCycle;
	}



	/**
	 * @return the readBufferSize
	 */
	public int getReadBufferSize() {
		return readBufferSize;
	}



	/**
	 * @param readBufferSize the readBufferSize to set
	 */
	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}



	/**
	 * @return the socketFactoryClassName
	 */
	public String getSocketFactoryClassName() {
		return socketFactoryClassName;
	}



	/**
	 * @param socketFactoryClassName the socketFactoryClassName to set
	 */
	public void setSocketFactoryClassName(String socketFactoryClassName) {
		this.socketFactoryClassName = socketFactoryClassName;
	}



	/**
	 * @return the proxyClassName
	 */
	public String getProxyClassName() {
		return proxyClassName;
	}



	/**
	 * @param proxyClassName the proxyClassName to set
	 */
	public void setProxyClassName(String proxyClassName) {
		this.proxyClassName = proxyClassName;
	}



	/**
	 * @return the proxyHost
	 */
	public String getProxyHost() {
		return proxyHost;
	}



	/**
	 * @param proxyHost the proxyHost to set
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}



	/**
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		return proxyPort;
	}



	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}



	/**
	 * @return the prompt
	 */
	public String getPrompt() {
		return prompt;
	}



	/**
	 * @return the userMessages
	 */
	public StringBuilder getUserMessages() {
		return userMessages;
	}



	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}



	/**
	 * @return
	 * @see org.helios.jcraft.jsch.UserInfo#getPassphrase()
	 */
	public String getPassphrase() {
		return passPhrase;
	}



	/**
	 * @return
	 * @see org.helios.jcraft.jsch.UserInfo#getPassword()
	 */
	public String getPassword() {
		return password;
	}



	/**
	 * @param message
	 * @return
	 * @see org.helios.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
	 */
	public boolean promptPassword(String message) {
		return false;
	}



	/**
	 * @param message
	 * @return
	 * @see org.helios.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	public boolean promptYesNo(String message) {
		return false;
	}



	/**
	 * @param message
	 * @see org.helios.jcraft.jsch.UserInfo#showMessage(java.lang.String)
	 */
	public void showMessage(String message) {
		userMessages.append(message).append("\n");
	}
	
	//=============================================
	//       SSH Session Details
	//=============================================
	/**
	 * @return
	 */
	public String getSSHHost() {
		return session.getHostKey().getHost();
	}
	
	/**
	 * @return
	 */
	public String getSSHKey() {
		return session.getHostKey().getKey();
	}
	
	/**
	 * @return
	 */
	public String getSSHType() {
		return session.getHostKey().getType();
	}
	
	/**
	 * @return
	 */
	public String getSSHFingerPrint() {
		return session.getHostKey().getFingerPrint(jsch);
	}

	/**
	 * @return the requestEndTimeOut
	 */
	public long getRequestEndTimeOut() {
		return requestEndTimeOut;
	}

	/**
	 * @param requestEndTimeOut the requestEndTimeOut to set
	 */
	public void setRequestEndTimeOut(long requestEndTimeOut) {
		this.requestEndTimeOut = requestEndTimeOut;
	}

	/**
	 * @return the connectElapsedTime
	 */
	public long getConnectElapsedTime() {
		return connectElapsedTime;
	}

	/**
	 * @return the totalBytesIn
	 */
	public long getTotalBytesIn() {
		return totalBytesIn;
	}

	/**
	 * @return the totalBytesOut
	 */
	public long getTotalBytesOut() {
		return totalBytesOut;
	}

	/**
	 * @return the totalRequestElapsedTime
	 */
	public long getTotalRequestElapsedTime() {
		return totalRequestElapsedTime;
	}

	/**
	 * @return the totalRequests
	 */
	public long getTotalRequests() {
		return totalRequests;
	}

	/**
	 * @return the threadGroupName
	 */
	public String getThreadGroupName() {
		return threadGroupName;
	}

	/**
	 * @param threadGroupName the threadGroupName to set
	 */
	public void setThreadGroupName(String threadGroupName) {
		this.threadGroupName = threadGroupName;
	}

	/**
	 * @return the threadGroupDaemon
	 */
	public boolean isThreadGroupDaemon() {
		return threadGroupDaemon;
	}

	/**
	 * @param threadGroupDaemon the threadGroupDaemon to set
	 */
	public void setThreadGroupDaemon(boolean threadGroupDaemon) {
		this.threadGroupDaemon = threadGroupDaemon;
	}

	/**
	 * @return the totalErrors
	 */
	public long getTotalErrors() {
		return totalErrors;
	}

	/**
	 * @return the totalTimeOuts
	 */
	public long getTotalTimeOuts() {
		return totalTimeOuts;
	}

	/**
	 * @return the totalWaitCycles
	 */
	public long getTotalWaitCycles() {
		return totalWaitCycles;
	}

	/**
	 * @return the totalWaitCycleTime
	 */
	public long getTotalWaitCycleTime() {
		return totalWaitCycleTime;
	}

	/**
	 * @return the totalIncompleteResponses
	 */
	public long getTotalIncompleteResponses() {
		return totalIncompleteResponses;
	}

	/**
	 * @return the disconnects
	 */
	public long getDisconnects() {
		return disconnects;
	}
	
	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + port;
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SSHShell other = (SSHShell) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (port != other.port)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	/**
	 * @return the customShell
	 */
	public String getCustomShell() {
		return customShell;
	}

	/**
	 * @param customShell the customShell to set
	 */
	public void setCustomShell(String customShell) {
		this.customShell = customShell;
	}

	/**
	 * @return the customShellEnabled
	 */
	public boolean isCustomShellEnabled() {
		return customShellEnabled;
	}

	/**
	 * @param customShellEnabled the customShellEnabled to set
	 */
	public void setCustomShellEnabled(boolean customShellEnabled) {
		this.customShellEnabled = customShellEnabled;
	}

	/**
	 * @return the failOnIncomplete
	 */
	public boolean isFailOnIncomplete() {
		return failOnIncomplete;
	}

	/**
	 * @param failOnIncomplete the failOnIncomplete to set
	 */
	public void setFailOnIncomplete(boolean failOnIncomplete) {
		this.failOnIncomplete = failOnIncomplete;
	}
	

}

