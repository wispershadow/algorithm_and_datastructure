package io.wispersd.infra.datastructure;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

//https://www.codeproject.com/articles/153898/yet-another-implementation-of-a-lock-free-circular
public class ArrayLockFreeQueue<T> {
	private final int queueSize;
	private final T[] circularArray;
	private final AtomicInteger writeIndex = new AtomicInteger(0);
	private final AtomicInteger readIndex = new AtomicInteger(0);
	//the separator index between fully written data and data being written(reserved)
	private final AtomicInteger maximumReadIndex = new AtomicInteger(0);
	
	public ArrayLockFreeQueue(Supplier<T[]> supplier) {
		this.circularArray = supplier.get();
		this.queueSize = circularArray.length;
	}

	private int countToIndex(int count) {
		return count % queueSize;
	}
	
	public boolean isFull() {
		return isFull(writeIndex.intValue(), readIndex.intValue());
	}
	
	private boolean isFull(int curWriteIndex, int curReadIndex) {
		final int convertedWriteIndex = countToIndex(curWriteIndex + 1);
		final int convertedReadIndex = countToIndex(curReadIndex);
		return convertedWriteIndex == convertedReadIndex;
	}
	
	
	public boolean isEmpty() {
		return isEmpty(readIndex.intValue(), maximumReadIndex.intValue());
	}
	
	private boolean isEmpty(int curReadIndex, int curMaxReadIndex) {
		final int convertedReadIndex = countToIndex(curReadIndex);
		final int convertedMaxReadIndex = countToIndex(curMaxReadIndex);
		return convertedReadIndex == convertedMaxReadIndex;
	}
	
	
	public boolean push(T data) {
		int curWriteIndex;
		int curReadIndex;
		do {
			curWriteIndex = writeIndex.get();
			curReadIndex = readIndex.get();
			if (isFull(curWriteIndex, curReadIndex)) {
				return false;
			}
		}
		while (writeIndex.compareAndSet(curWriteIndex, curWriteIndex + 1));
		circularArray[countToIndex(curWriteIndex)] = data;
		final int curMaxReadIndex = maximumReadIndex.get();
		//ensure same reserve order for read and write for multiple producers
		while (!maximumReadIndex.compareAndSet(curMaxReadIndex, curMaxReadIndex + 1)) {
			Thread.currentThread().yield();
		}
		return true;
	}
	
	public T pop() {
		int curReadIndex;
		int curMaxReadIndex;
		do {
			curReadIndex = readIndex.get();
			curMaxReadIndex = maximumReadIndex.get();
			if (isEmpty(curReadIndex, curMaxReadIndex)) {
				return null;
			}
			T data = circularArray[countToIndex(curReadIndex)];
			if (readIndex.compareAndSet(curReadIndex, curReadIndex + 1)) {
				return data;
			}
		}
		while (true);
	}
	
	

}
