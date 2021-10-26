package com.funbiscuit.pdfconvert;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "checksum", mixinStandardHelpOptions = true, version = "pdf-convert 1.0",
        description = "Converts pdf document to png images.")
class PdfConvertCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The pdf to convert.")
    private File file;

    @CommandLine.Option(names = {"--dpi"}, description = "300, 600, ...")
    private int dpi = 300;

    @CommandLine.Option(names = {"--output"}, description = "Where to store generated files (current directory by default)")
    private String outputDir = ".";

    @Override
    public Integer call() throws Exception {

        long start = System.currentTimeMillis();

        File parentDir = new File(outputDir);
        if (!parentDir.mkdirs()) {
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
