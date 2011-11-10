package jstreamserver.ui;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import jstreamserver.utils.GrowingFileInputStream;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey
 * Date: 08.11.11
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
public class MultithreadFileReadWrite {

    final String fileName = System.getProperty("java.io.tmpdir") + "/MultithreadFileReadWriteTest.tmp";
    long maxBytes = 4096 * 10;
    long writerBytesSym = 0;
    long readerBytesSym = 0;

    Boolean stop = false;
    final Object monitor = new Object();

    @Test
    public void testMultithreadReadWrite() throws Exception {
        Runnable fileWriter = new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;

                try {
                    File file = new File(fileName);
                    if (file.exists()) {
                        file.delete();
                        file.createNewFile();
                    }

                    os = new BufferedOutputStream(new FileOutputStream(file), 4096);
                    int i = 0;
                    while (i < maxBytes) {
                        int value = i % 255;
                        os.write(value);
                        writerBytesSym += value;
                        i++;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (os != null) {
                        try {
                            os.flush();
                            os.close();
                        } catch (IOException e) {
                            /*ignore*/
                        }
                    }
                }
            }
        };

        Runnable fileReader = new Runnable() {
            @Override
            public void run() {
                InputStream is = null;

                try {
                    is = new GrowingFileInputStream(new File(fileName));
                    long i = 0;
                    while (i < maxBytes) {
                        readerBytesSym += is.read();
                        i++;
                    }

                    synchronized (monitor) {
                        stop = true;
                        monitor.notify();
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            /*ignore*/
                        }
                    }
                }
            }
        };

        (new Thread(fileReader, "reader")).start();
        (new Thread(fileWriter, "writer")).start();

        synchronized (monitor) {
            while (!stop) {
                monitor.wait();
            }
        }

        System.out.println("Reader sum: " + readerBytesSym);
        assertEquals(writerBytesSym, readerBytesSym);
    }
}
