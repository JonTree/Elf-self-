package com.tree.shu.elf.tools;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class InternetUtils {


    /**
     * 向指定 URL 发送Get方法的请求
 * @param url   发送请求的 URL
     */
    public static Future<String> requeestByGet(final String url) {
        return requeestByGet(url, null);
    }

    /**
     * 向指定 URL 发送Get方法的请求
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式
     */
    public static Future<String> requeestByGet(final String url, String param) {
        Callable<String> task = () -> {
            return get(url, param);
        };
        return ThreadPoolUtils.getThreadPoolUtils().submit(task);
    }

    public static String get(String url) {
        return get(url, null);
    }

    public static String get(String url, String param) {
        String responseData = null;
        HttpURLConnection httpURLConnection = null;
        try {
            StringBuilder stringBuilder = new StringBuilder(url);
            if (param != null && !param.trim().equals("")) {
                stringBuilder.append("?")
                        .append(param);
            }
            URL urll = new URL(stringBuilder.toString());
            httpURLConnection = (HttpURLConnection) urll.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-javascript->json");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            responseData = inputStreamToString(httpURLConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }
        return responseData;
    }



    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式
     */
    public static Future<String> requeestByPost(final String url, String param) {
        Callable<String> task = () -> {
            return post(url, param);
        };
        return ThreadPoolUtils.getThreadPoolUtils().submit(task);
    }

    public static String post(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String responseData = "";
        try {
            URL realUrl = new URL(url); // 打开和URL之间的连接
            HttpURLConnection httpURLConnection = (HttpURLConnection) realUrl
                    .openConnection(); // 设置通用的请求属性
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  // 设置内容类型
            httpURLConnection.setRequestProperty("charset", "utf-8");   // 设置字符编码
            httpURLConnection.setUseCaches(false); // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);// 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setReadTimeout(10000);        // 将读超时设置为指定的超时，以毫秒为单位。
            httpURLConnection.setConnectTimeout(10000);    // 设置一个指定的超时值（以毫秒为单位）
            if (param != null && !param.trim().equals("")) { // 获取URLConnection对象对应的输出流
                out = new PrintWriter(httpURLConnection.getOutputStream()); // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            responseData = inputStreamToString(httpURLConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流和输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return responseData;
    }


    //输入流转字符串
    public static String inputStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }


}
