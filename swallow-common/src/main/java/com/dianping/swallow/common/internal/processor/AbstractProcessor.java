package com.dianping.swallow.common.internal.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.dianping.swallow.common.internal.message.SwallowMessage;

/**
 * @author mengwenchao
 *
 * 2015年3月27日 下午2:54:47
 */
public abstract class AbstractProcessor implements Processor{
	
	protected final Logger    logger = LogManager.getLogger(getClass());

	protected Map<String, String> getCreateInternalProperties(SwallowMessage message){
		
		Map<String, String> internalProperties = message.getInternalProperties();
		
		if(internalProperties == null){
			internalProperties = new HashMap<String, String>();
			message.setInternalProperties(internalProperties);
		}
		
		return internalProperties;
		
	}

}
