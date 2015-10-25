package com.shizhefei.view.largeimage;

import java.io.InputStream;

public interface IPhotoView {

	public void setScale(float scale, float offsetX, float offsetY);
	
	public void setImage(String filePath);

	public void setImage(InputStream inputStream);
	
	public int getImageWidth();

	public int getImageHeight();

	public Scale getScale();

	public static class Scale {
		volatile float scale;
		volatile int fromX;
		volatile int fromY;

		public Scale(float scale, int fromX, int fromY) {
			super();
			this.scale = scale;
			this.fromX = fromX;
			this.fromY = fromY;
		}

		public float getScale() {
			return scale;
		}

		void setScale(float scale) {
			this.scale = scale;
		}

		public int getFromX() {
			return fromX;
		}

		void setFromX(int fromX) {
			this.fromX = fromX;
		}

		public int getFromY() {
			return fromY;
		}

		void setFromY(int fromY) {
			this.fromY = fromY;
		}
	}

}
