/**
 * 
 */
package com.heliosapm.wiex.jmx.remote.alias;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.*;
import javax.naming.Context;
import javax.naming.InitialContext;




import com.heliosapm.wiex.jmx.dynamic.ManagedObjectDynamicMBean;
import com.heliosapm.wiex.jmx.dynamic.annotation.*;

/**
 * <p>Title: RemoteAliasFactoryService</p>
 * <p>Description: Sets up and tears down remote alias MBeans.</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

@JMXManagedObject (annotated=true, declared=true)
public class RemoteAliasFactoryService extends ManagedObjectDynamicMBean {

	protected ObjectName cluster = null;
	protected Map<String, MBeanServerConnection> remoteServers = new ConcurrentHashMap<String, MBeanServerConnection>();
	protected Map<String, String> remoteServerNames = new ConcurrentHashMap<String, String>();
	
	public RemoteAliasFactoryService() {
		
	}
	
	@JMXOperation (name="start", expose=true, description="Starts The RemoteAliasFactory Service")
	public void start() throws Exception {
		
	}
	
	@JMXOperation (name="stop", expose=true, description="Stops The RemoteAliasFactory Service")
	public void stop() {
		
	}
	
	/**
	 * Retrieves a list of cluster nodes from the cluster mbean and removes the current one.
	 * The remainder are returned in a list.
	 * @return A list of remote cluster nodes.
	 */
	public List getClusterNodes() {
		List nodes = new ArrayList();
		try {
			Vector currentView = (Vector)server.getAttribute(cluster, "CurrentView");
			String nodeName = (String)server.getAttribute(cluster, "NodeName");
			currentView.remove(nodeName);
			nodes.addAll(currentView);
		} catch (Exception e) {
			// log error message
		}
		return nodes;
	}
	
	public MBeanServerConnection aliasRemoteNode(String nodeName) {
		Context ctx = null;
		Properties p = new Properties();
		p.put(Context.PROVIDER_URL, nodeName);
		p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		p.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		p.put("jnp.disableDiscovery", true);
		try {
			ctx = new InitialContext(p);
			MBeanServerConnection remoteServer = (MBeanServerConnection)ctx.lookup("/jmx/rmi/RMIAdaptor");
			remoteServers.put(nodeName, remoteServer);
			initMBeans(remoteServer, nodeName);
			return remoteServer;
		} catch (Exception e) {
			throw new RuntimeException("Failed to alias remote mbean server", e);
		} finally {
			try { ctx.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Looks up the MBeanRegistry for the remote server and instantiates a local alias for each one.
	 * @param remoteServer
	 * @throws IOException
	 * @throws AttributeNotFoundException
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws NullPointerException
	 * @throws InstanceAlreadyExistsException
	 * @throws NotCompliantMBeanException
	 */
	public void initMBeans(MBeanServerConnection remoteServer, String nodeName) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, InstanceAlreadyExistsException, NotCompliantMBeanException {
		String hostName = (String) remoteServer.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostName");
		remoteServerNames.put(nodeName, hostName);
		for(ObjectName on : (Set<ObjectName>)remoteServer.queryNames(null, null)) {
			ObjectName aliasObjectName = new ObjectName(hostName + "." + on.getDomain(), on.getKeyPropertyList());
			RemoteAliasMBean remoteAliasMBean = new RemoteAliasMBean(remoteServer, aliasObjectName);
			server.registerMBean(remoteAliasMBean, aliasObjectName);			 
		}
	}
	
	/**
	 * Unregisters the alias mbeans registered for this host name.
	 * @param nodeName
	 * @throws MalformedObjectNameException
	 */
	public void tearDownMBeans(String nodeName) throws MalformedObjectNameException {
		String hostName = remoteServerNames.get(nodeName);		
		for(ObjectName on : (Set<ObjectName>)server.queryNames(new ObjectName(hostName + ".*:*"), null)) {
			try {
				server.unregisterMBean(on);
			} catch (Exception e) {}
		}
	}

	/**
	 * Callback from TopologyListener when a node enters or leaves the cluster.
	 * ArrayLists contains AddressPort objects. The toString format of them is "{host("+addr+"), port("+port+")}"
	 * @param deadMembers A list of nodes that dropped off the cluster.
	 * @param newMembers A list of nodes that joined the cluster.
	 * @param allMembers
	 * @param logCategoryName
	 * @see org.jboss.ha.framework.server.util.TopologyMonitorService
	 */
	@JMXOperation(description="Callback Entry Point from Cluster on Membership Change", expose=true, name="membershipChanged")
	public void membershipChanged(ArrayList deadMembers, ArrayList newMembers, ArrayList allMembers, String logCategoryName) {
		//AddressPort node = null;
		String address = null;
	}
	
	
}
