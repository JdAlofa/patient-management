package com.pm.billing_service.grpc;


import org.slf4j.Logger;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    @Override
    public void createBillingAccount(BillingRequest billingRequest, 
        StreamObserver<BillingResponse> responseObserver) {
                        Logger.info("createBillingAccount request received{}", billingRequest.toString());

            //business logic to create billing account
            BillingResponse response = BillingResponse.newBuilder()
            .setAccountId("12345")    
            .setStatus("ACTIVE")
            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
}