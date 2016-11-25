package com.shizhefei.view.largeimage.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.File;
import java.io.IOException;

public class FileBitmapDecoderFactory implements BitmapDecoderFactory {
    private String path;

    public FileBitmapDecoderFactory(String filePath) {
        super();
        this.path = filePath;
    }

    public FileBitmapDecoderFactory(File file) {
        super();
        this.path = file.getAbsolutePath();
    }

    @Override
    public BitmapRegionDecoder made() throws IOException {
        return BitmapRegionDecoder.newInstance(path, false);
    }
}