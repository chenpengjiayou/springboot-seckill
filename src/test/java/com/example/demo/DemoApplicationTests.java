package com.example.demo;

import com.jesper.seckill.MainApplication;
import com.jesper.seckill.service.GoodsService;
import com.jesper.seckill.vo.GoodsVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class DemoApplicationTests {
	@Autowired
	GoodsService goodsService;
	private int goodAmount = 50;
	/**
	 * 并发量
	 */
	private int threadNum = 1;

	//销售量
	private int goodSale = 0;

	//买成功的数量
	private int accountNum = 0;
	//买成功的人的ID集合
	private List<Integer> successUsers = new ArrayList<>();


	/*当创建 CountDownLatch 对象时，对象使用构造函数的参数来初始化内部计数器。每次调用 countDown() 方法,
     CountDownLatch 对象内部计数器减一。当内部计数器达到0时， CountDownLatch 对象唤醒全部使用 await() 方法睡眠的线程们。*/
	private CountDownLatch countDownLatch = new CountDownLatch(threadNum);

	@Test
	public void contextLoads() {
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(2L);
		for (int i = 0; i < threadNum; i++) {
			new Thread(new UserRequest(goodsVo, 1, i)).start();
//			countDownLatch.countDown();
		}

		//让主线程等待200个线程执行完，休息2秒，不休息的话200条线程还没执行完，就打印了
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("-----------购买成功的用户数量----------为" + accountNum);
		System.out.println("-----------销售量--------------------为" + goodSale);
		System.out.println("-----------剩余数量------------------为" + (goodAmount - goodSale));
		System.out.println(successUsers);
	}

	private class UserRequest implements Runnable {

		private GoodsVo goodsVo;
		private int buyCount;
		private int userId;

		public UserRequest(GoodsVo goodsVo, int buyCount, int userId) {
			this.goodsVo = goodsVo;
			this.buyCount = buyCount;
			this.userId = userId;
		}

		@Override
		public void run() {
			/*try {
				//让线程等待，等200个线程创建完一起执行
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/

			//如果更新数据库成功，也就代表购买成功了
			if (goodsService.reduceStock(goodsVo)) {
				//对service加锁，因为很多线程在访问同一个service对象，不加锁将导致购买成功的人数少于预期，且数量不对，可自行测试
				synchronized (goodsService) {
					//销售量
					goodSale += buyCount;
					accountNum++;
					//收录购买成功的人
					successUsers.add(userId);
				}
			}
		}
	}


}

