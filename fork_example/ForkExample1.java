
// Demonstrating multithreading and thread synchronization in Java
public class ForkExample1 implements Runnable {

	int i; // the ID of the thread, so we can control behavior
	boolean busy; // the flag, Thread 1 will wait until Thread 0 is no longer busy before continuing
	ForkExample1 producer; // reference to the producer thread we will synchronize on. This is needed so we can control behavior.

	private static final int NUM_THREADS = 10;
	// create the runnable object
	public ForkExample1(int i, ForkExample1 producer) {
		this.i = i; // set the thread ID (0 or 1)
		if (i == 0) { 
			busy = true; 
		} // set the busy flag so Thread 1 waits for Thread 0
		else { 
			this.producer = producer; 
		}
	}

	// synchronized method to test if thread is busy or not
	public synchronized boolean isBusy() { return busy; } // What happens if this isn't synchronized? 

	// run method needed by runnable interface
	public void run() {
		if (i == 0) { // 1st thread, sleep for a while, then notify threads waiting
			try {
				// System.out.println("Producer going for first sleep.");
				// Thread.sleep(4000); // What happens if you put this sleep inside the synchronized block?
				// System.out.println("Producer waking up from first sleep.");
				synchronized(this) {
					System.out.println("Notifying somebody.");
					notify(); // notify() will only notify threads waiting on *this* object;
				}
				System.out.println("Producer going for second sleep.");
				Thread.sleep(4000); // What happens if you put this sleep inside the synchronized block?
				System.out.println("Producer waking up from second sleep.");
				synchronized(this) {
					System.out.println("Notifying somebody.");
					notify(); // notify() will only notify threads waiting on *this* object;
					busy = false; // must synchronize while editing the flag
				}
			}
			catch(InterruptedException tie) { 
				tie.printStackTrace();
			}
		}
		else {
			while (producer.isBusy()) { // check if producer thread is still working
				System.out.println("Waiting from thread " + i + "!");
				// must sychnronize to wait on producer object
				try { 
					Thread.sleep(2000);
					synchronized(producer) { 
						producer.wait(); 
						System.out.println("Thread " + i + " has been notified.");
					} 
				} // note we have synchronized on the object we are going to wait on
				catch(InterruptedException tie) { 
					tie.printStackTrace(); 
				}
			}
			System.out.println("Finished from thread " + i + "!");
		}
	}

	public static void main (String[] args) {
		ForkExample1 producer = new ForkExample1(0, null);

		for (int i = 1; i <= NUM_THREADS; i++) { 
			ForkExample1 consumer = new ForkExample1(i, producer);
			(new Thread(consumer)).start();
		}

		(new Thread(producer)).start();
	}

}