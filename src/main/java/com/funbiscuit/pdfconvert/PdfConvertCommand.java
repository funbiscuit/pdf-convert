package com.funbiscuit.pdfconvert;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "pdf-convert", mixinStandardHelpOptions = true, version = "pdf-convert 0.1",
        description = "Converts pdf document to png images.")
class PdfConvertCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The pdf to convert.")
    private File file;

    @Option(names = {"--dpi"}, description = "300, 600, ...")
    private int dpi = 300;

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

        PDDocument document = PDDocument.load(file);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, dpi, ImageType.RGB);
            File outfile = new File(parentDir, String.format("%d.%s", page + 1, "png"));
            ImageIO.write(bim, "png", outfile);
        }
        document.close();
        double dur = (double) System.currentTimeMillis() - start;
        dur /= 1000;
        System.out.println("Conversion took: " + dur + "s");

        return 0;
    }
}
