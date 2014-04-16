package com.example.RunscopeEverything;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import de.robv.android.xposed.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedHook implements IXposedHookLoadPackage {
    String currentRunscopeSlug = null;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        final Class<?> httpUrlConnection = findClass("java.net.HttpURLConnection", lpparam.classLoader);
        final Class<?> activity = findClass("android.app.Activity", lpparam.classLoader);

        XposedBridge.log("Initializing Runscope hook");
        /*
        findAndHookMethod(activity, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity thisActivity = (Activity)param.thisObject;

                try {
                    Context ctx = thisActivity.createPackageContext("com.example.RunscopeEverything", 0);
                    SharedPreferences prefs = ctx.getSharedPreferences("runscopeEverything", Context.MODE_PRIVATE);
                    currentRunscopeSlug = prefs.getString("token", null);
                } catch (Throwable e) {
                    XposedBridge.log("Couldn't get Runscope token");
                }
            }
        });
        */

        XposedBridge.hookAllConstructors(httpUrlConnection, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String slug = currentRunscopeSlug != null ? currentRunscopeSlug : "fnd4w6iq3qz1";
                if (param.args.length != 1 || param.args[0].getClass() != URL.class || slug == null) {
                    return;
                }

                param.args[0] = rewriteUrlToRunscopeUrl((URL)param.args[0], slug);
            }
        });
    }

    private URL rewriteUrlToRunscopeUrl(URL sourceUrl, String runscopeSlug) throws MalformedURLException {
        String host = sourceUrl.getHost();
        String newHost = host.replaceAll("-", "--").replaceAll("\\.", "-") + String.format("-%s.runscope.net", runscopeSlug);
        String newUrl = sourceUrl.toString().replace(host, newHost);

        XposedBridge.log(String.format("About to rewrite '%s' => '%s'", sourceUrl.toString(), newUrl));
        return new URL(newUrl);
    }
}
