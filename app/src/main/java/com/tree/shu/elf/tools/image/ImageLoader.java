package com.tree.shu.elf.tools.image;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.tree.shu.elf.tools.ThreadPoolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


public class ImageLoader {
    private static ImageLoader imageLoader;//单列对象
    private Map<String, StatusMessage> statusMessageList;
    private ImageCache imageCache;


    //私有化构造方法
    public ImageLoader(Context context) throws PackageManager.NameNotFoundException {
        imageCache = ImageCache.getImageCache(context);
        statusMessageList = new HashMap<>();
    }

    //取得单例对象
    private static void getImageLoader(Context context) throws PackageManager.NameNotFoundException {
        if (imageLoader == null) {
            synchronized (ImageLoader.class) {
                if (imageLoader == null) {
                    imageLoader = new ImageLoader(context);
                }
            }
        }
    }


    /**
     * 传入上下文对象，若没有初始化单列对象就初始化
     *
     * @param context
     * @return imageLoader
     * @throws PackageManager.NameNotFoundException
     */
    public static ImageLoader with(Context context) {
        try {
            getImageLoader(context);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return imageLoader;
    }

    /**
     * 获取图片的起始方法（***     get() *********）
     * 获取图片状态对象StatusMessage
     *
     * @param uri
     * @return StatusMessage 用于最后加载图片
     * @throws Exception
     */
    public StatusMessage getImage(final String uri) {
        StatusMessage statusMessage = new StatusMessage(uri, this);
        if (uri == null) {
            return statusMessage;
        }
        statusMessageList.put(uri, statusMessage);
        try {
            imageCache.checkBitmap(uri, statusMessageList.get(uri));
            if (!statusMessage.islocalExists && !statusMessage.isMemoryExists) {
                getFromInter(uri, statusMessage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusMessageList.get(uri);
    }


    public Map<String, StatusMessage> getStatusMessageList() {
        return statusMessageList;
    }

    public ImageCache getImageCache() {
        return imageCache;
    }

    /**
     * 通过HttpURLConnection来获取图片
     * 建立一个Call来制造任务，返回Bitmap
     * 然后通过线程池来执行任务并通过Future来获取相应图片
     *
     * @param uri
     * @return imageLoader
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public StatusMessage getFromInter(final String uri, StatusMessage statusMessage) {
        Callable<Bitmap> task = () -> {
            InputStream inputStream = null;
            HttpURLConnection httpURLConnection = null;
            Bitmap bitmap = null;
            try {
                URL u = new URL(uri);
                httpURLConnection = (HttpURLConnection) u.openConnection();
                httpURLConnection.setRequestMethod("GET");
                inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                        httpURLConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        };
        statusMessage.setFuture(ThreadPoolUtils.getThreadPoolUtils().submit(task));
        return statusMessage;
    }

    /**
     * 设置路径名称
     *
     * @param name
     * @return
     */
    public ImageLoader setFile(String name) {
        imageCache.setPath(name);
        return imageLoader;
    }
}
