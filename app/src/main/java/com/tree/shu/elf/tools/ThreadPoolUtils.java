package com.tree.shu.elf.tools;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tree.shu.elf.R;
import com.tree.shu.elf.ViewControlContainer;
import com.tree.shu.elf.activity.MainActivity;
import com.tree.shu.elf.activity.PlayActivity;
import com.tree.shu.elf.tools.image.MyMessage;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;


public class ThreadPoolUtils {

    public final int UPDATE_IMAGE = 1;
    public final int SET_DEFAULT_IMAGE = 2;
    public final int UP_MAIN_VIEW = 3;
    public final int SHOW_SHORT_TOAST = 4;
    public final int SHOW_LONG_TOAST = 5;


    private static ThreadPoolUtils threadPoolUtils;//静态的对象引用
    private ThreadPoolExecutor threadPoolExecutor;//线程池的

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_IMAGE:
                    ImageView imageView = (ImageView) ((MyMessage) msg.obj).getObjectList().get(0);
                    Bitmap bitmap = (Bitmap) ((MyMessage) msg.obj).getObjectObjectMap().get(imageView);
                    imageView.setImageBitmap(bitmap);
                    break;
                case SET_DEFAULT_IMAGE:
                    ImageView imageView1 = (ImageView)(msg.obj);
                    imageView1.setImageResource(R.drawable.ic_occupation);
                    break;

                case UP_MAIN_VIEW:
                    MyMessage myMessage = (MyMessage) msg.obj;
                    TextView song_name = (TextView) (myMessage).getObjectList().get(0);
                    TextView singer_name = (TextView) (myMessage).getObjectList().get(1);
                    song_name.setText(myMessage.getObjectObjectMap().get(song_name)+"");
                    singer_name.setText(myMessage.getObjectObjectMap().get(singer_name)+"");
                    break;

                case SHOW_LONG_TOAST:
                    Toast.makeText(((PlayActivity) ViewControlContainer.getInstance().getPlayActivity()), ((String) msg.obj), Toast.LENGTH_LONG).show();
                    break;
                case SHOW_SHORT_TOAST:
                    Toast.makeText(((PlayActivity) ViewControlContainer.getInstance().getPlayActivity()), ((String) msg.obj), Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };


    private ThreadPoolUtils() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
    }


    public Handler getHandler() {
        return handler;
    }

    //创建单列对象
    public static ThreadPoolUtils getThreadPoolUtils() {
        if (threadPoolUtils == null) {
            synchronized (ThreadPoolUtils.class) {
                if (threadPoolUtils == null) {
                    threadPoolUtils = new ThreadPoolUtils();
                }
            }
        }
        return threadPoolUtils;
    }


    public void execute(Runnable task) {
        threadPoolExecutor.execute(task);
    }

    //一个有返回值的执行方法
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<T>(task);
        threadPoolExecutor.submit(futureTask);
        return futureTask;
    }
}
