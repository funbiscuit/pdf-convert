package com.funbiscuit.pdfconvert;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Command(name = "pdf-convert", mixinStandardHelpOptions = true, version = "pdf-convert 0.1",
        description = "Converts pdf document to png images.")
class PdfConvertCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The pdf to convert.")
    private File file;

    @Option(names = {"--dpi"}, description = "300, 600, ...")
    private int dpi = 300;

    @Option(names = {"-p", "--progress"}, description = "Display progress of conversion")
    private boolean progress = false;

    @Option(names = {"--out-dir"}, description = "Where to store generated files (current directory by default)")
    private String outputDir = "";

    @Override
    public Integer call() throws Exception {

        long start = System.currentTimeMillis();

        if (outputDir.isEmpty()) {
            outputDir = ".";
        }

        String filename = file.getName();
        if (!filename.toLowerCase().endsWith(".pdf")) {
            System.out.println("Invalid pdf filename: '" + filename + "'");
            return -1;
        }

        filename = filename.substring(0, filename.length() - 4);

        outputDir += "/" + filename + "/";

        File parentDir = new File(outputDir);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.out.println("Can't create output directory '" + outputDir + "'");
            return -1;
        }

        try (PDDocument document = PDDocument.load(file)) {
            Stream<Integer> pages = IntStream.range(0, document.getNumberOfPages()).boxed();

            if (progress) {
                pages = ProgressBar.wrap(pages, new ProgressBarBuilder()
                        .setTaskName("Convert to PNG")
                        .setUpdateIntervalMillis(500)
                        .setStyle(ProgressBarStyle.ASCII));
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            pages.forEach(page -> renderPage(page, pdfRenderer)
                    .ifPresent(img -> saveImage(img, parentDir, page + 1)));
        }

        double dur = (double) System.currentTimeMillis() - start;
        dur /= 1000;
        System.out.println("Conversion took: " + dur + "s");

        return 0;
    }

    private Optional<BufferedImage> renderPage(int pageIndex, PDFRenderer pdfRenderer) {
        try {
            return Optional.of(pdfRenderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void saveImage(BufferedImage image, File parentDir, int index) {
        try {
            ImageIO.write(image, "png", new File(parentDir, String.format("%d.%s", index, "png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
