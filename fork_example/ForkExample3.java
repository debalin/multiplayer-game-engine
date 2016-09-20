import java.util.concurrent.*;
import java.util.Random;

// Demonstrating multithreading and thread synchronization in Java
public class ForkExample3 implements Runnable {

	int threadID; // the ID of the thread, so we can control behavior
	ForkExample3 producer; // reference to the producer thread we will synchronize on. This is needed so we can control behavior.
	Object lock;

	private ConcurrentLinkedQueue<Integer> numbers;

	private static final int NUM_THREADS = 5;
	private static final int NUMBERS_SIZE = 20;
	
	public ForkExample3(int threadID, ForkExample3 producer, Object lock) {
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
				while (producer.getNumbers().size() > 0) {
					System.out.println("Thread " + threadID + " consuming " + producer.getNumbers().poll() + ".");
				}
			}
		}
	}

	public static void main (String[] args) {
		Object lock = new Object();
		ForkExample3 producer = new ForkExample3(0, null, lock);

		for (int i = 1; i <= NUM_THREADS; i++) { 
			ForkExample3 consumer = new ForkExample3(i, producer, lock);
			(new Thread(consumer)).start();
		}

		(new Thread(producer)).start();
	}

}