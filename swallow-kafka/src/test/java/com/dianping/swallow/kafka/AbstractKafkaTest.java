package com.dianping.swallow.kafka;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mengwenchao
 *
 * 2015年11月17日 下午5:41:56
 */
public class AbstractKafkaTest {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String zkAddress;
	private String kafkaAddress;
	
	private String topic =  "kafkaTopic";
	
	@Rule
	public TestName testName = new TestName();
	
	@Before
	public void beforeAbstractKafkaTest() throws IOException{
		
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("sever.properties"));
		
		zkAddress = properties.getProperty("zk");
		kafkaAddress = properties.getProperty("kafka");
	}
	
	public String getZkAddress() {
		return zkAddress;
	}
	
	protected String getKafkaAddress() {
		return kafkaAddress;
	}

	
	public String getTopic() {
		return topic;
	}

	public String randomMessage(int length){
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i < length;i++){
			int random = (int) ((double)26 * Math.random());
			sb.append((char)('a' + random));
		}
		
		return sb.toString();
	}
	
	public String commonString(int length){
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i < length;i++){
			sb.append('a');
		}
		
		return sb.toString();
	}
}
