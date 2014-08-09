/**
	Groovy Script to trace the performance of payroll batches on the AS/400 EasyPay Engine
	by reading the latest entries from the QS36F.CSBATCH table.
	Whitehead
	$Revision: 1.1 $
*/

import groovy.sql.Sql;
import java.text.*;

println "Executing csbatch.groovy"

static def INT_FORMAT="MMddyy";
static def TM_FORMAT="HHmmss";
static def SimpleDateFormat sdf = new SimpleDateFormat(INT_FORMAT);
static def SimpleDateFormat tdf = new SimpleDateFormat(TM_FORMAT);
//static def Binding binding = new Binding();

segment = null;
region = binding.getProperty("iseries.region");
println "CSBatch Region:${region}"
as400 = Sql.newInstance("jdbc:as400://${region}//dbpcpay; date format=USA", "dbpcpay", "dbpcpay", "com.ibm.as400.access.AS400JDBCDriver");
// Check to see if the csbatch query stream is initialized
Long initialBatchSeq = null; 
try { 
	initialBatchSeq = binding.getProperty("initialBatchSeq");
	segment = binding.getProperty("segmentPrefix");
	sp = binding.getProperty("segment.prefix");
	segment = tracer.buildSegment(segment, false, sp);
	binding.setProperty("segment", segment);
	println "\n\tSegment:${segment}"
} catch (Throwable t) { 
	initialBatchSeq = new Long(0);
	binding.setProperty("initialBatchSeq", initialBatchSeq);
	println "[${region}] initialBatchSeq initialized to ${initialBatchSeq}"
}
segment = binding.getProperty("segment");
if(initialBatchSeq.longValue()==0) {	
	today = sdf.format(new Date());
	println "Initializing Query Stream for date:${today}"
	row = as400.firstRow("select  max(CBSEQ#) SEQ  from QS36F.CSBATCH WHERE CBBTCD = ?", [today]);
	if(row.size()<1) return;
	println "SEQ:${row.getProperty("SEQ")} / ${row.getClass().getName()} / Size:${row.size()}"
	long batchSeq = row.getProperty("SEQ");
	initialBatchSeq = new Long(batchSeq);
	binding.setProperty("initialBatchSeq", initialBatchSeq);
	println "Initialized Query Stream with Batch Sequence:${initialBatchSeq}"
}
as400.eachRow("select CBJOBN,CBUSER,CBJOB# CBJOB, CBWKST,CBBTCD,CBBTCS,CBBTCE,CB#CLI CBCLI,CB#EMY CBEMY,CBCHKS,CB#RPG CBRPG,CB#INV CBINV,CB#QEC CBQEC,CBCKID,CBRPID,CBINID,CBPRTS,CBPRTE,CBSEQ# CBSEQ from QS36F.CSBATCH WHERE CBSEQ# > ? ORDER BY CBSEQ#", [initialBatchSeq.longValue()],  {		
	
	startTime = it.getString("CBBTCS").trim();
	endTime = it.getString("CBBTCE").trim();
	long sequence = it.CBSEQ;
	binding.setProperty("initialBatchSeq", new Long(sequence));
	if(!endTime.trim().equals("0")) {
		if(startTime.length() < 5) startTime = "0" + startTime;
		if(endTime.length() < 5) endTime = "0" + endTime;
		st = tdf.parse(startTime);
		et = tdf.parse(endTime);
		long elapsed = (et.getTime() - st.getTime())/1000;
		long clientCount = it.CBCLI;
		long employeeCount = it.CBEMY;
		long checkCount = it.CBCHKS;
		long clientRate = 0; try { clientRate = elapsed/clientCount; } catch (Exception e) {}
		long employeeRate = 0; try { employeeRate = elapsed/employeeCount; } catch (Exception e) {}
		long checkRate = 0; try { checkRate = elapsed/checkCount; } catch (Exception e) {}
		println "Job:${it.CBJOB}:\n\tStart Time:${startTime}\n\tEnd Time:${endTime}\n\tClient Count:${clientCount}\n\tEmployee Count:${employeeCount}\n\tCheck Count:${checkCount}\n\tElapsed:${elapsed}";
		
		if(clientCount < 2) {
			tracer.recordMetric(segment, "Batch Of One Time (s)", elapsed);
			tracer.recordMetric(segment, "Batch Of One Client Rate", clientRate);
			tracer.recordMetric(segment, "Batch Of One Client Count", clientCount);
			tracer.recordMetric(segment, "Batch Of One Employee Rate", employeeRate);
			tracer.recordMetric(segment, "Batch Of One Employee Count", employeeCount);
			tracer.recordMetric(segment, "Batch Of One Check Rate", checkRate);
			tracer.recordMetric(segment, "Batch Of One Check Count", checkCount);
			tracer.recordIntervalIncident(segment, "Batch Of One", 1);

			
		} else {
			tracer.recordMetric(segment, "Batch Time (s)", elapsed);
			tracer.recordMetric(segment, "Batch Client Rate", clientRate);
			tracer.recordMetric(segment, "Batch Client Count", clientCount);
			tracer.recordMetric(segment, "Batch Employee Rate", employeeRate);
			tracer.recordMetric(segment, "Batch Employee Count", employeeCount);
			tracer.recordMetric(segment, "Batch Check Rate", checkRate);		
			tracer.recordMetric(segment, "Batch Check Count", checkCount);	
		}
	} else {
		println "Job:${it.CBJOBN}/${sequence} had zero end time";
		tracer.recordIntervalIncident(segment, "Aborted Batch", 1);
		
	}
});

