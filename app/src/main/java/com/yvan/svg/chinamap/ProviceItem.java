package com.yvan.svg.chinamap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * @author yvan
 * @date 2023/8/31
 * @description
 */
public class ProviceItem {

    /**
     * 绘制路径
     */
    private final Path path;

    /**
     * 绘制颜色
     */
    private int drawColor;

    public ProviceItem(Path path) {
        this.path = path;
    }

    /**
     * item绘制
     *
     * @param canvas
     * @param paint
     * @param isSelect
     */
    public void drawItem(Canvas canvas, Paint paint, boolean isSelect) {
        if (isSelect) {
            //选中时，绘制描边效果
            paint.clearShadowLayer();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);

            // 边框绘制
            paint.setStyle(Paint.Style.STROKE);
            int strokeColor = 0xFFD0E8F4;
            paint.setColor(strokeColor);
            canvas.drawPath(path, paint);
        } else {
            // 设置边界
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(8, 0, 0, 0xffffff);
            canvas.drawPath(path, paint);

            // 后面是填充
            paint.clearShadowLayer();
            paint.setColor(drawColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 设置填充颜色
     *
     * @param drawColor
     */
    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }

    /**
     * 判断点击是否在Path区域内
     *
     * @param x 点击的x坐标
     * @param y 点击的y坐标
     * @return 点击是否在Path区域内
     */
    public boolean isTouchPath(float x, float y) {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        //区域
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains((int) x, (int) y);
    }

}
