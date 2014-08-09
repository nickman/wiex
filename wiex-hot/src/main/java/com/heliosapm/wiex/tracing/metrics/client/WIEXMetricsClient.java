package com.heliosapm.wiex.tracing.metrics.client;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.heliosapm.wiex.jmx.util.MBeanServerHelper;

public class WIEXMetricsClient
{
  private static Map clients = Collections.synchronizedMap(new HashMap());

  public static String defaultConfigMBean = "com.heliosapm.wiex.metrics:service=WIEXMetrics";
  private MBeanServer mbeanServer;
  private ObjectName defaultObjectName;
  private String privateMBeanName = null;

  private ThreadLocal startTime = new ThreadLocal();

  private ThreadLocal startCPU = new ThreadLocal();

  private ThreadLocal opNames = new ThreadLocal();

  private boolean isThreadMXBeanSupported = false;

  private Object threadMXBean = null;

  private Method getCurrentThreadCpuTimeMethod = null;

  private Method getIdMethod = null;

  private static final Object[] NO_ARGS = new Object[0];

  private static final String[] WIEXMETRICS_FULL_INVOKE = { "java.lang.String", Long.TYPE.getName(), Long.TYPE.getName(), Long.TYPE.getName(), Long.TYPE.getName() };

  private static final String[] WIEXMETRICS_ABR_INVOKE = { "java.lang.String", Long.TYPE.getName(), Long.TYPE.getName() };

  private static final Long zeroLong = new Long(0L);

  protected static final Logger LOG = Logger.getLogger(WIEXMetricsClient.class);

  private WIEXMetricsClient()
  {
    try
    {
      this.privateMBeanName = defaultConfigMBean;
      this.startTime.set(new Stack());
      checkJavaVersion();
      if (this.isThreadMXBeanSupported) {
        initThreadXMBean();
      }
      setServer();
    }
    catch (Exception e) {
      LOG.error("Exception in getting MBean Server" + e.getMessage(), e);
      throw new RuntimeException("Could not create WIEXConfigurator");
    }
  }

  protected void checkJavaVersion()
  {
    try
    {
      Class.forName("java.lang.Enum");
      this.isThreadMXBeanSupported = true;
    }
    catch (Exception ex) {
      this.isThreadMXBeanSupported = false;
    }
  }

  protected void initThreadXMBean()
    throws Exception
  {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Class clazz = cl.loadClass("java.lang.management.ManagementFactory");

    Method method = clazz.getMethod("getThreadMXBean", null);
    this.threadMXBean = method.invoke(null, null);

    this.getCurrentThreadCpuTimeMethod = this.threadMXBean.getClass().getMethod("getCurrentThreadCpuTime", null);

    this.getIdMethod = Thread.class.getMethod("getId", null);
  }

  private WIEXMetricsClient(String mbeanName)
  {
    try
    {
      this.privateMBeanName = mbeanName;
      this.startTime = new ThreadLocal();
      this.startCPU = new ThreadLocal();
      checkJavaVersion();
      if (this.isThreadMXBeanSupported) {
        initThreadXMBean();
      }
      setServer();
    }
    catch (Exception e) {
      LOG.error("Exception in getting MBean Server" + e.getMessage(), e);
      throw new RuntimeException("Could not create WIEXConfigurator");
    }
  }

  public void startMetric(String metricName)
  {
    ((Stack)this.opNames.get()).push(metricName);

    ((Stack)this.startTime.get()).push(new Long(System.currentTimeMillis()));

    ((Stack)this.startCPU.get()).push(new Long(getCurrentCPUTime()));
  }

  public long getCurrentCPUTime()
  {
    if (this.isThreadMXBeanSupported) {
      try {
        return ((Long)this.getCurrentThreadCpuTimeMethod.invoke(this.threadMXBean, NO_ARGS)).longValue();
      } catch (Throwable ex) {
        return 0L;
      }
    }
    return 0L;
  }

  public void cancelMetric()
  {
    try
    {
      ((Stack)this.startTime.get()).pop();
      ((Stack)this.startCPU.get()).pop();
      ((Stack)this.opNames.get()).pop();
    }
    catch (Exception e)
    {
    }
  }

  public void endMetric(String metricName)
  {
    try {
      Long tEnd = new Long(System.currentTimeMillis());
      Long cEnd = new Long(getCurrentCPUTime());
      Long tStart = (Long)((Stack)this.startTime.get()).pop();
      Long cStart = (Long)((Stack)this.startCPU.get()).pop();
      String startOpName = (String)((Stack)this.opNames.get()).pop();
      if ((startOpName == null) || (tStart == null) || (cStart == null) || (!startOpName.equals(metricName))) {
        throw new Exception("Start/End Mismatch  (" + startOpName + "/" + metricName + ")");
      }
      this.mbeanServer.invoke(this.defaultObjectName, "updateMetrics", new Object[] { metricName, tStart, tEnd, cStart, cEnd }, WIEXMETRICS_FULL_INVOKE);
    }
    catch (Exception ex)
    {
      LOG.error("Exception logging Metric", ex);
    }
  }

  public void endMetric(String metricName, String newMetricName)
  {
    try
    {
      Long tEnd = new Long(System.currentTimeMillis());
      Long cEnd = new Long(getCurrentCPUTime());
      Long tStart = (Long)((Stack)this.startTime.get()).pop();
      Long cStart = (Long)((Stack)this.startCPU.get()).pop();
      String startOpName = (String)((Stack)this.opNames.get()).pop();
      if ((startOpName == null) || (tStart == null) || (cStart == null) || (!startOpName.equals(metricName))) {
        throw new Exception("Start/End Mismatch  (" + startOpName + "/" + metricName + ")");
      }
      this.mbeanServer.invoke(this.defaultObjectName, "updateMetrics", new Object[] { newMetricName, tStart, tEnd, cStart, cEnd }, WIEXMETRICS_FULL_INVOKE);
    }
    catch (Exception ex)
    {
      LOG.error("Exception logging Metric", ex);
    }
  }

  public void updateMetrics(String opname, long start, long end)
  {
    registerMetric(opname, start, end);
  }

  public void registerMetric(String metricName, long start, long end)
  {
    try
    {
      this.mbeanServer.invoke(this.defaultObjectName, "updateMetrics", new Object[] { metricName, new Long(start), new Long(end), zeroLong, zeroLong }, WIEXMETRICS_FULL_INVOKE);
    }
    catch (Exception ex)
    {
      LOG.error("Exception logging Metric", ex);
    }
  }

  public void registerElapsedMetric(String metricName, long elapsed)
  {
    try
    {
      this.mbeanServer.invoke(this.defaultObjectName, "updateMetrics", new Object[] { metricName, new Long(elapsed), zeroLong }, WIEXMETRICS_ABR_INVOKE);
    }
    catch (Exception ex)
    {
      LOG.error("Exception logging Metric", ex);
    }
  }

  public void registerElapsedMetric(String metricName, long elapsed, long cpu)
  {
    try
    {
      this.mbeanServer.invoke(this.defaultObjectName, "updateMetrics", new Object[] { metricName, new Long(elapsed), new Long(cpu) }, WIEXMETRICS_ABR_INVOKE);
    }
    catch (Exception ex)
    {
      LOG.error("Exception logging Metric", ex);
    }
  }

  public static WIEXMetricsClient getInstance()
  {
    return getInstance(defaultConfigMBean);
  }

  public static WIEXMetricsClient getInstance(String mbeanName)
  {
    if (LOG.isDebugEnabled()) LOG.debug("Returning WIEXMetricClient for:" + mbeanName);
    WIEXMetricsClient client = (WIEXMetricsClient)clients.get(mbeanName);
    if (client == null) {
      synchronized (WIEXMetricsClient.class) {
        client = new WIEXMetricsClient(mbeanName);
        clients.put(mbeanName, client);
      }
    }
    client.checkInit();
    return client;
  }

  private void checkInit()
  {
    if (this.startTime.get() == null) this.startTime.set(new Stack());
    if (this.startCPU.get() == null) this.startCPU.set(new Stack());
    if (this.opNames.get() == null) this.opNames.set(new Stack());
  }

  private void setServer()
    throws Exception
  {
    this.mbeanServer = MBeanServerHelper.getJBossInstance();
    this.defaultObjectName = new ObjectName(this.privateMBeanName);
  }
}