package main.sortBigFile.sort.kWayMerge;

import main.sortBigFile.sort.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.buffers.SectionWriters;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.readers.MergeArrayReader;
import main.sortBigFile.readers.IMergeArrayReader;
import main.sortBigFile.writers.ArrayWriter;
import main.sortBigFile.writers.IArrayWriter;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Merge file using k-way merge
 *
 * @param <T> type of sorting elements
 */
public class MergeFiles<T> {

    private final CyclicBufferHolder<T> cyclicBufferHolder;
    private final FileNamesHolder holder;
    private final IValueScanner<T> valueScanner;
    private final ICompareStrategy<T> compareStrategy;

    public MergeFiles(CyclicBufferHolder<T> cyclicBufferHolder, FileNamesHolder holder, IValueScanner<T> valueScanner, ICompareStrategy<T> compareStrategy) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.holder = holder;
        this.valueScanner = valueScanner;
        this.compareStrategy = compareStrategy;
    }

    /**
     * Merge files using k-way merge
     *
     * @param size           size of files which are taking part in merge
     * @param outputFileName name of file with results of merge
     * @throws IOException
     */
    public void merge(int size, String outputFileName) throws IOException {
        List<Scanner> scanners = createScanners(size);

        if (scanners.size() == 0) {
            return;
        }

        String newName = holder.getNewUniqueName(outputFileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(newName));
             OutputStreamWriter fw = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             SectionWriters<T> sectionWriters = new SectionWriters<>(cyclicBufferHolder, scanners)
        ) {
            IArrayWriter arrayWriter = new ArrayWriter<>(sectionWriters, valueScanner);
            IMergeArrayReader arrayReader = new MergeArrayReader<>(sectionWriters, compareStrategy);

            do {
                arrayWriter.fillBuffer();

                arrayReader.mergeTillMinMax(out);
                sectionWriters.tryFreeMemory();
            } while (sectionWriters.getUsedScanners().size() > 0);

            arrayReader.mergeTillEmpty(out);
        } finally {
            holder.pull(newName);
        }
    }

    private List<Scanner> createScanners(int size) throws FileNotFoundException {
        List<Scanner> scanners = new ArrayList<>(size);
        List<String> fileNames = holder.get(size);
        for (String name : fileNames) {
            File file = new File(name);
            if (file.exists()) {
                scanners.add(new Scanner(file, StandardCharsets.UTF_8.toString()));
            }
        }

        return scanners;
    }

}