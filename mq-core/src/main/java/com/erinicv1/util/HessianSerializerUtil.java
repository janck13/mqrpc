package com.erinicv1.util;

import com.caucho.hessian.io.*;
import com.caucho.hessian.server.HessianSkeleton;

import java.io.*;
import java.lang.reflect.Method;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by Administrator on 2017/4/26 0026.
 */
public class HessianSerializerUtil {

    private static final SerializerFactory serializerFactory = new SerializerFactory();

    public static SerializerFactory getSerializerFactory(){
        return serializerFactory;
    }

    /**
     * 转hessian输入流
     * @param inputStream
     * @return
     */
    public static AbstractHessianInput getHessianInput(InputStream inputStream){
        AbstractHessianInput input = new Hessian2Input(inputStream);
        input.setSerializerFactory(serializerFactory);
        return input;
    }

    /**
     * 转hessian输出流
     * @param outputStream
     * @return
     */
    public static AbstractHessianOutput getHessianOutput(OutputStream outputStream){
        AbstractHessianOutput output = new Hessian2Output(outputStream);
        output.setSerializerFactory(serializerFactory);
        return output;
    }

    /**
     * 转hessian输出流
     * @param headerType
     * @param outputStream
     * @return
     */
    public static AbstractHessianOutput getHessianOutput(HessianInputFactory.HeaderType headerType,OutputStream outputStream){
        AbstractHessianOutput output;
        HessianFactory factory = new HessianFactory();
        switch (headerType){
            case CALL_1_REPLY_1:
                output = factory.createHessianOutput(outputStream);
                break;
            case CALL_1_REPLY_2:
            case HESSIAN_2:
                output = factory.createHessian2Output(outputStream);
                break;
            default:
                throw new IllegalStateException(headerType + " is unknown hessian call");
        }

        return output;
    }

    /**
     * 生产者响应消息体
     * @param response
     * @param method
     * @param isCompress
     * @return
     * @throws Throwable
     */
    public static Object clienResponseBody(byte[] response, Method method,boolean isCompress)throws Throwable{
        AbstractHessianInput input;
        InputStream is = new ByteArrayInputStream(response);
        if (isCompress){
            is = new InflaterInputStream(is,new Inflater(true));
        }
        int code = is.read();
        if (code == 'H'){
            is.read();
            is.read();
            input = getHessianInput(is);
            return input.readReply(method.getReturnType());
        }else if (code == 'r'){
            is.read();
            is.read();
            input = getHessianInput(is);
            input.startReplyBody();
            Object object = input.readReply(method.getReturnType());
            input.completeReply();
            return object;
        }else {
            throw new HessianProtocolException((char)code + " is unknown code ");
        }
    }

    /**
     * 生产者请求消息体
     * @param requests
     * @param method
     * @param isCompress
     * @return
     * @throws IOException
     */
    public static byte[] clientRequestBody(Object[] requests, Method method, boolean isCompress)throws IOException{
        OutputStream os;
        ByteArrayOutputStream payload = new ByteArrayOutputStream(256);
        String methodName = method.getName();

        if (isCompress){
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION,true);
            os = new DeflaterOutputStream(payload,deflater);
        }else {
            os = payload;
        }
        AbstractHessianOutput out = getHessianOutput(os);
        out.call(methodName,requests);
        if (os instanceof DeflaterOutputStream){
            ((DeflaterOutputStream) os).finish();
        }

        out.flush();
        return payload.toByteArray();
    }

    /**
     * 消费端调用成功响应体
     * @param requests
     * @param isCompress
     * @param serviceImpl
     * @param serviceApi
     * @return
     * @throws Exception
     */
    public static byte[] serverResponseBody(byte[] requests, boolean isCompress,Object serviceImpl,Class serviceApi)throws Exception{
        InputStream inputStream = new ByteArrayInputStream(requests);
        if (isCompress){
            inputStream = new InflaterInputStream(inputStream, new Inflater(true));
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream out;
        if (isCompress){
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION,true);
            out = new DeflaterOutputStream(bout,deflater);
        }else {
            out = bout;
        }

        HessianSkeleton skeleton = new HessianSkeleton(serviceImpl,serviceApi);
        skeleton.invoke(inputStream,out,serializerFactory);
        if (out instanceof DeflaterOutputStream){
            ((DeflaterOutputStream) out).finish();
        }
        out.close();
        return bout.toByteArray();
    }

    /**
     * 消费端调用异常
     * @param requests
     * @param throwable
     * @return
     */
    public static byte[] serverFautl(byte[] requests, Throwable throwable){
        try{
            InputStream is = new ByteArrayInputStream(requests);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            AbstractHessianOutput output = getHessianOutput(new HessianInputFactory().readHeader(is),os);
            output.writeFault(throwable.getClass().getSimpleName(),throwable.getMessage(),throwable);
            output.close();
            return os.toByteArray();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
