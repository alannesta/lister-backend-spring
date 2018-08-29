package fetcher.services;

import io.grpc.stub.StreamObserver;

public class AnalyticsRPCServiceImpl extends AnalyticsRPCServiceGrpc.AnalyticsRPCServiceImplBase {

    public void getUserFavoriteReport(Empty request, StreamObserver<UserWatchRecords> responseObserver) {
        // return UserWatchRecord.newBuilder().setDateSpan(12L).build();
    }
}
