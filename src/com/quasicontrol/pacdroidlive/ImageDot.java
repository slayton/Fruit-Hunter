package com.quasicontrol.pacdroidlive;


import android.graphics.Bitmap;

public class ImageDot extends Dot{
	protected Bitmap image;
	
	ImageDot(int x, int y, Bitmap image){
		super(x,y);
		this.image = image;
	}

	public void draw()
	{
		
	}
}
