package com.example.btdemo.customview;

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
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.example.btdemo.R;

public class FishView extends ImageView implements AnimationListener {

	private static final int FISH_PREFERRED_WIDTH = 70;
	private static final int FISH_PREFERRED_HEIGHT = 100;
	public static final int FISH_CAUGHT = 1;
	public static final int FISH_SWIMMING = 0;
	public static final int FISH_FREE = -1;
	public static final int LEFT_RIGHT = -10;
	public static final int RIGHT_LEFT = 10;

	public int type;
	public double fishWeight;
	public int speed;
	public int direction;

	private int state;
	private Bitmap fish_pic;
	private Animation anim = null;

	public FishView(Context context, int type) {
		super(context);

		Resources res = context.getResources();
		Bitmap fish_tmp;
		switch (type) {
		case 0:
			fish_tmp = BitmapFactory.decodeResource(res, R.drawable.sakana0);
			speed = 6000;
			fishWeight = 0.7;
			break;
		case 1:
			fish_tmp = BitmapFactory.decodeResource(res, R.drawable.sakana1);
			speed = 7000;
			fishWeight = 0.9;
			break;
		case 2:
			fish_tmp = BitmapFactory.decodeResource(res,
					R.drawable.bubbling_fish);
			speed = 4000;
			fishWeight = 0.3;
			break;
		default:
			fish_tmp = BitmapFactory.decodeResource(res, R.drawable.sakana1);
			speed = 7000;
			fishWeight = 0.9;
			break;
		}

		Matrix transMatrix = new Matrix();
		RectF src = new RectF(0, 0, fish_tmp.getWidth(), fish_tmp.getHeight());
		RectF dst = new RectF(0, 0, FISH_PREFERRED_WIDTH, FISH_PREFERRED_HEIGHT);
		transMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
		fish_pic = Bitmap.createBitmap(fish_tmp, 0, 0, fish_tmp.getWidth(),
				fish_tmp.getHeight(), transMatrix, true);

		this.setImageBitmap(fish_pic);
		this.type = type;
		this.state = FISH_FREE;
		direction = RIGHT_LEFT;

		this.setVisibility(View.GONE);
	}

	public void flip() {
		Matrix matrix = new Matrix();
		matrix.setScale(-1, 1);
		fish_pic = Bitmap.createBitmap(fish_pic, 0, 0, fish_pic.getWidth(),
				fish_pic.getHeight(), matrix, true);
		this.setImageBitmap(fish_pic);
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int i) {
		if (getDirection() != i) {
			direction = i;
			flip();
		}
	}

	public void startAnim(PointF start, PointF stop, int speed) {
		anim = new TranslateAnimation(start.x, stop.x, start.y, stop.y);
		anim.setFillAfter(false);
		anim.setDuration(speed);

		anim.setAnimationListener(this);
		this.startAnimation(anim);
	}

	public void stopAnim() {
		if (anim != null) {
			anim.cancel();
		}
	}

	public PointF getCurrentPosition() {
		if ((anim != null) && (anim.hasStarted()) && (!anim.hasEnded())) {
			Transformation trans = new Transformation();
			anim.getTransformation(AnimationUtils.currentAnimationTimeMillis(),
					trans);
			float val[] = new float[9];
			trans.getMatrix().getValues(val);
			return new PointF(val[Matrix.MTRANS_X], val[Matrix.MTRANS_Y]);
		}
		return new PointF(0, 0);
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		switch (state) {
		case FISH_CAUGHT:
		case FISH_SWIMMING:
		case FISH_FREE:
			this.state = state;
			break;
		default:
			this.state = FISH_FREE;
			break;
		}
	}

	public Bitmap getCaughtBitmap() {
		Matrix m = new Matrix();
		if (this.getDirection() == RIGHT_LEFT) {
			m.setRotate(90);
		} else {
			m.setRotate(-90);
		}
		return Bitmap.createBitmap(fish_pic, 0, 0, fish_pic.getWidth(),
				fish_pic.getHeight(), m, true);
	}

	public void onAnimationEnd(Animation animation) {
		setState(FISH_FREE);
		this.setVisibility(View.GONE);
	}

	public void onAnimationRepeat(Animation animation) {
	}

	public void onAnimationStart(Animation animation) {
		setState(FISH_SWIMMING);
		this.setVisibility(View.VISIBLE);
	}
}
