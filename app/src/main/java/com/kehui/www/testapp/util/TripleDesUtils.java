package com.kehui.www.testapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * 3DES加密解密工具类
 * @author dl
 * @date 2017/12/18
 */
public class TripleDesUtils {

    // 定义 加密算法,可用
    private static final String ALGORITHM = "DESede";

    // DES,DESede,Blowfish

    // keybyte为加密密钥，长度为24字节
    // src为被加密的数据缓冲区（源）
    public static String encryptMode(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
            // 加密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            // 开始加密运算
            byte[] encryptedByteArray = c1.doFinal(src);
            // 加密运算之后 将byte[]转化为base64的String
            BASE64Encoder enc = new BASE64Encoder();
            return enc.encode(encryptedByteArray);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param keybyte 为加密密钥，长度为24字节
     * @param src     为加密后的缓冲区
     * @return
     */
    public static byte[] decryptMode(byte[] keybyte, String src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
            // 解密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            // 解密运算之前
            BASE64Decoder dec = new BASE64Decoder();
            byte[] encryptedByteArray = dec.decodeBuffer(src);
            // 解密运算 将base64的String转化为byte[]
            return c1.doFinal(encryptedByteArray);

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    // 转换成十六进制字符串
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        String s = hs.toUpperCase();

        return s;
    }

    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }

        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789ABCDEF";
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 两个字符对应一个byte
            int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
            int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
            if (h == -1 || l == -1) { // 非16进制字符
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }

    /**
     * md5加密产生，产生128位（bit）的mac
     * 将128bit Mac转换成16进制代码
     *
     * @param strSrc
     * @param key
     * @return
     */
    public static String MD5Encode(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF8"));

            String result = "";
            byte[] temp;
            temp = md5.digest(key.getBytes("UTF8"));

            System.out.println("temp--------->temp:" + temp.length);
            for (int i = 0; i < temp.length; i++) {
                result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
            }


            System.out.println("temp--------->temp:" + result);
            return result;

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
