wiex
====

Wily Introscope Extensions

Download JBoss 4.2.3 (http://superb-dca3.dl.sourceforge.net/project/jboss/JBoss/JBoss-4.2.3.GA/jboss-4.2.3.GA-jdk6.zip)

Replace:

 * [jboss-home]\client\trove.jar 
 * [jboss-home]\server\default\deploy\jboss-aop-jdk50.deployer\trove.jar
 
with trove4j 3.0.2:

```
<dependency>
  <groupId>net.sf.trove4j</groupId>
  <artifactId>trove4j</artifactId>
  <version>3.0.2</version>
</dependency>
```
 
Also here: https://repository.sonatype.org/service/local/repositories/central-proxy/content/net/sf/trove4j/trove4j/3.0.2/trove4j-3.0.2.jar

Add this to static [jboss-home]\server\default\conf\jboss-service.xml after the section titled:

**Log4j Initialization**

```
   <mbean code="com.heliosapm.wiex.server.server.boot.WIEXBootstrap" 
      name="com.heliosapm.wiex:service=WIEXBootstrap">
      <attribute name="WiexProperties">
				wiex.jmx.domain=jboss
				wiex.tracer.class.name=com.heliosapm.wiex.tracing.tracing.LoggingTracer      	
      </attribute>      
   </mbean>


   <mbean code="com.heliosapm.wiex.server.server.boot.JMXMPServerConnectorService"
      name="com.heliosapm.wiex.jmx.remoting:service=JMXMPConnector,port=18089,target=platform">
      <attribute name="JmxServiceUrl">service:jmx:jmxmp://0.0.0.0:4089</attribute>
      <attribute name="TargetServer">platform</attribute>      
   </mbean>

   
   <mbean code="com.heliosapm.wiex.server.server.boot.JMXMPServerConnectorService"
      name="com.heliosapm.wiex.jmx.remoting:service=JMXMPConnector,port=18088,target=jboss">
      <attribute name="JmxServiceUrl">service:jmx:jmxmp://0.0.0.0:4088</attribute>
      <attribute name="TargetServer">jboss</attribute>      
   </mbean>

```
