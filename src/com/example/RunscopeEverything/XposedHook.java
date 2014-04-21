package com.example.RunscopeEverything;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import de.robv.android.xposed.*;

import java.net.*;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XSharedPreferences;

public class XposedHook implements IXposedHookLoadPackage {
    String currentRunscopeSlug = null;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        final Class<?> activity = findClass("android.app.Activity", lpparam.classLoader);

        XposedBridge.log("Initializing Runscope hook");

        XSharedPreferences sharedPrefs = new XSharedPreferences("com.example.RunscopeEverything", "runscope");
        currentRunscopeSlug = sharedPrefs.getString("token", null);

        // NB: No Runscope slug set? Bail.
        if (currentRunscopeSlug == null) {
            XposedBridge.log("No Runscope slug set, leaving!");
        }

        final Class<?> httpUrlConnection = findClass("java.net.HttpURLConnection", lpparam.classLoader);
        XposedBridge.hookAllConstructors(httpUrlConnection, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String slug = currentRunscopeSlug;
                if (param.args.length != 1 || param.args[0].getClass() != URL.class || slug == null) {
                    return;
                }

                URL newUrl = rewriteUrlToRunscopeUrl((URL)param.args[0], slug, "HttpURLConnection");
                if (newUrl != null) {
                    param.args[0] = newUrl;
                }
            }
        });

        final Class<?> httpRequestBase = findClass("org.apache.http.client.methods.HttpRequestBase", lpparam.classLoader);
        findAndHookMethod(httpRequestBase, "setURI", URI.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String slug = currentRunscopeSlug;
                if (slug == null) {
                    return;
                }

                URI newUri = rewriteUriToRunscopeUri((URI) param.args[0], slug, "OkHttpClient open");
                if (newUri != null)  {
                    param.args[0] = newUri;
                }
            }
        });

        // NB: Unlike the above hooks, not every app will have OkHttp available
        try {
            final Class<?> okHttpClient = findClass("com.squareup.okhttp.OkHttpClient", lpparam.classLoader);

            findAndHookMethod(okHttpClient, "open", URI.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String slug = currentRunscopeSlug;
                    if (slug == null) {
                        return;
                    }

                    URI newUri = rewriteUriToRunscopeUri((URI) param.args[0], slug, "OkHttpClient open");
                    if (newUri != null)  {
                        param.args[0] = newUri;
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError _) {
        } catch (NoSuchMethodError _){
        }

        // https://code.google.com/p/httpclientandroidlib, used in Instagram
        try {
            final Class<?> boyeHttpRequestBase = findClass("ch.boye.httpclientandroidlib.client.methods.HttpRequestBase", lpparam.classLoader);
            findAndHookMethod(boyeHttpRequestBase, "setURI", URI.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String slug = currentRunscopeSlug;
                    if (slug == null) {
                        return;
                    }

                    URI newUri = rewriteUriToRunscopeUri((URI) param.args[0], slug, "boye.ch HttpClient");
                    if (newUri != null)  {
                        param.args[0] = newUri;
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError _) {
        } catch (NoSuchMethodError _){
        }
    }

    private URL rewriteUrlToRunscopeUrl(URL sourceUrl, String runscopeSlug, String methodHint) throws MalformedURLException {
        String host = sourceUrl.getHost();
        String newHost = host.replaceAll("-", "--").replaceAll("\\.", "-") + String.format("-%s.runscope.net", runscopeSlug);
        String newUrl = sourceUrl.toString().replace(host, newHost);

        // NB: For some reason some apps like to setURI on the result of getURI. :-/
        if (host.contains("runscope.net")) {
            return null;
        }

        XposedBridge.log(String.format("About to rewrite '%s' => '%s (%s)'", sourceUrl.toString(), newUrl, methodHint));
        return new URL(newUrl);
    }
      
    private URI rewriteUriToRunscopeUri(URI sourceUrl, String runscopeSlug, String methodHint) throws URISyntaxException {
        String host = sourceUrl.getHost();
        String newHost = host.replaceAll("-", "--").replaceAll("\\.", "-") + String.format("-%s.runscope.net", runscopeSlug);
        String newUrl = sourceUrl.toString().replace(host, newHost);

        // NB: For some reason some apps like to setURI on the result of getURI. :-/
        if (host.contains("runscope.net")) {
            return null;
        }

        XposedBridge.log(String.format("About to rewrite '%s' => '%s (%s)'", sourceUrl.toString(), newUrl, methodHint));
        return new URI(newUrl);
    }
}
