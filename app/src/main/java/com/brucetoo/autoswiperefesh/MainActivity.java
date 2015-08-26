package com.brucetoo.autoswiperefesh;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private QuickAdapter<String> adapter;
    private String[] strs = new String[]{"content 1","content 2","content 3","content 4","content 2","content 2","content 2","content 2","content 2","content 2"};
    private View progress;
    private boolean visible;
    private String TAG = MainActivity.class.getSimpleName();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 1){
                //刷新数据之前delete footView
                if(listView.getFooterViewsCount() != 0) {
                    listView.removeFooterView(progress);
                    Log.e(TAG, "removeFooterView");
                }
                adapter.addAll(Arrays.asList(strs));
                //刷新数据之后add footView
                listView.addFooterView(progress);
                Log.e(TAG, "addFooterView");
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.refresh);
        listView = (ListView) this.findViewById(R.id.listview);

        progress = getLayoutInflater().inflate(R.layout.footer_view,null);
        adapter = new QuickAdapter<String>(this,R.layout.list_item,Arrays.asList(strs)) {
            @Override
            protected void convert(BaseAdapterHelper helper, String item) {
                helper.setText(R.id.text,item);
            }
        };

        //必须先addFooterView 才能再加载完后 delete
        listView.addFooterView(progress);
        listView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this);

        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == SCROLL_STATE_IDLE) {
//                    if (view.getLastVisiblePosition() == adapter.getCount() - 1) {
//                        listView.addFooterView(progress);
//                        //imitate getting data
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                handler.sendEmptyMessage(1);
//                            }
//                        }, 2000);
//                    }
//                }
                if(scrollState == SCROLL_STATE_IDLE && visible){
                    Log.e(TAG,"onScrollStateChanged:visible-"+visible);
                    handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(1);
                            }
                        }, 2000);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //第二种方式 判断是否滚动到最底部
              visible =  (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount - 1);
                if(visible){
                    Log.e(TAG,"onScroll:visible-true");
                }

                //可见的最后一个item的位置
//                int lastItem = firstVisibleItem + visibleItemCount;
//                if(lastItem == totalItemCount) {  //最后一个位置等于当前item的总数
//                    //最有一个item 顶部 和 listview底部相等时(滑动到最底部),执行刷新逻辑  先删除footview 在数据刷新完后在add
////                    View lastItemView = (View) listView.getChildAt(listView.getChildCount() - 1);
////                    if ((listView.getBottom()) == lastItemView.getBottom()) {
//                        if (progress != null && listView.getFooterViewsCount() != 0) {
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    handler.sendEmptyMessage(1);
//                                }
//                            }, 2000);
//
//                        }
////                    }
//                }
        }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //set auto refresh when first reach page
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                refreshLayout.setRefreshing(true);
//            }
//        }, 500);
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.replaceAll(Arrays.asList(strs));
                refreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

}
