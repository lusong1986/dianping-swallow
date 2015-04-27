package com.dianping.swallow.web.util;

import java.util.Date;
import org.bson.types.BSONTimestamp;

public class MongoUtils {
   private MongoUtils() {
   }

   public static BSONTimestamp longToBSONTimestamp(Long messageId) {
      int time = (int) (messageId >>> 32);
      int inc = (int) (messageId & 0xFFFFFFFF);
      BSONTimestamp timestamp = new BSONTimestamp(time, inc);
      return timestamp;
   }

   public static Long BSONTimestampToLong(BSONTimestamp timestamp) {
      int time = timestamp.getTime();
      int inc = timestamp.getInc();
      Long messageId = ((long) time << 32) | inc;
      return messageId;
   }

   public static Long getLongByCurTime() {
      return getLongByDate(new Date());
   }
   
   public static Long getLongByDate(Date date) {
	      int time = (int) (date.getTime() / 1000);
	      BSONTimestamp bst = new BSONTimestamp(time, 1);
	      return BSONTimestampToLong(bst);
	   }

   
   public static void main(String[] args) {
	   
	  System.out.println(-1 << 32);
	   
      System.out.println(MongoUtils.longToBSONTimestamp(6141115050670161925L).getTime());
      System.out.println(MongoUtils.longToBSONTimestamp(6141115050670161925L));
      BSONTimestamp ts = new BSONTimestamp(1376825482,573);
      System.out.println(new Date(ts.getTime()*1000L));
      System.out.println(Integer.MAX_VALUE/86400/30/12);
   }
}
