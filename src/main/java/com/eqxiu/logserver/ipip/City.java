package com.eqxiu.logserver.ipip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class City {
    private byte[] data;

    private long indexSize;
    private File ipFile;
    private String ipFilePath;
    private Long lastModifyTime = 0L;
    private ReentrantLock lock = new ReentrantLock();

    public City(String ipFilePath) {
        this.ipFilePath = ipFilePath;
        load();
        watch();
    }

    private void load() {
        System.out.println("ip file is loading...");
        ipFile = new File(ipFilePath);
        lastModifyTime = ipFile.lastModified();
        lock.lock();
        try {
            Path path = Paths.get(this.ipFilePath);
            data = Files.readAllBytes(path);
            indexSize = Util.bytesToLong(data[0], data[1], data[2], data[3]);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            lock.unlock();
        }
        System.out.println("ip file loading done.");
    }

    private void watch() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long time = ipFile.lastModified();
                if (time > lastModifyTime) {
                    lastModifyTime = time;
                    load();
                }
            }
        }, 1000L, 5000L, TimeUnit.MILLISECONDS);
    }


    public String[] find(String ips) throws IPv4FormatException {

        if (!Util.isIPv4Address(ips)) {
            throw new IPv4FormatException();
        }

        lock.lock();

        try {
            long val = Util.ip2long(ips);
            int start = 262148;
            int low = 0;
            int mid = 0;
            int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
            int pos = 0;
            while (low <= high) {
                mid = new Double((low + high) / 2).intValue();
                pos = mid * 9;

                long s = 0;
                if (mid > 0) {
                    int pos1 = (mid - 1) * 9;
                    s = Util.bytesToLong(data[start + pos1], data[start + pos1 + 1], data[start + pos1 + 2], data[start + pos1 + 3]);
                }

                long end = Util.bytesToLong(data[start + pos], data[start + pos + 1], data[start + pos + 2], data[start + pos + 3]);
                if (val > end) {
                    low = mid + 1;
                } else if (val < s) {
                    high = mid - 1;
                } else {

                    byte b = 0;
                    long off = Util.bytesToLong(b, data[start + pos + 6], data[start + pos + 5], data[start + pos + 4]);
                    long len = Util.bytesToLong(b, b, data[start + pos + 7], data[start + pos + 8]);

                    int offset = new Long(off - 262144 + indexSize).intValue();

                    byte[] loc = Arrays.copyOfRange(data, offset, offset + new Long(len).intValue());

                    return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
                }
            }
        } finally {
            lock.unlock();
        }


        return null;
    }


    public static void main(String[] args) throws IPv4FormatException {
        City city = new City("/Users/sun/IdeaProjects/eqxiu-bigdata-log-server/src/main/resources/mydata4vipweek2.datx");
        String[] arr = city.find("211.103.135.120");

        System.out.println(arr[0]);
    }
}