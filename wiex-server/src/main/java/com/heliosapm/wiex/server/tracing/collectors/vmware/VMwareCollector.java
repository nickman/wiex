/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.vmware;

import com.heliosapm.wiex.server.tracing.collectors.BaseCollector;

/**
 * <p>Title: VMwareCollector</p>
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.2 $
 */

public class VMwareCollector extends BaseCollector {

	/**
	 * 
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#collect()
	 */
	@Override
	public void collect() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Creates a new instance of a VMwareCollector.
	 */
	public VMwareCollector() {
		super();
	}
	
	/**
	 * Initializes the VERSION and MODULE.
	 * @see com.heliosapm.wiex.server.collectors.BaseCollector#init()
	 */
	public void init() {
		VERSION = "$Revision: 1.2 $";
		MODULE = "VMwareCollector";
	}	
		

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//			ManagedObjectReference _svcRef = new ManagedObjectReference();
//			_svcRef.setType("ServiceInstance");
//			_svcRef.set_value("ServiceInstance");
			//_svcRef.setValue("ServiceInstance");
//			VimServiceLocator _locator = new VimServiceLocator();
//			_locator.setMaintainSession(true);
			
			//VimPortType _service = _locator.getVimPort(new URL("https://<your_server>/sdk"));
//			VimPortType _service = _locator.getVimPort(new URL("https://par1vmt2.nj.adp.com/sdk"));
//			ServiceContent _sic = _service.retrieveServiceContent(_svcRef);
//			_service.login(_sic.getSessionManager(), "root", "Secret1", null);
//			log("Connected");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void log(Object message) {
		System.out.println(message);
	}

}
