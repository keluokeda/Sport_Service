package com.happyheng.cache;

import java.sql.Connection;

import com.happyheng.dao.NewsCountDao;
import com.happyheng.dao.impl.NewsCountDaoImplement;
import com.happyheng.utils.ConnectionFactory;
import com.happyheng.utils.Redis;

import redis.clients.jedis.Jedis;

public class NewsCountCache {
	
	public static final String KEY_ARTICLE_COUNT = "articleCount";
	public static final String KEY_FIELD_COUNT = "art:";
	
	public String getAndAddCountFromCache(String id) throws Exception {
		
		Jedis jedis = Redis.getConnection();
		String fieldCountKey = KEY_FIELD_COUNT + id;
		
		//1、查看key中是否有
		if (!jedis.exists(KEY_ARTICLE_COUNT) || !jedis.hexists(KEY_ARTICLE_COUNT, fieldCountKey)) {
			
			//2、从数据库中进行读取并存入Redis中
			Connection connection = ConnectionFactory.getInstance().makeConnection();
			NewsCountDao dao = new NewsCountDaoImplement();
			String readCount = dao.getNewsReadCount(connection, id);
			
			if (readCount.equals("")) {
				throw new Exception();
			}
			
			jedis.hset(KEY_ARTICLE_COUNT, fieldCountKey, readCount);
		}
		
		//3、先+1，然后获取
		jedis.hincrBy(KEY_ARTICLE_COUNT, fieldCountKey, 1);
		String count = jedis.hget(KEY_ARTICLE_COUNT, fieldCountKey);
		return count;
		
	}
}
