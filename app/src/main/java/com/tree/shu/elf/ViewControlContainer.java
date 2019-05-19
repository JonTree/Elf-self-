package com.tree.shu.elf;

import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.tree.shu.elf.activity.IActivity;
import com.tree.shu.elf.activity.MainActivity;
import com.tree.shu.elf.activity.PlayActivity;
import com.tree.shu.elf.tools.InternetUtils;
import com.tree.shu.elf.tools.MyApplication;
import com.tree.shu.elf.tools.ThreadPoolUtils;
import com.tree.shu.elf.view.LrcView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ViewControlContainer implements View.OnClickListener {
    private static ViewControlContainer viewControlContainer;
    private RelativeLayout content_fra_evaluation;
    private FrameLayout content_fra_evaluation_botton;
    private FrameLayout content_fra_play_botton;
    private LinearLayout no_evaluate_content;
    private GridLayout yes_evaluate_content;
    private LinearLayout no_evaluate;
    private ImageView mood_clam;
    private ImageView mood_exciting;
    private ImageView mood_happy;
    private ImageView mood_unhappy;
    private LinearLayout content_evaluate;
    private ImageView play_image_view_botton;
    private ImageView play_button_in_playactivity;
    private SeekBar seek_bar_in_playactivity;
    private List<ImageView> moodList;
    private String presentMood;
    private IActivity mainActivity;
    private IActivity playActivity;

    LrcView playLrcView;

    boolean isOtherSeekBarUP = false;


    public IActivity getMainActivity() {
        return mainActivity;
    }

    public IActivity getPlayActivity() {
        return playActivity;
    }

    private ViewControlContainer() {
        moodList = new ArrayList<>();
        presentMood = "HAPPY";
    }

    public static ViewControlContainer getInstance() {
        if (viewControlContainer == null) {
            synchronized (ViewControlContainer.class) {
                if (viewControlContainer == null) {
                    viewControlContainer = new ViewControlContainer();
                }
            }
        }
        return viewControlContainer;
    }

    public List<ImageView> getMoodList() {
        return moodList;
    }

    public void init() {
        if (mood_happy!=null){
            return;
        }
        {
            ViewGroup view = (ViewGroup) View.inflate(MyApplication.getContext(), R.layout.layout_mood, null);
            mood_clam = view.findViewById(R.id.mood_clam);
            mood_clam.setTag("CLAM");
            moodList.add(mood_clam);
            mood_exciting = view.findViewById(R.id.mood_exciting);
            mood_exciting.setTag("EXCITING");
            moodList.add(mood_exciting);
            mood_unhappy = view.findViewById(R.id.mood_unhappy);
            mood_unhappy.setTag("UNHAPPY");
            moodList.add(mood_unhappy);
            mood_happy = view.findViewById(R.id.mood_happy);
            mood_happy.setTag("HAPPY");
            moodList.add(mood_happy);
            view.removeAllViews();
        }
        {//表情评价点击栏
            content_evaluate = View.inflate(MyApplication.getContext(), R.layout.content_evaluate, null).findViewById(R.id.content_evaluation);
            no_evaluate = content_evaluate.findViewById(R.id.no_evaluate);
            no_evaluate_content = content_evaluate.findViewById(R.id.no_evaluate_content);
            no_evaluate_content.setTag(false);
            for (ImageView imageView : moodList) {
                imageView.setOnClickListener(this);
                if (imageView != mood_happy) {
                    if (((ViewGroup) imageView.getParent()) != null) {
                        ((ViewGroup) imageView.getParent()).removeView(imageView);
                    }
                    no_evaluate_content.addView(imageView);
                }
            }
            yes_evaluate_content = content_evaluate.findViewById(R.id.yes_evaluate_content);
            yes_evaluate_content.addView(mood_happy);
        }
        {
            content_fra_evaluation = (RelativeLayout) View.inflate(MyApplication.getContext(), R.layout.content_play_evaluation_floating, null);
            content_fra_play_botton = content_fra_evaluation.findViewById(R.id.content_fra_play_botton);
            play_image_view_botton = content_fra_play_botton.findViewById(R.id.play_image_view_botton);
            play_image_view_botton.setOnClickListener(this);
            content_fra_evaluation_botton = content_fra_evaluation.findViewById(R.id.content_fra_evaluation_botton);
            content_fra_evaluation_botton.addView(content_evaluate);
        }
        {
//           playLrcView = (LrcView) View.inflate(MyApplication.getContext(), R.layout.lrc_view, null);
            play_button_in_playactivity = (ImageView) View.inflate(MyApplication.getContext(),R.layout.button_paly,null);
            play_button_in_playactivity.setOnClickListener(this);
            seek_bar_in_playactivity = (SeekBar) View.inflate(MyApplication.getContext(), R.layout.seek_bar, null);
        }
    }

    public void removeParent() {
        if (seek_bar_in_playactivity.getParent() != null) {
            ((ViewGroup) seek_bar_in_playactivity.getParent()).removeAllViews();
        }
        if (play_button_in_playactivity.getParent() != null) {
            ((ViewGroup) play_button_in_playactivity.getParent()).removeAllViews();
        }
//        if (playLrcView.getParent() != null) {
//            ((ViewGroup) playLrcView.getParent()).removeAllViews();
//        }
    }


    public SeekBar getSeek_bar_in_playactivity() {
        return seek_bar_in_playactivity;
    }

    public ImageView getPlay_button_in_playactivity() {
        return play_button_in_playactivity;
    }


//    public LrcView getPlayLrcView() {
//        return playLrcView;
//    }


    public RelativeLayout getContent_fra_evaluation() {
        return content_fra_evaluation;
    }


    public void upSeekBar(int time) {
        Message message = new Message();
        message.what = ThreadPoolUtils.getThreadPoolUtils().UPDATA_SEEK_BAR;
        message.arg1 = time;
        ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(message);
    }

    public void startSeekBar() {
        Runnable runnable = () -> {
            upLrcView();
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isOtherSeekBarUP) {
                    return;
                }
//                ViewControlContainer.getInstance().getPlayLrcView().updateTime(Music.getInstance().getMediaPlayer().getCurrentPosition());
//                playLrcView.updateTime(Music.getInstance().getMediaPlayer().getCurrentPosition());
                upSeekBar(Music.getInstance().getMediaPlayer().getCurrentPosition());
            }
        };
        new Thread(runnable).start();
    }

    public void upLrcView() {
        Runnable runnable = () -> {
            String data = InternetUtils.get(
                    Music.getInstance().getURL_LRC()
                            + Music.getInstance().getSongListInformationBean().getPlaylist().getTracks()
                            .get(Music.getInstance().getNowPaly()).getId());
            try {
                JSONObject jsonObject = new JSONObject(data);
                String lrc = (String) jsonObject.getJSONObject("lrc").get("lyric");
                Message message = new Message();
                message.what = ThreadPoolUtils.getThreadPoolUtils().LOAD_LRC;
                message.obj = lrc;
                ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        };
        ThreadPoolUtils.getThreadPoolUtils().execute(runnable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mood_clam:
            case R.id.mood_exciting:
            case R.id.mood_happy:
            case R.id.mood_unhappy:
                if (!((Boolean) no_evaluate_content.getTag())) {
                    no_evaluate.setVisibility(View.VISIBLE);
                    no_evaluate_content.setTag(true);
                } else {
                    no_evaluate.setVisibility(View.GONE);
                    no_evaluate_content.setTag(false);
                    View view = yes_evaluate_content.getChildAt(0);
                    if (view != v) {
                        no_evaluate_content.removeView(v);
                        yes_evaluate_content.removeAllViews();
                        no_evaluate_content.addView(view);
                        yes_evaluate_content.addView(v);
                        presentMood = (String) v.getTag();
                        Music.getInstance().setIsFirstStart(false);
                        mainActivity.songChange();
                    }
                }
                break;
            case R.id.play_image_view_botton:
                Intent intent = new Intent(MyApplication.getContext(), PlayActivity.class);
                MyApplication.getContext().startActivity(intent);
                break;
            case R.id.play:
                if (Music.getInstance().getIsPause()) {
                    Music.getInstance().play();
                    play_button_in_playactivity.setImageResource(R.drawable.ic_play_running);
                    Music.getInstance().setIsPause(false);
                }else {
                    if (Music.getInstance().getMediaPlayer().isPlaying()) {
                        Music.getInstance().setIsPause(true);
                        Music.getInstance().getMediaPlayer().pause();
                        play_button_in_playactivity.setImageResource(R.drawable.ic_play_pause);
                    }
                }
                break;

        }
    }



    public String getPresentMood() {
        return presentMood;
    }

    public void initForThis(ViewGroup content_fra_evaluation, ViewGroup content_play_image_view) {

        if (content_play_image_view != null) {
            if (play_image_view_botton.getParent() != null) {
                ((ViewGroup) play_image_view_botton.getParent()).removeAllViews();
                content_play_image_view.addView(play_image_view_botton);
            } else {
                content_play_image_view.addView(play_image_view_botton);
            }
        }
        if (content_fra_evaluation != null) {
            if (getContent_fra_evaluation().getParent() != null) {
                ((ViewGroup) getContent_fra_evaluation().getParent()).removeAllViews();
                content_fra_evaluation.addView(getContent_fra_evaluation());
            } else {
                content_fra_evaluation.addView(getContent_fra_evaluation());
            }
        }
    }

    public void onBindMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void unBindMainActivity() {
        this.mainActivity = null;
    }

    public void onBindPlayActivity(PlayActivity playActivity) {
        this.playActivity = playActivity;
    }

    public void unBindPlayActivity() {
        this.playActivity = null;
    }

}
