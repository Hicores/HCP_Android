package cc.hicore.MiraiHCP.PluginManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cc.hicore.Utils.DataUtils;

public class PluginDecoder {
    public static void unpackToDic(String hcpPath,String destDir) throws Exception{
        ZipInputStream HCPRead = new ZipInputStream(new HCPPackReader(new FileInputStream(hcpPath)));
        ZipEntry entry;
        while ((entry = HCPRead.getNextEntry()) != null){
            File destFile = new File(destDir,entry.getName());
            FileOutputStream out = new FileOutputStream(destFile);
            DataUtils.copyIns(HCPRead,out);
            out.close();
        }
        HCPRead.close();
    }
    private static class HCPPackReader extends InputStream {
        private InputStream in;
        public HCPPackReader(InputStream ins){
            in = ins;
        }
        @Override
        public int read() throws IOException {
            int result = in.read();
            if (result != -1){;
                return result ^ 88;
            }
            return result;
        }
        @Override
        public void close() throws IOException {
            in.close();
        }
        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }
        @Override
        public int available() throws IOException {
            return in.available();
        }
        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
        }
        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }
        @Override
        public boolean markSupported() {
            return in.markSupported();
        }
    }
}
