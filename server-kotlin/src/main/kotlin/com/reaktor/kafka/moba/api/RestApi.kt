package com.reaktor.kafka.moba.api

import com.reaktor.kafka.moba.model.GameState
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import java.net.SocketException
import javax.ws.rs.*
import javax.ws.rs.client.Client
import javax.ws.rs.core.MediaType
import javax.ws.rs.client.ClientBuilder
import org.glassfish.jersey.jackson.JacksonFeature;
import java.io.PrintWriter
import java.io.StringWriter
import java.util.stream.Collectors
import javax.ws.rs.core.Response

@Path("api")
class RestApi(
    private val streams: KafkaStreams,
    private val host: String,
    private val port: Int
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    fun hello(): Response {
        return Response.ok("hi there!").build()
    }

    @GET
    @Path("/games/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun game(@PathParam("gameId") gameId: Int): Response {
        val stores = streams.allMetadata().map {
                metadata -> metadata.stateStoreNames()
        }.stream().collect(Collectors.toList())

        val keyValueStore = streams.store(stores.first().first(), QueryableStoreTypes.keyValueStore<Int, GameState>())
        val gameState = keyValueStore.get(gameId)
        if (gameState == null ) return Response.status(Response.Status.NOT_FOUND).entity(GameState()).build()
        else return Response.ok(gameState).build()

        return Response.ok().build()
    }


    @GET
    @Path("/games/")
    @Produces(MediaType.APPLICATION_JSON)
    fun allGames(): Response {
        val stores = streams.allMetadata().map {
            metadata -> metadata.stateStoreNames()
        }.stream().collect(Collectors.toList())

        val store = streams.store(stores.first().first(), QueryableStoreTypes.keyValueStore<String, GameState>())
        val range = store.all()
        val games = mutableListOf<String>()
        range.forEach { games.add(it.key) }
        range.close()
        return Response.ok(games).build()

        return Response.ok().build()
    }

    fun start():() -> Unit {
        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = "/"

        val jettyServer = Server()
        jettyServer.handler = context

        val rc = ResourceConfig()
        rc.register(this)
        rc.register(JacksonFeature::class.java)

        val sc = ServletContainer(rc)
        val holder = ServletHolder(sc)
        context.addServlet(holder, "/*")

        val connector = ServerConnector(jettyServer)
        connector.host = host
        connector.port = port
        jettyServer.addConnector(connector)

        context.start()
        try {
            println(">>>> Server started in $host:$port")
            jettyServer.start()
        } catch (exception: SocketException) {
            println("ERROR: address unavailable $host:$port")
            throw Exception(exception.toString())
        }
        return {
            println("stopping jetty server")
            jettyServer.stop()
        }
    }
}