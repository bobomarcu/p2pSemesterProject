package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.*;

@GrpcService
public class ShardServiceImpl extends P2PServiceGrpc.P2PServiceImplBase {

    private final ShardStorageService shardStorageService;

    public ShardServiceImpl(ShardStorageService shardStorageService) {
        this.shardStorageService = shardStorageService;
    }

    @Override
    public void storeShard(ShardRequest request, StreamObserver<ShardResponse> responseObserver) {
        try {
            shardStorageService.saveShard(
                    request.getShardId(),
                    request.getDocumentId(),
                    request.getOwnerId(),
                    request.getContent().toByteArray()
            );
            
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Stored")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getShard(ShardId request, StreamObserver<ShardResponse> responseObserver) {
        try {
            byte[] content = shardStorageService.getShard(
                    request.getShardId(),
                    request.getDocumentId(),
                    request.getOwnerId()
            );
            
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(true)
                    .setContent(ByteString.copyFrom(content))
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteShard(ShardId request, StreamObserver<ShardResponse> responseObserver) {
        try {
            boolean deleted = shardStorageService.deleteShard(
                    request.getShardId(),
                    request.getDocumentId(),
                    request.getOwnerId()
            );
            
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(deleted)
                    .setMessage(deleted ? "Deleted" : "Not Found")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ShardResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}
