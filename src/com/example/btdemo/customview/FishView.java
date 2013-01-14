package com.example.btdemo.customview;

import com.example.btdemo.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class FishView extends ImageView {

	private static final int FISH_PREFERRED_WIDTH = 70;
	private static final int FISH_PREFERRED_HEIGHT = 100;
	public static final int FISH_FREE = 0;
	public static final int FISH_CAUGHT = 1;

	private int type;
	private float fishWeight;
	private Bitmap fish_pic;
	private PointF coordinate;
	private int status;
	private Animation anim;

	public FishView(Context context, int type, float weight) {
		super(context);

		Resources res = context.getResources();

		Bitmap fish_tmp = BitmapFactory.decodeResource(res,
				R.drawable.sakana1);

		Matrix transMatrix = new Matrix();
		RectF src = new RectF(0, 0, fish_tmp.getWidth(), fish_tmp.getHeight());
		RectF dst = new RectF(0, 0, FISH_PREFERRED_WIDTH, FISH_PREFERRED_HEIGHT);
		transMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
		fish_pic = Bitmap.createBitmap(fish_tmp, 0, 0, fish_tmp.getWidth(),
				fish_tmp.getHeight(), transMatrix, true);

		this.setImageBitmap(fish_pic);
		this.type = type;
		this.fishWeight = weight;

		this.setVisibility(View.GONE);
	}

	public void startAnim(PointF start, PointF stop, int speed) {
		anim = new TranslateAnimation(start.x, stop.x, start.y, stop.y);
		anim.setFillAfter(false);
		anim.setDuration(speed);

		this.setVisibility(View.VISIBLE);
		this.startAnimation(anim);
	}

	public void stopAnim() {
		anim.setFillAfter(true);
		anim.cancel();
	}

	public PointF getCurrentPosition() {
		return new PointF(this.getLeft(), this.getTop());
	}
}
