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
package net.sf.tingdl.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aploese
 */
public class TingConfig {

    /**
     * @return the tingDevice
     */
    public File getTingDevice() {
        return tingDevice;
    }

    /**
     * @return the sw0
     */
    public int getSw0() {
        return sw0;
    }

    /**
     * @param sw0 the sw0 to set
     */
    public void setSw0(int sw0) {
        this.sw0 = sw0;
    }

    /**
     * @return the sw1
     */
    public int getSw1() {
        return sw1;
    }

    /**
     * @param sw1 the sw1 to set
     */
    public void setSw1(int sw1) {
        this.sw1 = sw1;
    }

    /**
     * @return the sw2
     */
    public int getSw2() {
        return sw2;
    }

    /**
     * @param sw2 the sw2 to set
     */
    public void setSw2(int sw2) {
        this.sw2 = sw2;
    }

    /**
     * @return the sw3
     */
    public int getSw3() {
        return sw3;
    }

    /**
     * @param sw3 the sw3 to set
     */
    public void setSw3(int sw3) {
        this.sw3 = sw3;
    }

    public enum PenType {

        STANDARD, // smart
        LIGHT; // classic
    }
    
    private final static DateFormat SETTINGS_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final static Charset SETTINGS_CHARSET = Charset.forName("UTF-16BE");
    private final static TingConfig tingConfig = new TingConfig();
    private final static Logger LOG = LoggerFactory.getLogger(TingConfig.class);
    private final static long EMPTY_SERIAL_VERSION = 0L;
    private File tingBackupDir;
    private File tingDir;
    private File tingDevice;
    private String alternativeServer = "alternative.ting.eu";
    private String area = "en";
    private boolean automaticdownload = true;
    private boolean autooff;
    private int book;
    private Date cdfs = new Date();
    private String codec = "MP3";
    private boolean firsttime = true;
    private Date fscheck = new Date();
    private boolean fullscreen;
    private String fw = "145d";
    private boolean hidemp3panel = true;
    private int index;
    private int language = 49;
    private int maxvolume = 20;
    private String mode = "OID";
    private String mp3;
    private int oufpos;
    private int playlist;
    private int position;
    private boolean registration;
    private long serial = EMPTY_SERIAL_VERSION;
    private String server = "system.ting.eu";
    private boolean showallfiles = true;
    private String startview = "maxi";
    private int statePause;
    private int sw0;
    private int sw1;
    private int sw2;
    private int sw3;
    private String tbd = "verbose";
    private boolean testpen;
    private PenType type = PenType.LIGHT;
    private int volume = 6;
    private int windowheight = 537;
    private int windowposx = 11;
    private int windowposy = 134;
    private int windowwidth = 1151;

    private TingConfig() {
        super();
    }

    public static TingConfig getTingConfig() {
        return tingConfig;
    }

    public boolean findMountedTing() {
//Use this on Win???        File[] roots = File.listRoots();

        try (FileInputStream fis = new FileInputStream("/etc/mtab");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] mountData = line.split(" ");
                if (mountData[1].endsWith("Ting")) {
                    File tingMountDir = new File(mountData[1]);
                    setTingDir(new File(tingMountDir, "$ting"));
                    if (getTingDir().exists() && getTingDir().isDirectory()) {
                        tingDevice = new File(mountData[0]);
                        System.out.printf("$ting found at %s\n", getTingDir());
                        return true;
                    } else {
                        setTingDir(null);
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
        }
        return false;
    }

    public void readSettings() {
        File tingSettings = new File(getTingDir(), "SETTINGS.INI");
        try (InputStream is = new FileInputStream(tingSettings);
                InputStreamReader isr = new InputStreamReader(is, SETTINGS_CHARSET);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] prop = line.split("=");
                if (prop.length == 1) {
                    continue;
                }
                switch (prop[0]) {
                    case "alternative_server":
                        alternativeServer = prop[1];
                        break;
                    case "area":
                        area = prop[1];
                        break;
                    case "automaticdownload":
                        automaticdownload = "yes".equals(prop[1]);
                        break;
                    case "autooff":
                        autooff = "1".equals(prop[1]);
                        break;
                    case "book":
                        book = Integer.valueOf(prop[1]);
                        break;
                    case "cdfs":
                        try {
                            cdfs = SETTINGS_DATE_FORMAT.parse(prop[1]);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "codec":
                        codec = prop[1];
                        break;
                    case "firsttime":
                        firsttime = "yes".equals(prop[1]);
                        break;
                    case "fscheck":
                        try {
                            fscheck = SETTINGS_DATE_FORMAT.parse(prop[1]);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "fullscreen":
                        fullscreen = "yes".equals(prop[1]);
                        break;
                    case "fw":
                        fw = prop[1];
                        break;
                    case "hidemp3panel":
                        hidemp3panel = "yes".equals(prop[1]);
                        break;
                    case "index":
                        index = Integer.parseInt(prop[1]);
                        break;
                    case "language":
                        language = Integer.parseInt(prop[1]);
                        break;
                    case "maxvolume":
                        maxvolume = Integer.parseInt(prop[1]);
                        break;
                    case "mode":
                        mode = prop[1];
                        break;
                    case "mp3":
                        mp3 = "null".equals(prop[1]) ? null : prop[1];
                        break;
                    case "oufpos":
                        oufpos = Integer.parseInt(prop[1]);
                        break;
                    case "playlist":
                        playlist = Integer.parseInt(prop[1]);
                        break;
                    case "position":
                        position = Integer.parseInt(prop[1]);
                        break;
                    case "registration":
                        registration = "yes".equals(prop[1]);
                        break;
                    case "serial":
                        serial = parseSerialNumber(prop[1]);
                        System.out.printf("Found serial number: %d\n", serial);
                    case "server":
                        server = prop[1];
                        break;
                    case "showallfiles":
                        showallfiles = "yes".equals(prop[1]);
                        break;
                    case "startview":
                        startview = prop[1];
                        break;
                    case "state_pause":
                        statePause = Integer.parseInt(prop[1]);
                        break;
                    case "sw":
                        String sw = prop[1];
                        if (sw.equals("0\uffa1")) { // buggy fw on ting smart ...
                            System.err.printf("Found wrong sw: %s \n", sw);
                            sw0 = 0;
                            sw1= 0;
                            sw2 = 1;
                            sw3 = 965;
                        } else {
                            int start = 0;
                            int end = sw.indexOf('.');
                            sw0 = Integer.parseInt(sw.substring(start, end));
                            start = end + 1;
                            end = sw.indexOf('.', start);
                            sw1 = Integer.parseInt(sw.substring(start, end));
                            start = end + 1;
                            end = sw.indexOf('.', start);
                            sw2 = Integer.parseInt(sw.substring(start, end));
                            start = end + 1;
                            sw3 = Integer.parseInt(sw.substring(start));
                        }
                        break;
                    case "tbd":
                        tbd = prop[1];
                        break;
                    case "testpen":
                        testpen = "yes".equals(prop[1]);
                        break;
                    case "type":
                        type = PenType.valueOf(prop[1]);
                        break;
                    case "volume":
                        volume = Integer.parseInt(prop[1]);
                        break;
                    case "windowheight":
                        windowheight = Integer.parseInt(prop[1]);
                        break;
                    case "windowposx":
                        windowposx = Integer.parseInt(prop[1]);
                        break;
                    case "windowposy":
                        windowposy = Integer.parseInt(prop[1]);
                        break;
                    case "windowwidth":
                        windowwidth = Integer.parseInt(prop[1]);
                        break;
                    default:
                        throw new RuntimeException("Unknown prop: " + line);
                }
            }
        } catch (IOException ex) {
        }
    }

    /**
     * @return the tingDir
     */
    public File getTingDir() {
        return tingDir;
    }

    /**
     * @return the alternativeServer
     */
    public String getAlternativeServer() {
        return alternativeServer;
    }

    /**
     * @return the area
     */
    public String getArea() {
        return area;
    }

    /**
     * @return the automaticdownload
     */
    public boolean getAutomaticdownload() {
        return automaticdownload;
    }

    /**
     * @param automaticdownload the automaticdownload to set
     */
    public void setAutomaticdownload(boolean automaticdownload) {
        this.automaticdownload = automaticdownload;
    }

    /**
     * @return the autooff
     */
    public boolean isAutooff() {
        return autooff;
    }

    /**
     * @param autooff the autooff to set
     */
    public void setAutooff(boolean autooff) {
        this.autooff = autooff;
    }

    /**
     * @return the book
     */
    public int getBook() {
        return book;
    }

    /**
     * @param book the book to set
     */
    public void setBook(int book) {
        this.book = book;
    }

    /**
     * @return the cdfs
     */
    public Date getCdfs() {
        return cdfs;
    }

    /**
     * @param cdfs the cdfs to set
     */
    public void setCdfs(Date cdfs) {
        this.cdfs = cdfs;
    }

    /**
     * @return the codec
     */
    public String getCodec() {
        return codec;
    }

    /**
     * @param codec the codec to set
     */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /**
     * @return the firsttime
     */
    public boolean isFirsttime() {
        return firsttime;
    }

    /**
     * @param firsttime the firsttime to set
     */
    public void setFirsttime(boolean firsttime) {
        this.firsttime = firsttime;
    }

    /**
     * @return the fscheck
     */
    public Date getFscheck() {
        return fscheck;
    }

    /**
     * @param fscheck the fscheck to set
     */
    public void setFscheck(Date fscheck) {
        this.fscheck = fscheck;
    }

    /**
     * @return the fullscreen
     */
    public boolean isFullscreen() {
        return fullscreen;
    }

    public boolean isUninitializedSerialVersion() {
        return (EMPTY_SERIAL_VERSION == serial);
    }

    /**
     * @param fullscreen the fullscreen to set
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * @return the fw
     */
    public String getFw() {
        return fw;
    }

    /**
     * @param fw the fw to set
     */
    public void setFw(String fw) {
        this.fw = fw;
    }

    /**
     * @return the hidemp3panel
     */
    public boolean isHidemp3panel() {
        return hidemp3panel;
    }

    /**
     * @param hidemp3panel the hidemp3panel to set
     */
    public void setHidemp3panel(boolean hidemp3panel) {
        this.hidemp3panel = hidemp3panel;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the language
     */
    public int getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(int language) {
        this.language = language;
    }

    /**
     * @return the maxvolume
     */
    public int getMaxvolume() {
        return maxvolume;
    }

    /**
     * @param maxvolume the maxvolume to set
     */
    public void setMaxvolume(int maxvolume) {
        this.maxvolume = maxvolume;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the mp3
     */
    public String getMp3() {
        return mp3;
    }

    /**
     * @param mp3 the mp3 to set
     */
    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    /**
     * @return the oufpos
     */
    public int getOufpos() {
        return oufpos;
    }

    /**
     * @param oufpos the oufpos to set
     */
    public void setOufpos(int oufpos) {
        this.oufpos = oufpos;
    }

    /**
     * @return the playlist
     */
    public int getPlaylist() {
        return playlist;
    }

    /**
     * @param playlist the playlist to set
     */
    public void setPlaylist(int playlist) {
        this.playlist = playlist;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the registration
     */
    public boolean isRegistration() {
        return registration;
    }

    /**
     * @param registration the registration to set
     */
    public void setRegistration(boolean registration) {
        this.registration = registration;
    }

    /**
     * @return the serial
     */
    public long getSerial() {
        return serial;
    }

    /**
     * @param serial the serial to set
     */
    public void setSerial(long serial) {
        this.serial = serial;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the showallfiles
     */
    public boolean isShowallfiles() {
        return showallfiles;
    }

    /**
     * @param showallfiles the showallfiles to set
     */
    public void setShowallfiles(boolean showallfiles) {
        this.showallfiles = showallfiles;
    }

    /**
     * @return the startview
     */
    public String getStartview() {
        return startview;
    }

    /**
     * @param startview the startview to set
     */
    public void setStartview(String startview) {
        this.startview = startview;
    }

    /**
     * @return the statePause
     */
    public int getStatePause() {
        return statePause;
    }

    /**
     * @param statePause the statePause to set
     */
    public void setStatePause(int statePause) {
        this.statePause = statePause;
    }

    /**
     * @return the tbd
     */
    public String getTbd() {
        return tbd;
    }

    /**
     * @param tbd the tbd to set
     */
    public void setTbd(String tbd) {
        this.tbd = tbd;
    }

    /**
     * @return the testpen
     */
    public boolean isTestpen() {
        return testpen;
    }

    /**
     * @param testpen the testpen to set
     */
    public void setTestpen(boolean testpen) {
        this.testpen = testpen;
    }

    /**
     * @return the type
     */
    public PenType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(PenType type) {
        this.type = type;
    }

    /**
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * @return the windowheight
     */
    public int getWindowheight() {
        return windowheight;
    }

    /**
     * @param windowheight the windowheight to set
     */
    public void setWindowheight(int windowheight) {
        this.windowheight = windowheight;
    }

    /**
     * @return the windowposx
     */
    public int getWindowposx() {
        return windowposx;
    }

    /**
     * @param windowposx the windowposx to set
     */
    public void setWindowposx(int windowposx) {
        this.windowposx = windowposx;
    }

    /**
     * @return the windowposy
     */
    public int getWindowposy() {
        return windowposy;
    }

    /**
     * @param windowposy the windowposy to set
     */
    public void setWindowposy(int windowposy) {
        this.windowposy = windowposy;
    }

    /**
     * @return the windowwidth
     */
    public int getWindowwidth() {
        return windowwidth;
    }

    /**
     * @param windowwidth the windowwidth to set
     */
    public void setWindowwidth(int windowwidth) {
        this.windowwidth = windowwidth;
    }

    public Collection<Integer> readTbdFile() throws IOException {
        List<Integer> result;
        try (FileReader fr = new FileReader(new File(getTingDir(), "TBD.TXT"));
                BufferedReader br = new BufferedReader(fr)) {
            result = new ArrayList();
            String line;
            while ((line = br.readLine()) != null) {
                result.add(Integer.parseInt(line));
            }
        }
        return result;
    }

    public void writeTbdFile(Collection<Integer> entries) throws IOException {
        try (FileWriter fw = new FileWriter(new File(getTingDir(), "TBD.TXT"));
                BufferedWriter bw = new BufferedWriter(fw)) {
            boolean firstTime = true;
            for (Integer enty : entries) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    bw.newLine();
                }
                bw.append(enty.toString());
            }
        }
    }

    public Collection<Book> getLatestBooksFromBackup() {
        List<Book> result = new ArrayList();
        Pattern bookIdPattern = Pattern.compile("\\d\\d\\d\\d\\d");
        Pattern bookVersionPattern = Pattern.compile("\\d\\d\\d");

        for (File bookDir : tingBackupDir.listFiles()) {
            if (bookDir.isDirectory() && bookIdPattern.matcher(bookDir.getName()).matches()) {

                Book maxVer = null;
                for (File versionDir : bookDir.listFiles()) {
                    if (versionDir.isDirectory() && bookVersionPattern.matcher(versionDir.getName()).matches()) {
                        if (maxVer == null ? true : maxVer.getBookVersion() < Integer.parseInt(versionDir.getName())) {
                            try {
                                maxVer = new Book(new File(versionDir, String.format("%s_%s.txt", bookDir.getName(), getArea())));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                if (maxVer != null) {
                    result.add(maxVer);
                }
            }
        }
        return result;
    }

    public void clearTbdFile() throws IOException {
        writeTbdFile(new ArrayList<Integer>());
    }

    /**
     * @param tingDir the tingDir to set
     */
    public void setTingDir(File tingDir) {
        this.tingDir = tingDir;
    }

    /**
     * @return the tingBackupDir
     */
    public File getTingBackupDir() {
        return tingBackupDir;
    }

    /**
     * @param tingBackupDir the tingBackupDir to set
     */
    public void setTingBackupDir(File tingBackupDir) {
        this.tingBackupDir = tingBackupDir;
        if (!this.tingBackupDir.exists()) {
            this.tingBackupDir.mkdirs();
        }
    }

    public boolean isBackup() {
        return tingBackupDir != null;
    }

    public Collection<Book> getInstalledBooksOnTing() {
        File[] files = tingDir.listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        final Pattern p = Pattern.compile(String.format("\\d\\d\\d\\d\\d_%s.txt", getTingConfig().getArea()));
        Collection<Book> result = new ArrayList(files.length);
        for (File f : files) {
            if (p.matcher(f.getName()).matches()) {
                try {
                    result.add(new Book(f));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return result;
    }

    public Collection<Book> getAllBooksInBackup() {
        List<Book> result = new ArrayList();
        Pattern bookIdPattern = Pattern.compile("\\d\\d\\d\\d\\d");
        Pattern bookVersionPattern = Pattern.compile("\\d\\d\\d");

        for (File bookDir : tingBackupDir.listFiles()) {
            if (bookDir.isDirectory() && bookIdPattern.matcher(bookDir.getName()).matches()) {

                for (File versionDir : bookDir.listFiles()) {
                    if (versionDir.isDirectory() && bookVersionPattern.matcher(versionDir.getName()).matches()) {
                        try {
                            result.add(new Book(new File(versionDir, String.format("%s_%s.txt", bookDir.getName(), getArea()))));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static long parseSerialNumber(String serial) {
        String[] ser = serial.split(":");
        long result = 0;
        for (int i = 0; i < ser.length; i++) {
            result <<= 8;
            result |= Long.parseLong(ser[i], 16);
        }
        return result;
    }

    public static String formatSerialNumber(long serial) {
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x", (serial >> 40) & 0xFF, (serial >> 32) & 0xFF, (serial >> 24) & 0xFF, (serial >> 16) & 0xFF, (serial >> 8) & 0xFF, serial & 0xFF);
    }

    public void writeSettings() {
        File tingSettings = new File(getTingDir(), "SETTINGS.INI");
        try (OutputStream os = new FileOutputStream(tingSettings);
                OutputStreamWriter osw = new OutputStreamWriter(os, SETTINGS_CHARSET);
                BufferedWriter bw = new BufferedWriter(osw)) {
            String line;
            bw.write("alternative_server=");
            bw.write(alternativeServer);
            bw.write("\n");
            bw.write("area=");
            bw.write(area);
            bw.write("\n");
            bw.write("automaticdownload=");
            bw.write(automaticdownload ? "yes" : "no");
            bw.write("\n");
            bw.write("autooff=");
            bw.write(autooff ? "1" : "0");
            bw.write("\n");
            bw.write("book=");
            bw.write(String.format("%05d", book));
            bw.write("\n");
            bw.write("cdfs=");
            bw.write(SETTINGS_DATE_FORMAT.format(cdfs));
            bw.write("\n");
            bw.write("codec=");
            bw.write(codec);
            bw.write("\n");
            bw.write("firsttime=");
            bw.write(firsttime ? "yes" : "no");
            bw.write("\n");
            bw.write("fscheck=");
            bw.write(SETTINGS_DATE_FORMAT.format(fscheck));
            bw.write("\n");
            bw.write("fullscreen=");
            bw.write(fullscreen ? "yes" : "no");
            bw.write("\n");
            bw.write("fw=");
            bw.write(fw);
            bw.write("\n");
            bw.write("hidemp3panel=");
            bw.write(hidemp3panel ? "yes" : "no");
            bw.write("\n");
            bw.write("index=");
            bw.write(Integer.toString(index));
            bw.write("\n");
            bw.write("language=");
            bw.write(Integer.toString(language));
            bw.write("\n");
            bw.write("maxvolume=");
            bw.write(Integer.toString(maxvolume));
            bw.write("\n");
            bw.write("mode=");
            bw.write(mode);
            bw.write("\n");
            bw.write("mp3=");
            bw.write(mp3 == null ? "null" : "mp3");
            bw.write("\n");
            bw.write("oufpos=");
            bw.write(Integer.toString(oufpos));
            bw.write("\n");
            bw.write("playlist=");
            bw.write(Integer.toString(playlist));
            bw.write("\n");
            bw.write("position=");
            bw.write(Integer.toString(position));
            bw.write("\n");
            bw.write("registration=");
            bw.write(registration ? "yes" : "no");
            bw.write("\n");
            bw.write("serial=");
            bw.write(formatSerialNumber(serial));
            bw.write("\n");
            bw.write("server=");
            bw.write(server);
            bw.write("\n");
            bw.write("showallfiles=");
            bw.write(showallfiles ? "yes" : "no");
            bw.write("\n");
            bw.write("startview=");
            bw.write(startview);
            bw.write("\n");
            bw.write("state_pause=");
            bw.write(Integer.toString(statePause));
            bw.write("\n");
            bw.write("sw=");
            bw.write(Integer.toString(sw0));
            bw.write('.');
            bw.write(Integer.toString(sw1));
            bw.write('.');
            bw.write(Integer.toString(sw2));
            bw.write('.');
            bw.write(Integer.toString(sw3));
            bw.write("\n");
            bw.write("tbd=");
            bw.write(tbd);
            bw.write("\n");
            bw.write("testpen=");
            bw.write(testpen ? "yes" : "no");
            bw.write("\n");
            bw.write("type=");
            bw.write(type.name());
            bw.write("\n");
            bw.write("volume=");
            bw.write(Integer.toString(volume));
            bw.write("\n");
            bw.write("windowheight=");
            bw.write(Integer.toString(windowheight));
            bw.write("\n");
            bw.write("windowposx=");
            bw.write(Integer.toString(windowposx));
            bw.write("\n");
            bw.write("windowposy=");
            bw.write(Integer.toString(windowposy));
            bw.write("\n");
            bw.write("windowwidth=");
            bw.write(Integer.toString(windowwidth));
            bw.write("\n");
        } catch (IOException ex) {
        }
    }
}
