package com.xiaoai.badgeview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TabWidget;
import android.widget.TextView;

public class BadgeView extends TextView {

	public static final int POSITION_TOP_LEFT = 1;
	public static final int POSITION_TOP_RIGHT = 2;
	public static final int POSITION_BOTTOM_LEFT = 3;
	public static final int POSITION_BOTTOM_RIGHT = 4;

	private static final int DEFAULT_MARGIN_DIP = 5;
	private static final int DEFAULT_LR_PADDING_DIP = 5;
	private static final int DEFAULT_CORNER_RADIUS_DIP = 9;
	private static final int DEFAULT_POSITION = POSITION_TOP_RIGHT;

	private static final int DEFAULT_BADGE_COLOR = Color.RED;
	private static final int DEFAULT_TEXT_COLOR = Color.WHITE;

	private static Animation fadeIn;
	private static Animation fadeOut;

	private Context context;

	private View target;

	private int badgePosition;
	private int badgeMargin;
	private int badgeColor;

	private boolean isShown;

	private ShapeDrawable badgeBg;

	private int targetTabIndex;

	public BadgeView(Context context) {
		this(context, (AttributeSet) null, android.R.attr.textViewStyle);
	}

	public BadgeView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public BadgeView(Context context, View target) {
		this(context, null, android.R.attr.textViewStyle, target, 0);
	}

	public BadgeView(Context context, TabWidget target, int index) {
		this(context, null, android.R.attr.textViewStyle, target, index);
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, null, 0);
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle,
			View target, int tabIndex) {
		super(context, attrs, defStyle);
		init(context, target, tabIndex);
	}

	private void init(Context context, View target, int tabIndex) {
		this.context = context;
		this.target = target;
		this.targetTabIndex = tabIndex;

		// apply defaults
		badgePosition = DEFAULT_POSITION;
		badgeMargin = dipToPixels(DEFAULT_MARGIN_DIP);
		badgeColor = DEFAULT_BADGE_COLOR;

		setTypeface(Typeface.DEFAULT_BOLD);
		int paddingPixels = dipToPixels(DEFAULT_LR_PADDING_DIP);
		setPadding(paddingPixels, 0, paddingPixels, 0);
		setTextColor(DEFAULT_TEXT_COLOR);

		fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setDuration(200);

		fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(200);

		isShown = false;

		if (this.target != null) {
			applyTo(this.target);
		} else {
			show();
		}
	}

	private void applyTo(View target) {
		LayoutParams lp = target.getLayoutParams();
		ViewParent parent = target.getParent();
		FrameLayout container = new FrameLayout(context);

		if (target instanceof TabWidget) {

			// set target to the relevant tab child container
			target = ((TabWidget) target).getChildTabViewAt(targetTabIndex);
			this.target = target;

			((ViewGroup) target).addView(container, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			this.setVisibility(View.GONE);
			container.addView(this);

		} else {

			// verify that parent is indeed a ViewGroup
			ViewGroup group = (ViewGroup) parent;
			int index = group.indexOfChild(target);

			group.removeView(target);
			group.addView(container, index, lp);

			container.addView(target);

			this.setVisibility(View.GONE);
			container.addView(this);

			group.invalidate();
		}
	}

	public void show() {
		show(false, null);
	}

	public void show(boolean animate) {
		show(animate, fadeIn);
	}

	public void show(Animation anim) {
		show(true, anim);
	}

	public void hide() {
		hide(false, null);
	}

	public void hide(boolean animate) {
		hide(animate, fadeOut);
	}

	public void hide(Animation anim) {
		hide(true, anim);
	}

	public void toggle() {
		toggle(false, null, null);
	}

	public void toggle(boolean animate) {
		toggle(animate, fadeIn, fadeOut);
	}

	public void toggle(Animation animIn, Animation animOut) {
		toggle(true, animIn, animOut);
	}

	private void show(boolean animate, Animation anim) {
		if (getBackground() == null) {
			if (badgeBg == null) {
				badgeBg = getDefaultBackground();
			}
			setBackgroundDrawable(badgeBg);
		}
		applyLayoutParams(-1, -1);

		if (animate) {
			this.startAnimation(anim);
		}
		this.setVisibility(View.VISIBLE);
		isShown = true;
	}

	private void hide(boolean animate, Animation anim) {
		this.setVisibility(View.GONE);
		if (animate) {
			this.startAnimation(anim);
		}
		isShown = false;
	}

	private void toggle(boolean animate, Animation animIn, Animation animOut) {
		if (isShown) {
			hide(animate && (animOut != null), animOut);
		} else {
			show(animate && (animIn != null), animIn);
		}
	}

	public int increment(int offset) {
		CharSequence txt = getText();
		int i;
		if (txt != null) {
			try {
				i = Integer.parseInt(txt.toString());
			} catch (NumberFormatException e) {
				i = 0;
			}
		} else {
			i = 0;
		}
		i = i + offset;
		setText(String.valueOf(i));

		return i;
	}

	public int decrement(int offset) {
		return increment(-offset);
	}

	private ShapeDrawable getDefaultBackground() {
		ShapeDrawable drawable = null;
		int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP);
		float[] outerR = new float[] { r, r, r, r, r, r, r, r };

		RoundRectShape rr = new RoundRectShape(outerR, null, null);
		drawable = new ShapeDrawable(rr);
		drawable.getPaint().setColor(badgeColor);

		return drawable;
	}

	private void applyLayoutParams(int width, int height) {
		int _width = width <= 0 ? LayoutParams.WRAP_CONTENT : width;
		int _height = height <= 0 ? LayoutParams.WRAP_CONTENT : height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(_width,
				_height);

		switch (badgePosition) {
		case POSITION_TOP_LEFT:
			lp.gravity = Gravity.LEFT | Gravity.TOP;
			lp.setMargins(badgeMargin, badgeMargin, 0, 0);
			break;
		case POSITION_TOP_RIGHT:
			lp.gravity = Gravity.RIGHT | Gravity.TOP;
			lp.setMargins(0, badgeMargin, badgeMargin, 0);
			break;
		case POSITION_BOTTOM_LEFT:
			lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
			lp.setMargins(badgeMargin, 0, 0, badgeMargin);
			break;
		case POSITION_BOTTOM_RIGHT:
			lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
			lp.setMargins(0, 0, badgeMargin, badgeMargin);
			break;
		default:
			break;
		}

		setLayoutParams(lp);
	}

	public View getTarget() {
		return target;
	}

	@Override
	public boolean isShown() {
		return isShown;
	}

	public int getBadgePosition() {
		return badgePosition;
	}

	public void setBadgePosition(int layoutPosition) {
		this.badgePosition = layoutPosition;
	}

	public int getBadgeMargin() {
		return badgeMargin;
	}

	public void setBadgeMargin(int badgeMargin) {
		this.badgeMargin = badgeMargin;
	}

	public int getBadgeBackgroundColor() {
		return badgeColor;
	}

	public void setBadgeBackgroundColor(int badgeColor) {
		this.badgeColor = badgeColor;
		badgeBg = getDefaultBackground();
	}

	private int dipToPixels(int dip) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
				r.getDisplayMetrics());
		return (int) px;
	}

}
