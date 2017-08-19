package tw.ming.app.helloworid.mynetwork;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView img;
    private Bitmap bmp;
    private UIHandler handler;
    private File sdroot;
    private ProgressBar progressBar;
    private File savePDF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);
        }else{
            init();
        }

    }

    private void init(){
        handler = new UIHandler();
        img = (ImageView)findViewById(R.id.img);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        sdroot = Environment.getExternalStorageDirectory();
    }
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                init();
            }else{
                finish();
            }
        }
    }
    public void test1(View view){
        //因不能使用於主執行緒,則需增加其他執行緒
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL("http://www.iii.org.tw/");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ( (line = br.readLine()) !=null){
                        Log.i("ming", line);
                    }
                    br.close();
                } catch (Exception e) {
                    Log.i("ming", e.toString());
                }

            }
        }.start();
    }
    public void test2(View view){
        new Thread(){
            @Override
            public void run() {
                try{
                    URL url = new URL("http://www.iii.org.tw/assets/images/information-news/image004.jpg");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    bmp = BitmapFactory.decodeStream(conn.getInputStream());
                    handler.sendEmptyMessage(0);
                }catch(Exception e){
                    Log.i("ming", e.toString());
                }
            }
        }.start();
    }

    public void test3(View view){
        progressBar.setVisibility(View.VISIBLE);//執行時顯示
        new Thread(){
            @Override
            public void run() {
                try {
                     savePDF = new File(sdroot,"url.pdf");
                    BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(savePDF));
                    URL url = new URL("http://pdfmyurl.com/");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    BufferedInputStream bin = new BufferedInputStream(conn.getInputStream());
                    byte[] buf = new byte[4096];int len=0;
                    while((len = bin.read(buf))!=-1){
                        bout.write(buf,0,len);
                    }
                    bin.close();
                    bout.flush();
                    bout.close();
                    handler.sendEmptyMessage(1);
                    //Toast.makeText(MainActivity.this, "Save OK", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.i("ming", e.toString());
                    handler.sendEmptyMessage(1);//若出現例外則依樣關掉
                }
            }
        }.start();
    }

    public void test4(View view){
        new Thread(){
            @Override
            public void run() {
                try {
                    MultipartUtility mu = new MultipartUtility(//10.0.2.2為外面
                            "http://10.0.2.2:8080/servlet/servlet11singleUpdate","UTF-8","");
                    mu.addFilePart("upload",savePDF);
                    List<String> ret = mu.finish();
                    for(String line:ret){
                        Log.i("ming",line);
                    }
                } catch (Exception e) {
                    Log.i("ming",e.toString());
                }
            }
        }.start();
    }
    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //img.setImageBitmap(bmp);
            switch (msg.what){
                case 0: img.setImageBitmap(bmp); break;
                case 1:
                    Toast.makeText(MainActivity.this, "Save OK", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    //showPDF();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "Save OK", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    }
    private void parseJson(String json) {
        try {
            JSONArray root = new JSONArray(json);

        }catch(Exception e){
            Log.i("ming",e.toString());
        }
    }
    private void showPDF(){
        Intent it = new Intent(Intent.ACTION_VIEW);
        it.setDataAndType(Uri.fromFile(savePDF), "application/pdf");
        it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent2 = Intent.createChooser(it, "Open File");
        try {
            startActivity(intent2);
        } catch (ActivityNotFoundException e) {
            Log.i("brad", e.toString());
            // Instruct the user to install a PDF reader here, or something
        }
        //startActivity(it);
    }

}
