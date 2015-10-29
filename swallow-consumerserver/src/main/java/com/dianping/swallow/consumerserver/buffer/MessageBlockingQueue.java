package com.dianping.swallow.consumerserver.buffer;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.dianping.swallow.common.consumer.ConsumerType;
import com.dianping.swallow.common.consumer.MessageFilter;
import com.dianping.swallow.common.internal.consumer.ConsumerInfo;
import com.dianping.swallow.common.internal.message.SwallowMessage;
import com.dianping.swallow.common.internal.observer.Observable;
import com.dianping.swallow.consumerserver.config.ConfigManager;
import com.dianping.swallow.consumerserver.worker.impl.ConsumerConfigChanged;
import com.dianping.swallow.consumerserver.worker.impl.ConsumerWorkerImpl;

/**
 * @author mengwenchao
 *
 * 2015年8月17日 下午3:37:55
 */
public final class MessageBlockingQueue extends ConcurrentLinkedQueue<SwallowMessage> implements CloseableBlockingQueue<SwallowMessage> {

	private static final long serialVersionUID = -633276713494338593L;
	
	private static final Logger logger = LogManager.getLogger(MessageBlockingQueue.class);

	private final ConsumerInfo consumerInfo;

	protected transient MessageRetriever messageRetriever;

	private AtomicBoolean isClosed = new AtomicBoolean(false);

	private AtomicInteger checkSendMessageSize = new AtomicInteger();

	protected MessageFilter messageFilter;
	
	/** 最小剩余数量,当queue的消息数量小于threshold时，会触发从数据库加载数据的操作 */
	private final int minThreshold;
	private final int maxThreshold;

	protected volatile Long tailMessageId;
	protected volatile Long tailBackupMessageId;

	private ExecutorService retrieverThreadPool;

	private RetriveStrategy retriveStrategy, backupRetriveStrategy;
	
	private Object getTailMessageIdLock = new Object();

	public MessageBlockingQueue(ConsumerInfo consumerInfo, int minThreshold,
			int maxThreshold, int capacity, Long messageIdOfTailMessage,
			Long tailBackupMessageId, MessageFilter messageFilter,
			ExecutorService retrieverThreadPool) {
		// 能运行到这里，说明capacity>0
		this.consumerInfo = consumerInfo;
		if (minThreshold < 0 || maxThreshold < 0 || minThreshold > maxThreshold) {
			throw new IllegalArgumentException("wrong threshold: "
					+ minThreshold + "," + maxThreshold);
		}
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		if (messageIdOfTailMessage == null) {
			throw new IllegalArgumentException("messageIdOfTailMessage is null.");
		}
		this.tailMessageId = messageIdOfTailMessage;
		this.tailBackupMessageId = tailBackupMessageId;
		this.messageFilter = messageFilter;
		this.retrieverThreadPool = retrieverThreadPool;

		this.retriveStrategy = new DefaultRetriveStrategy(consumerInfo, ConfigManager.getInstance().getMinRetrieveInterval(), this.maxThreshold, 
				ConfigManager.getInstance().getMaxRetriverTaskCountPerConsumer());
		this.backupRetriveStrategy = new DefaultRetriveStrategy(consumerInfo, ConfigManager.getInstance().getBackupMinRetrieveInterval(), this.maxThreshold,
				ConfigManager.getInstance().getMaxRetriverTaskCountPerConsumer());
	}

	public void init() {
	}

	@Override
	public SwallowMessage poll() {

		ensureLeftMessage();
		
		if(logger.isDebugEnabled() && size() >= maxThreshold){
			logger.debug("[poll]" + size());
		}

		SwallowMessage message = super.poll();
		decreaseMessageCount(message);
		return message;
	}

	@Override
	public SwallowMessage peek() {
		ensureLeftMessage();
		SwallowMessage message = super.peek();
		return message;
	}


	
	private void decreaseMessageCount(SwallowMessage message) {
		if(message != null){
			retriveStrategy.decreaseMessageCount();
			backupRetriveStrategy.decreaseMessageCount();
			
			checkSendMessageSize.incrementAndGet();
		}
	}

	private void increaseMessageCount() {
		retriveStrategy.increaseMessageCount();
		backupRetriveStrategy.increaseMessageCount();
	}

	/**
	 * 唤醒“获取DB数据的后台线程”去DB获取数据，并添加到Queue的尾部
	 */
	private void ensureLeftMessage() {

		if (retriveStrategy.messageCount() < minThreshold) {

			if(retriveStrategy.canPutNewTask()){
				retrieverThreadPool.execute(new MessageRetrieverTask(retriveStrategy, consumerInfo, messageRetriever, this, messageFilter));
				retriveStrategy.offerNewTask();
			}

			if (consumerInfo.getConsumerType() == ConsumerType.DURABLE_AT_LEAST_ONCE) {
				if(backupRetriveStrategy.canPutNewTask()){
					retrieverThreadPool.execute(new BackupMessageRetrieverTask(backupRetriveStrategy, consumerInfo, messageRetriever, this, messageFilter));
					backupRetriveStrategy.offerNewTask();
				}
			}
		}
	}

	public void setMessageRetriever(MessageRetriever messageRetriever) {
		this.messageRetriever = messageRetriever;
	}

	@Override
	public void close() {
		if (isClosed.compareAndSet(false, true)) {
		}
	}

	@Override
	public void isClosed() {
		if (isClosed.get()) {
			throw new RuntimeException("MessageBlockingQueue- already closed! ");
		}
	}

	public void putMessage(List<SwallowMessage> messages) {
		
		for (SwallowMessage message : messages) {
			
			boolean result = offer(message);
			if(result){
				increaseMessageCount();
			}else{
				logger.warn("[putMessage][fail]");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Add message to (topic=" + consumerInfo.getDest().getName() + ",cid=" + consumerInfo.getConsumerId() + ") queue:" + message.toString());
			}
		}
	}


	public Long getTailMessageId() {
		return tailMessageId;
	}

	public void setTailMessageId(Long tailMessageId) {
		synchronized (getTailMessageIdLock) {
			this.tailMessageId = tailMessageId;
		}
	}

	public Long getTailBackupMessageId() {
		return tailBackupMessageId;
	}

	public void setTailBackupMessageId(Long tailBackupMessageId) {
		if(logger.isDebugEnabled()){
			logger.debug("[setTailBackupMessageId]" + tailBackupMessageId);
		}
		synchronized (getTailMessageIdLock) {
			this.tailBackupMessageId = tailBackupMessageId;
		}
	}

	@Override
	public Long getEmptyTailMessageId(boolean isBackup) {

		synchronized (getTailMessageIdLock) {
			
			if(isEmpty()){
				
				if(isBackup){
					return getTailBackupMessageId();
				}
				return getTailMessageId();
			}
		}
		
		return null;
	}

	@Override
	public void update(Observable observable, Object args) {
		
		if(!(observable instanceof ConsumerWorkerImpl)){
			throw new IllegalArgumentException("observable not supported!" + observable.getClass());
		}
		
		ConsumerConfigChanged changed = (ConsumerConfigChanged) args;
		
		switch (changed.getConsumerConfigChangeType()) {
		
			case MESSAGE_FILTER:
				this.messageFilter = changed.getNewMessageFilter();
				break;
			default:
				throw new IllegalArgumentException("type not supported!" + changed.getConsumerConfigChangeType());
		}
		
	}

}
