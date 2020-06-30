package com.wxipad.proto;

import io.grpc.stub.ClientCalls;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.17.1)",
        comments = "Source: WechatProto.proto")
public final class WechatGrpc {

    public static final String SERVICE_NAME = "WechatProto.Wechat";
    private static final int METHODID_HELLO_WECHAT = 0;
    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<WechatMsg,
            WechatMsg> getHelloWechatMethod;
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    private WechatGrpc() {
    }

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "HelloWechat",
            requestType = WechatMsg.class,
            responseType = WechatMsg.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<WechatMsg,
            WechatMsg> getHelloWechatMethod() {
        io.grpc.MethodDescriptor<WechatMsg, WechatMsg> getHelloWechatMethod;
        if ((getHelloWechatMethod = WechatGrpc.getHelloWechatMethod) == null) {
            synchronized (WechatGrpc.class) {
                if ((getHelloWechatMethod = WechatGrpc.getHelloWechatMethod) == null) {
                    WechatGrpc.getHelloWechatMethod = getHelloWechatMethod =
                            io.grpc.MethodDescriptor.<WechatMsg, WechatMsg>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "WechatProto.Wechat", "HelloWechat"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            WechatMsg.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            WechatMsg.getDefaultInstance()))
                                    .setSchemaDescriptor(new WechatMethodDescriptorSupplier("HelloWechat"))
                                    .build();
                }
            }
        }
        return getHelloWechatMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static WechatStub newStub(io.grpc.Channel channel) {
        return new WechatStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static WechatBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new WechatBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static WechatFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new WechatFutureStub(channel);
    }

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (WechatGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new WechatFileDescriptorSupplier())
                            .addMethod(getHelloWechatMethod())
                            .build();
                }
            }
        }
        return result;
    }

    /**
     *
     */
    public static abstract class WechatImplBase implements io.grpc.BindableService {

        /**
         *
         */
        public void helloWechat(WechatMsg request,
                                io.grpc.stub.StreamObserver<WechatMsg> responseObserver) {
            asyncUnimplementedUnaryCall(getHelloWechatMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getHelloWechatMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            WechatMsg,
                                            WechatMsg>(
                                            this, METHODID_HELLO_WECHAT)))
                    .build();
        }
    }

    /**
     *
     */
    public static final class WechatStub extends io.grpc.stub.AbstractStub<WechatStub> {
        private WechatStub(io.grpc.Channel channel) {
            super(channel);
        }

        private WechatStub(io.grpc.Channel channel,
                           io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected WechatStub build(io.grpc.Channel channel,
                                   io.grpc.CallOptions callOptions) {
            return new WechatStub(channel, callOptions);
        }

        /**
         *
         */
        public void helloWechat(WechatMsg request,
                                io.grpc.stub.StreamObserver<WechatMsg> responseObserver) {
            ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getHelloWechatMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     *
     */
    public static final class WechatBlockingStub extends io.grpc.stub.AbstractStub<WechatBlockingStub> {
        private WechatBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private WechatBlockingStub(io.grpc.Channel channel,
                                   io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected WechatBlockingStub build(io.grpc.Channel channel,
                                           io.grpc.CallOptions callOptions) {
            return new WechatBlockingStub(channel, callOptions);
        }

        /**
         *
         */
        public WechatMsg helloWechat(WechatMsg request) {
            return blockingUnaryCall(
                    getChannel(), getHelloWechatMethod(), getCallOptions(), request);
        }
    }

    /**
     *
     */
    public static final class WechatFutureStub extends io.grpc.stub.AbstractStub<WechatFutureStub> {
        private WechatFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private WechatFutureStub(io.grpc.Channel channel,
                                 io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected WechatFutureStub build(io.grpc.Channel channel,
                                         io.grpc.CallOptions callOptions) {
            return new WechatFutureStub(channel, callOptions);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<WechatMsg> helloWechat(
                WechatMsg request) {
            return futureUnaryCall(
                    getChannel().newCall(getHelloWechatMethod(), getCallOptions()), request);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final WechatImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(WechatImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_HELLO_WECHAT:
                    serviceImpl.helloWechat((WechatMsg) request,
                            (io.grpc.stub.StreamObserver<WechatMsg>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    private static abstract class WechatBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        WechatBaseDescriptorSupplier() {
        }

        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return WechatProto.getDescriptor();
        }

        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("Wechat");
        }
    }

    private static final class WechatFileDescriptorSupplier
            extends WechatBaseDescriptorSupplier {
        WechatFileDescriptorSupplier() {
        }
    }

    private static final class WechatMethodDescriptorSupplier
            extends WechatBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        WechatMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }
}
