package com.tree.shu.elf.tools;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class Trson {

    private static Trson trson;
    private final int FIELD = 1;
    private final int OBJECT = 2;
    private final int ARRAY = 3;
    private Type parameterType;


    private Trson() {
    }


    public static Trson getTrson() {
        if (trson == null) {
            synchronized (Trson.class) {
                if (trson == null) {
                    trson = new Trson();
                }
            }
        }
        return trson;
    }

    public <T> T factoryBean(String json, T mT) {
        try {
            JSONObject jsonObject = new JSONObject(json);//总的JSONobject
            Class mClass = mT.getClass();//总的Class
            Field fields[] = mClass.getDeclaredFields();
            fieldsTransfer(mT, fields, mClass, jsonObject);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return mT;
    }

    public <T> T factoryBean(String json, Type type) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type rawType = parameterizedType.getRawType();
        parameterType = parameterizedType.getActualTypeArguments()[0];
        T mT = null;
        try {
            JSONObject jsonObject = new JSONObject(json);//总的JSONobject
            mT = (T) Class.forName(((Class) rawType).getName()).newInstance();
            Class mClass = (Class) rawType;//总的Class
            Field fields[] = mClass.getDeclaredFields();
            fieldsTransfer(mT, fields, mClass, jsonObject);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return mT;
    }

    //遍历属性数组通过属性类型来调用函数
    private void fieldsTransfer(Object mObject, Field fields[], Class mClass, JSONObject jsonObject) throws JSONException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        for (Field f : fields) {
            if (judgeExist(f, jsonObject) && judge(f.getName(), jsonObject) == FIELD) {
                handleField(mObject, f, mClass, jsonObject);
            } else if (judgeExist(f, jsonObject) && judge(f.getName(), jsonObject) == OBJECT) {
                handleObject(mObject, f, mClass, jsonObject);
            } else if (judgeExist(f, jsonObject) && judge(f.getName(), jsonObject) == ARRAY) {
                handleArray(mObject, f, mClass, jsonObject);
            }
        }
    }

    //通过属性名判断是否为一个对象
    private int judge(String fieldName, JSONObject jsonObject) throws JSONException {
        char name[] = jsonObject.getString(fieldName).toCharArray();
        if (name.length > 0) {
            switch (name[0]) {
                case '{':
                    return OBJECT;
                case '[':
                    return ARRAY;
                default:
                    return FIELD;
            }
        }
        return FIELD;
    }


    //处理属性
    private void handleField(Object mObject, Field mF, Class mClass, JSONObject jsonObject) throws NoSuchMethodException, JSONException, InvocationTargetException, IllegalAccessException {
        Method setMethod = mClass.getDeclaredMethod(findSetMethodName(mF.getName()), mF.getType());
//        Class<?> parameterType = (setMethod.getParameterTypes())[0];
        try {
            if ("string".equalsIgnoreCase(mF.getType().getSimpleName())) {
                setMethod.invoke(mObject, jsonObject.get(mF.getName()));
            } else if ("int".equalsIgnoreCase(mF.getType().getSimpleName())
                    || "interger".equalsIgnoreCase(mF.getType().getSimpleName())) {
                setMethod.invoke(mObject, Integer.parseInt(jsonObject.get(mF.getName()).toString()));
            } else if ("double".equalsIgnoreCase(mF.getType().getSimpleName())) {
                setMethod.invoke(mObject, Double.parseDouble(jsonObject.get(mF.getName()).toString()));
            } else if ("boolean".equalsIgnoreCase(mF.getType().getSimpleName())) {
                setMethod.invoke(mObject, Boolean.parseBoolean(jsonObject.get(mF.getName()).toString()));
            } else if ("long".equalsIgnoreCase(mF.getType().getSimpleName()) | "Long".equalsIgnoreCase(mF.getType().getSimpleName())) {
                setMethod.invoke(mObject, Long.parseLong(jsonObject.get(mF.getName()).toString()));
            }
        } catch (Exception e) {
            return;
        }

    }

    //处理对象
    private void handleObject(Object mObject, Field mF, Class mClass, JSONObject mJsonObject) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException, InstantiationException, ClassNotFoundException {
        Method setMethod = mClass.getDeclaredMethod(findSetMethodName(mF.getName()), mF.getType());
        JSONObject jsonObject = mJsonObject.getJSONObject(mF.getName());//获得当前对象的JSONobject
        Object object = (Object) (mF.getType() == Object.class ? Class.forName(((Class) parameterType).getName()).newInstance() : mF.getType().newInstance());//获得当前的对象
        Class nClass = object.getClass() == Class.class ? (Class) parameterType : object.getClass();//获得当前的对象的class对象
        Field fields[] = nClass.getDeclaredFields();//获得当前的类中的属性
        fieldsTransfer(object, fields, nClass, jsonObject);//进行处理
        setMethod.invoke(mObject, object);//设置当前对象至外部对象
    }

    //处理数组
    private void handleArray(Object mObject, Field mF, Class mClass, JSONObject mJsonObject) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, JSONException, InstantiationException, ClassNotFoundException {
        Method setMethod = mClass.getDeclaredMethod(findSetMethodName(mF.getName()), mF.getType());
        JSONArray jsonArray = mJsonObject.getJSONArray(mF.getName());//获取当前的JsonArray
        Type genericType = mF.getGenericType();//获得属性类型
        Class<?> genericClazz = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            try {
                genericClazz = (Class<?>) pt.getActualTypeArguments()[0];//得到泛型里的class类型对象
            } catch (Exception e) {
                return;
            }
        }
        List<Object> beans = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            char[] a = jsonArray.get(i).toString().toCharArray();
            Object object = null;
            if (a[0] != '{') {
                Log.d(TAG, "handleArray: " + genericClazz.getSimpleName());
                if ("string".equalsIgnoreCase(genericClazz.getSimpleName())) {
                    object = jsonArray.get(i).toString();
                } else if ("int".equalsIgnoreCase(genericClazz.getSimpleName())
                        || "interger".equalsIgnoreCase(genericClazz.getSimpleName())) {
                    object = Integer.parseInt(jsonArray.get(i).toString());
                } else if ("double".equalsIgnoreCase(genericClazz.getSimpleName())) {
                    object = Double.parseDouble(jsonArray.get(i).toString());
                } else if ("boolean".equalsIgnoreCase(genericClazz.getSimpleName())) {
                    object = Boolean.parseBoolean(jsonArray.get(i).toString());
                }
            } else {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                assert genericClazz != null;
                object = genericClazz.newInstance();
                Field[] fields = genericClazz.getDeclaredFields();
                fieldsTransfer(object, fields, genericClazz, jsonObject);
            }
            beans.add(object);
        }
        setMethod.invoke(mObject, beans);
    }

    //通过属性名来找到设置方法名
    private String findSetMethodName(String fieldName) {
        return "set" + transform(fieldName);
    }


    //首字母大写
    private String transform(String word) {
        char abc[] = word.toCharArray();
        if (abc[0] >= 97 && abc[0] <= 122) {  //防止格式不正确的属性
            abc[0] -= 32;
            return String.valueOf(abc);
        }
        return word;
    }

    /**
     * 判断json数据里是否存在该数据
     *
     * @param field
     * @param jsonObject
     * @return
     */
    private boolean judgeExist(Field field, JSONObject jsonObject) {
        try {
            jsonObject.get(field.getName());
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static class TypeToken<T> {
        private final Type type;

        @SuppressWarnings("unchecked")
        protected TypeToken() {
            this.type = getSuperclassTypeParameter(getClass());
        }

        private static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            assert parameterized != null;
            return parameterized.getActualTypeArguments()[0];
        }

        public final Type getType() {
            return type;
        }
    }
}

