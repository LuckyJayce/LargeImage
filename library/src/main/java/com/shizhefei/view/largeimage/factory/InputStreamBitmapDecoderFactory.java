package com.shizhefei.view.largeimage.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamBitmapDecoderFactory implements BitmapDecoderFactory {
    private InputStream inputStream;

    public InputStreamBitmapDecoderFactory(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    @Override
    public BitmapRegionDecoder made() throws IOException {
        return BitmapRegionDecoder.newInstance(inputStream, false);
    }

}