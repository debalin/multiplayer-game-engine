import java.util.concurrent.*;
import java.util.Random;

// Demonstrating multithreading and thread synchronization in Java
public class ForkExample4 implements Runnable {

	int threadID; // the ID of the thread, so we can control behavior
	ForkExample4 producer; // reference to the producer thread we will synchronize on. This is needed so we can control behavior.
	Object lock;

	CyclicBarrier barrier;

	private ConcurrentLinkedQueue<Integer> numbers;

	private static final int NUM_THREADS = 5;
	private static final int NUMBERS_SIZE = 20;
	
	public ForkExample4(int threadID, ForkExample4 producer, Object lock, CyclicBarrier barrier) {
		this.threadID = threadID; // set the thread ID
		this.lock = lock;
		if (threadID != 0) { 
			this.producer = producer;
			this.barrier = barrier; 
		}
		else {
			numbers = new ConcurrentLinkedQueue<Integer>();
		}

	}

	public ConcurrentLinkedQueue getNumbers() {
		return numbers;
	} 

	public void run() {
		if (threadID == 0) { 
			System.out.println("Starting this endless loop of life, er producing and waiting.");
			Random random = new Random();
			while (true) {
				while (numbers.size() <= NUMBERS_SIZE) {
					numbers.add(random.nextInt(25));
				}
			}
		}
		else {
			System.out.println("Starting this endless loop of life, er consuming and waiting, from thread " + threadID + ".");
			while (true) {
				int count = 2;
				while (producer.getNumbers().size() > 0 && count > 0) {
					System.out.println("Thread " + threadID + " consuming " + producer.getNumbers().poll() + ".");
					count--;
				}
				try {
					barrier.await();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static void main (String[] args) {
		Object lock = new Object();
		ForkExample4 producer = new ForkExample4(0, null, lock, null);
		CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);

		for (int i = 1; i <= NUM_THREADS; i++) { 
			ForkExample4 consumer = new ForkExample4(i, producer, lock, barrier);
			(new Thread(consumer)).start();
		}

		(new Thread(producer)).start();
	}

}