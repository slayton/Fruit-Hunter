package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;

import com.quasicontrol.live.Sprite;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ClassicRunner extends PacDroid{
	
	ClassicRunner(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds, imageList);
	}
	ClassicRunner(int x, int y, Rect bounds, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		super(x,y,bounds, imageList, targets);
	}

	public void draw(final Canvas c, final Paint mPaint)
	{	
		this.move();
		
		if (imgIdx ==0)
			closingMouth = true;
		else if (imgIdx == imageArray.size()-1)
			closingMouth = false;
		
		imgIdx  += ((closingMouth) ? 1 : -1);
				
		drawMe = Bitmap.createBitmap(imageArray.get(imgIdx), 0, 0, image.getWidth(), image.getHeight(), m, true);
		c.drawBitmap(drawMe, this.x-image.getWidth()/2, this.y-image.getHeight()/2, mPaint);
	}
}

