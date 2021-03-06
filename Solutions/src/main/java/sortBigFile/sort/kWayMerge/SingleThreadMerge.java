package sortBigFile.sort.kWayMerge;

import sortBigFile.buffers.CyclicBufferHolder;
import sortBigFile.readers.ICompareStrategy;
import sortBigFile.sort.FileNamesHolder;
import sortBigFile.writers.IValueScanner;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Merge several files i single thread
 *
 * @param <T>  type of sorting elements
 */
public class SingleThreadMerge<T> implements IMergeStrategy {

    protected final CyclicBufferHolder<T> cyclicBufferHolder;
    protected final IValueScanner<T> valueScanner;
    protected final ICompareStrategy<T> compareStrategy;

    public SingleThreadMerge(CyclicBufferHolder<T> cyclicBufferHolder, IValueScanner<T> valueScanner, ICompareStrategy<T> compareStrategy) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.valueScanner = valueScanner;
        this.compareStrategy = compareStrategy;
    }

    @Override
    public String merge(final List<String> fileNames, String outputFilePath) {
        if (fileNames == null || fileNames.isEmpty() || outputFilePath == null)
            return null;

        try {
            Queue<String> queue = new ArrayDeque<>(fileNames);

            while (queue.size() > 1) {
                int size = Integer.min(cyclicBufferHolder.getSize(), queue.size());
                List<String> temp = new ArrayList<>(size);
                for (int i = 0; i < size; i++)
                    temp.add(queue.poll());

                queue.add(kWayMerge(temp, outputFilePath));
            }

            return queue.poll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String kWayMerge(List<String> fileNames, String outputFilePath) throws IOException {
        MergeFiles mergeFiles = new MergeFiles<>(cyclicBufferHolder, valueScanner, compareStrategy);
        String newName = FileNamesHolder.getNewUniqueName(outputFilePath);
        mergeFiles.merge(fileNames, newName);
        return newName;
    }

}
