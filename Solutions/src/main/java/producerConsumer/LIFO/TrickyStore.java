//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package producerConsumer.LIFO;

import producerConsumer.IStore;
import producerConsumer.IWaitStrategy;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;

public class TrickyStore<T> implements IStore<T> {
    protected final int maxSize;
    protected final T[] array;
    protected volatile int currentSize;
    protected final ReentrantLock lock = new ReentrantLock();
    protected final IWaitStrategy waitStrategy;

    protected AtomicIntegerFieldUpdater<TrickyStore> currentSizeUpdater = AtomicIntegerFieldUpdater.newUpdater(TrickyStore.class, "currentSize");

    public TrickyStore(int maxSize, Class<T> cls, IWaitStrategy waitStrategy) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.array = ((T[]) Array.newInstance(cls, maxSize));
        this.waitStrategy = waitStrategy;
    }

    public T get() throws InterruptedException {
        {
            do {
                if (this.currentSize > 0) {
                    this.lock.lockInterruptibly();
                    try {
                        if (this.currentSize > 0) {
                            currentSizeUpdater.lazySet(this, this.currentSize - 1);
                            T item = this.array[this.currentSize];
                            this.array[this.currentSize] = null;

                            return item;
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }

                waitStrategy.trySpinWait();
            } while (waitStrategy.canRun());
        }

        return null;
    }

    public void put(T item) throws InterruptedException {
        do {
            if (this.currentSize < this.maxSize) {
                this.lock.lockInterruptibly();

                try {
                    if (this.currentSize < this.maxSize) {
                        this.array[this.currentSize] = item;
                        currentSizeUpdater.lazySet(this, this.currentSize + 1);
                        return;
                    }
                } finally {
                    this.lock.unlock();
                }
            }

            waitStrategy.trySpinWait();
        } while (waitStrategy.canRun());
    }

    public boolean IsEmpty() {
        return this.currentSize == 0;
    }

    @Override
    public void clear() throws InterruptedException {
        if (currentSize > 0) {
            lock.lockInterruptibly();
            try {
                for (int i = 0; i < currentSize; i++)
                    array[--currentSize] = null;
            } finally {
                lock.unlock();
            }
        }
    }
}
