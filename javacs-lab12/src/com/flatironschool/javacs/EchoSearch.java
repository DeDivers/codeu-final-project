package com.flatironschool.javacs;
import redis.clients.jedis.Jedis;
public class EchoSearch {
	public static void main(String[] args) throws Exception{
		if (args.length != 2) {
			if (args.length < 2) {
				System.out.println("Too few args");
				System.out.println(args[0]);
			} else {
				System.out.println("Too many args");
			}
			System.exit(1);
		}
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		WikiSearch.search(args[0], index).print();
	}
}