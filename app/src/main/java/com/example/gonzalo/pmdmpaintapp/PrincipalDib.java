package com.example.gonzalo.pmdmpaintapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;

public class PrincipalDib extends AppCompatActivity {

    private FloatingActionButton fab1, fab2, fab3, fab4;
    private Dibujo dibujo;
    private Handler mUiHandler = new Handler();
    private RelativeLayout rl;


//    *********** onCreate, inicializa los elementos de la aplicacion y sus respectivos onClick ******************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_dib);

        final FloatingActionMenu menu3 = (FloatingActionMenu) findViewById(R.id.menu3);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dibujo = new Dibujo(this);
        rl = (RelativeLayout) findViewById(R.id.lienzo);
        rl.addView(dibujo);

        menu3.hideMenuButton(false);

        int delay = 400;
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                menu3.showMenuButton(true);
            }
        }, delay);


        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dibujo.setColor(1);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dibujo.setColor(2);
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dibujo.setColor(3);
            }
        });
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(PrincipalDib.this, ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        dibujo.setColor(color);
                    }
                });
                colorPickerDialog.show();
            }
        });
        createCustomAnimation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }


//    **************** Funcionalidad de los elementos de la barra de menu ******************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.linea:
                dibujo.setEstilo(1);
                break;

            case R.id.circulo:
                dibujo.setEstilo(2);
                break;

            case R.id.rectangulo:
                dibujo.setEstilo(3);
                break;

            case R.id.estilolibre:
                dibujo.setEstilo(4);
                break;

            case R.id.nuevo:
                dibujo.nuevo();
                break;

            case R.id.guardar:
                guardar();
                break;

            case R.id.cargar:
                cargarImagen();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


//    *********************** Métodos para cargar una imagen como lienzo de fondo ********************
    public String getImagePath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    public void cargarImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();
            if (uri != null) {
                File f = new File(getImagePath(uri));
                dibujo.importarN(f);
            }
        }
    }


//  ****************** Método para guardar un lienzo como una imagen *******************************
    public void guardar() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        int res = R.layout.dialog;
        final View vista = inflater.inflate(res, null);
        alert.setView(vista);

        alert.setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText et = (EditText) vista.findViewById(R.id.etNombre);
                dibujo.guardar(et.getText().toString());
            }
        })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //no hacer nada al cancelar
                    }
                })
                .show();
    }


    //********************************** animacion del floating menu y sus botones **********************************
    private void createCustomAnimation() {
        final FloatingActionMenu menu3 = (FloatingActionMenu) findViewById(R.id.menu3);

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menu3.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menu3.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menu3.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menu3.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                menu3.getMenuIconView().setImageResource(menu3.isOpened()
                        ? R.drawable.paleta : R.drawable.cubo);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        menu3.setIconToggleAnimatorSet(set);
    }
}
