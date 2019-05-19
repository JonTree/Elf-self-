package com.tree.shu.elf.tools.image;


import android.os.Message;

import com.tree.shu.elf.tools.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成一个对象用来用于在handler里传递消息
 */

public class MyMessage {
    List<Object> objectList;
    Map<Object, Object> objectObjectMap;


    public MyMessage() {
        objectObjectMap = new HashMap<>();
        objectList = new ArrayList<>();
    }

    public MyMessage(Object object,Object data) {
        this();
        addObjectList(object, data);
    }


    public void removeAllString() {
        for (int i = 0; i < objectList.size(); i++) {
            objectList.remove(0);
        }
        objectObjectMap.clear();
    }

    public List<Object> getObjectList() {
        return objectList;
    }

    public Map<Object, Object> getObjectObjectMap() {
        return objectObjectMap;
    }

    public Map addObjectList(Object object, Object data) {
        objectList.add(object);
        objectObjectMap.put(object, data);
        return objectObjectMap;
    }

    public void sendMessage(int identifier) {
        Message message = new Message();
        message.obj = this;
        message.what = identifier;
        ThreadPoolUtils.getThreadPoolUtils().getHandler().sendMessage(message);
    }
}
