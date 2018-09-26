package com.tutsplus.stitchtutorial

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.core.StitchAppClient
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential
import kotlinx.android.synthetic.main.activity_main.*
import org.bson.Document
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Stitch.initializeDefaultAppClient(
                resources.getString(R.string.my_app_id)
        )

        val stitchAppClient = Stitch.getDefaultAppClient()

        stitchAppClient.auth.loginWithCredential(AnonymousCredential())
                .addOnSuccessListener {
                    val mongoClient = stitchAppClient.getServiceClient(
                            RemoteMongoClient.factory,
                            "mongodb-atlas"
                    )

                    val myCollection = mongoClient.getDatabase("test")
                            .getCollection("my_collection")

                    val myFirstDocument = Document()
                    myFirstDocument["time"] = Date().time
                    myFirstDocument["user_id"] = it.id

                    myCollection.insertOne(myFirstDocument)
                            .addOnSuccessListener {
                                Log.d("STITCH", "One document inserted")
                            }

                    val query = myCollection.find()
                            .sort( Document("time", -1) )
                            .limit(5)

                    val result = mutableListOf<Document>()
                    query.into(result).addOnSuccessListener {
                        val output = StringBuilder("You opened this app: \n\n")

                        // Loop through the results
                        result.forEach {
                            output.append(
                                DateUtils.getRelativeDateTimeString(
                                        this@MainActivity,
                                        it["time"] as Long, // Convert the value of the 'time' key
                                        DateUtils.SECOND_IN_MILLIS,     // to a Long
                                        DateUtils.WEEK_IN_MILLIS,
                                        0
                                )
                            ).append("\n")
                        }

                        // Update the TextView
                        viewer.text = output
                    }
                }
    }
}
