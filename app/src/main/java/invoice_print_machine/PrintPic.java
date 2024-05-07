package invoice_print_machine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PrintPic {
    private Canvas canvas = null;
    private Paint paint = null;
    private Bitmap bm = null;
    private int width;
    private float length = 0.0F;
    private byte[] bitbuf = null;

    public PrintPic() {
    }

    public int getLength() {
        return (int)this.length;
    }

    public int getWidth() {
        return this.width;
    }

    public void initCanvas(int w) {
        int h = 10 * w;
        this.bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        this.canvas = new Canvas(this.bm);
        this.canvas.drawColor(-1);
        this.width = w;
        this.bitbuf = new byte[this.width / 8];
    }

    public void initPaint() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setColor(-16777216);
        this.paint.setStyle(Paint.Style.STROKE);
    }

    public void drawImage(Bitmap btm) {
        try {
            this.bm = btm;
            if (this.length < 0 + (float)btm.getHeight()) {
                this.length = 0 + (float)btm.getHeight();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void drawImage(float x, float y, String path) {
        try {
            Bitmap btm = BitmapFactory.decodeFile(path);
            this.canvas.drawBitmap(btm, x, y, this.paint);
            if (this.length < y + (float)btm.getHeight()) {
                this.length = y + (float)btm.getHeight();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void printPng() {
        File f = new File("/mnt/sdcard/0.png");
        FileOutputStream fos = null;
        Bitmap nbm = Bitmap.createBitmap(this.bm, 0, 0, this.width, this.getLength());

        try {
            fos = new FileOutputStream(f);
            nbm.compress(Bitmap.CompressFormat.PNG, 50, fos);
        } catch (FileNotFoundException var5) {
            var5.printStackTrace();
        }

    }

    public byte[] printDraw() {
        if (this.getLength() == 0) {
            return null;
        } else {
            Bitmap nbm = this.bm;
            byte[] imgbuf = new byte[this.width / 8 * this.getLength()];
            int s = 0;

            for(int i = 0; i < this.getLength(); ++i) {
                int k;
                for(k = 0; k < this.width / 8; ++k) {
                    int c0 = nbm.getPixel(k * 8 + 0, i);
                    byte p0;
                    if (c0 == -1) {
                        p0 = 0;
                    } else {
                        p0 = 1;
                    }

                    int c1 = nbm.getPixel(k * 8 + 1, i);
                    byte p1;
                    if (c1 == -1) {
                        p1 = 0;
                    } else {
                        p1 = 1;
                    }

                    int c2 = nbm.getPixel(k * 8 + 2, i);
                    byte p2;
                    if (c2 == -1) {
                        p2 = 0;
                    } else {
                        p2 = 1;
                    }

                    int c3 = nbm.getPixel(k * 8 + 3, i);
                    byte p3;
                    if (c3 == -1) {
                        p3 = 0;
                    } else {
                        p3 = 1;
                    }

                    int c4 = nbm.getPixel(k * 8 + 4, i);
                    byte p4;
                    if (c4 == -1) {
                        p4 = 0;
                    } else {
                        p4 = 1;
                    }

                    int c5 = nbm.getPixel(k * 8 + 5, i);
                    byte p5;
                    if (c5 == -1) {
                        p5 = 0;
                    } else {
                        p5 = 1;
                    }

                    int c6 = nbm.getPixel(k * 8 + 6, i);
                    byte p6;
                    if (c6 == -1) {
                        p6 = 0;
                    } else {
                        p6 = 1;
                    }

                    int c7 = nbm.getPixel(k * 8 + 7, i);
                    byte p7;
                    if (c7 == -1) {
                        p7 = 0;
                    } else {
                        p7 = 1;
                    }

                    int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7;
                    this.bitbuf[k] = (byte)value;
                }

                for(k = 0; k < this.width / 8; ++k) {
                    imgbuf[s] = this.bitbuf[k];
                    ++s;
                }
            }

            return imgbuf;
        }
    }
}
