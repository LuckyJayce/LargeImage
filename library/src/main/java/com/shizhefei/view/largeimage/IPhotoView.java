/*
Copyright 2015 shizhefei（LuckyJayce）
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
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
