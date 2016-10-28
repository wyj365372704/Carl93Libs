package lib.carl93.example.activity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import lib.carl93.example.R;
import lib.carl93.networkaccesslib.NetworkAccessTool;

@EActivity(R.layout.activity_okhttp)
public class OkHttpActivity extends AppCompatActivity implements NetworkAccessTool.DownloadCallBack {
    @ViewById(R.id.download_pb)
    ProgressBar progressBar;
    @ViewById(R.id.download_bt)
    Button button;

    @Click(R.id.download_bt)
    void downLoad() {
        NetworkAccessTool networkAccessTool = new NetworkAccessTool.Builder().build();

        networkAccessTool.download("http://192.168.0.105:8080/UltraISO.v.9.6.2.3059.exe", new File(Environment.getExternalStorageDirectory().getPath() + "/aaa/UltraISO.v.9.6.2.3059.exe"), this, 1);
    }

    @Override
    public void onProgress(int progress, float networkSpeed) {
        Log.d("wyj", "progress is " + progress + " networkSpeed is " + networkSpeed);
        progressBar.setProgress(progress);
        button.setText("当前速度:" + networkSpeed);
    }

    @Override
    public void onStart(int requestTag) {
        button.setText("开始下载");
    }

    @Override
    public void onSuccess(String data, int requestTag) {
        button.setText("下载完成");
    }

    @Override
    public void onError(String msg, int requestTag) {
        button.setText("下载失败");
    }

}
