package org.icegeneral.rrcallbak.retrofit;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by iceGeneral on 16/7/14.
 */

public class DX168GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final Type type;

    DX168GsonResponseBodyConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody responseBody) throws IOException {
        String value = responseBody.string();
        try {
            JSONObject response = new JSONObject(value);
            int code = response.optInt("code");
            String msg = response.optString("msg");
            if (code != DX168API.RESULT_OK) {
                //返回的code不是RESULT_OK时Toast显示msg
                throw new DX168Exception(code, msg, value);
            }
            if (type instanceof Class) {
                if (type == String.class) {
                    return (T) value;
                }
                if (type == JSONObject.class) {
                    //如果返回结果是JSONObject则无需经过Gson
                    return (T) response;
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType() == DX168Response.class) {
                    String data = response.optString("data");
                    Type dataType = parameterizedType.getActualTypeArguments()[0];
                    if (dataType == JSONObject.class) {
                        return (T) new DX168Response(code, msg, new JSONObject(data));
                    }
                }
            }
            return gson.fromJson(value, type);
        } catch (JSONException e) {
            //服务端返回的不是JSON，服务端出问题
            throw new DX168Exception(-1, "", value);
        }
    }
}
