package com.shizhefei.view.largeimage.factory;

import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;

import java.io.IOException;

public interface BitmapDecoderFactory {
    BitmapRegionDecoder made() throws IOException;
    BitmapFactory.Options getImageInfo();
}