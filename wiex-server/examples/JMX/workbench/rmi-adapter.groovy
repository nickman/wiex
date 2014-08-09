import javax.management.*;
import javax.naming.*;
import javax.management.openmbean.*;

class RMIAdapter {
        static Map connections = new HashMap();
	
        public static void getInstance(String url, Closure closure) {
                if(!connections.containsKey(url)) { 
                        Properties p = new Properties();
                        p.put(Context.PROVIDER_URL, url);
                        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
                        p.put("jnp.disableDiscovery", "true");
                        Context ctx = new InitialContext(p);
                        MBeanServerConnection rmi = (MBeanServerConnection)ctx.lookup("/jmx/rmi/RMIAdaptor");
                        ctx.close();
                        connections.put(url, rmi);
                }
                MBeanServerConnection connection = (MBeanServerConnection)connections.get(url);
                closure.call(connection);
        }       
}

RMIAdapter.getInstance("192.168.31.221:21099", {
        String hostName = (String)it.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "HostName");
        println "Host:${hostName}"
	CompositeData cd = it.getAttribute(new ObjectName("java.lang:name=PS Perm Gen,type=MemoryPool"), "Usage");
	//println "Usage:${cd}"
	println "Type:${cd.getCompositeType().getClass().getName()}"
	println "Description:${cd.getCompositeType().getDescription()}"
	println "Keys:"
	cd.getCompositeType().keySet().each() { key ->
		println "\t${key}:${cd.get(key)}"
	}
});