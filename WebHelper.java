package SEU.PACOTE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class WebHelper {
    private static final int REQ = 7001;
    private static ValueCallback<Uri[]> callback;
    private static Activity act;

    public static void config(Activity a, WebView w) {
        act = a;
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        w.addJavascriptInterface(new Ponte(), "Android");
        w.setWebViewClient(new WebViewClient());
        w.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (callback != null) callback.onReceiveValue(null);
                callback = cb;
                try {
                    act.startActivityForResult(p.createIntent(), REQ);
                } catch (Exception e) {
                    callback = null;
                    return false;
                }
                return true;
            }
        });
    }

    public static void result(int req, int res, Intent data) {
        if (req != REQ || callback == null) return;
        callback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
        callback = null;
    }

    public static class Ponte {
        @JavascriptInterface
        public void salvar(String nome, String texto) {
            String msg;
            try {
                byte[] dados = texto.getBytes("UTF-8");
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues v = new ContentValues();
                    v.put("_display_name", nome);
                    v.put("mime_type", nome.endsWith(".csv") ? "text/csv" : "application/json");
                    v.put("relative_path", Environment.DIRECTORY_DOWNLOADS);
                    Uri u = act.getContentResolver().insert(Uri.parse("content://media/external/downloads"), v);
                    OutputStream o = act.getContentResolver().openOutputStream(u);
                    o.write(dados);
                    o.close();
                } else {
                    File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    d.mkdirs();
                    FileOutputStream o = new FileOutputStream(new File(d, nome));
                    o.write(dados);
                    o.close();
                }
                msg = "Salvo em Downloads: " + nome;
            } catch (Exception e) {
                msg = "Erro ao salvar: " + e.getMessage();
            }
            final String m = msg;
            act.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(act, m, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
