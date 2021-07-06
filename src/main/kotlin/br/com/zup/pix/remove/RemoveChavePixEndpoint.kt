package br.com.zup.pix.remove

import br.com.zup.KeymanagerRemoveServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.RemoveChavePixResponse
import br.com.zup.shared.errors.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoveChavePixEndpoint(
    @Inject private val service: RemoveChavePixService
): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceImplBase(){

    override fun remove(request: RemoveChavePixRequest,
                        responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        service.remove(pixId = request.pixId, clienteId= request.clienteId)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build())
        responseObserver?.onCompleted()

    }


}