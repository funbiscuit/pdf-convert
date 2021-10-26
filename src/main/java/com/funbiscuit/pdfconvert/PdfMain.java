package com.funbiscuit.pdfconvert;

import lombok.SneakyThrows;
import picocli.CommandLine;


public class PdfMain {
    @SneakyThrows
    public static void main(String[] args) {
        int exitCode = new CommandLine(new PdfConvertCommand()).execute(args);
        System.exit(exitCode);
    }
}
