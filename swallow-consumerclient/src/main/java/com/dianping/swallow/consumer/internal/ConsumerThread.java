package com.dianping.swallow.consumer.internal;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * ConsumerThread的作用是，它会不断的保持与ConsumerServer的连接(一个channel关闭后继续建立新的channel)<br>
 * 使用时，一个Consumer拥有master和slave2个线程，2个线程不断交替尝试连接master和slave服务器，直到其中一个连接成功。<br>
 * 当master和slave线程其中一个连接成功后，另外一个线程将阻塞<br>
 * 已经连接成功的线程，当连接被关闭后，会睡眠一会，然后继续进入2个线程交替尝试的场景。<br>
 * 
 * @author wukezhu
 */
public class ConsumerThread extends Thread {

   private static final Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

   private Bootstrap     bootstrap;

   private InetSocketAddress   remoteAddress;

   private long                interval;

   private ConsumerImpl consumerImpl;
   
   public ConsumerThread(ConsumerImpl consumerImpl) {
	   
	   this.consumerImpl = consumerImpl;
   }

	public void setBootstrap(Bootstrap bootstrap) {
      this.bootstrap = bootstrap;
	}

   public void setRemoteAddress(InetSocketAddress remoteAddress) {
      this.remoteAddress = remoteAddress;
   }

   public void setInterval(long interval) {
      this.interval = interval;
   }

   @Override
   public void run() {
	   
      ChannelFuture future = null;
      while (!Thread.currentThread().isInterrupted()) {
         synchronized (bootstrap) {
            if (!Thread.currentThread().isInterrupted()) {
               try {
            	   if(logger.isInfoEnabled()){
            		   logger.info("[run][connecting][" + getDesc() + "]" + remoteAddress);
            	   }
                  future = bootstrap.connect(remoteAddress);
                  future.await();
                  if (future.channel().isActive()) {
                     SocketAddress localAddress = future.channel().localAddress();
                     if(logger.isInfoEnabled()){
                    	 logger.info("[run][connected][" + getDesc() + "]" + localAddress + "->" + remoteAddress);
                     }
                     future.channel().closeFuture().await();//等待channel关闭，否则一直阻塞！
                     if(logger.isInfoEnabled()){
                    	 logger.info("[run][closed   ][" + getDesc() + "]" + localAddress + "->" + remoteAddress);
                     }
                  }
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               } catch (RuntimeException e) {
                  logger.error(e.getMessage(), e);
               }
            }
         }
         try {
            Thread.sleep(interval);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
      
      if(future!=null && future.channel()!=null){
          future.channel().close();//线程被中断了，主动关闭连接
      }
      
      if(logger.isInfoEnabled()){
    	  logger.info("ConsumerThread(remoteAddress=" + remoteAddress + ") done.");
      }
   }

	private String getDesc() {
		return consumerImpl.toString() + "@" + toString();
	}
}
