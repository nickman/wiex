import groovy.sql.Sql
import groovy.text.*;

String checktemplate = "C:\\projects3.2\\SBSCollectors\\examples\\templates\\DBAvailabilityCheck.xml";
String output = "C:\\projects3.2\\SBSCollectors\\examples\\templates\\db-availability-check-service.xml";
File f = new File(output);
f.delete();
HashMap binding = new HashMap();
FileWriter out = new FileWriter(output, true);
//Writer out = new BufferedWriter(new OutputStreamWriter(System.out));
engine = new SimpleTemplateEngine();
FileReader fr = new FileReader(checktemplate);
template = engine.createTemplate(fr);



oracle = Sql.newInstance("jdbc:oracle:thin:@192.168.31.121:1521:NETPROD", "PAYNET", "PAYNET", "oracle.jdbc.driver.OracleDriver");

oracle.eachRow("select rtrim(system_n) DBNAME, rtrim(svcctr_n) REGION from service_center", {
	binding.put("mbean", "com.adp.sbs.easypaynet.database:service=DatabaseAvailability,type=DB2,name=${it.DBNAME}");
	binding.put("type", "DB2");
	binding.put("name", "${it.DBNAME}(${it.REGION})");
	binding.put("query", "SELECT CURDATE() FROM SYSIBM.SYSDUMMY1");
	binding.put("driver", "com.ibm.as400.access.AS400JDBCDriver");
	binding.put("url", "jdbc:as400://${it.DBNAME}//dbpcpay; date format=USA");
	binding.put("user", "DBPCPAY");
	binding.put("password", "DBPCPAY");
	binding.put("timeout", "10000");
	out << template.make(binding);
	out.flush();
	
});

