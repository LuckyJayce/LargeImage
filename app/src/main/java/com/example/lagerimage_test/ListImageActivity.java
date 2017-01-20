package com.example.lagerimage_test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        private String[] ss = {"111.jpg","aaa.jpg","ccc.jpg"};

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(getLayoutInflater().inflate(R.layout.item_image,parent,false)) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            UpdateImageView updateImageView = itemViewHolder.updateImageView;
            try {
                updateImageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open(ss[position])));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return ss.length;
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
                    int width = updateImageView.getWidth();
                    int height = width * imageHeight/imageWidth;
                    ViewGroup.LayoutParams layoutParams = updateImageView.getLayoutParams();
                    layoutParams.height = height;
                    updateImageView.setLayoutParams(layoutParams);
                }

                @Override
                public void onLoadFail(Exception e) {

                }
            };
        }
    }

}
