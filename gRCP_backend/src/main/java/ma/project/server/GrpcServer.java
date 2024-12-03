package ma.project.server;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import ma.project.services.BankGrpcService;
import java.io.IOException;


public class GrpcServer {
    public static void main(String[] args) throws IOException,
            InterruptedException {
        Server server = ServerBuilder.forPort(5555)
                .addService(new BankGrpcService())
                .build();
        System.out.println("Server started on port 5555");
        server.start();
        server.awaitTermination();
    }
}
