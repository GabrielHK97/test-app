package com.example.app;

import android.os.Build;
import android.os.Bundle;

import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patchJSInjection();
    }

    private void patchJSInjection() {
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                Method getJsInjector = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    getJsInjector = Arrays.stream(bridge.getClass().getDeclaredMethods())
                            .filter(method -> method.getName().equals("getJSInjector"))
                            .findFirst()
                            .get();
                }

                getJsInjector.setAccessible(true);
                var injector = getJsInjector.invoke(bridge);

                Method getScriptString = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    getScriptString = Arrays.stream(injector.getClass().getDeclaredMethods())
                            .filter(method -> method.getName().equals("getScriptString"))
                            .findFirst()
                            .get();
                }
                var scriptString = (String) getScriptString.invoke(injector);

                Set<String> allowedOrigins = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allowedOrigins = Arrays.stream(bridge.getConfig().getAllowNavigation())
                            .filter(str -> str.contains("yourdomain.com") || str.contains("otherdomain.com"))
                            .filter(str -> str.contains("https://"))
                            // WebViewCompat likes things formatted particularly, trim trailing /*
                            .map(str -> str.replaceAll("/\\*$", ""))
                            .collect(Collectors.toSet());
                }

                Logger.info("patchJSInjection", "Injecting custom rules " + allowedOrigins);
                WebViewCompat.addDocumentStartJavaScript(bridge.getWebView(), scriptString, allowedOrigins);
            }
        } catch (Exception e) {
            Logger.error( e.getMessage(), e);
        }
    }
}
