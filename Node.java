import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantLock.*;

public class Node{
	public int value;
	public final AtomicBoolean flag;
	public Node(int priority){
		value = priority;
		flag = new AtomicBoolean(false);
	}
	public boolean lock(long timeout){
		long startTime = System.currentTimeMillis();
		while(true && ((System.currentTimeMillis()-startTime)<=timeout)){
			while(flag.get()==true && (System.currentTimeMillis()-startTime)<=timeout){};
			if((System.currentTimeMillis()-startTime)>=timeout)
				break;
			if(!flag.get() && !flag.getAndSet(true))
				return true; //got it
		}
		return false;
	}
	public void unlock(){
		flag.set(false);
		return;
	}
}
