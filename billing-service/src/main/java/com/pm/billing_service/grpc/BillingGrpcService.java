package com.pm.billing_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(
            BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
            StreamObserver<BillingResponse> responseObserver) {
        logger.info("CreateBillingAccount request received {}", billingRequest.toString());
        // business logic to create billing account
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
