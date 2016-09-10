package lib.carl93.networkaccesslib;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Carl on 2016-09-09 009.
 */
public class NetworkAccessTool {
    private final String TAG = "NetworkAccessTool";
    private static NetworkAccessTool netWorkAccessTool;
    private OkHttpClient client;
    private Handler handler;

    private static boolean debugMode = true;

    private NetworkAccessTool() {
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        handler = new Handler(Looper.getMainLooper());
    }

    private static NetworkAccessTool getInstance() {
        if (null == netWorkAccessTool)
            netWorkAccessTool = new NetworkAccessTool();
        return netWorkAccessTool;
    }

    public Call asynGet(String url, Map<String, Object> params, final int requestTag, final NetworkAccessTool.CallBack callBack) {
        Request request = new Request.Builder().url(url).get().build();

        Call call = client.newCall(request);

        onStart(callBack, requestTag);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                onError(callBack, e.getMessage(), requestTag);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess(callBack, response.body().string().trim(), requestTag);
                } else {
                    onError(callBack, response.message(), requestTag);
                }
            }
        });
        return call;
    }

    public Call asynPost(String url, Map<String, Objects> params, final int requestTag, final NetworkAccessTool.CallBack callBack) {
        FormBody.Builder builder = new FormBody.Builder();
        if (null != params) {
            for (Map.Entry<String, Objects> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue().toString());
            }
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder().url(url).post(requestBody).build();

        Call call = client.newCall(request);

        onStart(callBack, requestTag);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                onError(callBack, e.getMessage(), requestTag);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess(callBack, response.body().string().trim(), requestTag);
                } else {
                    onError(callBack, response.message(), requestTag);
                }
            }
        });
        return call;
    }

    public void cancelCall(Call call) {
        if (null != call)
            call.cancel();
    }

    private void onStart(final NetworkAccessTool.CallBack callBack, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onStart: the requestTag is " + requestTag);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onStart(requestTag);
                }
            });
        }
    }

    private void onSuccess(final NetworkAccessTool.CallBack callBack, final String data, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onSuccess: the requestTag is " + requestTag + " ,the data is " + data);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onSuccess(data, requestTag);
                }
            });
        }
    }

    private void onError(final NetworkAccessTool.CallBack callBack, final String msg, final int requestTag) {
        if (debugMode) {
            Log.i(TAG, "---->onError: the requestTag is " + requestTag + " ,the msg is " + msg);
        }
        if (null != callBack) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(msg, requestTag);
                }
            });
        }
    }

    public interface CallBack {
        /**
         * Running on UI thread.
         *
         * @param requestTag
         */
        void onStart(int requestTag);

        /**
         * Running on UI thread.
         *
         * @param data
         * @param requestTag
         */
        void onSuccess(String data, int requestTag);

        /**
         * Running on UI thread.
         *
         * @param msg
         * @param requestTag
         */
        void onError(String msg, int requestTag);
    }

    public static class Builder {
        public NetworkAccessTool build() {
            return getInstance();
        }

        public Builder debugMode(boolean mode) {
            debugMode = mode;
            return this;
        }
    }
}
