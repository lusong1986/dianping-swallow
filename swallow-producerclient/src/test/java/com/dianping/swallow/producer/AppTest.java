package com.dianping.swallow.producer;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.dianping.swallow.producer.impl.ProducerFactoryImpl;
import com.dianping.swallow.producer.impl.ProducerImpl;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
   private ProducerFactoryImpl pf = null;
   private String              message = "";

   /**
    * Create the test case
    * 
    * @param testName name of the test case
    */
   public AppTest(String testName) {
      super(testName);
      try {
         pf = ProducerFactoryImpl.getInstance(5000);
      } catch (Exception e3) {
         System.out.println(e3.toString());
      }
      for(int i = 0; i<10; i++){
         message += "AAbbCCddEEffGGhhII jKKllMMnnOOppQQrr SttUUvvWWxxYYzz11@@33$$55^^77**99))aaeeeffggesswweedd!@#$%^&*()";
      }
      
   }

   /**
    * @return the suite of tests being tested
    */
   public static Test suite() {
      return new TestSuite(AppTest.class);
   }

   /**
    * Rigourous Test :-)
    */
   public void testApp() {
      assertTrue(true);
   }

   private class task implements Runnable {

      public task(String content) {
      }

      @Override
      public void run() {
         Map<ProducerOptionKey, Object> pOptions = new HashMap<ProducerOptionKey, Object>();
         pOptions.put(ProducerOptionKey.PRODUCER_MODE, ProducerMode.ASYNC_MODE);
         pOptions.put(ProducerOptionKey.ASYNC_THREAD_POOL_SIZE, 5);
         pOptions.put(ProducerOptionKey.ASYNC_IS_CONTINUE_SEND, false);
         pOptions.put(ProducerOptionKey.RETRY_TIMES, 5);

         ProducerImpl ps = null;
         try {
            ps = pf.getProducer("songtong", pOptions);
         } catch (Exception e2) {
            System.out.println(e2.toString());
         }
         String str = "";
         long begin = System.currentTimeMillis();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("zip", "true");
         try {
            int idx = 0;
//                        for (int i = 0; i < 10000; i++) {
            while (true) {
               //			content += i++;
               str = ps.sendMessage("______[" + (idx++) + "]["+ message + "]", properties);
               //               sumTime += (end - begin);
               try {
                  Thread.sleep(1000);
               } catch (Exception e) {
                  // 
               }
               System.out.println(idx + ": " + str);
            }
         } catch (Exception e1) {
            System.out.println(e1.toString());
         }
         long end = System.currentTimeMillis();
         System.out.println(end - begin);
      }
   }

   public void doTest() {
      for (int i = 0; i < 100; i++) {
         String newContent = "NO_" + i;
         Thread td = new Thread(new task(newContent));
         td.start();
      }
   }

   public static void main(String[] args) {
      new AppTest("111").doTest();
      //      ProducerConfigure pc = new ProducerConfigure("producer.properties");
      //      System.out.println(pc.getRemoteServiceTimeout());
      //      System.out.println(pc.getThreadPoolSize());
      //      System.out.println(pc.getDestinationName());
      //      System.out.println(pc.getProducerModeStr());
      //      System.out.println(pc.isContinueSend());
   }
}
