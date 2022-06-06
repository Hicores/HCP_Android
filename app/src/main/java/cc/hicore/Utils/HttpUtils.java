package cc.hicore.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class HttpUtils {
    public static String getContent(String Path) {
        try {
            if (Thread.currentThread().getName().equals("main")) {
                StringBuilder builder = new StringBuilder();
                Thread thread = new Thread(() -> builder.append(getContent(Path)));
                thread.start();
                thread.join();
                return builder.toString();
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(Path).openConnection();
            InputStream ins = connection.getInputStream();
            String Content = new String(DataUtils.readAllBytes(ins), StandardCharsets.UTF_8);
            ins.close();
            return Content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean DownloadToFile(String url, String local) {
        try {
            if (url.startsWith("http://"))url = url.replace("http://","https://");
            if (Thread.currentThread().getName().equals("main")) {
                AtomicBoolean builder = new AtomicBoolean();
                String finalUrl = url;
                Thread thread = new Thread(() ->builder.getAndSet(DownloadToFile(finalUrl, local)));
                thread.start();
                thread.join();
                return builder.get();
            }
            File parent = new File(local).getParentFile();
            if (!parent.exists()) parent.mkdirs();
            FileOutputStream fOut = new FileOutputStream(local);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            InputStream ins = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = ins.read(buffer)) != -1) {
                fOut.write(buffer, 0, read);
            }
            fOut.flush();
            fOut.close();
            ins.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String PostForResult(String URL, String key, byte[] buffer, int size) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestProperty("key", key);
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(buffer, 0, size);
            out.flush();
            out.close();
            InputStream ins = connection.getInputStream();
            byte[] result = DataUtils.readAllBytes(ins);
            ins.close();
            return new String(result);
        } catch (Exception e) {
            return "";
        }
    }
    public static long GetFileLength(String Url) {
        AtomicLong mLong = new AtomicLong();
        Thread mThread = new Thread(()->{
            InputStreamReader isr = null;
            try {
                URL urlObj = new URL(Url);
                URLConnection uc = urlObj.openConnection();

                mLong.set(uc.getContentLengthLong());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != isr) {
                        isr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mLong.get();
    }
}
