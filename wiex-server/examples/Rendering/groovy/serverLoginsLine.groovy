import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.ui.*;
import org.jfree.data.time.*;
import org.jfree.chart.renderer.xy.*;


public String getMimeType() {
	return "image/jpeg";
}

public byte[] render() {
	String[][] resultSet = null;
	String[] row = null;
	Map history = null;
	long historyKey = 0;
	
	try {
		history = (Map)localMBeanServer.getAttribute(targetMBean, targetAttribute);
	} catch (Exception e) {
		println "Failed to acquire result set:${e}"
		return null;	
	}
	TimeSeriesCollection tsc = new TimeSeriesCollection();
	Map seriesSet = new HashMap();
	TimeSeries timeSeries = null;
	history.each() {
		resultSet = it.value;
		historyKey = Long.parseLong(it.key.toString());
		resultSet.each() {
			row = it;			
			if(!seriesSet.containsKey(row[0])) {				
				timeSeries = new TimeSeries(row[0].replace("BPRODPAYN", ""), FixedMillisecond.class);
				seriesSet.put(row[0], timeSeries);
				tsc.addSeries(timeSeries);
			}
			timeSeries = seriesSet.get(row[0]);
			timeSeries.add(new FixedMillisecond(historyKey), Integer.parseInt(row[1]));			
		}
	}
	
	
	JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Time", "Logins", tsc, true, false, false);
	XYPlot plot = (XYPlot)chart.getPlot();
	XYItemRenderer r = plot.getRenderer();
	if (r instanceof XYLineAndShapeRenderer) {
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
		renderer.setBaseShapesVisible(true);
		renderer.setBaseShapesFilled(true);
	}
	
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	int vSize = Integer.parseInt(chartVSize.toString());
	int hSize = Integer.parseInt(chartHSize.toString());
	ChartUtilities.writeChartAsJPEG(out, chart, hSize, vSize);
	out.flush();
	return out.toByteArray();
}
