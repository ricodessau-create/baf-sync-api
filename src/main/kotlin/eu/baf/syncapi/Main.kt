package eu.baf.syncapi

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class SyncRequest(
    val token: String,
    val uuid: String,
    val rank: String
)

fun main() {
    val serviceAccount = File("serviceAccountKey.json").inputStream()

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/api/sync") {
                val request = call.receive<SyncRequest>()
                val db = FirestoreClient.getFirestore()

                val tokenDoc = db.collection("sync_tokens")
                    .document(request.token)
                    .get()
                    .get()

                if (!tokenDoc.exists()) {
                    call.respond(mapOf("success" to false, "error" to "Invalid token"))
                    return@post
                }

                val uid = tokenDoc.getString("uid") ?: run {
                    call.respond(mapOf("success" to false, "error" to "Token invalid"))
                    return@post
                }

                db.collection("users")
                    .document(uid)
                    .update(
                        mapOf(
                            "uuid" to request.uuid,
                            "rank_ingame" to request.rank,
                            "synced" to true
                        )
                    )
                    .get()

                db.collection("sync_tokens")
                    .document(request.token)
                    .delete()

                call.respond(mapOf("success" to true))
            }
        }
    }.start(wait = true)
}
