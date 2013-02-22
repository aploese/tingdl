/*
 * ting-dl - Open Source downloader for TING - http://tingdl.sf.net
 * Copyright (C) 2013  Arne Plöse.
 *
 * This file is part of ting-dl.
 *
 * Ting-dl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Papaya is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ting-dl.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.sf.tingdl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.tingdl.config.Book;
import net.sf.tingdl.config.TingConfig;
import net.sf.tingdl.dl.TingDownloadJob;
import net.sf.tingdl.dl.TingDownloader;

/**
 *
 * @author aploese
 */
public class Main {

    private TingConfig tingConfig;
    private TingDownloader tingDownloader;

    public static void main(String[] args) throws Exception {
        Main app = new Main();
        app.init();
        app.updateBooks();
        app.checkVersions();
    }

    private void init() {
        tingConfig = new TingConfig();
        tingConfig.findMountedTing();
        tingConfig.readSettings();
        tingDownloader = new TingDownloader();
    }

    private void updateBooks() {
        List<Book> booksToDownload = new ArrayList();
        Map<Integer, Book> booksToUpdateOnTing = new HashMap();
        Collection<Integer> tbdBooks;
        try {
            tbdBooks = tingConfig.readTbdFile();
            for (Integer i : tbdBooks) {
                booksToUpdateOnTing.put(i, new Book(i, tingConfig.getArea()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File[] bookTxtFiles = tingConfig.getTingDir().listFiles(new FilenameFilter() {
            Pattern p = Pattern.compile(String.format("\\d\\d\\d\\d\\d_%s.txt", tingConfig.getArea()));

            @Override
            public boolean accept(File dir, String name) {
                Matcher m = p.matcher(name);
                return m.matches();
            }
        });
        for (File bookTxtFile : bookTxtFiles) {
            try {
                Book b = new Book(bookTxtFile);
                booksToUpdateOnTing.put(b.getId(), b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Collection<Book> latestBooksInBackupDir = tingConfig.getLatestBooksFromBackup();

        List<TingDownloadJob> jobs = new ArrayList();
        for (Book b : latestBooksInBackupDir) {
            TingDownloadJob job = new TingDownloadJob();
            job.setBackup(true);
            job.setBook(b);
            Book tb = booksToUpdateOnTing.remove(b.getId());
            if (tb != null) {
                if (tb.updateNeeded(b)) {
                    job.setTing(true);
                }
            }
            jobs.add(job);
        }
        // add books not already in backup
        for (Book tb : booksToUpdateOnTing.values()) {
            TingDownloadJob job = new TingDownloadJob();
            job.setBackup(true);
            job.setTing(true);
            job.setBook(tb);
            jobs.add(job);
        }
        booksToUpdateOnTing.clear();

        // get serverIP which can handle the request
        InetAddress server = tingDownloader.getServerIp(tingConfig.getServer());
        //SerialNumber registration, if needed
        if (tingConfig.isUninitializedSerialVersion()) {
            tingConfig.setSerial(tingDownloader.addSerialNumber(tingConfig.getSerial(), tingConfig.getFw(), tingConfig.getSw()));
        } else {
            tingDownloader.setSerialNumber(tingConfig.getSerial());
        }

        tingDownloader.downloadBooks(jobs, tingConfig.getTingDir());

        try {
            //throw out junk 
            tingConfig.clearTbdFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkVersions()  throws IOException {
        Properties verionCheckProps = tingDownloader.checkCdfsVersion("win", tingConfig.getFw(), tingConfig.getSw());
        if (!verionCheckProps.isEmpty()) {
            System.err.printf("New Ting CDFS\n");
        }
        verionCheckProps = tingDownloader.checkFwVersion("win", tingConfig.getFw(), tingConfig.getSw());
        if (!verionCheckProps.isEmpty()) {
            System.err.printf("New Ting Firmware\n");
        }
    }
}
