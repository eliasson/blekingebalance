/*
 * Copyright 2014 Markus Eliasson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.fourspaces.blekingebalance.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

import se.fourspaces.blekingebalance.R;

/**
 * The card view represents the card which is read. It presents the current state
 * and indicates progress.
 */
public class CardView extends View {
    public enum CardState {
        Unsupported,
        Disabled,
        Idle,
        InProgress,
        Complete,
        Error
    }
    private CardState mState = CardState.Idle;
    private double mBalance = 0d;
    private float mTextSize = 12f;
    private final float mRadius = 30.0f;

    private Paint mCardPaint;
    private Paint mCardStripePaint;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public CardView(Context context) {
        super(context);
        init(null, 0);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setState(CardState state) {
        mState = state;
        invalidateTextPaintAndMeasurements();
        this.invalidate();
    }

    public void setBalance(Double balance) {
        mBalance = balance;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderCard(canvas);
        if(mState == CardState.Complete) {
            renderBalance(canvas);
        }
        else {
            renderInstruction(canvas);
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CardView, defStyle, 0);

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(R.styleable.CardView_textSize, mTextSize);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(Color.WHITE);

        mCardPaint = new Paint();
        mCardPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mCardPaint.setColor(Color.parseColor("#d81733"));
        mCardStripePaint = new Paint();
        mCardStripePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mCardStripePaint.setColor(Color.WHITE);

        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextSize);
        mTextWidth = mTextPaint.measureText(getInstructionText());

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    private void renderCard(Canvas canvas) {
        final int height = getHeight();
        final int width = getWidth();
        canvas.drawRoundRect(new RectF(0, 0, width , height), mRadius, mRadius, mCardPaint);
        canvas.drawRect(0, getStripeTopPadding(), width , getStripeHeight(), mCardStripePaint);
    }

    private void renderInstruction(Canvas canvas) {
        final float height = getHeight() - getStripeHeight() - getStripeTopPadding() - mTextHeight;
        canvas.drawText(getInstructionText(),
                (getWidth() - mTextWidth) / 2,
                (height / 2) + getStripeHeight() + getStripeTopPadding(),
                mTextPaint);
    }

    private void renderBalance(Canvas canvas) {
        final String balance = String.format(Locale.getDefault(), "%.2f kr", mBalance);
        final float textWidth = mTextPaint.measureText(balance);

        final float height = getHeight() - getStripeHeight() - getStripeTopPadding() - mTextHeight;
        canvas.drawText(balance,
                (getWidth() - textWidth) / 2,
                (height / 2) + getStripeHeight() + getStripeTopPadding(),
                mTextPaint);
    }

    private int getStripeTopPadding() {
        return getHeight() / 7;
    }

    private int getStripeHeight() {
        return 2 * (getHeight() / 7);
    }

    private String getInstructionText() {
        switch(mState) {
            case Unsupported:
                return getResources().getString(R.string.scan_unsupported);
            case Disabled:
                return getResources().getString(R.string.scan_disabled);
            case InProgress:
                return getResources().getString(R.string.scan_inprogress);
            case Idle:
            default:
                return getResources().getString(R.string.scan_instruction);
        }
    }
}
