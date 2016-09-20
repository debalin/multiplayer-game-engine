import java.util.concurrent.*;
import java.util.Random;

// Demonstrating multithreading and thread synchronization in Java
public class ForkExample2 implements Runnable {

	int threadID; // the ID of the thread, so we can control behavior
	ForkExample2 producer; // reference to the producer thread we will synchronize on. This is needed so we can control behavior.
	Object lock;

	private ConcurrentLinkedQueue<Integer> numbers;

	private static final int NUM_THREADS = 5;
	private static final int NUMBERS_SIZE = 20;
	
	public ForkExample2(int threadID, ForkExample2 producer, Object lock) {
		this.threadID = threadID; // set the thread ID
		this.lock = lock;
		if (threadID != 0) { 
			this.producer = producer; 
		}
		else {
			numbers = new ConcurrentLinkedQueue<Integer>();
		}

	}

	public ConcurrentLinkedQueue getNumbers() {
		return numbers;
	} 

	// run method needed by runnable interface
	public void run() {
		if (threadID == 0) { // 1st thread, sleep for a while, then notify threads waiting
			System.out.println("Starting this endless loop of life, er producing and waiting.");
			Random random = new Random();
			while (true) {
				while (numbers.size() <= NUMBERS_SIZE) {
					numbers.add(random.nextInt(25));
				}
				System.out.println("Added enough numbers, will wait now to get this empty.");
				synchronized(lock) {
					try {
						lock.notify();
						lock.wait();
					}
					catch (InterruptedException e) {}
				}
			}
		}
		else {
			System.out.println("Starting this endless loop of life, er consuming and waiting, from thread " + threadID + ".");
			while (true) {
				while (producer.getNumbers().size() > 0) {
					System.out.println("Thread " + threadID + " consuming " + producer.getNumbers().poll() + ".");
				}
				System.out.println("Nothing to consume, let's wake up everybody, which would also include the producer, from thread " + threadID + ".");
				synchronized(lock) {
					try {
						lock.notify();
						lock.wait();
					}
					catch (InterruptedException e) {}
				}
			}
		}
	}

	public static void main (String[] args) {
		Object lock = new Object();
		ForkExample2 producer = new ForkExample2(0, null, lock);

		for (int i = 1; i <= NUM_THREADS; i++) { 
			ForkExample2 consumer = new ForkExample2(i, producer, lock);
			(new Thread(consumer)).start();
		}

		(new Thread(producer)).start();
	}

}