/**
 * Example of a groovy script that dispatches a JMX object trace
 * to the JSR77Collector class.
 * Whitehead
 * $Revision: 1.2 $
 */

collectorKey = "JSR77Collector" + "~" + remoteObjectName.getKeyProperty("name");
collector = stateMap.get(collectorKey);

if(collector==null) {
	Class clazz = stateMap.get("JSR77Collector.Class");
	if(clazz==null) {
		shell = new GroovyShell();
		URL url = new URL("file:///c:/projects/Groovy/Introscope/jsr77.groovy");
		InputStream is = url.openStream();
		long start = System.currentTimeMillis();
		clazz = shell.evaluate(is);		
		long elapsed = System.currentTimeMillis()-start;
		stateMap.put("JSR77Collector.Class", clazz);
		is.close();				
	} 
	collector = clazz.newInstance();
	collector.init(binding);
	println "Collector:\n${collector.toString()}"
	stateMap.put(collectorKey, collector);	
}
collector.processJMXStats(jmxObject);
return null;


