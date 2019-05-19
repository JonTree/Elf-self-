package com.tree.shu.elf.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;


public class Show {
    static public void shortToast(String content) {
        Message message = new Message();
        message.obj = content;
        ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(message);
    }
    static public void longToast(String content) {
        Message message = new Message();
        message.obj = content;
        ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(message);
    }

    static public void dialog(Context context,String content) {
        new AlertDialog.Builder(context).setMessage(content).setPositiveButton("确定",(dialog, which) -> {

            dialog.dismiss();
        }).show();
    }
}
