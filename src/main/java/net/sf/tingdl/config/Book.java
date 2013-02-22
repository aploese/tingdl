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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author aploese
 */
public class Book {

    public Book(File bookTxtFile) throws IOException {
        this(Integer.parseInt(bookTxtFile.getName().substring(0, 5)), bookTxtFile.getName().substring(6, 8));
        try (FileReader fr = new FileReader(bookTxtFile)) {
            fillData(fr);
        }
    }

    public Book(String filename) throws IOException {
        this(new File(filename));
    }

    public Book(int id, String lang, InputStream is) throws IOException {
        this(id, lang);
        try (InputStreamReader isr = new InputStreamReader(is)) {
            fillData(isr);
        }
    }

    private void fillData(Reader r) throws IOException {
        try (BufferedReader br = new BufferedReader(r)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] pair = line.split(": ");
                switch (pair[0]) {
                    case "Name":
                        name = pair.length == 1 ? "" : pair[1];
                        break;
                    case "Publisher":
                        publisher = pair.length == 1 ? "" : pair[1];
                        break;
                    case "Author":
                        author = pair.length == 1 ? "" : pair[1];
                        break;
                    case "Book Version":
                        bookVersion = pair.length == 1 ? 0 : Integer.parseInt(pair[1]);
                        break;
                    case "URL":
                        uRL = pair.length == 1 ? "" : pair[1];
                        break;
                    case "ThumbMD5":
                        thumbMD5 = pair.length == 1 ? "" : pair[1];
                        break;
                    case "FileMD5":
                        fileMD5 = pair.length == 1 ? "" : pair[1];
                        break;
                    case "Book Area Code":
                        bookAreaCode = pair.length == 1 ? "" : pair[1];
                        break;
                    default:
                        throw new RuntimeException("Unknown key: " + line);
                }
            }
        }
    }
    final private int id;
    final private String lang;
    private String name;
    private String publisher;
    private String author;
    private int bookVersion;
    private String uRL;
    private String thumbMD5;
    private String fileMD5;
    private String bookAreaCode;

    public Book(int id, String lang) {
        this.id = id;
        this.lang = lang;
    }

    public String getThumName() {
        return String.format("%05d_%s.png", id, lang);
    }
    
    public File getThumbFile(File parent) {
        return new File(parent, getThumName());
    }
    
    public String getArchiveName() {
        return String.format("%05d_%s.ouf", id, lang);
    }
    
    public File getArchiveFile(File parent) {
        return new File(parent, getArchiveName());
    }

    public String getDescriptionName() {
        return String.format("%05d_%s.txt", id, lang);
    }
    
    public File getDescriptionFile(File parent) {
        return new File(parent, getDescriptionName());
    }

    public void writeToFile(File parent) throws IOException {
        try (FileWriter fw = new FileWriter(getDescriptionFile(parent)); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("Name: ");
            bw.write(name);
            bw.newLine();
            bw.write("Publisher: ");
            bw.write(publisher);
            bw.newLine();
            bw.write("Author: ");
            bw.write(author);
            bw.newLine();
            bw.write("Book Version: ");
            bw.write(Integer.toString(bookVersion));
            bw.newLine();
            bw.write("URL: ");
            bw.write(uRL);
            bw.newLine();
            bw.write("ThumbMD5: ");
            bw.write(thumbMD5);
            bw.newLine();
            bw.write("FileMD5: ");
            bw.write(fileMD5);
            bw.newLine();
            bw.write("Book Area Code: ");
            bw.write(bookAreaCode);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name).append("\n");
        sb.append("Publisher: ").append(publisher).append("\n");
        sb.append("Author: ").append(author).append("\n");
        sb.append("Book Version: ").append(bookVersion).append("\n");
        sb.append("URL: ").append(uRL).append("\n");
        sb.append("ThumbMD5: ").append(thumbMD5).append("\n");
        sb.append("FileMD5: ").append(fileMD5).append("\n");
        sb.append("Book Area Code: ").append(bookAreaCode);
        return sb.toString();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the bookVersion
     */
    public int getBookVersion() {
        return bookVersion;
    }

    /**
     * @param bookVersion the bookVersion to set
     */
    public void setBookVersion(int bookVersion) {
        this.bookVersion = bookVersion;
    }

    /**
     * @return the uRL
     */
    public String getuRL() {
        return uRL;
    }

    /**
     * @param uRL the uRL to set
     */
    public void setuRL(String uRL) {
        this.uRL = uRL;
    }

    /**
     * @return the thumbMD5
     */
    public String getThumbMD5() {
        return thumbMD5;
    }

    /**
     * @param thumbMD5 the thumbMD5 to set
     */
    public void setThumbMD5(String thumbMD5) {
        this.thumbMD5 = thumbMD5;
    }

    /**
     * @return the fileMD5
     */
    public String getFileMD5() {
        return fileMD5;
    }

    /**
     * @param fileMD5 the fileMD5 to set
     */
    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    /**
     * @return the bookAreaCode
     */
    public String getBookAreaCode() {
        return bookAreaCode;
    }

    /**
     * @param bookAreaCode the bookAreaCode to set
     */
    public void setBookAreaCode(String bookAreaCode) {
        this.bookAreaCode = bookAreaCode;
    }

    public boolean descriptionFileExists(File path) {
        return getDescriptionFile(path).exists();
    }
    
    public File getBackupDir() {
        File f = new File(TingConfig.TING_USER_DIR, String.format("%05d", getId()));
        f.mkdir();
        f = new File(f, String.format("%d", getBookVersion()));
        f.mkdir();
        return f;
    }
    
    /**
     * Check if the given book is more recent ant this book must be updated.
     * 
     * @param b the book to compare
     * @return wheter the book b has a higher version
     */
    public boolean updateNeeded(Book b) {
        return getId() != b.getId() ? false : getBookVersion() < b.getBookVersion();
    } 

}
