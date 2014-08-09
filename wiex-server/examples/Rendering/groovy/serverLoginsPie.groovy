import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.ui.*;
import java.text.*;


public String getMimeType() {
	return "image/jpeg";
}

public byte[] render() {
	String[][] resultSet = null;
	try {
		resultSet = (String[][])localMBeanServer.getAttribute(targetMBean, targetAttribute);
	} catch (Exception e) {
		println "Failed to acquire result set:${e}"
		return null;	
	}

	DefaultKeyedValues dfk = new DefaultKeyedValues();
	int i = 0;
	resultSet.each() {
		if(i!=0) dfk.addValue(it[0].replace("BPRODPAYN", ""), Integer.parseInt(it[1]));
		i++;
	}
	
	//JFreeChart chart = new JFreeChart(title.toString(), JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	
	
	DefaultPieDataset dpd = new DefaultPieDataset(dfk);
	JFreeChart chart = ChartFactory.createPieChart(title, dpd, true, false, false); 
	PiePlot plot = (PiePlot)chart.getPlot();

	plot.setIgnoreZeroValues(true);
	plot.setLabelGenerator(new StandardPieSectionLabelGenerator());
	plot.setInsets(new RectangleInsets(0.0D, 5D, 5D, 5D));
	plot.setLabelGenerator(new StandardPieSectionLabelGenerator(label));

	
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	int vSize = Integer.parseInt(chartVSize.toString());
	int hSize = Integer.parseInt(chartHSize.toString());
	ChartUtilities.writeChartAsJPEG(out, chart, hSize, vSize);
	out.flush();
	return out.toByteArray();
}
