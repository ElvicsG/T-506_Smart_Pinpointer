package com.kehui.www.testapp.util;

import java.nio.charset.StandardCharsets;
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

    /**
     * 定义 加密算法,可用
     */
    private static final String ALGORITHM = "DESede";

    /**
     * @param keyByte   加密密钥，长度为24字节
     * @param src   被加密的数据缓冲区（源）
     * @return  返回加密结果
     */
    public static String encryptMode(byte[] keyByte, byte[] src) {
        try {
            // 生成密钥
            SecretKey desKey = new SecretKeySpec(keyByte, ALGORITHM);
            // 加密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, desKey);
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
     * @param keyByte   加密密钥，长度为24字节
     * @param src   加密后的数据缓冲区（源）
     * @return  返回解密结果
     */
    public static byte[] decryptMode(byte[] keyByte, String src) {
        try {
            // 生成密钥
            SecretKey desKey = new SecretKeySpec(keyByte, ALGORITHM);
            // 解密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.DECRYPT_MODE, desKey);
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

    /**
     * @param b 字节数组
     * @return  返回十六进制字符串
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String sTmp;
        for (int n = 0; n < b.length; n++) {
            sTmp = (Integer.toHexString(b[n] & 0XFF));
            if (sTmp.length() == 1) {
                hs = hs + "0" + sTmp;
            } else {
                hs = hs + sTmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        return hs.toUpperCase();
    }

    /**
     * @param hexString 十六进制字符串
     * @return  返回字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789ABCDEF";
        for (int i = 0; i < length; i++) {
            // 两个字符对应一个byte
            int pos = i * 2;
            // 注1
            int h = hexDigits.indexOf(hexChars[pos]) << 4;
            // 注2
            int l = hexDigits.indexOf(hexChars[pos + 1]);
            // 非16进制字符
            if (h == -1 || l == -1) {
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }

    /**
     * md5加密产生，产生128位（bit）的mac
     * 将128bit Mac转换成16进制代码
     */
    public static String md5Encode(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes(StandardCharsets.UTF_8));

            String result = "";
            byte[] temp;
            temp = md5.digest(key.getBytes(StandardCharsets.UTF_8));

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
