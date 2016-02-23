package com.example.gonzalo.pmdmpaintapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


/**
 * Created by Gonzalo on 22/02/2016.
 */
//Clase Dibujo se encarga del control de la vista en la que dibujaremos
public class Dibujo extends View {

    public Canvas lienzoFondo;
    private Context context;
    private Paint pincel;
    private Path rectaPoligonal = new Path();
    private int ancho, alto;
    private float x1, y1, x2, y2;
    private ArrayList<Integer> lista;
    private Bitmap mapaDeBits;
    private int estilo = 0;
    private int color = 0;


    //****************************************** Métodos pojo ****************************************************
    public Dibujo(Context context) {
        super(context);
        this.context = context;

    }

    public void setEstilo(int estilo) {
        this.estilo = estilo;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


    //**************************************** Añadir un nuevo lienzo *******************************************************
    public void nuevo() {
        new AlertDialog.Builder(context).setTitle("Ooops!").setMessage("Si creas un nuevo dibujo, se borrará el actual. ¿Continuar?")
                .setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mapaDeBits = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                        lienzoFondo = new Canvas(mapaDeBits);
                        lienzoFondo.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
                        invalidate();
                    }
                })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.alert)
                .show();
    }


    //*************************************** Guardar un lienzo como jpeg *******************************************
    public void guardar(String nombre) {
        Bitmap imagen = getBitmap(lienzoFondo);
        File carpeta = new File(Environment.getExternalStorageDirectory().getPath());
        String name = nombre + ".jpeg";
        try {
            File archivo = new File(carpeta, name);
            FileOutputStream fos = new FileOutputStream(archivo);
            imagen.compress(Bitmap.CompressFormat.PNG, 0, fos);
            loadImg(archivo);

            Toast.makeText(context, "Dibujo guardado", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Bitmap getBitmap(Canvas canvas) {
        try {
            java.lang.reflect.Field field = Canvas.class.getDeclaredField("mBitmap");
            field.setAccessible(true);
            return (Bitmap) field.get(canvas);
        } catch (Throwable t) {
            return null;
        }
    }

    public void loadImg(File f) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }



    //****************************** Cargar una imagen como lienzo *******************************
    public void importarN(File f) {
        mapaDeBits = Bitmap.createBitmap(ancho, alto,
                Bitmap.Config.ARGB_8888);
        if (f.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            mapaDeBits = BitmapFactory.decodeFile(
                    f.getAbsolutePath(), options);
        }
        lienzoFondo = new Canvas(mapaDeBits);
        invalidate();
    }



    //**************************** Métodos para dibujar en el lienzo ********************************
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ancho = w;
        alto = h;

        mapaDeBits = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        lienzoFondo = new Canvas(mapaDeBits);
    }

    @Override
    protected void onDraw(Canvas c) {
        pincel = new Paint();
        switch (color) {
            case 1:
                pincel.setColor(Color.BLUE);
                break;
            case 2:
                pincel.setColor(Color.RED);
                break;
            case 3:
                pincel.setColor(Color.YELLOW);
                break;
            default:
                pincel.setColor(color);
                break;

        }
        pincel.setAntiAlias(true);
        pincel.setStrokeWidth(5);
        pincel.setStyle(Paint.Style.STROKE);

        c.drawBitmap(mapaDeBits, 0, 0, null);
        switch (estilo) {
            case 1:
                c.drawPath(rectaPoligonal, pincel);
                break;
            case 2:

                break;
            case 3:

                break;
            case 4:
                c.drawPath(rectaPoligonal, pincel);
                break;
            default:

                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (estilo) {
            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = x;
                        y1 = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = x;
                        y2 = y;
                        lienzoFondo.drawLine(x1, y1, x2, y2, pincel);
                        x1 = x2;
                        y1 = y2;

                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = x;
                        y2 = y;
                        break;
                }
                invalidate();
                return true;

            case 2:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = x;
                        y1 = y;
                        rectaPoligonal.moveTo(x1, y1);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = x;
                        y2 = y;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        float radio = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
                        lienzoFondo.drawCircle(x1, y1, radio, pincel);
                        rectaPoligonal.reset();
                        invalidate();
                        break;
                }
                return true;

            case 3:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = x;
                        y1 = y;
                        rectaPoligonal.moveTo(x1, y1);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = x;
                        y2 = y;
                        lienzoFondo.drawRect(x1, y1, x2, y2, pincel);
                        rectaPoligonal.reset();
                        invalidate();
                        break;
                }
                return true;

            case 4:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = x;
                        y1 = y;
                        rectaPoligonal.moveTo(x1, y1);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = x;
                        y2 = y;
                        rectaPoligonal.quadTo(x1, y1, (x + x2) / 2, (y + y2) / 2);
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = x;
                        y2 = y;
                        lienzoFondo.drawPath(rectaPoligonal, pincel);
                        rectaPoligonal.reset();
                        break;
                }
                invalidate();
                return true;
            default:
                break;
        }
        return true;
    }
}
