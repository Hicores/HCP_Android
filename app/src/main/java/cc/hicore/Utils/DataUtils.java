package cc.hicore.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class DataUtils {
    public static byte[] HexToByteArray(String hex){
        if(hex.length()%2!=0){
            hex="0"+hex;
        }
        byte[] result=new byte[hex.length()/2];
        for(int i=0;i<hex.length();i+=2){
            result[i/2]=(byte)Integer.parseInt(hex.substring(i,i+2),16);
        }
        return result;
    }
    public static String ByteArrayToHex(byte[] bytes){
        String result="";
        for(byte b:bytes){
            result=result+Integer.toHexString(b&0xff);
        }
        return result;
    }
    public static long getCRC32(byte[] data){
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
    public static byte[] readAllBytes(InputStream inp) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inp.read(buffer)) != -1) out.write(buffer, 0, read);
        return out.toByteArray();
    }
    public static String getStrMD5(String data){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
        digest.update(data.getBytes(StandardCharsets.UTF_8));
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16).toUpperCase();
    }
}
