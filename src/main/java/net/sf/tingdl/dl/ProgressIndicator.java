/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.tingdl.dl;

/**
 *
 * @author aploese
 */
public class ProgressIndicator {

    private final long expectedFileLength;
    private final long startSysTime;

    public ProgressIndicator(long expectedFileLength) {
        this.expectedFileLength = expectedFileLength;
        this.startSysTime = System.currentTimeMillis();
    }

    private final static double BYTE_TO_MB = 1d / 1024 / 1024;
    private final static double BYTE_TO_KB = 1d / 1024;

    public void updateProgress(final long fileLength) {
        final int width = 50; // progress bar width in chars
        final int progressPercentage = (int) (fileLength * 100 / expectedFileLength);
        final int drawPercent = (int) ((double) fileLength / expectedFileLength * width);

        final double flKB = BYTE_TO_KB * fileLength;
        final double flMB = BYTE_TO_MB * fileLength;
        final double runTime = (double) (System.currentTimeMillis() - startSysTime) / 1000;
        final double kB_s = flKB / runTime;
        final long eta = Math.round(BYTE_TO_KB * (expectedFileLength - fileLength) / kB_s);
        final long eta_s = eta % 60;
        final long eta_m = (eta / 60) % 60;
        final long eta_h = eta / 3600;

        StringBuilder sb = new StringBuilder(128);
        sb.append(String.format("\r%3d%% [", progressPercentage));
        for (int i = 0; i < drawPercent; i++) {
            sb.append('=');
        }
        sb.append('>');
        for (int i = drawPercent; i < width; i++) {
            sb.append(' ');
        }

        sb.append(String.format("] %.3f MB\t %.2f kB/s\t ETA %d:%02d:%02d", flMB, kB_s, eta_h, eta_m, eta_s));
        System.out.print(sb);
    }

    void printSaveFile(String fileName) {
             System.out.printf("Save file: %s length: %.3fMB\n", fileName, BYTE_TO_MB * expectedFileLength );
    }

}
