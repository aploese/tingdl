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
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.tingdl.config.Book;
import net.sf.tingdl.config.TingConfig;
import net.sf.tingdl.dl.TingDownloadJob;
import net.sf.tingdl.dl.TingDownloader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author aploese
 */
public class Main {

    private enum Job {

        NOTHING,
        CHECK_BACKUP,
        CHECK_TING,
        CHECK_ALL,
        DLOWNLOAD,
        CHECK_VERSIONS;
    }
    private static final String CMDL_OPT_HELP = "help";
    private static final String CMDL_OPT_ENABLE_BACKUP = "enable-backup";
    private static final String CMDL_OPT_BACKUP_DIR = "backup-dir";
    private static final String CMDL_OPT_TING_DIR = "ting-dir";
    private static final String CMDL_OPT_CHECK_ALL = "check-all";
    private static final String CMDL_OPT_CHECK_TING = "check-ting";
    private static final String CMDL_OPT_CHECK_BACKUP = "check-backup";
    private static final String CMDL_OPT_CHECK_VERSIONS = "check-versions";
    private TingDownloader tingDownloader;

    public static void main(String[] args) throws Exception {

        Main app = new Main();
        Job j = app.init(args);
        switch (j) {
            case NOTHING:
                break;
            case CHECK_ALL:
                app.checkBackupMd5();
                app.checkTingMd5();
                break;
            case CHECK_BACKUP:
                app.checkBackupMd5();
                break;
            case CHECK_TING:
                app.checkTingMd5();
                break;
            case DLOWNLOAD:
                app.updateBooks();
                break;
            case CHECK_VERSIONS:
                app.checkVersions();
                break;
        }
    }

    private TingConfig getTingConfig() {
        return TingConfig.getTingConfig();
    }

    private Job init(String[] args) throws ParseException {
        Options opts = buildCmdlOptions();

        CommandLineParser cmdParser = new PosixParser();
        CommandLine cml = cmdParser.parse(opts, args);
        if (cml.hasOption(CMDL_OPT_HELP)) {
            printHelp(opts);
            return Job.NOTHING;
        }


        if (cml.hasOption(CMDL_OPT_TING_DIR)) {
            getTingConfig().setTingDir(new File(cml.getOptionValue(CMDL_OPT_TING_DIR)));
        } else {
            getTingConfig().findMountedTing();
        }

        if (cml.hasOption(CMDL_OPT_BACKUP_DIR)) {
            getTingConfig().setTingBackupDir(new File(cml.getOptionValue(CMDL_OPT_BACKUP_DIR)));
        } else {
            if (cml.hasOption(CMDL_OPT_ENABLE_BACKUP) || cml.hasOption(CMDL_OPT_CHECK_ALL) || cml.hasOption(CMDL_OPT_CHECK_BACKUP)) {
                getTingConfig().setTingBackupDir(new File(System.getProperties().getProperty("user.home"), ".ting"));
            }
        }

        if (cml.hasOption(CMDL_OPT_CHECK_ALL)) {
            return Job.CHECK_ALL;
        }

        if (cml.hasOption(CMDL_OPT_CHECK_BACKUP)) {
            return Job.CHECK_BACKUP;
        }

        if (cml.hasOption(CMDL_OPT_CHECK_TING)) {
            return Job.CHECK_TING;
        }

        if (cml.hasOption(CMDL_OPT_CHECK_VERSIONS)) {
            return Job.CHECK_VERSIONS;
        }

        getTingConfig().readSettings();
        tingDownloader = new TingDownloader();

        return Job.DLOWNLOAD;
    }

    private void updateBooks() {
        Map<Integer, Book> booksToUpdateOnTing = new HashMap();
        Collection<Integer> tbdBooks;
        try {
            tbdBooks = getTingConfig().readTbdFile();
            for (Integer i : tbdBooks) {
                booksToUpdateOnTing.put(i, new Book(i, getTingConfig().getArea()));
                System.out.printf("New book on TING: %d\n", i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Book b : getTingConfig().getInstalledBooksOnTing()) {
            booksToUpdateOnTing.put(b.getId(), b);
            System.out.printf("Found book on TING: %d version: %d\n", b.getId(), b.getBookVersion());
        }

        List<TingDownloadJob> jobs = new ArrayList();
        if (getTingConfig().isBackup()) {
            Collection<Book> latestBooksInBackupDir = getTingConfig().getLatestBooksFromBackup();
            for (Book b : latestBooksInBackupDir) {
                System.out.printf("Found book in backup: %d version: %d\n", b.getId(), b.getBookVersion());
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
        InetAddress server = tingDownloader.getServerIp(getTingConfig().getServer());
        //SerialNumber registration, if needed
        if (getTingConfig().isUninitializedSerialVersion()) {
            getTingConfig().setSerial(tingDownloader.addSerialNumber(getTingConfig().getSerial(), getTingConfig().getFw(), getTingConfig().getSw()));
        } else {
            tingDownloader.setSerialNumber(getTingConfig().getSerial());
        }

        tingDownloader.downloadBooks(jobs, getTingConfig().getTingDir());

        try {
            //throw out junk 
            getTingConfig().clearTbdFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkVersions() throws IOException {
        Properties verionCheckProps = tingDownloader.checkCdfsVersion("win", getTingConfig().getFw(), getTingConfig().getSw());
        if (!verionCheckProps.isEmpty()) {
            System.err.printf("New Ting CDFS\n");
        }
        verionCheckProps = tingDownloader.checkFwVersion("win", getTingConfig().getFw(), getTingConfig().getSw());
        if (!verionCheckProps.isEmpty()) {
            System.err.printf("New Ting Firmware\n");
        }
    }

    private void printHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(300);
        formatter.printHelp("ting-dl", opts);
    }

    private Options buildCmdlOptions() {
        Options options = new Options();
        Option opt;
        OptionGroup optg;

        opt = new Option("h", CMDL_OPT_HELP, false, "print this help message");
        options.addOption(opt);

        opt = new Option("e", CMDL_OPT_ENABLE_BACKUP, true, "enable the use of backup dir");
        opt.setArgName(CMDL_OPT_ENABLE_BACKUP);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option("b", CMDL_OPT_BACKUP_DIR, true, "backup dir to use");
        opt.setArgName(CMDL_OPT_BACKUP_DIR);
        opt.setType(String.class);
        options.addOption(opt);

        opt = new Option("t", CMDL_OPT_TING_DIR, true, "ting dir to use");
        opt.setArgName(CMDL_OPT_TING_DIR);
        opt.setType(String.class);
        options.addOption(opt);

        optg = new OptionGroup();

        opt = new Option("ca", CMDL_OPT_CHECK_ALL, false, "check all files (exists and md5) then exit");
        opt.setArgName(CMDL_OPT_CHECK_ALL);
        optg.addOption(opt);

        opt = new Option("ct", CMDL_OPT_CHECK_TING, false, "check ting (exists and md5) then exit");
        opt.setArgName(CMDL_OPT_CHECK_TING);
        optg.addOption(opt);

        opt = new Option("cb", CMDL_OPT_CHECK_BACKUP, false, "check backup (exists and md5) then exit");
        opt.setArgName(CMDL_OPT_CHECK_BACKUP);
        optg.addOption(opt);

        opt = new Option("u", CMDL_OPT_CHECK_VERSIONS, true, "check ting versions and exit");
        opt.setArgName(CMDL_OPT_CHECK_VERSIONS);
        optg.addOption(opt);

        options.addOptionGroup(optg);

        return options;
    }

    private void checkBackupMd5() {
        for (Book b : getTingConfig().getAllBooksInBackup()) {
            b.checkMd5(b.getBackupDir());
        }
    }

    private void checkTingMd5() {
        for (Book b : getTingConfig().getInstalledBooksOnTing()) {
            b.checkMd5(getTingConfig().getTingDir());
        }
    }
}
