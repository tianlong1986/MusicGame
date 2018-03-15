package com.example.gy.musicgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.MusicListAdapter;
import base.BaseActivity;
import bean.RecommendMusic;
import bean.dao.RecommendMusicDao;
import butterknife.BindView;
import butterknife.ButterKnife;
import utils.Constant;
import utils.CurrentMusicUtils;
import utils.DownloadUtil;
import utils.HttpUtils;
import utils.ImmersedStatusbarUtils;
import utils.MoreDialog;
import utils.MusicDaoUtils;
import utils.MusicUtils;
import utils.NetWorkUtils;
import utils.ToastUtils;
import view.CircleImageView;
import view.LoadListView;

/**
 * Created by Administrator on 2018/1/15.
 */

public class ListenMainActivity extends BaseActivity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, LoadListView.ILoadListener, View.OnClickListener, AdapterView.OnItemLongClickListener {
    @BindView(R.id.music_list)
    LoadListView listView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.back_listen)
    TextView back_listen;
    @BindView(R.id.title_txt)
    TextView title_txt;
    @BindView(R.id.content)
    RelativeLayout content;
    @BindView(R.id.msg)
    TextView msg_t;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.music_img)
    CircleImageView music_img;
    @BindView(R.id.singer_name)
    TextView singer_name;
    @BindView(R.id.singer)
    TextView singer;
    @BindView(R.id.play)
    TextView play;
    @BindView(R.id.music_next)
    TextView music_next;
    @BindView(R.id.lin)
    LinearLayout lin;
    private static boolean flag = true;
    private static int item_position;
    private static final String url = Constant.BASE_URL + "/music/getSongList";
    private static List<RecommendMusic> list = new ArrayList<>();
    private static List<String> playUrls = new ArrayList<>();
    private MusicListAdapter adapter;
    private static int offset = 0;
    private static int type;
    private static String title;
    private static int size = 20;
    private static String tinguid;

    private static RecommendMusic temp;

    private static boolean b = false;

    private static RecommendMusicDao musicDao = null;
    private RecommendMusic recommendMusic;


    private static final String TAG = "ListenMainActivity";
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (list.size() == 0) {
                    loading.setVisibility(View.GONE);
                    msg_t.setText("没有获取到数据...");
                } else {
                    content.setVisibility(View.GONE);
                    list.remove(recommendMusic);
                    adapter = new MusicListAdapter(list, ListenMainActivity.this);
                    listView.setAdapter(adapter);
                }
            } else if (msg.what == 0) {
                loading.setVisibility(View.GONE);
                msg_t.setText("呀,发生了错误啦,下拉刷新试试...");
                ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_icon, "发生了错误");
            } else if (msg.what == 3) {
                MusicUtils.play(playUrls.get(0));
                Picasso.with(ListenMainActivity.this).load(temp.getPic_small()).into(music_img);
                singer_name.setText(temp.getTitle());
                singer.setText(temp.getAuthor());
                play.setBackgroundResource(R.mipmap.music_stop);
            } else if (msg.what == 4) {
                ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_icon, "下载完成");
            } else if (msg.what == 6) {
                ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_warning, "下载失败");
            } else if (msg.what == 7) {
                content.setVisibility(View.GONE);
                adapter = new MusicListAdapter(list, ListenMainActivity.this);
                listView.setAdapter(adapter);
                listView.loadComplete(size + 20);
            } else if (msg.what == 8) {
                MusicUtils.play(playUrls.get(0));
            }
            if (msg.what == 9) {
                if (list.size() == 0) {
                    loading.setVisibility(View.GONE);
                    msg_t.setText("没有获取到数据...");
                } else {
                    content.setVisibility(View.GONE);
                    list.remove(recommendMusic);
                    adapter = new MusicListAdapter(list, ListenMainActivity.this);
                    listView.setAdapter(adapter);
                }
                swipe.setRefreshing(false);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_main);
        musicDao = MusicDaoUtils.initDbHelp(this);
        ButterKnife.bind(this);

        /*设置沉侵式导航栏*/
        ImmersedStatusbarUtils.initAfterSetContentView(this, lin);

        listView.setLoadListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        swipe.setOnRefreshListener(this);
        loading.setVisibility(View.VISIBLE);

        play.setOnClickListener(this);
        music_next.setOnClickListener(this);

        music_img.setOnClickListener(this);

        setTitle();
        initPlayBar();

        if (NetWorkUtils.checkNetworkState(this)) {
            sendHttp(url, type, offset, 1);
        } else {
            loading.setVisibility(View.GONE);
            msg_t.setText("貌似没有网哎...");
        }
    }

    private void initPlayBar() {
        if (NetWorkUtils.checkNetworkState(this)) {
            recommendMusic = CurrentMusicUtils.getRecommendMusic();
            if (recommendMusic != null) {
                Picasso.with(ListenMainActivity.this).load(recommendMusic.getPic_small()).into(music_img);
                singer_name.setText(recommendMusic.getTitle());
                singer.setText(recommendMusic.getAuthor());
                play.setEnabled(true);
                music_next.setEnabled(true);
                list.add(recommendMusic);

                if (MusicUtils.playState()) {
                    play.setBackgroundResource(R.mipmap.music_stop);
                }
                b = true;
            }
        } else {
            ToastUtils.showToast(this, R.mipmap.music_warning, "无网络连接");
        }
    }

    /**
     * 设置标题栏
     */
    private void setTitle() {
        type = getIntent().getIntExtra("position", 0);
        title = getIntent().getStringExtra("title");

        back_listen.setOnClickListener(this);
        title_txt.setText(title);
    }

    private void getPlayUrls(final int currentNum, final int what) {
        String songid = list.get(currentNum).getSong_id();
        Map<String, Object> map = new HashMap<>();
        map.put("songid", songid);
        final HttpUtils httpUtils = new HttpUtils(new HttpUtils.IHttpResponseListener() {
            @Override
            public void onSuccess(String json) {
                parseJsonUrl(json);
                handler.sendEmptyMessage(what);
            }

            @Override
            public void onFail(String error) {
                handler.sendEmptyMessage(0);
            }
        });
        String url = Constant.BASE_URL + "/music/PlaySong";
        httpUtils.sendGetHttp(url, map);
    }

    private void parseJsonUrl(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject bitrate = jsonObject.optJSONObject("bitrate");
            String show_link = bitrate.optString("show_link");
            playUrls.clear();
            playUrls.add(show_link);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendHttp(String url, int type, int offset, final int flag) {
        HttpUtils httpUtils = new HttpUtils(new HttpUtils.IHttpResponseListener() {
            @Override
            public void onSuccess(String json) {
                parseJson(json);
                handler.sendEmptyMessage(flag);
            }

            @Override
            public void onFail(String error) {
                handler.sendEmptyMessage(0);
            }
        });
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("size", size);
        map.put("offset", offset);
        httpUtils.sendGetHttp(url, map);
    }

    private void parseJson(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray song = jsonObject.optJSONArray("song_list");
            for (int i = 0; i < song.length(); i++) {
                Gson gson = new Gson();
                RecommendMusic recommendMusic = gson.fromJson(song.get(i).toString(), RecommendMusic.class);
                list.add(recommendMusic);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        item_position = position + 1;
        temp = list.get(position);
        b = false;
        if (NetWorkUtils.checkNetworkState(ListenMainActivity.this)) {
            play.setEnabled(true);
            music_next.setEnabled(true);
            getPlayUrls(position, 3);
            /*
            * 加入这条音乐到数据库中
            * */
            /*
            * 查询是否存在这条音乐
            * */
            CurrentMusicUtils.setRecommendMusic(temp);
            List<RecommendMusic> music = MusicDaoUtils.queryOneMusic(musicDao, temp);
            if (music.size() == 0) {
                MusicDaoUtils.addMusic(temp, musicDao);
            }
        } else {
            ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_warning, "无网络连接");
        }
    }

    @Override
    public void onRefresh() {
        if (NetWorkUtils.checkNetworkState(ListenMainActivity.this)) {
            sendHttp(url, type, 0, 9);
        } else {
            swipe.setRefreshing(false);
            ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_warning, "无网络连接");
        }
    }

    @Override
    public void onLoad() {
        if (NetWorkUtils.checkNetworkState(ListenMainActivity.this)) {
            sendHttp(url, type, ++offset, 7);
        } else {
            ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_warning, "无网络连接");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_listen:
                finish();
                break;
            case R.id.find:
                Intent intent = new Intent(ListenMainActivity.this, SingerInfoActivity.class);
                intent.putExtra("tinguid", tinguid);
                ListenMainActivity.this.startActivity(intent);
                MoreDialog.hidden();
                break;
            case R.id.cancel:
                MoreDialog.hidden();
                break;
            case R.id.play:
                if (b) {
                    //播放
                    MusicUtils.pause();
                    play.setBackgroundResource(R.mipmap.music_play);
                    b = false;
                } else {
                    MusicUtils.playContinue();
                    play.setBackgroundResource(R.mipmap.music_stop);
                    b = true;
                }
                if (flag) {
                    //播放
                    MusicUtils.pause();
                    play.setBackgroundResource(R.mipmap.music_play);
                    flag = false;
                } else {
                    MusicUtils.playContinue();
                    play.setBackgroundResource(R.mipmap.music_stop);
                    flag = true;
                }
                break;
            case R.id.music_next:
                //下一首
                if (b) {
                    ToastUtils.showToast(this, R.mipmap.music_warning, "没有下一曲");
                } else {
                    if (item_position + 1 > list.size()) {
                        ToastUtils.showToast(this, R.mipmap.music_warning, "亲,已经是最后一首了");
                    } else {
                        temp = list.get(item_position);
                        if (NetWorkUtils.checkNetworkState(this)) {
                            getPlayUrls(item_position, 3);
                            item_position++;
                        } else {
                            ToastUtils.showToast(this, R.mipmap.music_warning, "没有网络,无法播放下一首");
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        tinguid = list.get(position).getTing_uid();
        if (NetWorkUtils.checkNetworkState(ListenMainActivity.this)) {
            getPlayUrls(position, 7);
        } else {
            ToastUtils.showToast(ListenMainActivity.this, R.mipmap.music_warning, "无网络连接");
        }
        MoreDialog.show(ListenMainActivity.this);
        MoreDialog.find.setOnClickListener(this);
        MoreDialog.cancel.setOnClickListener(this);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        list.clear();
    }
}
