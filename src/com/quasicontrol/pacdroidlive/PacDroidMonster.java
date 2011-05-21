package com.quasicontrol.pacdroidlive;

import java.util.ArrayList;
import com.quasicontrol.live.Sprite;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class PacDroidMonster extends com.quasicontrol.live.Sprite{

	public boolean vulnerable;
	
	PacDroidMonster(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds, imageList);
		this.vel = 5;
	}
	PacDroidMonster(int x, int y, Rect bounds, Bitmap bitmap, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		super(x, y, bounds, imageList, targets);
		this.vel = 5;
	}
	
	public void draw(final Canvas c, final Paint mPaint)
	{	
		//imgIdx = (vulnerable)?1:0;
		this.move();
		if (enabled)
			c.drawBitmap(this.imageArray.get(imgIdx), this.x-this.image.getWidth()/2, this.y-this.image.getHeight()/2, mPaint);
	}
}
