package com.funbiscuit.pdfconvert;

import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PdfDocProcessor implements AutoCloseable {
    private final File file;
    private final ExecutorService executorService;

    private final List<PDDocument> documents = new ArrayList<>();
    private int numberOfPages;

    @Setter
    private Runnable onClose = () -> {
    };

    public PdfDocProcessor(File file, int numThreads) {
        this.file = file;
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
        onClose.run();
    }

    public void processPages(PageProcessor pageProcessor) {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfPages; i++) {
            int page = i;
            futures.add(executorService.submit(() ->
                    pageProcessor.process(page, numberOfPages,
                            documents.get(page % documents.size()))));
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
