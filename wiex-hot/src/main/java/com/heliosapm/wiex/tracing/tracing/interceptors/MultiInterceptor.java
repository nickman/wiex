package com.heliosapm.wiex.tracing.tracing.interceptors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EnvEntryMetaData;
import org.jboss.metadata.MetaData;

import com.heliosapm.wiex.tracing.tracing.ITracer;
import com.heliosapm.wiex.tracing.tracing.TracerFactory;
import com.heliosapm.wiex.tracing.tracing.thread.ThreadStats;

/**
 * <p>Title: MultiInterceptor</p>
 * <p>Description: JBoss interceptor and Servlet Filter for recording Metrics in the EJB interceptor stack and Servlet call and registering with Introscope</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.11 $ 
 */
public class MultiInterceptor implements Interceptor, Filter {

	/** The next interceptor in the chain. */
	protected Interceptor nextInterceptor;
	/** The container the interceptor is associated with */
	protected Container container;
	/** The bean name that this interceptor is attached to */
	protected String beanName = "UNKNOWN";
	/** The generalized resource prefix for the introscope metrics collected by this interceptor */ 
	protected String ejbShortKey = "UNKNOWN";
	
	protected Logger LOG = Logger.getLogger(MultiInterceptor.class);
	
	/** The instance pool for the EJB that this interceptor is attached to */
	protected InstancePoolContainer instancePoolContainer = null;
	/** The concurrency tracker for the EJB interceptor */
	protected AtomicLong ejbConcurrent = new AtomicLong(0L);
	/** The concurrency tracker for the Servlet filter */
	protected AtomicLong httpConcurrent = new AtomicLong(0L);  
	/** The tracer */
	protected ITracer tracer = null;
	/** The metric segment delimeter */
	protected String RSEG_DELIM = null;
	/** The user segment prefix */
	protected String USER_NAME_PREFIX = null;
	/** The http prefix */
	protected String HTTP_PREFIX = null;
	/** The ejb prefix */
	protected String EJB_PREFIX = null;
	/** The http aggregate prefix */
	protected String HTTP_PREFIX_AGGR = null;
	/** The ejb aggregate prefix */
	protected String EJB_PREFIX_AGGR = null;
	/** The ejb TYPE */
	protected String ejbType = null;
	
	
	/**
	 * 
	 */
	public MultiInterceptor() {
		tracer = TracerFactory.getInstance();		
		RSEG_DELIM = tracer.getSegmentDelimeter();
		USER_NAME_PREFIX = tracer.getUserIdPefix();
		HTTP_PREFIX = "Servlets" + RSEG_DELIM + "Extended" + RSEG_DELIM;
		EJB_PREFIX = "EJB" + RSEG_DELIM + "Extended" + RSEG_DELIM;	
		HTTP_PREFIX_AGGR = HTTP_PREFIX + "Total";
		EJB_PREFIX_AGGR = EJB_PREFIX + "Total";
	}

	/**
	 * 
	 */
   public void setContainer(Container container)  {	
	   
		if(container==null) return;
		long start = System.currentTimeMillis();
		this.container = container;
		BeanMetaData bmd = container.getBeanMetaData();
		if(container instanceof InstancePoolContainer) {
			instancePoolContainer = (InstancePoolContainer)container;
		}			
		if (container != null) {
			beanName = bmd.getJndiName();
			ejbType = ejbType(bmd);
			EJB_PREFIX = EJB_PREFIX + ejbType + RSEG_DELIM;			
			ejbShortKey = new StringBuilder(EJB_PREFIX).append(beanName).toString();
			if(LOG.isDebugEnabled()) LOG.debug("Bean Name:" + beanName);
		}					
		StringBuilder buff = new StringBuilder("Bean MetaData\n");
		buff.append("\tEJB Type:").append(ejbType).append("\n");
		buff.append("\tBean Class:").append(container.getBeanClass().getName()).append("\n");
		buff.append("\tConfiguration Name:").append(bmd.getConfigurationName()).append("\n");
		buff.append("\tContainer JNDI Name:").append(bmd.getContainerObjectNameJndiName()).append("\n");
		buff.append("\tBean Info:").append(bmd.toString()).append("\n");
		buff.append("\tDeployment Info:").append(container.getDeploymentInfo().getCanonicalName()).append("\n");
		buff.append("\tContainer Managed TX:").append(bmd.isContainerManagedTx()).append("\n");
		buff.append("\tCall By Value:").append(bmd.isCallByValue()).append("\n");
		buff.append("\tClustered:").append(bmd.isClustered()).append("\n");
		if(bmd.isClustered()) {
			buff.append("\t\tPartition Name:").append(bmd.getClusterConfigMetaData().getPartitionName()).append("\n");
			buff.append("\t\tBean Load Balance Policy:").append(bmd.getClusterConfigMetaData().getBeanLoadBalancePolicy()).append("\n");
			buff.append("\t\tHome Load Balance Policy:").append(bmd.getClusterConfigMetaData().getHomeLoadBalancePolicy()).append("\n");
			buff.append("\t\tHA Session State Name:").append(bmd.getClusterConfigMetaData().getHaSessionStateName()).append("\n");						
		}
		buff.append("\tEnforce EJB Restrictions:").append(bmd.getApplicationMetaData().getEnforceEjbRestrictions()).append("\n");
		buff.append("\tEnvironment Entries:\n");
		Iterator iter = bmd.getEnvironmentEntries();
		while(iter.hasNext()) {
			EnvEntryMetaData entry = (EnvEntryMetaData)iter.next();
			buff.append("\t\t").append(entry.getName()).append(" : ").append(entry.getValue()).append("\n");
		}
		buff.append("\tInvoker Bindings:\n");
		iter = bmd.getInvokerBindings();
		while(iter.hasNext()) {
			String invokerName = iter.next().toString();
			buff.append("\t\t").append(invokerName).append("\n");
		}
		buff.append("\tInterceptors:\n");
		Interceptor interceptor = this;
				
		while(true) {
			try {								
				buff.append("\t\t").append(interceptor.getClass().getName()).append("\n");
				interceptor = interceptor.getNext();
				if(interceptor==null) break;
			} catch (Exception e) {
				break;
			}
			
		}
		
		buff.append("\tMethod Info:\n");
		for(Method m: container.getBeanClass().getMethods()) {
			buff.append("\t\t").append(m.toGenericString()).append("\n");
			buff.append("\t\t\tTransaction TimeOut:").append(bmd.getTransactionTimeout(m)).append("\n");			
			buff.append("\t\t\tMethod Read Only:").append(bmd.isMethodReadOnly(m)).append("\n");
			buff.append("\t\t\tTX Type:\n");
			buff.append("\t\t\t\t").append("Remote:").append(decodeTXBit(bmd.getTransactionMethod(m, InvocationType.REMOTE))).append("\n");
			buff.append("\t\t\t\t").append("Local:").append(decodeTXBit(bmd.getTransactionMethod(m, InvocationType.LOCAL))).append("\n");
			buff.append("\t\t\t\t").append("LocalHome:").append(decodeTXBit(bmd.getTransactionMethod(m, InvocationType.LOCALHOME))).append("\n");
			buff.append("\t\t\t\t").append("Home:").append(decodeTXBit(bmd.getTransactionMethod(m, InvocationType.HOME))).append("\n");
			buff.append("\t\t\t\t").append("ServiceEndPoint:").append(decodeTXBit(bmd.getTransactionMethod(m, InvocationType.SERVICE_ENDPOINT))).append("\n");
		}
		
		tracer.recordMetric(ejbShortKey, "Bean MetaData", buff.toString());
		tracer.recordMetric(ejbShortKey, "Bean MetaData Collection Time", "" + (System.currentTimeMillis()-start) + " ms.");
		tracer.recordMetric(ejbShortKey, "Last Bean Initialization", new Date().toString());	      
   }

   /**
    * 
    * @return The container for the current EJB.
    */
   public Container getContainer()  {
      return container;
   }

   /**
    * 
    */
   public void setNext(final Interceptor interceptor) {
      nextInterceptor = interceptor;
   }

   /**
    * 
    */
   public Interceptor getNext()  {
      return nextInterceptor;
   }

   /**
    * 
    */
   public void create() throws Exception  {
      // empty
   }

   /**
    * 
    */
   public void start() throws Exception  {
      // empty
   }

   /**
    * 
    */
   public void stop()  {
      // empty
   }

   /**
    * 
    */
   public void destroy() {
      // empty
   }
   
	/* (non-Javadoc)
	 * @see org.jboss.ejb.plugins.AbstractInterceptor#invokeHome(org.jboss.invocation.Invocation)
	 */
	public Object invokeHome(Invocation mi) throws Exception {
		String type = null;
		if (mi.getType().equals(InvocationType.LOCALHOME)) {
			type = "LocalHome";
		} else {
			type = "RemoteHome";
		}
		return invokeGeneric(mi, true, type);
	}

	/* (non-Javadoc)
	 * @see org.jboss.ejb.plugins.AbstractInterceptor#invoke(org.jboss.invocation.Invocation)
	 */
	public Object invoke(Invocation mi) throws Exception {
		String type = null;
		if (mi.getType().equals(InvocationType.LOCAL)) {
			type = "Local";
		} else {
			type = "Remote";
		}
		return invokeGeneric(mi, false, type);
	}
	
	
	/**
	 * Centralizes the home and bean instance method invocation instrumentation.
	 * @param mi
	 * @param isHome
	 * @param type
	 * @return The object returned from the upstream invocation.
	 * @throws Exception
	 */
	protected Object invokeGeneric(Invocation mi, boolean isHome, String type) throws Exception {			
		long start = System.currentTimeMillis();
		long invokeElapsed = 0L;		
		Object returnObject = null;
		long concurT = 0L;
		String key = null;
		String userKey =  null;
		String methodName = null;
		String userName = tracer.getUserId();
		ThreadStats ts = null;
		try {
			//if(LOG.isTraceEnabled()) LOG.trace("invoking:" + beanName + "." + mi.getMethod().getName());
			concurT = ejbConcurrent.incrementAndGet();
			ts = tracer.getThreadStatsInstance();
			methodName = new String(beanName);
			if (mi.getMethod() != null) {
				methodName = mi.getMethod().getName();
			}
			
			key = tracer.getStringBuilder().append(ejbShortKey).append(tracer.getSegmentDelimeter()).append(type).append(RSEG_DELIM).append(methodName).toString();
			userKey =  tracer.getStringBuilder().append(USER_NAME_PREFIX).append(userName).append(RSEG_DELIM).append(key).toString();
		} catch (Throwable t) {}
		try {
			long start2 = System.currentTimeMillis();
			if(isHome) {
				returnObject = getNext().invokeHome(mi);
			} else {
				returnObject = getNext().invoke(mi);
			}
			try {
				invokeElapsed = System.currentTimeMillis()-start2;
				tracer.deltaThreadStats(ts);
				try { tracer.recordMetric(key,ts); } catch (Throwable e) {}
				try { tracer.recordMetric(userKey,ts); } catch (Throwable e) {}
				try { tracer.recordMetric(ejbShortKey, ITracer.CONCURRENT_COUNT, concurT); } catch (Throwable e) {}
				//if(instancePoolContainer!=null) {
				//	try { tracer.recordCounterMetric(ejbShortKey, ITracer.POOL_SIZE, instancePoolContainer.getInstancePool().getCurrentSize()); } catch (Throwable e) {}
				//	try { tracer.recordCounterMetric(ejbShortKey, ITracer.MAX_POOL_SIZE, instancePoolContainer.getInstancePool().getMaxSize()); } catch (Throwable e) {}					
				//}							
				long elapsed = System.currentTimeMillis()-start;
				long overhead = System.currentTimeMillis()-(start+invokeElapsed);
				
				try { tracer.recordMetric(EJB_PREFIX_AGGR, "Elapsed (ms.)", elapsed); } catch (Throwable e) {}
				try { tracer.recordMetric(key, ITracer.AGENT_ELAPSED, overhead); } catch (Throwable e) {}
				try { tracer.recordMetric(EJB_PREFIX_AGGR, ITracer.AGENT_ELAPSED, overhead); } catch (Throwable e) {}
				
			} catch (Throwable t) {}
			return returnObject;
		} catch (Exception e) {			
			try {tracer.recordMetric(key, ITracer.ERROR_COUNT);} catch (Throwable t) {}
			throw e;
		} finally {
			try { ejbConcurrent.decrementAndGet(); } catch (Throwable t) {}
			tracer.trace(key);
			tracer.trace(userKey);
		}		
	}
	   

	
	




	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		long start = System.currentTimeMillis();
		long invokeElapsed = 0L;
		long concurT = 0L;
		String methodName = "Undefined";
		String type = "Undefined";
		String httpShortKey = null;
		String key = null;
		String userKey =  null;
		String userName = tracer.getUserId();
		ThreadStats ts = null;
		try {
			ts = tracer.getThreadStatsInstance();
			concurT = httpConcurrent.incrementAndGet();
			try {
				methodName = ((HttpServletRequest)req).getRequestURI();
				type = ((HttpServletRequest)req).getMethod();
				httpShortKey = new StringBuilder(HTTP_PREFIX).append(methodName).toString();
			} catch (Exception e) {
				// NOOP
			}
			key = tracer.getStringBuilder().append(httpShortKey).append(RSEG_DELIM).append(type).toString();
			userKey =  tracer.getStringBuilder().append(USER_NAME_PREFIX).append(userName).append(RSEG_DELIM).append(key).toString();
		} catch (Throwable t) {}
		try {
			long start2 = System.currentTimeMillis();
			filterChain.doFilter(req, resp);
			invokeElapsed = System.currentTimeMillis()-start2;
			long elapsed = System.currentTimeMillis()-start;
			long overhead = System.currentTimeMillis()-(start+invokeElapsed);
			
			try { tracer.deltaThreadStats(ts); } catch (Throwable e) {}
			try { tracer.recordMetric(key,ts); } catch (Throwable e) {}
			try { tracer.recordMetric(userKey,ts); } catch (Throwable e) {}
			try { tracer.recordMetric(httpShortKey, ITracer.CONCURRENT_COUNT, concurT); } catch (Throwable e) {}			
			try { tracer.recordMetric(HTTP_PREFIX_AGGR, "Elapsed (ms.)", elapsed); } catch (Throwable e) {}
			try { tracer.recordMetric(key, ITracer.AGENT_ELAPSED, overhead); } catch (Throwable e) {}
			try { tracer.recordMetric(HTTP_PREFIX_AGGR, ITracer.AGENT_ELAPSED, overhead); } catch (Throwable e) {}
			
			
			
			
			
		} catch (Exception e) {
			try { tracer.recordMetric(key, ITracer.ERROR_COUNT); } catch (Throwable t) {}
			throw new ServletException(e);
		} finally {
			try { httpConcurrent.decrementAndGet(); } catch (Throwable e) {}
			tracer.trace(key);
			tracer.trace(userKey);
		}		
		

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	public static String ejbType(BeanMetaData bmd) {
		StringBuffer buff = new StringBuffer();
		if(bmd.isBeanManagedTx()) {
			buff.append("Bean Managed TX ");
		} else if(bmd.isContainerManagedTx()) {
			buff.append("Container Managed TX ");
		} else {
			buff.append("TX Unknown ");
		}
		if(bmd.isSession()) {
			buff.append("Session Bean");
		} else if(bmd.isEntity()) {
			buff.append("Entity Bean");
		} else if(bmd.isMessageDriven()) {
			buff.append("Message Bean");
		} else {
			buff.append("Type Unknown Bean");
		}
		buff.append(" EJB");
		if(bmd.getApplicationMetaData().isEJB1x()) {
			buff.append(" 1.x");
		} else if(bmd.getApplicationMetaData().isEJB2x()) {
			buff.append(" 2.x");
		} else if(bmd.getApplicationMetaData().isEJB21()) {
			buff.append(" 2.1");
		} else {
			buff.append(" ?.x");
		}
		
		return buff.toString();
	}
	
	public static String decodeTXBit(byte tx) {
		switch(tx) {
		case MetaData.TX_NOT_SUPPORTED:
			return "TX_NOT_SUPPORTED";
		case MetaData.TX_REQUIRED:
			return "TX_REQUIRED";
		case MetaData.TX_SUPPORTS:
			return "TX_SUPPORTS";
		case MetaData.TX_REQUIRES_NEW:
			return "TX_REQUIRES_NEW";
		case MetaData.TX_MANDATORY:
			return "TX_MANDATORY";
		case MetaData.TX_NEVER:
			return "TX_NEVER";			
		default:
			return "TX_UNKNOWN";
		}
	}



}
