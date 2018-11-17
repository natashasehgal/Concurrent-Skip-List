import java.util.Random;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
//javac -d . *.java
//java Program
public class CoarseTest{
        static Coarse_Priority_Queue<Integer> concurrentQueue = new Coarse_Priority_Queue<Integer>();

        public static void main(String[] args){
          List<String> L = MetaTesting.parseInput("metadata.txt");
          if(L==null || L.size() < 4){
            System.out.println("Error parsing input");
            return;
          }
          final int minInt = Integer.parseInt(L.get(0));
          final int maxInt = Integer.parseInt(L.get(1));
          final int threadCount = Integer.parseInt(L.get(2));
          final AtomicInteger totalSteps = new AtomicInteger(Integer.parseInt(L.get(3)));
          final int percentageInsert = Integer.parseInt(L.get(4));

          final AtomicInteger step = new AtomicInteger(0);
          Thread[] threads = new Thread[threadCount];
          Random rand = new Random();

          long instanceStart = System.currentTimeMillis();
          for (int i = 0; i < threads.length; i++) {
      			threads[i] = new Thread(new Runnable() {
                private ThreadLocal<Integer> myStep = new ThreadLocal<Integer>();
      					public void run() {//START ADDING
                  while (step.incrementAndGet() <= totalSteps.get()){
                      myStep.set(step.get());
                    int number = rand.nextInt(100);
        						if((number%100)+1 < percentageInsert){
                    int toInsert = rand.nextInt(maxInt-minInt+1)+minInt;
                    System.out.println("Step " + myStep.get()+": Thread "  + Thread.currentThread().getId()+ " - trying to insert..." + toInsert);
                    if(concurrentQueue.Add(toInsert))
                      System.out.println("Finished Step " + myStep.get() + ": Successfully inserted: " + toInsert);
                    else
                      System.out.println("Finished Step " + myStep.get() + ": Unable to insert " + toInsert);
                  }
                  else{
                    System.out.println("Step " + myStep.get()+": Thread "  + Thread.currentThread().getId()+ " - trying to extract min...");
                    Integer min = concurrentQueue.Dequeue();
                    if(min!=null)
                      System.out.println("Finished Step " + myStep.get() + ": Extracted min... " + min);
                    else
                      System.out.println("Finished Step " + myStep.get() +" Unable to extract min");
                  }

      					}
              }
      			});
      			threads[i].start();
      		}
          for (Thread thread : threads) {
      			try{
      				thread.join();
      			}catch(InterruptedException e){System.out.println("Interrupted Exception");}
      				catch(NullPointerException e){System.out.println("Null Pointer exception");}
      			}
          long instanceFinish = System.currentTimeMillis();
          System.out.println("Time taken:" + (instanceFinish - instanceStart) + " milliseconds.");
          concurrentQueue.Check();
      }
    }
