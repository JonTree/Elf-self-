package com.tree.shu.elf;

import android.media.MediaPlayer;
import android.widget.ImageView;
import android.widget.TextView;

import com.tree.shu.elf.Bean.SongListBean;
import com.tree.shu.elf.Bean.SongListInformationBean;
import com.tree.shu.elf.activity.IActivity;
import com.tree.shu.elf.tools.DP_PX;
import com.tree.shu.elf.tools.InternetUtils;
import com.tree.shu.elf.tools.MyApplication;
import com.tree.shu.elf.tools.ThreadPoolUtils;
import com.tree.shu.elf.tools.Trson;
import com.tree.shu.elf.tools.image.ImageLoader;
import com.tree.shu.elf.tools.image.MyMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Music {
    private static Music music;
    private SongListBean songListBean;

    private SongListInformationBean songListInformationBean;

    private String URL_BASE = "http://elf.egos.hosigus.com";

    private String URL_RecommendedSongList = URL_BASE + "/getRecommendID.php";

    private String URL_MoodSongList = URL_BASE + "/getSongListID.php";

    private String URL_musicDetails = "http://elf.egos.hosigus.com/music/playlist/detail";

    private Map<String, SongListInformationBean> moodDataMap;

    private List<Integer> songHistory;

    boolean isFirstStart = true;

    int nowPaly = 0;

    private String presentMood = "HAPPY";

    private MediaPlayer mediaPlayer;

    private boolean isPause;


    private Music() {
        moodDataMap = new HashMap<>();
        songHistory = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
    }

    public static Music getInstance() {
        if (music == null) {
            synchronized (Music.class) {
                if (music == null) {
                    music = new Music();
                }
            }
        }
        return music;
    }


    public int getNowPaly() {
        return nowPaly;
    }

    public Map<String, SongListInformationBean> getMoodDataMap() {
        return moodDataMap;
    }

    public Future<SongListInformationBean> recommendedSongList(String url) {
        return recommendedSongList(url, null);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void init() {
//        if (!moodDataMap.isEmpty()) {
//            return;
//        }
//        for (ImageView imageView : ViewControlContainer.getInstance().getMoodList()) {
//            String data1 = InternetUtils.get(URL_MoodSongList, "type=" + ((String) imageView.getTag()));
//            SongListBean songListBean = Trson.getTrson().factoryBean(data1, new SongListBean());
//            String data2 = InternetUtils.get(URL_musicDetails, "id=" + songListBean.getData().getId());
//            moodDataMap.put((String) imageView.getTag(), Trson.getTrson().factoryBean(data2, new SongListInformationBean()));
//            if (((String) imageView.getTag()).equals("HAPPY")) {
//                songListInformationBean = moodDataMap.get(((String) imageView.getTag()));
//            }
//        }
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(0.5f, 0.5f);
    }


    // 播放
    private void play() {
        try {
            if (isPause) {
                mediaPlayer.start();
                updateMusicView();
            } else {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("http://music.163.com/song/media/outer/url?id=" + songListInformationBean.getPlaylist().getTracks().get(nowPaly).getId() + ".mp3");
                mediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 异步准备Prepared完成监听
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            // 开始播放
            mediaPlayer.start();
            updateMusicView();
        });
    }

    private void updateMusicView() {

    }

    public Future<SongListInformationBean> recommendedSongList(String url, String param) {
        Callable<SongListInformationBean> task = () -> {
            songListBean = new SongListBean();
            Future<String> future = null;
            if (param != null) {
                future = InternetUtils.requeestByGet(url, param);
            } else {
                future = InternetUtils.requeestByGet(url);
            }
            try {
                Trson.getTrson().factoryBean(future.get(), songListBean);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SongListInformationBean songListInformationBean = null;
            if (songListBean.getStatus() == 200) {
                Future<String> future1 = InternetUtils.requeestByGet(URL_musicDetails, "id=" + songListBean.getData().getId());
                try {
                    if (moodDataMap.containsKey(ViewControlContainer.getInstance().getPresentMood())
                            && moodDataMap.get(ViewControlContainer.getInstance().getPresentMood()).getData().equals(future1.get())) {
                        songListInformationBean = moodDataMap.get(ViewControlContainer.getInstance().getPresentMood());
                    } else {
                        songListInformationBean = Trson.getTrson().factoryBean(future1.get(), new SongListInformationBean());
                        songListInformationBean.setData(future1.get());
                        moodDataMap.put(ViewControlContainer.getInstance().getPresentMood(), songListInformationBean);
                        for (int i = 0; i < songHistory.size(); i++) {
                            songHistory.remove(0);
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return songListInformationBean;
        };
        return ThreadPoolUtils.getThreadPoolUtils().submit(task);
    }


    public void setActivityView(IActivity activity, TextView song_name, TextView singer_name, ImageView song_icon) {
        setActivityView(false, activity, song_name, singer_name, song_icon);
    }

    public void setActivityView(boolean isForward, IActivity activity, TextView song_name, TextView singer_name, ImageView song_icon) {
        setActivityView(isForward, activity, song_name, singer_name, song_icon, false);
    }

    public void setActivityView(IActivity activity, TextView song_name, TextView singer_name, ImageView song_icon, boolean isBack) {
        setActivityView(false, activity, song_name, singer_name, song_icon, isBack);
    }

    public void setActivityView(boolean isForward, IActivity activity, TextView song_name, TextView singer_name, ImageView song_icon, boolean isBack) {
        if (isBack) {
            if (songHistory.isEmpty()) {
                ViewControlContainer.getInstance().getPlayActivity().showShortTaost("没有上一首了哦");
                return;
            } else {
                songHistory.remove(songHistory.size() - 1);
                nowPaly = songHistory.get(songHistory.size() - 1);
                loadDataIntoView(activity, song_name, singer_name, song_icon);
                return;
            }
        }
        Runnable task = () -> {
            if (isForward || !presentMood.equals(ViewControlContainer.getInstance().getPresentMood()) || songListInformationBean == null) {
                presentMood = ViewControlContainer.getInstance().getPresentMood();
                Future<SongListInformationBean> future_RecommendedSongList_isDone = recommendedSongList(URL_MoodSongList, "type=" + ViewControlContainer.getInstance().getPresentMood());
                try {
                    songListInformationBean = future_RecommendedSongList_isDone.get();
                    Random random = new Random();
                    nowPaly = random.nextInt(songListInformationBean.getPrivileges().size());
                    songHistory.add(nowPaly);
                    if (isFirstStart) {
                        play();
                        isFirstStart = false;
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loadDataIntoView(activity, song_name, singer_name, song_icon);
            } else {
                loadDataIntoView(activity, song_name, singer_name, song_icon);
            }
        };
        ThreadPoolUtils.getThreadPoolUtils().execute(task);
    }

    /**
     * 加载数据进图标，歌手名，歌曲名控件
     *
     * @param activity 控件所在的活动
     */
    private void loadDataIntoView(IActivity activity, TextView song_name, TextView singer_name, ImageView song_icon) {
        ImageLoader.with(MyApplication.getContext())
                .getImage(songListInformationBean.getPlaylist().getTracks().get(nowPaly).getAl().getPicUrl())
                .intoView(song_icon, DP_PX.dp2px(MyApplication.getContext(), 212));
        MyMessage myMessage = new MyMessage();
        String na = songListInformationBean.getPlaylist().getTracks().get(nowPaly).getName();
        String naa = songListInformationBean.getPlaylist().getTracks().get(nowPaly).getAr().get(0).getName();
        myMessage.addObjectList(song_name, songListInformationBean.getPlaylist().getTracks().get(nowPaly).getName());
        myMessage.addObjectList(singer_name, songListInformationBean.getPlaylist().getTracks().get(nowPaly).getAr().get(0).getName());
        myMessage.sendMessage(ThreadPoolUtils.getThreadPoolUtils().UP_MAIN_VIEW);
        activity.setNowPlayMusic(songListInformationBean.getPlaylist().getTracks().get(nowPaly).getName());
    }


    public SongListInformationBean getSongListInformationBean() {
        return songListInformationBean;
    }


    /**
     * 取得当前播放的歌曲的名称
     */
    public String getNowPalyName() {
        return songListInformationBean.getPlaylist().getTracks().get(nowPaly).getName();
    }
}
