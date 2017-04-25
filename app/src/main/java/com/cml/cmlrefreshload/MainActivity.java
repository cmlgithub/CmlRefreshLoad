package com.cml.cmlrefreshload;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cml.cmlrefreshload_library.CmlRefreshLoadListener;
import com.cml.cmlrefreshload_library.CmlRefreshLoadMoreLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private List<String> mDatas = new ArrayList<>();
    private CmlRefreshLoadMoreLayout cmlLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = new ProgressBar(this);

        ImageView imageViewHeader = new ImageView(this);
        ImageView imageViewFooter = new ImageView(this);
        imageViewHeader.setImageResource(R.mipmap.ic_launcher);
        imageViewFooter.setImageResource(R.mipmap.ic_launcher);
        cmlLayout = (CmlRefreshLoadMoreLayout) findViewById(R.id.cml);
        cmlLayout.setHeaderView(progressBar);
        cmlLayout.setFooterView(imageViewFooter);

        cmlLayout.setCmlRefreshLoadMoreLayout(new CmlRefreshLoadListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cmlLayout.finishRefreshOrLoadMore();
                    }
                },2000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cmlLayout.finishRefreshOrLoadMore();
                    }
                },2000);
            }
        });


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        for (int i = 0;i<250;i++){
            mDatas.add(i+"");
        }
        mRecyclerView.setAdapter(new MainAdapter());


    }


    class MainAdapter extends RecyclerView.Adapter<MainViewHolder>{

        @Override
        public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MainViewHolder(new TextView(MainActivity.this));
        }

        @Override
        public void onBindViewHolder(MainViewHolder holder, int position) {
            holder.textView.setText(mDatas.get(position));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

    class MainViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public MainViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
