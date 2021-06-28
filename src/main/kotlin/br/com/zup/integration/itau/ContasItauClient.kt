package br.com.zup.integration.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface ContasItauClient {

    @Get(value = "/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ContaClientResponse>
}