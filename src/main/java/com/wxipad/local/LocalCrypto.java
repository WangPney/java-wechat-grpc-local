package com.wxipad.local;

import com.wxipad.wechat.WechatDebug;
import com.wxipad.wechat.tools.crypto.Digest;
import com.wxipad.wechat.tools.tool.ToolBytes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class LocalCrypto {

    public static final String ALGORITHM_EC = "EC";
    public static final String ALGORITHM_ECDH = "ECDH";
    public static final String ALGORITHM_HMACSHA256 = "HmacSHA256";
    public static final String CURVE_SECP224R1 = "secp224r1";
    public static final String CURVE_PRIME256V1 = "prime256v1";
    private static final Provider PROVIDER_BC = new BouncyCastleProvider();
    private static final byte[] HEAD_SECP224R1_X509 = ToolBytes.hex2Bytes("30,4E,30,10,06,07,2A,86,48,CE,3D,02,01,06,05,2B,81,04,00,21,03,3A,00");
    private static final byte[] HEAD_PRIME256V1_X509 = ToolBytes.hex2Bytes("30,59,30,13,06,07,2A,86,48,CE,3D,02,01,06,08,2A,86,48,CE,3D,03,01,07,03,42,00");

    /**
     * 生成EC密钥对
     * @return 密钥对
     */
    public static KeyPair genKeyPair(String curve) {
        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance(ALGORITHM_EC, PROVIDER_BC);
            kpgen.initialize(new ECGenParameterSpec(curve), new SecureRandom());
            return kpgen.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            WechatDebug.echo(e);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 生成握手共享密钥
     * @param pubKey    他人的公钥
     * @param priKey    自己的私钥
     * @param algorithm 最终密钥的散列算法
     * @return 握手成功后的共享密钥
     */
    public static byte[] getShareKey(PublicKey pubKey, PrivateKey priKey, Digest.ALGORITHM algorithm) {
        try {
            KeyAgreement ka = KeyAgreement.getInstance(ALGORITHM_ECDH, PROVIDER_BC);
            ka.init(priKey);
            ka.doPhase(pubKey, true);
            byte[] keyAgreement = ka.generateSecret();
            Digest digest = new Digest(algorithm);
            return digest.update(keyAgreement).digest();
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 从字节数组转换为EC公钥
     * @param keyBytes 字节数组
     * @return EC公钥
     */
    public static PublicKey getPublicKey(byte[] keyBytes) {
        try {
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM_EC, PROVIDER_BC);
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyBytes);
            return kf.generatePublic(pkSpec);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeySpecException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 获取EC曲线公钥头部
     * @param curve EC曲线
     * @return 公钥头部字节数组
     */
    public static byte[] getECPubHead(String curve) {
        if (CURVE_SECP224R1.equals(curve)) {
            return HEAD_SECP224R1_X509;
        } else if (CURVE_PRIME256V1.equals(curve)) {
            return HEAD_PRIME256V1_X509;
        }
        return null;
    }

    /**
     * 获取EC公钥数据
     * @param key   EC公钥
     * @param curve EC曲线
     * @return EC公钥数据
     */
    public static byte[] parseECPubKey(PublicKey key, String curve) {
        byte[] head = getECPubHead(curve);
        if (head != null) {
            byte[] x509Data = key.getEncoded();
            return ToolBytes.subBytes(x509Data, head.length, x509Data.length);
        }
        return null;
    }

    /**
     * 从EC公钥数据还原EC公钥
     * @param data  EC公钥数据
     * @param curve EC曲线
     * @return EC公钥
     */
    public static PublicKey solveECPubKey(byte[] data, String curve) {
        byte[] head = getECPubHead(curve);
        if (head != null) {
            byte[] full = new byte[head.length + data.length];
            System.arraycopy(head, 0, full, 0, head.length);
            System.arraycopy(data, 0, full, head.length, data.length);
            return getPublicKey(full);
        }
        return null;
    }

    /**
     * 使用AES-GCM算法进行加密
     * @param key   密钥
     * @param nonce 向量
     * @param aad   关联数据
     * @param data  需要加密的数据
     * @return 加密后的数据
     */
    public static byte[] aesGcmEncryptData(byte[] key, byte[] nonce, byte[] aad, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", PROVIDER_BC);
            if (nonce != null) {
                GCMParameterSpec parameterSpec = new GCMParameterSpec(128, nonce);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), parameterSpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            }
            if (aad != null) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (InvalidAlgorithmParameterException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 使用AES-GCM算法进行解密
     * @param key   密钥
     * @param nonce 向量
     * @param aad   关联数据
     * @param data  需要解密的数据
     * @return 解密后的数据
     */
    public static byte[] aesGcmDecryptData(byte[] key, byte[] nonce, byte[] aad, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", PROVIDER_BC);
            if (nonce != null) {
                GCMParameterSpec parameterSpec = new GCMParameterSpec(128, nonce);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), parameterSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            }
            if (aad != null) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (InvalidAlgorithmParameterException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 使用AES-CBC算法进行加密
     * @param key  密钥
     * @param iv   向量
     * @param data 需要加密的数据
     * @return 加密后的数据
     */
    public static byte[] aesCbcEncryptData(byte[] key, byte[] iv, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", PROVIDER_BC);
            if (iv != null) {
                IvParameterSpec parameterSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), parameterSpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (InvalidAlgorithmParameterException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 使用AES-CBC算法进行解密
     * @param key  密钥
     * @param iv   向量
     * @param data 需要解密的数据
     * @return 解密后的数据
     */
    public static byte[] aesCbcDecryptData(byte[] key, byte[] iv, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", PROVIDER_BC);
            if (iv != null) {
                IvParameterSpec parameterSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), parameterSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (NoSuchPaddingException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        } catch (InvalidAlgorithmParameterException e) {
            WechatDebug.echo(e);
        } catch (BadPaddingException e) {
            WechatDebug.echo(e);
        } catch (IllegalBlockSizeException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

    /**
     * 计算信息数据的SHA256摘要
     * @param infos 信息数据
     * @return SHA256摘要结果
     */
    public static byte[] genSha256(ArrayList<byte[]> infos) {
        Digest digest = new Digest(Digest.ALGORITHM.SHA256);
        for (byte[] info : infos) {
            digest.update(info);
        }
        return digest.digest();
    }

    /**
     * 计算信息数据的MD5摘要
     * @param infos 信息数据
     * @return MD5摘要结果
     */
    public static byte[] genMd5(byte[]... infos) {
        Digest digest = new Digest(Digest.ALGORITHM.MD5);
        for (int i = 0; i < infos.length; i++) {
            digest.update(infos[i]);
        }
        return digest.digest();
    }

    /**
     * 获取HKDF密钥扩展
     * @param key  密钥
     * @param info 信息
     * @param len  长度
     * @return 返回密钥扩展字节
     */
    public static byte[] genHkdf(byte[] key, byte[] info, int len) {
        if (key == null || key.length <= 0 || len <= 0) {
            return null;
        } else if (info == null) {
            info = new byte[0];
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMACSHA256);
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM_HMACSHA256);
            mac.init(secretKey);
            byte[] block = new byte[0];
            int iterations = (int) Math.ceil(((double) len) / ((double) mac.getMacLength()));
            if (iterations > 255) {
                return null;//长度超限
            }
            ToolBytes.BytesWriter writer = new ToolBytes.BytesWriter();
            int remain = len, size;
            for (int i = 0; i < iterations; i++) {
                mac.update(block);
                mac.update(info);
                mac.update((byte) (i + 1));
                block = mac.doFinal();
                size = Math.min(remain, block.length);
                writer.write(block, 0, size);
                remain -= size;
            }
            return writer.finish();
        } catch (NoSuchAlgorithmException e) {
            WechatDebug.echo(e);
        } catch (InvalidKeyException e) {
            WechatDebug.echo(e);
        }
        return null;
    }

}
