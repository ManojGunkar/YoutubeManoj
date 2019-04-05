package com.manoj.youtube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.manoj.youtube.adapter.MyListAdapter;
import com.manoj.youtube.modal.YoutubeModal;
import com.manoj.youtube.utils.Config;
import com.manoj.youtube.utils.DataHolder;
import com.manoj.youtube.web.RestClient;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by Manoj on 11/01/2016.
 */
public class MainVideoListActivity extends Activity {

    private ListView mListVIew;
    private MyListAdapter mAdapter;
    private ProgressDialog mDialog;
    private ArrayList<YoutubeModal.Item> mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_video_list);

        mListVIew= (ListView) findViewById(R.id.list_home);
        mListVIew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId=mList.get(position).getId().getVideoId();
                Intent intent=new Intent(MainVideoListActivity.this,YoutubeActivity.class);
                intent.putExtra("id", videoId);
                startActivity(intent);
            }
        });
        if (isNetworkConnected())
        getYoutubeFeeds();
        else {
            Toast.makeText(MainVideoListActivity.this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
            if (DataHolder.getInstance().getList()!=null){
                mAdapter = new MyListAdapter(MainVideoListActivity.this, DataHolder.getInstance().getList());
                mListVIew.setAdapter(mAdapter);
            }
        }
    }

    private void getYoutubeFeeds(){
        mDialog=new ProgressDialog(this);
        mDialog.setTitle("loading...");
        mDialog.setCancelable(false);
        mDialog.show();
        RestClient.GitApiInterface service=RestClient.getClient();
        Call<YoutubeModal> call=service.getYoutubeVideosList(Config.API_KEY,Config.CHANNEL_ID,"snippet",Config.ORDER,"20");
        call.enqueue(new Callback<YoutubeModal>() {
            @Override
            public void onResponse(Response<YoutubeModal> response) {
                if (response.isSuccess()) {
                    if (mDialog != null)
                        mDialog.cancel();
                    Toast.makeText(MainVideoListActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    ArrayList<YoutubeModal.Item> list = (ArrayList<YoutubeModal.Item>) response.body().getItems();
                    mList = list;
                    DataHolder.getInstance().setList(list);
                    mAdapter = new MyListAdapter(MainVideoListActivity.this, list);
                    mListVIew.setAdapter(mAdapter);
                } else {
                    if (mDialog != null)
                        mDialog.cancel();
                    Toast.makeText(MainVideoListActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mDialog != null)
                    mDialog.cancel();
                Toast.makeText(MainVideoListActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
