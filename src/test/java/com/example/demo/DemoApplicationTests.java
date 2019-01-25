package com.example.demo;

import com.jesper.seckill.MainApplication;
import com.jesper.seckill.controller.SeckillController;
import com.jesper.seckill.service.SeckillService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class DemoApplicationTests {
	@Autowired
	SeckillService seckillService;
	/**
	 * 并发量
	 */
	private int threadNum = 500;

	//销售量

	private int goodSale = 0;

	//买成功的数量
	private int accountNum = 0;
	//买成功的人的ID集合
	private List<Long> successUsers = new ArrayList<>();


	/*当创建 CountDownLatch 对象时，对象使用构造函数的参数来初始化内部计数器。每次调用 countDown() 方法,
     CountDownLatch 对象内部计数器减一。当内部计数器达到0时， CountDownLatch 对象唤醒全部使用 await() 方法睡眠的线程们。*/
	private CountDownLatch startCountDownLatch = new CountDownLatch(1);
	private CountDownLatch endDownLatch=new CountDownLatch(threadNum);



	@Before
	public void setUp() throws Exception {
		seckillService.prepare(SeckillController.fieldId,SeckillController.goodsId.toString(),SeckillController.siteNo);
	}

	@Test
	public void contextLoads() throws InterruptedException {
		for (int i = 0; i < threadNum; i++) {
			new Thread(new UserRequest(SeckillController.goodsId, 1, i)).start();
		}
		System.out.println("before startCountDownLatch contdown");
		startCountDownLatch.countDown();
		System.out.println("end startCountDownLatch contdown");
		endDownLatch.await();

		System.out.println("end endDownLatch ");
		System.out.println("-----------购买成功的用户数量----------为" + accountNum);
		System.out.println("-----------销售量--------------------为" + goodSale);

		System.out.println(successUsers);
		//等待消息队列执行完成
		Thread.sleep(5000);
	}

	private class UserRequest implements Runnable {

		private Long goodsId;
		private int buyCount;
		private long userId;

		public UserRequest(long goodsId, int buyCount, long userId) {
			this.goodsId = goodsId;
			this.buyCount = buyCount;
			this.userId = userId;
		}

		@Override
		public void run() {
			try {
				startCountDownLatch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			//如果更新数据库成功，也就代表购买成功了
			String uuid = UUID.randomUUID().toString();
			if (seckillService.seckill(SeckillController.fieldId,goodsId,SeckillController.siteNo,userId,uuid)) {
				//对service加锁，因为很多线程在访问同一个service对象，不加锁将导致购买成功的人数少于预期，且数量不对，可自行测试
				synchronized (UserRequest.class) {
					//销售量
					goodSale += buyCount;
					accountNum++;
					//收录购买成功的人
					successUsers.add(userId);
				}
			}
			endDownLatch.countDown();
		}
	}


}

