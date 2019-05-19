package com.tree.shu.elf.tools.image;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;

import static android.content.ContentValues.TAG;

public class ImageCache {

    LruCache<String, Bitmap> lruCache;
    private static ImageCache imageCache;
    private MD5 md5;
    private StringBuilder path = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());


    //默认使用APP名称成为本地缓存的目录
    private ImageCache(Context context) throws PackageManager.NameNotFoundException {
        md5 = new MD5();
        int maxSize = (int) Runtime.getRuntime().maxMemory() / 8;
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        String localDirectory;
        {//获取当前的应用名称
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            localDirectory = context.getResources().getString(labelRes);
        }
        path.append("/").append(localDirectory);
    }

    /**
     * 获取单例对象
     * @param context
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    public static ImageCache getImageCache(Context context) throws PackageManager.NameNotFoundException {
        if (imageCache == null) {
            synchronized (ImageCache.class) {
                if (imageCache == null) {
                    imageCache = new ImageCache(context);
                }
            }

        }
        return imageCache;
    }

    /**
     * 设置文件路径
     * @param name
     */
    public void setPath(String name) {
        StringBuilder stringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append("/").append(name);
        this.path = stringBuilder;
    }

    /**
     * 依次向缓存里放东西
     * @param uri
     * @param bitmap
     * @throws Exception
     */
    public void putBitmap(String uri, Bitmap bitmap) throws Exception {
        lruCache.put(uri, bitmap);
    }

    /**
     * 检查本地和内存当中是否有所需图片,并更新状态消息对象的属性值
     * @param uri
     * @param statusMessage 用来更新这个状态消息
     * @throws Exception
     */
    public void checkBitmap(String uri,StatusMessage statusMessage) throws Exception {
        if (lruCache.get(uri) == null) {
            checkLocal(uri,statusMessage);
        }else {
            statusMessage.isMemoryExists = true;
        }
    }

    /**
     * 从本地获取图片
     * @param uri 图片的网络链接
     * @return Bitmap
     * @throws Exception
     */
    public Bitmap getFromLocal(String uri,float ViewHeight) throws Exception {
        Bitmap bitmap = null;
        String name = md5.encode(uri);
        File file = new File(path.toString(),name);
        if (file.exists()) {
            int bitmapHeight = getBitmapHeight(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = countSampleSize(ViewHeight, bitmapHeight);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file),null,options);
        }
        return bitmap;
    }

    private int getBitmapHeight(File file) throws FileNotFoundException {
        int bitmapHeight;//预先获取图片信息
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(file),null,options);
        bitmapHeight = options.outHeight;
        return bitmapHeight;
    }

    //检查本地是否存在文件并更新状态消息
    private void checkLocal(String uri, StatusMessage statusMessage) throws Exception {
        String name = md5.encode(uri);
        File file = new File(path.toString(), name);
        if (file.exists()) {
            statusMessage.islocalExists = true;
        }
    }


    /**
     * 向本地储存中放图片
     * @param uri 图片的网络连接
     * @param bitmap
     * @throws Exception
     */
    public void putToLocal(String uri, Bitmap bitmap) throws Exception {
        String name = md5.encode(uri);
        Log.d(TAG, "putToLocal: "+path.toString());
        File dir = new File(path.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, name);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
    }



    /**
     *计算合适压缩率,使加载的图片不会超过控件大小的2倍
     * @param
     * @returnn
     */
    private int countSampleSize(float viewHeight,float bitmapHeight) {
        float result = bitmapHeight;
        int i = 1;
        while (result > viewHeight * 2) {
            i++;
            result = bitmapHeight/i;
        }
        return i;
    }



    //加密内部类
    public class MD5 {
        public String encode(String string) throws Exception {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        }
    }
}
