package com.quasicontrol.pacdroidlive;


import com.quasicontrol.live.Sprite;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class PacDroid extends Sprite{

	protected boolean closingMouth = false;
	
	PacDroid(int x, int y, Rect bounds, ArrayList<Bitmap> imageList) {
		super(x, y, bounds,  imageList);
		
		//this.imageArray = imageList;
		//this.image = imageArray.get(1);
		this.tag = 555;
		this.vel = 2;
	}
	PacDroid(int x, int y, Rect bounds, ArrayList<Bitmap> imageList, ArrayList<Sprite> targets) {
		super(x, y, bounds, imageList);
		
		this.targets = targets;		
		this.tag = 555;
		this.vel = 2;
	}
	
	
	public void draw(final Canvas c, final Paint mPaint)
	{			
		if (imgIdx ==0)
			closingMouth = false;
		else if (imgIdx == imageArray.size()-1)
			closingMouth = true;
		
		imgIdx  += ((closingMouth) ? -1 : 1);
		
		drawMe = this.imageArray.get(imgIdx);
		
		c.drawBitmap(drawMe, this.x-image.getWidth()/2, this.y-image.getHeight()/2, mPaint);
	}
}

