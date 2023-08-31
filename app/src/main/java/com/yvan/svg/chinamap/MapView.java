package com.yvan.svg.chinamap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author yvan
 * @date 2023/8/31
 * @description
 */
public class MapView extends View {

    private static final String TAG = MapView.class.getSimpleName();

    private List<ProviceItem> proviceItems;
    private Paint paint;
    private RectF totalRect;
    private float scale = 1.0f;
    private ProviceItem selectProviceItem;
    private final int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFFFFFFF};

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        loadThread.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        initScale(width);

        Log.i(TAG, "onMeasure totalRect:" + totalRect + ",scale:" + scale + ",width:" + width);
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleTouch(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }

    /**
     * 点击事件处理
     *
     * @param x
     * @param y
     */
    private void handleTouch(float x, float y) {
        if (proviceItems == null) {
            return;
        }
        ProviceItem selectItem = null;
        for (ProviceItem proviceItem : proviceItems) {
            // 这里的坐标x,y轴需要根据缩放换算才能对上位置
            if (proviceItem.isTouchPath(x / scale, y / scale)) {
                selectItem = proviceItem;
            }
        }
        // 记录当前被选中的省份，并刷新展示
        if (selectItem != null) {
            selectProviceItem = selectItem;
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (proviceItems != null) {
            canvas.save();
            canvas.scale(scale, scale);
            Log.i(TAG, "onDraw scale:" + scale);
            for (ProviceItem proviceItem : proviceItems) {
                proviceItem.drawItem(canvas, paint, false);
            }
            if (selectProviceItem != null) {
                selectProviceItem.drawItem(canvas, paint, true);
            }
        }
    }

    /**
     * 对点列表中数据进行颜色设置后，调postInvalidate开始draw渲染
     */
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (proviceItems == null) {
                return;
            }
            for (int i = 0; i < proviceItems.size(); i++) {
                proviceItems.get(i).setDrawColor(colorArray[i % (colorArray.length - 1)]);
            }

            postInvalidate();
        }
    };

    /**
     * 该线程计算china.svg中的坐标点
     * 获取到点信息列表proviceItems、大小边距totalRect
     */
    private final Thread loadThread = new Thread() {

        @Override
        public void run() {
            InputStream inputStream = getContext().getResources().openRawResource(R.raw.china);
            List<ProviceItem> list = new ArrayList<>();
            try {
                // 取得DocumentBuilderFactory实例
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                // 从factory获取DocumentBuilder实例
                DocumentBuilder builder;
                builder = factory.newDocumentBuilder();
                // 解析输入流 得到Document实例
                Document doc = builder.parse(inputStream);
                Element rootElement = doc.getDocumentElement();
                NodeList items = rootElement.getElementsByTagName("path");
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;
                for (int i = 0; i < items.getLength(); i++) {
                    Element element = (Element) items.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);
                    ProviceItem proviceItem = new ProviceItem(path);
                    list.add(proviceItem);
                    RectF rect = new RectF();
                    path.computeBounds(rect, true);
                    left = left == -1 ? rect.left : Math.min(left, rect.left);
                    right = right == -1 ? rect.right : Math.max(right, rect.right);
                    top = top == -1 ? rect.top : Math.min(top, rect.top);
                    bottom = bottom == -1 ? rect.bottom : Math.max(bottom, rect.bottom);
                    totalRect = new RectF(left, top, right, bottom);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "loadThread run totalRect2:" + totalRect);
            initScale(getMeasuredWidth());

            proviceItems = list;
            handler.sendEmptyMessage(1);
        }
    };

    /**
     * 缩放倍数计算
     *
     * @param width
     */
    private void initScale(int width) {
        if (totalRect != null) {
            double mapWidth = totalRect.width();
            scale = (float) (width / mapWidth);
        }
    }

}
