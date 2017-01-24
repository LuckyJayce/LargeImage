package com.example.lagerimage_test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.shizhefei.view.largeimage.BlockImageLoader;
import com.shizhefei.view.largeimage.UpdateImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.IOException;

public class ListImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_image);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private String[] sss = {"111.jpg","aaa.jpg","ccc.jpg"};
//        private String[] sss = {
//        "http://imgsrc.baidu.com/forum/pic/item/a8ec8a13632762d006deaa12a0ec08fa503dc6bf.jpg",
//        "http://img4.duitang.com/uploads/item/201408/30/20140830185456_Eijik.jpeg",
//        "http://difang.kaiwind.com/zhejiang/jctp/201407/18/W020140718488039321020.jpg",
//        "http:/pic.58pic.com/58pic/15/35/05/95258PICQnd_1024.jpg",
//        "http://img01.taopic.com/141114/318762-1411140J63541.jpg",
//        "http://img.pconline.com.cn/images/upload/upc/tx/photoblog/1107/05/c5/8235345_8235345_1309860288554.jpg",
//        "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1307/23/c0/23656308_1374564438338_800x600.jpg",
//        "http://s16.sinaimg.cn/mw690/6efe02a7tcf2328ae981f&690",
//        "http://img161.poco.cn/mypoco/myphoto/20100328/20/54786084201003282047212640036631376_003.jpg",
//        "http://img.sj33.cn/uploads/allimg/201210/2012101121161762.jpg",
//        "http://pic2.ooopic.com/12/13/96/45bOOOPICe8_1024.jpg",
//        "http://img4q.duitang.com/uploads/item/201207/23/20120723200549_ZhRre.thumb.700_0.jpeg",
//        "http://imgsrc.baidu.com/forum/pic/item/faf2b2119313b07e1d33eaad0cd7912396dd8c8d.jpg",
//        "http://img0.ph.126.net/eRv-A9o1L8v4MKZbiobhow==/6608664116770962144.jpg",
//        "http://img.club.pchome.net/kdsarticle/2014/05small/13/a7afab3aca7bb2cba1f3acd23204f71d_1000x750.jpg"
//        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(getLayoutInflater().inflate(R.layout.item_image,parent,false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            UpdateImageView updateImageView = itemViewHolder.updateImageView;
            try {
                updateImageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open(sss[position])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return sss.length;
        }

        private class ItemViewHolder extends RecyclerView.ViewHolder
        {
            private  UpdateImageView updateImageView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                updateImageView = (UpdateImageView) itemView.findViewById(R.id.imageView);
                updateImageView.setOnImageLoadListener(onImageLoadListener);
            }

            private BlockImageLoader.OnImageLoadListener onImageLoadListener = new BlockImageLoader.OnImageLoadListener() {
                @Override
                public void onBlockImageLoadFinished() {

                }

                @Override
                public void onLoadImageSize(int imageWidth, int imageHeight) {
                    Log.d("pppp","onLoadImageSize imageWidth:"+imageWidth+" imageHeight:"+imageHeight);
                    int width = updateImageView.getWidth();
                    int height = width * imageHeight/imageWidth;

                    ViewGroup.LayoutParams layoutParams = updateImageView.getLayoutParams();
                    if(layoutParams.height<300&&height>0){
                        layoutParams.height = height;
                        updateImageView.setLayoutParams(layoutParams);
                    }

                }

                @Override
                public void onLoadFail(Exception e) {

                }
            };
        }
    }

}
