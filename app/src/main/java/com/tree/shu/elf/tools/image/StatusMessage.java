package com.tree.shu.elf.tools.image;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Message;
import android.widget.ImageView;


import com.tree.shu.elf.R;
import com.tree.shu.elf.tools.ThreadPoolUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 加载每张图片自己的状态类
 * 每加载一张图片都会生成一个对应图片的特定的状态对象
 */


public class StatusMessage {

    private String uri;
    boolean islocalExists = false;
    boolean isMemoryExists = false;

    private ImageLoader imageLoader;
    private Future<?> future;
    private Bitmap bitmap;
    float viewHeight = 0;


    public StatusMessage(String uri, ImageLoader imageLoader) {
        this.uri = uri;
        this.imageLoader = imageLoader;
    }

    public StatusMessage setViewHeight(float viewHeight) {
        this.viewHeight = viewHeight;
        return this;
    }


    /**
     * 将图片加载进View
     */
    public void into(final ImageView imageView) {
        if (uri == null) {
            return;
        }
        if (this.viewHeight == 0) {
            imageView.getViewTreeObserver().addOnDrawListener(() -> {
                float imageViewHeight = imageView.getHeight();
                intoView(imageView, imageViewHeight);
            });
        } else {
            intoView(imageView, viewHeight);
        }
    }

    /**
     * 真正把图片加载到空间的函数
     *
     * @param imageView  需要加载的控件
     * @param viewHeight 控件的高度
     */
    public void intoView(ImageView imageView, float viewHeight) {
        if (isMemoryExists) {
            bitmap = imageLoader.getImageCache().lruCache.get(uri);
            MyMessage myMessage = new MyMessage(imageView, bitmap);
            myMessage.sendMessage(ThreadPoolUtils.getThreadPoolUtils().UPDATE_IMAGE);
        } else {
            Message m = new Message();
            m.obj = imageView;
            ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(m);
            Runnable task = () -> {
                try {
                    Message message = new Message();
                    if (islocalExists) {
                        bitmap = imageLoader.getImageCache().getFromLocal(uri, viewHeight);
                        if (bitmap == null) {
                            islocalExists = false;
                            imageLoader.getFromInter(uri, this).into(imageView);
                            return;
                        }
                    } else {
                        bitmap = (Bitmap) getFuture().get();
                        imageLoader.getImageCache().putToLocal(uri, bitmap);
                        bitmap = imageLoader.getImageCache().getFromLocal(uri, viewHeight);
                    }
                    imageLoader.getImageCache().putBitmap(uri, bitmap);
                    MyMessage myMessage = new MyMessage(imageView, bitmap);
                    myMessage.sendMessage(ThreadPoolUtils.getThreadPoolUtils().UPDATE_IMAGE);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            ThreadPoolUtils.getThreadPoolUtils().execute(task);
        }
        imageLoader.getStatusMessageList().remove(uri);
        try {
            imageLoader = new ImageLoader(null);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }
}
