package com.dianping.swallow.web.dao.impl;

import com.dianping.swallow.common.internal.config.DynamicConfig;
import com.dianping.swallow.common.internal.config.impl.AbstractSwallowServerConfig;
import com.dianping.swallow.common.internal.config.impl.DefaultDynamicConfig;
import com.dianping.swallow.common.internal.dao.impl.mongodb.MongoConfig;
import com.dianping.swallow.common.internal.util.MongoUtils;
import com.dianping.swallow.web.dao.SimMongoDbFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import javax.annotation.PostConstruct;

/**
 * @author mengwenchao
 *
 *         2015年4月18日 下午9:31:12
 */
@Configuration
public class MongoTemplateFactory {

	protected final Logger logger = LogManager.getLogger(getClass());

	public static final String MAP_KEY_DOT_REPLACEMENT = "__";

	@Value("${swallow.web.mongodbname.stats}")
	private String statsMongoDbName;

	@Value("${swallow.web.mongodbname.web}")
	private String webMongoDbName;

	@Value("${swallow.web.mongodbname.statsdata}")
	private String statsDataMongoDbName;

	public static final String SWALLOW_STATS_MONGO_URL_KEY = "swallow.mongourl";

	public static final String SWALLOW_MONGO_ADDRESS_FILE = "swallow-store-lion.properties";

	public static final String SWALLOW_MONGO_CONFIG_FILE = "swallow-web-mongo.properties";

	private MongoClient mongo;

	private MongoConfig config;
	private DynamicConfig dynamicConfig;

	@PostConstruct
	public void getMongo() {

		dynamicConfig = new DefaultDynamicConfig(SWALLOW_MONGO_ADDRESS_FILE);
		String mongoUrl = dynamicConfig.get(SWALLOW_STATS_MONGO_URL_KEY);
		config = new MongoConfig(SWALLOW_MONGO_CONFIG_FILE);

		mongo = new MongoClient(MongoUtils.parseUriToAddressList(mongoUrl), config.buildMongoOptions());
		if (logger.isInfoEnabled()) {
			logger.info("[getMongo]" + mongo);
		}
	}

	@Bean(name = "statsMongoTemplate")
	public MongoTemplate getStatisMongoTemplate() {

		// MongoTemplate statisMongoTemplate =
		// createMongoTemplate(statsMongoDbName);
		//
		// //create collection
		//
		// long size =
		// Long.parseLong(dynamicConfig.get("swallow.mongo.web.stais.cappedCollectionSize"));
		// long max =
		// Long.parseLong(dynamicConfig.get("swallow.mongo.web.stais.cappedCollectionMaxDocNum"));
		// createCappedCollection(statisMongoTemplate,
		// ProducerMonitorData.class.getSimpleName(), size, max);
		// createCappedCollection(statisMongoTemplate,
		// ConsumerMonitorData.class.getSimpleName(), size, max);

		// return statisMongoTemplate;
		return null;
	}

	@Bean(name = "statsDataMongoTemplate")
	public MongoTemplate getAlarmStatisMongoTemplate() {

		return new MongoTemplate(new SimMongoDbFactory(mongo, statsDataMongoDbName));
	}

	@SuppressWarnings("unused")
	private synchronized void createCappedCollection(MongoTemplate mongoTemplate, String collectionName, long size,
			long max) {

		if (!mongoTemplate.collectionExists(collectionName)) {
			if (logger.isInfoEnabled()) {
				logger.info("[createCappedCollection][createCollection]" + collectionName + ",size:" + size + ",max:"
						+ max);
			}
			mongoTemplate.getDb().createCollection(collectionName, getCappedOptions(size, max));
		}

	}

	private DBObject getCappedOptions(long size, long max) {

		DBObject options = new BasicDBObject();
		if (size > 0) {
			options.put("capped", true);
			options.put("size", size * AbstractSwallowServerConfig.MILLION);
			if (max > 0) {
				options.put("max", max * AbstractSwallowServerConfig.MILLION);
			}
		}
		return options;
	}

	@Bean(name = "webMongoTemplate")
	public MongoTemplate getTopicMongoTemplate() {

		return new MongoTemplate(new SimMongoDbFactory(mongo, webMongoDbName));
	}

	@SuppressWarnings("unused")
	private MongoTemplate createMongoTemplate(String mongoDbName) {

		if (logger.isInfoEnabled()) {
			logger.info("[createMongoTemplate]" + mongoDbName);
		}
		MongoTemplate template = new MongoTemplate(mongo, mongoDbName);
		initMongoTemplate(template);
		return template;
	}

	private void initMongoTemplate(MongoTemplate template) {

		MongoConverter converter = template.getConverter();
		if (converter instanceof MappingMongoConverter) {
			if (logger.isInfoEnabled()) {
				logger.info("[initMongoTemplate]" + MAP_KEY_DOT_REPLACEMENT);
			}
			((MappingMongoConverter) converter).setMapKeyDotReplacement(MAP_KEY_DOT_REPLACEMENT);
		}

		if (logger.isInfoEnabled()) {
			logger.info("[initMongoTemplate][setWriteResultChecking]exception");
		}
		template.setWriteResultChecking(WriteResultChecking.EXCEPTION);

		if (logger.isInfoEnabled()) {
			logger.info("[initMongoTemplate][set write concern]safe");
		}
		template.setWriteConcern(WriteConcern.SAFE);

	}

}
