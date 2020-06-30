package com.wxipad.wechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.Metadata;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class WechatTool {

    //缓冲字节数组大小
    private static final int BUFF_SIZE = 4096;

    //Gson序列化实例
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * 将队形转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String gsonString(Object obj) {
        String str = null;
        if (gson != null && obj != null) {
            str = gson.toJson(obj);
        }
        return str;
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param str JSON字符串
     * @param cls 对象类
     * @param <T> 对象类参数
     * @return 对象
     */
    public static <T> T gsonObj(String str, Class<T> cls) {
        T obj = null;
        if (gson != null && str != null) {
            obj = gson.fromJson(str, cls);
        }
        return obj;
    }

    /**
     * 将JSON字符串转换为对象列表
     *
     * @param str JSON字符串
     * @param cls 对象类
     * @param <T> 对象类参数
     * @return 对象列表
     */
    public static <T> List<T> gsonList(String str, Class<T[]> cls) {
        List<T> list = null;
        if (gson != null && str != null) {
            T[] arr = gson.fromJson(str, cls);
            list = Arrays.asList(arr);
        }
        return list;
    }

    /**
     * 构建GRPC请求附加数据
     *
     * @param appId  请求APPID
     * @param appKey 请求APPKEY
     * @return 附加数据对象
     */
    public static Metadata getMetaData(String appId, String appKey) {
        Metadata data = new Metadata();
        data.put(Metadata.Key.of("appid", Metadata.ASCII_STRING_MARSHALLER), appId);
        data.put(Metadata.Key.of("appkey", Metadata.ASCII_STRING_MARSHALLER), appKey);
        return data;
    }

    public static Metadata getMetaData(String appId, String appKey, String cmd) {
        Metadata data = getMetaData(appId, appKey);
        data.put(Metadata.Key.of("cmd", Metadata.ASCII_STRING_MARSHALLER), cmd);
        return data;
    }

    public static Metadata getMetaData(Metadata data, String cmd) {
        data.put(Metadata.Key.of("cmd", Metadata.ASCII_STRING_MARSHALLER), cmd);
        return data;
    }

    /**
     * 将空字符串转换为null
     *
     * @param str 字符串
     * @return 去除两边空格的字符串，空字符串则返回null
     */
    public static String emptyToNull(String str) {
        if (str != null && !str.trim().isEmpty()) {
            return str.trim();
        }
        return null;
    }

    /**
     * 将字符串转换为整数
     *
     * @param str 字符串
     * @param def 默认值
     * @return 整数值
     */
    public static int parseInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 将字符串转换为整数
     *
     * @param str 字符串
     * @param def 默认值
     * @return 整数值
     */
    public static long parseLong(String str, long def) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 判断指定文件是否存在
     *
     * @param path 文件路径
     * @return 文件是否存在
     */
    public static boolean existFile(String path) {
        return new File(path).exists();
    }

    /**
     * 删除指定文件
     *
     * @param path 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String path) {
        return new File(path).delete();
    }

    /**
     * 写文件
     *
     * @param path 文件路径
     * @param data 数据字节
     * @return 是否写成功
     */
    public static boolean writeFile(String path, byte[] data) {
        try {
            Files.write(Paths.get(path), data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 写文件
     *
     * @param path    文件路径
     * @param content 文件内容
     * @param charset 文件编码
     * @return 是否写成功
     */
    public static boolean writeFile(String path, String content, Charset charset) {
        return writeFile(path, content.getBytes(charset));
    }

    /**
     * 读文件
     *
     * @param path 文件路径
     * @return 数据字节，读取失败返回null
     */
    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 读文件
     *
     * @param path    文件路径
     * @param charset 文件编码
     * @return 文件内容，读取失败返回null
     */
    public static String readFile(String path, Charset charset) {
        byte[] data = readFile(path);
        return data != null ? new String(data, charset) : null;
    }

    /**
     * 从输入流读取字节数组
     *
     * @param in 输入流
     * @return 字节数组
     */
    public static byte[] readBytes(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            try {
                int read;
                byte[] buff = new byte[1024];
                while ((read = in.read(buff)) != -1) {
                    out.write(buff, 0, read);
                }
                return out.toByteArray();
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 生成随机长度的字节数组
     *
     * @param length 字节长度
     * @return 随机字节数组
     */
    public static byte[] randomBytes(int length) {
        byte[] data = new byte[Math.max(length, 0)];
        new Random().nextBytes(data);
        return data;
    }

    /**
     * 连接多个字节数组
     *
     * @param datas 多个字节数组
     * @return 连接后的结果字节数组
     */
    public static byte[] joinBytes(byte[]... datas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            try {
                for (byte[] data : datas) {
                    out.write(data);
                }
                return out.toByteArray();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 从指定字节数组中截取部分字节数组
     *
     * @param data   字节数组
     * @param offset 截取偏移
     * @param length 截取长度
     * @return 截取的部分字节数组
     */
    public static byte[] cutBytes(byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        } else if (offset + length > data.length) {
            return null;
        } else {
            byte[] cutBytes = new byte[length];
            System.arraycopy(data, offset, cutBytes, 0, length);
            return cutBytes;
        }
    }

    /**
     * 将十六进制字符串转为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : hex.toCharArray()) {
            if (ch >= 'A' && ch <= 'F') {
                sb.append((char) (ch + 0x20));
            } else if (ch >= 'a' && ch <= 'f') {
                sb.append(ch);
            } else if (ch >= '0' && ch <= '9') {
                sb.append(ch);
            }
        }
        String src = sb.toString();
        byte[] bytes = new byte[(int) (src.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(src.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /**
     * 将字节数组转为十六进制字符串
     *
     * @param data 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        if (data != null) {
            for (byte val : data) {
                int num = val < 0 ? val + 256 : val;
                String hex = Integer.toHexString(num);
                if (hex.length() < 2) {
                    sb.append("0");
                }
                sb.append(hex);
            }
        }
        return sb.toString();
    }

    /**
     * 计算指定字符串的MD5值
     *
     * @param strs 字符串
     * @return MD5值
     */
    public static byte[] md5(String... strs) {
        byte[][] datas = new byte[strs.length][];
        for (int i = 0; i < strs.length; i++) {
            datas[i] = strs[i].getBytes(WechatConst.CHARSET);
        }
        return md5(datas);
    }

    /**
     * 计算指定数据字节的MD5值
     *
     * @param datas 数据字节
     * @return MD5值
     */
    public static byte[] md5(byte[]... datas) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (byte[] data : datas) {
                digest.update(data);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 生成随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 从字节数组中取大端4字节整数
     *
     * @param data   字节数组
     * @param offset 整数数据偏移
     * @return 取得的整数
     */
    public static int bytesToInt(byte[] data, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (data[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 将字节数组转换为整数数组
     *
     * @param data 字节数组
     * @return 整数数组
     */
    public static int[] bytesToInts(byte[] data) {
        int[] res = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = data[i] & 0xff;
        }
        return res;
    }

    /**
     * 从字节流去读指定长度的数据
     *
     * @param in     字节流
     * @param length 需要的数据长度
     * @return 读取的字节数据
     * @throws IOException
     */
    public static byte[] needBytes(InputStream in, int length) throws IOException {
        if (length <= 0) {
            return new byte[0];
        } else {
            byte[] buff = new byte[length];
            int read;
            for (int offset = 0; offset < length; offset += read) {
                read = in.read(buff, offset, length - offset);
                if (read <= 0) {
                    return null;
                }
            }
            return buff;
        }
    }

    /**
     * 打开图片
     *
     * @param path 图片路径
     * @return 图片数据缓冲
     */
    public static BufferedImage openImage(String path) {
        return openImage(new File(path));
    }

    /**
     * 打开图片
     *
     * @param file 图片文件
     * @return 图片数据缓冲
     */
    public static BufferedImage openImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 获取XML的文档
     *
     * @param xml XML文本
     * @return XML文档
     */
    public static Document getDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(xml);
            try {
                InputSource source = new InputSource(reader);
                Document doc = builder.parse(source);
                return doc;
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 使用AES-ECB算法进行加密
     *
     * @param key  密钥
     * @param data 需要加密的数据
     * @return 加密后的数据
     */
    public static byte[] aesEncrypt(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 使用AES-ECB算法进行解密
     *
     * @param key  密钥
     * @param data 需要解密的数据
     * @return 解密后的数据
     */
    public static byte[] aesDecrypt(byte[] key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * Zlib压缩字节数组
     *
     * @param data 待压缩数据
     * @return 压缩后的数据
     */
    public static byte[] compress(byte[] data) {
        byte[] output = null;
        Deflater compresser = new Deflater();
        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[BUFF_SIZE];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                WechatDebug.echo(e);
            }
        }
        compresser.end();
        return output;
    }

    /**
     * Zlib解压缩字节数组
     *
     * @param data 待压缩的数据
     * @return 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = null;
        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[BUFF_SIZE];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            WechatDebug.echo(e);
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                WechatDebug.echo(e);
            }
        }
        decompresser.end();
        return output;
    }

    /**
     * 从标准输出打印指定消息字符串
     *
     * @param msgs 消息字符串
     */
    public static void echo(String... msgs) {
        for (String msg : msgs) {
            System.out.println(msg);
        }
    }
    /**
     * 当前线程等待
     *
     * @param ms 等待的毫秒数
     */
    public static void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
