
import java.util.concurrent.atomic.*;
public class Fine_Priority_Queue<T>
{
    public F_LockFreeSkipList<T> skipList;
    private AtomicInteger length;

    public int getLength(){
      return length.get();
    }

    public Fine_Priority_Queue(){
        length = new AtomicInteger(0);
        skipList = new F_LockFreeSkipList<T>();
    }

    public boolean Add(T value){
        boolean res = skipList.add(value);
        if(res){
            length.getAndIncrement();
            return true;
        }
        return false;
    }
    public T Dequeue(){
        T min = skipList.FindAndMarkMin();
        return min;
    }
    public void Check(){
      System.out.println();
      boolean wrong = skipList.Check();
      System.out.println();
			if(wrong)
				System.out.println("Priority Queue propery isn't satisified!");
			else
				System.out.println("Priority Queue propery is satisified!");
      System.out.println();
    }

}
