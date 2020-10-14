/*
 * CopierFactory.java
 * 
 * Copyright (C) 2010-2011 O. Givi, Toj
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dirsyncpro.tools.copy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import dirsyncpro.DirSyncPro;

public class CopierFactory {
  public interface Copier {
    void copy (FileInputStream is, FileOutputStream os, long fileSize) throws IOException;
  }
  
  public static Copier getCopier() {
      return channelCopier;
  }

  private interface GUIUpdater {
    void updateGUI(long position, double scaledFileSize);
  }
  
  private final static GUIUpdater guiUpdater = new GUIUpdater() {

    @Override
    public void updateGUI (long position, double scaledFileSize) {
    	if (DirSyncPro.isGuiMode()){
    		DirSyncPro.getGui().registerProgressBars(-1, -1, -1, "", false, (int) (position / scaledFileSize), 100, "", false);
    	}
    }
    
  };
  
  private final static GUIUpdater nonGuiUpdater = new GUIUpdater() {

    @Override
    public void updateGUI (long position, double scaledFileSize) {
    }
    
  };

  private static GUIUpdater getGUIUpdater() {
    if (DirSyncPro.isGuiMode()) {
      return guiUpdater;
    }
    return nonGuiUpdater;
  }
  
  private final static Copier channelCopier = new Copier() {
    
    private final static int BUFFER_SIZE = 512 * 1024;
    
    @Override
    public void copy (FileInputStream is, FileOutputStream os, long fileSize) throws IOException {
      FileChannel inChannel = is.getChannel ();
      FileChannel outChannel = os.getChannel ();
      double scaledFileSize = fileSize / 100f;
      GUIUpdater guiUpdater = getGUIUpdater();
      long position = 0;
      long copied;
      while (position < inChannel.size()) {
        copied = inChannel.transferTo(position, BUFFER_SIZE, outChannel);
        position += copied;
        guiUpdater.updateGUI (position, scaledFileSize);
      }
    }
    
  };
  
  private final static Copier bufferedStreamCopier = new Copier() {

    private final static int BUFFER_SIZE = 8 * 1024;

    @Override
    public void copy (FileInputStream is, FileOutputStream os, long fileSize) throws IOException {
      BufferedInputStream bis = null;
      BufferedOutputStream bos = null;
      double scaledFileSize = fileSize / 100f;
      GUIUpdater guiUpdater = getGUIUpdater();
      try {
        bis = new BufferedInputStream(is);
        bos = new BufferedOutputStream(os);
        byte[] buffers = new byte[BUFFER_SIZE]; 
        long position = 0;
        int copied;
        while (-1 != (copied = bis.read(buffers))) {
          bos.write(buffers, 0, copied);
          position += copied;
          guiUpdater.updateGUI (position, scaledFileSize);
        }
      } finally {
        try {bis.close();} catch (Exception e) {} 
        try {bos.close();} catch (Exception e) {} 
      }
    }
    
  };

  private final static Copier plainStreamCopier = new Copier() {

    private final static int BUFFER_SIZE = 8 * 1024;

    @Override
    public void copy (FileInputStream is, FileOutputStream os, long fileSize) throws IOException {
      byte[] buffers = new byte[BUFFER_SIZE]; 
      double scaledFileSize = fileSize / 100f;
      GUIUpdater guiUpdater = getGUIUpdater();
      long position = 0;
      int copied;
      while (-1 != (copied = is.read(buffers))) {
        os.write(buffers, 0, copied);
        position += copied;
        guiUpdater.updateGUI (position, scaledFileSize);
      }
    }
    
  };

  // for tests only
  public static void main (String[] args) throws Exception {
    File srcFile = new File(args[0]);
    long len = srcFile.length ();

    FileInputStream in = null;
    FileOutputStream out = null;
    final int runs = 10; // > 3
    final int channel = 0;
    final int buffered = 1;
    final int variants = 3;
    final int plain = 2;
    long[] results = new long[runs * variants];
    long t;
    // run tests; create results
    for (int i = 0; i < runs; i++) {
      try {
        in = new FileInputStream(srcFile);
        out = new FileOutputStream(args[0] + ".out");
        t = System.currentTimeMillis ();
        CopierFactory.channelCopier.copy (in, out, len);
        results[variants * i + channel] = System.currentTimeMillis () - t;
      } finally {
        try {in.close();} catch (Exception e) {} 
        try {out.close();} catch (Exception e) {} 
      }

      try {
        in = new FileInputStream(srcFile);
        out = new FileOutputStream(args[0] + ".out");
        t = System.currentTimeMillis ();
        CopierFactory.bufferedStreamCopier.copy (in, out, len);
        results[variants * i + buffered] = System.currentTimeMillis () - t;
      } finally {
        try {in.close();} catch (Exception e) {} 
        try {out.close();} catch (Exception e) {} 
      }

      try {
        in = new FileInputStream(srcFile);
        out = new FileOutputStream(args[0] + ".out");
        t = System.currentTimeMillis ();
        CopierFactory.plainStreamCopier.copy (in, out, len);
        results[variants * i + plain] = System.currentTimeMillis () - t;
      } finally {
        try {in.close();} catch (Exception e) {} 
        try {out.close();} catch (Exception e) {} 
      }
    }

    // remove slowest and fastest; ignore the first one 
    int[] minMaxIndexes = new int[variants * 2]; // min/max -> 2
    minMaxIndexes[2 * channel] = variants * 1 + channel;
    minMaxIndexes[2 * channel+1] = variants * 1 + channel;
    minMaxIndexes[2 * buffered] = variants * 1 + buffered;
    minMaxIndexes[2 * buffered+1] = variants * 1 + buffered;
    minMaxIndexes[2 * plain] = variants * 1 + plain;
    minMaxIndexes[2 * plain+1] = variants * 1 + plain;
    for (int i = 1; i < runs; i++) {
      minMaxIndexes[2 * channel] = results[variants * i + channel] < results[minMaxIndexes[2 * channel]] ? variants * i + channel : minMaxIndexes[2 * channel];
      minMaxIndexes[2 * channel+1] = results[variants * i + channel] > results[minMaxIndexes[2 * channel + 1]] ? variants * i + channel : minMaxIndexes[2 * channel + 1];
      minMaxIndexes[2 * buffered] = results[variants * i + buffered] < results[minMaxIndexes[2 * buffered]] ? variants * i + buffered : minMaxIndexes[2 * buffered];
      minMaxIndexes[2 * buffered+1] = results[variants * i + buffered] > results[minMaxIndexes[2 * buffered + 1]] ? variants * i + buffered : minMaxIndexes[2 * buffered + 1];
      minMaxIndexes[2 * plain] = results[variants * i + plain] < results[minMaxIndexes[2 * plain]] ? variants * i + plain : minMaxIndexes[2 * plain];
      minMaxIndexes[2 * plain+1] = results[variants * i + plain] > results[minMaxIndexes[2 * plain + 1]] ? variants * i + plain : minMaxIndexes[2 * plain + 1];
    }
    results[minMaxIndexes[2 * channel]] = 
      results[minMaxIndexes[2 * channel + 1]] =
        results[minMaxIndexes[2 * buffered]] = 
          results[minMaxIndexes[2 * buffered + 1]] = 
            results[minMaxIndexes[2 * plain]] = 
              results[minMaxIndexes[2 * plain + 1]] = 0;

    double[] avr = new double[variants];
    for (int i = 1; i < runs; i++) {
      avr[channel] += results[variants * i + channel];
      avr[buffered] += results[variants * i + buffered];
      avr[plain] += results[variants * i + plain];
    }
    for (int i = 0; i < variants; i++) {
      avr[i] = avr[i] / (runs - 3); // remove slowest and fastest; ignore the first one -> -3
    }
    System.out.println(MessageFormat.format ("average runtime channelCopier {0,number,0.00} ms"
      , new Object[] {avr[channel]}));
    System.out.println(MessageFormat.format ("average runtime bufferedStreamCopier {0,number,0.00} ms"
      , new Object[] {avr[buffered]}));
    System.out.println(MessageFormat.format ("average runtime plainStreamCopier {0,number,0.00} ms"
      , new Object[] {avr[plain]}));
  }
}

