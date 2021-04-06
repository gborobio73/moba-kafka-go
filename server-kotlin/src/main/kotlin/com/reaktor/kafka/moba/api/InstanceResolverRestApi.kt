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
import javax.ws.rs.core.Response

@Path("api")
class InstanceResolverRestApi(
    private val streams: KafkaStreams,
    private val storeName: String,
    private val host: String,
    private val port: Int
) {

    private val client: Client = ClientBuilder.newBuilder().register(JacksonFeature::class.java).build()

    private fun withErrorHandling(f: () ->Response ): Response {
        return try {
            f()
        } catch (e: Throwable) {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            println("ERROR: ${e.localizedMessage}\nStackTrace: $sw")
            Response.serverError().entity(e.localizedMessage).build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    fun hello(): Response {
        return withErrorHandling { Response.ok("hi there!").build() }
    }

    @GET
    @Path("/games/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun game(@PathParam("gameId") gameId: Int): Response {
        return withErrorHandling {
            val hostStoreInfo = streamsMetadataForStoreAndKey(streams, storeName, gameId)
            if (!thisHost(hostStoreInfo)) {
                println(">>>> redirecting query to $hostStoreInfo")
                return@withErrorHandling fetchByKey(hostStoreInfo, "api/games/$gameId");
            }
            println(">>>> querying from local store")
            val keyValueStore = streams.store(storeName, QueryableStoreTypes.keyValueStore<Int, GameState>())
            val gameState = keyValueStore.get(gameId)
            println(">>>> gameState $gameState")
            if (gameState == null ) Response.status(Response.Status.NOT_FOUND).entity(GameState()).build()
            else Response.ok(gameState).build()
        }
    }

    private fun fetchByKey(host: HostStoreInfo, path: String): Response {
        val stage = client
            .target(String.format("http://%s:%d", host.host, host.port))
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .rx()
            .get()
        val response = stage.toCompletableFuture().get()
        val state = response.readEntity(GameState::class.java)

        return Response.status(response.status).entity(state).build()
    }

    private fun thisHost(host: HostStoreInfo): Boolean {
        return host.host == this.host && host.port == this.port
    }

    @GET
    @Path("/games")
    @Produces(MediaType.APPLICATION_JSON)
    fun allGames(): Response {
        return withErrorHandling {
            val keyValueStore = streams.store(storeName, QueryableStoreTypes.keyValueStore<String, GameState>())
            val games = mutableListOf<String>()
            val range = keyValueStore.all()
            range.forEach {
                games.add(it.key)
            }
            range.close()
            Response.ok(games).build()
        }
    }

    @GET
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
    fun streamsMetadataForStore(): Response {
        return withErrorHandling {
            val metadata = streamsAllMetadata(streams)
            Response.ok(metadata).build()
        }
    }

    @GET
    @Path("/instances/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMetadata(
        @PathParam("key") key: Int
    ): Response {
        return withErrorHandling {
            Response.ok(
                streamsMetadataForStoreAndKey(
                    streams,
                    storeName,
                    key
                )
            ).build()
        }
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