import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.chart.renderer.xy.*;



public String getMimeType() {
	return "text/html";
}

public byte[] render() {
	String greyc = "#C0C0C0";
	String noc = "#FFFFFF";

	String[][] resultSet = null;
	String colour = null;
	boolean grey = false;

	try {
		resultSet = (String[][])localMBeanServer.getAttribute(targetMBean, targetAttribute);
	} catch (Exception e) {
		println "Failed to acquire result set:${e}"
		return null;	
	}
	StringBuilder buff = new StringBuilder('<table border="1">');
	buff.append("<tr><th>${resultSet[0][0]}</th><th>${resultSet[0][1]}</th></tr>");
	int i = 0;
	int total = 0;
	resultSet.each() {
		if(i>0) {
			if(grey) {
				colour = greyc;
			} else {
				colour = noc;	
			}		
			buff.append("<tr><td bgcolor=\"${colour}\">${resultSet[i][0].replace("BPRODPAYN", "")}</td><td bgcolor=\"${colour}\">${resultSet[i][1]}</td></tr>");		
			total+= Integer.parseInt(resultSet[i][1]);
		}
		i++;
		grey = !grey;
	}
	if(grey) {
		colour = greyc;
	} else {
		colour = noc;	
	}			
	buff.append("<tr><td bgcolor=\"${colour}\"><b>Total</b></td><td bgcolor=\"${colour}\">${total}</td></tr></table>");		


	ByteArrayOutputStream out = new ByteArrayOutputStream();
	out.write(buff.toString().getBytes());
	out.flush();
	return out.toByteArray();
}
