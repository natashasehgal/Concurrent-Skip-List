import java.util.concurrent.atomic.*;
import java.util.Random;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantLock;

public final class C_LockFreeSkipList<T> {
	static final int MAX_LEVEL = 4;
	final Node<T> head = new Node<T>(Integer.MIN_VALUE);
	final Node<T> tail = new Node<T>(Integer.MAX_VALUE);
	Lock lock;

	public C_LockFreeSkipList() {
		for (int i = 0; i < head.next.length; i++) {
			head.next[i]= new AtomicMarkableReference<C_LockFreeSkipList.Node<T>>(tail, false);
		}
		lock = new ReentrantLock();
	}
	//NODES
	public static final class Node<T> {
		final T value;
		final int key;
		final AtomicMarkableReference<Node<T>>[] next;
		private int topLevel;

		public Node(int key) {
			value = null; this.key = key;
			next = (AtomicMarkableReference<Node<T>>[])new AtomicMarkableReference[MAX_LEVEL + 1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<Node<T>>(null,false);
			}
			topLevel = MAX_LEVEL;
		}

		public Node(T x, int height) {
			value = x;
			key = x.hashCode();
			topLevel = randomLevel();//height; //1 earlier
			next = (AtomicMarkableReference<Node<T>>[])new AtomicMarkableReference[topLevel + 1];
			for (int i = 0; i < next.length; i++) {
				next[i] = new AtomicMarkableReference<Node<T>>(null,false);
			}
		}
	}
	boolean add(T x) {

		int topLevel = 1; //FOR NOW//randomLevel();
		int bottomLevel = 0;
		Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
		while (true) {
			boolean found = find(x, preds, succs);
			if (found) {
				return false;
			} else {
				Node<T> newNode = new Node(x, topLevel);
				for (int level = bottomLevel; level <= topLevel; level++) {
					Node<T> succ = succs[level];
					newNode.next[level].set(succ, false);
				}
				Node<T> pred = preds[bottomLevel];
				Node<T> succ = succs[bottomLevel];
				newNode.next[bottomLevel].set(succ, false);
				if (!pred.next[bottomLevel].compareAndSet(succ, newNode,false, false)) {
					continue;
				}
				for (int level = bottomLevel+1; level <= topLevel; level++) {
					while (true) {
						pred = preds[level];
						succ = succs[level];
						if (pred.next[level].compareAndSet(succ, newNode, false, false))
							break;
						find(x, preds, succs);
					}
				}
				return true;
			}
		}
	}
	boolean remove(T x) {
		int bottomLevel = 0;
		Node<T>[] preds = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T>[] succs = (Node<T>[]) new Node[MAX_LEVEL + 1];
		Node<T> succ = null;
		while (true) {
			boolean found = find(x, preds, succs);
			if (!found) {
				return false;
			} else {
				Node<T> nodeToRemove = succs[bottomLevel];
				for (int level = nodeToRemove.topLevel;
						level >= bottomLevel+1; level--) {
					boolean[] marked = {false};
					try{
						succ = nodeToRemove.next[level].get(marked);//get succ
					}catch(Exception ex){}//System.out.println("HOHO");}
					try{
						while (!marked[0]) {
						nodeToRemove.next[level].attemptMark(succ, true);
						succ = nodeToRemove.next[level].get(marked);
						}
					}catch(Exception ex){}//System.out.println("HOHO");}
				}
				boolean[] marked = {false};
				succ = nodeToRemove.next[bottomLevel].get(marked);

				while (true) {
					boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ,false, true);
					succ = succs[bottomLevel].next[bottomLevel].get(marked);
					if (iMarkedIt) {
						find(x, preds, succs);
						return true;
					}
					else if (marked[0]) return false;
				}
			}
		}
	}

	boolean find(T x, Node<T>[] preds, Node<T>[] succs) {
		int bottomLevel = 0;
		int key = x.hashCode();
		boolean[] marked = {false};
		boolean snip;
		Node<T> pred = null, curr = null, succ = null;
retry:
		while (true) {
			pred = head;
			for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
				curr = pred.next[level].getReference();
				while (true) {
					succ = curr.next[level].get(marked);
					while (marked[0]) {
						snip = pred.next[level].compareAndSet(curr, succ,
								false, false);
						if (!snip) continue retry;
						curr = pred.next[level].getReference();
						succ = curr.next[level].get(marked);
					}
					if (curr.key < key){
						pred = curr; curr = succ;
					} else {
						break;
					}
				}
				preds[level] = pred;
				succs[level] = curr;
			}
			return (curr.key == key);
		}
	}

	public boolean compare(Node<T> a, Node<T> b)
	{
		try{
			Integer a1 = (Integer) a.value;
			Integer a2 = (Integer) b.value;
			return (a1>a2);
		}catch(NullPointerException ex){return false;}
	}

//IN THE END
	public boolean Check() //head is the smallest
	  {
			Node<T> pred = null;
		 	Node<T> curr = head.next[0].getReference();
			boolean wrong = false;
			while(curr!=tail)
			{

					System.out.print(curr.value+" ");
					if(pred!=null && compare(pred,curr))
						wrong = true;


				curr = curr.next[0].getReference();
				pred = curr;
			}
			return wrong;
	  }

  public T FindAndMarkMin()
  {
		Node<T> curr= null,succ = null,next=null,nextNext=null;T value = null;
		try{
			lock.lock();
			System.out.println("GOTHEAD");
			curr = head.next[0].getReference();

			if(curr==null || curr.value==null)return null;
			value = curr.value;

				for (int level = MAX_LEVEL;level >= 0; level--) {

					curr = head.next[level].getReference();
					if(curr.value == value) //make head.next[level] as my next
					{
						while(true)
						{
							next = curr.next[level].getReference();
							if(head.next[level].compareAndSet(curr,next,false,false))
								break;
						}
					}
		  }
	}finally{lock.unlock();}
	return value;
}

	public static int randomLevel() {
	    int lvl = (int)(Math.log(1.-Math.random())/Math.log(1.-0.5));
			System.out.print(lvl+"HEY");
			return Math.min(lvl+1, MAX_LEVEL);
	}
}
