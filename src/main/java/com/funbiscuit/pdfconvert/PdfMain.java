package com.funbiscuit.pdfconvert;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import lombok.SneakyThrows;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import picocli.CommandLine;

import javax.imageio.spi.IIORegistry;


public class PdfMain {
    @SneakyThrows
    public static void main(String[] args) {
        IIORegistry.getDefaultInstance().registerServiceProvider(new JBIG2ImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());

        int exitCode = new CommandLine(new PdfConvertCommand()).execute(args);
        System.exit(exitCode);
    }
}
