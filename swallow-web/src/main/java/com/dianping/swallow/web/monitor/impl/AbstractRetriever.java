package com.dianping.swallow.web.monitor.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import com.dianping.swallow.common.internal.lifecycle.impl.AbstractLifecycle;
import com.dianping.swallow.common.internal.util.CommonUtils;
import com.dianping.swallow.common.internal.util.DateUtils;
import com.dianping.swallow.common.server.monitor.collector.AbstractCollector;
import com.dianping.swallow.web.monitor.Retriever;
import com.dianping.swallow.web.monitor.StatsData;
import com.dianping.swallow.web.monitor.StatsDataDesc;

/**
 * @author mengwenchao
 *
 *         2015年5月28日 下午3:02:25
 */
public abstract class AbstractRetriever extends AbstractLifecycle implements Retriever {

	protected final int DEFAULT_INTERVAL = 30;// 每隔多少秒采样

	protected final int BUILD_TIMES_AGEO = 60000;// 每次构建时，构建此时间(ms)以前的数据

	@Value("${swallow.web.monitor.keepinmemory}")
	public int keepInMemoryHour = 3;// 保存最新小时

	public static int keepInMemoryCount;

	protected ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(CommonUtils.DEFAULT_CPU_COUNT);

	protected long lastBuildTime = System.currentTimeMillis(), current = System.currentTimeMillis();

	private ScheduledFuture<?> future;

	@Override
	protected void doStart() throws Exception {

		super.doStart();

		startBuilder();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();

		future.cancel(false);
	}

	private void startBuilder() {

		future = scheduled.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {

				current = System.currentTimeMillis() - BUILD_TIMES_AGEO;
				try {
					doBuild();

					long removeKey = getKey(current - keepInMemoryHour * 3600000L);
					doRemove(removeKey);
				} catch (Throwable th) {
					logger.error("[startBuilder]", th);
				} finally {
					if (lastBuildTime < current) {
						lastBuildTime = current;
					}
				}

			}

		}, getBuildInterval(), getBuildInterval(), TimeUnit.SECONDS);
	}

	protected long getBuildInterval() {

		return DEFAULT_INTERVAL;
	}

	protected abstract void doBuild();

	protected abstract void doRemove(long toKey);

	protected boolean dataExistInMemory(long start, long end) {

		long oldest = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(keepInMemoryHour, TimeUnit.HOURS);

		// 允许10s内的误差
		if (oldest <= (start + 10 * 1000)) {
			return true;
		}
		return false;
	}

	protected static Long getKey(long timeMili) {

		return timeMili / AbstractCollector.SEND_INTERVAL / 1000;
	}

	@Override
	public int getKeepInMemoryHour() {
		return keepInMemoryHour;
	}

	public void setKeepInMemoryHour(int keepInMemoryHour) {
		this.keepInMemoryHour = keepInMemoryHour;
	}

	protected long getDefaultEnd() {

		return System.currentTimeMillis();
	}

	protected long getDefaultStart() {
		return System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(keepInMemoryHour, TimeUnit.HOURS);
	}

	/**
	 * 采样时间间隔
	 * 
	 * @return
	 */
	protected int getSampleIntervalTime() {

		return DEFAULT_INTERVAL;
	}

	protected int getSampleIntervalCount() {
		return getSampleIntervalTime() / AbstractCollector.SEND_INTERVAL;
	}

	@Override
	public Set<String> getTopics() {

		return getTopics(getDefaultStart(), getDefaultEnd());
	}

	@Override
	public Set<String> getTopics(long start, long end) {

		if (dataExistInMemory(start, end)) {
			getTopicsInMemory(start, end);
		}

		return getTopicsInDb(start, end);
	}

	protected Set<String> getTopicsInDb(long start, long end) {

		return getTopicsInMemory(start, end);
	}

	protected abstract Set<String> getTopicsInMemory(long start, long end);

	protected long getStartTime(NavigableMap<Long, Long> rawData, long start, long end) {

		if (rawData == null) {
			return end;
		}
		try {
			return rawData.firstKey().longValue() * AbstractCollector.SEND_INTERVAL * 1000;
		} catch (NoSuchElementException e) {
			if (logger.isInfoEnabled()) {
				logger.info("[getRealStartTime][no element, end instead]" + DateUtils.toPrettyFormat(end));
			}
			return end;
		}
	}

	protected List<Long> getValue(NavigableMap<Long, Long> rawData) {

		List<Long> result = new LinkedList<Long>();

		if (rawData != null) {
			for (Long value : rawData.values()) {
				result.add(value);
			}
		}
		return result;
	}

	protected StatsData createStatsData(StatsDataDesc desc, NavigableMap<Long, Long> rawData, long start, long end) {

		return new StatsData(desc, getValue(rawData), getStartTime(rawData, start, end), getSampleIntervalTime());
	}
	
}
