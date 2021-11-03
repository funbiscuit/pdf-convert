package com.funbiscuit.pdfconvert;

import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PdfDocProcessor implements AutoCloseable {
    private final ExecutorService executorService;

    private final BlockingQueue<PDDocument> documents = new LinkedBlockingQueue<>();

    private int numberOfPages;

    @Setter
    private Runnable onClose;

    public PdfDocProcessor(File file, int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            try {
                PDDocument document = PDDocument.load(file);
                int pages = document.getNumberOfPages();
                documents.add(document);
                if (i == 0) {
                    numberOfPages = pages;
                } else if (numberOfPages != pages) {
                    throw new IllegalStateException("Different number of pages in the same file");
                }

            } catch (IOException e) {
                throw new IllegalArgumentException("Can't open PDF file", e);
            }
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        for (PDDocument document : documents) {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (onClose != null) {
            onClose.run();
        }
    }

    public void processPages(IntRange range, PageProcessor pageProcessor) {
        List<Future<?>> futures = new ArrayList<>();
        List<Integer> pages = range.getValues(numberOfPages);
        int count = pages.size();
        for (int page : pages) {
            futures.add(executorService.submit((Callable<Void>) (() ->
            {
                PDDocument document = documents.take();
                pageProcessor.process(page - 1, count, document);
                documents.add(document);
                return null;
            })));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Page processing failed", e);
            }
        }
    }

    @FunctionalInterface
    public interface PageProcessor {
        void process(int page, int total, PDDocument document);
    }
}
