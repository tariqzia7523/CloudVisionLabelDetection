package com.cloud.vision.label.detection

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class LabelDetection {
    var context : Context
    var APIKey : String
    var onLabelDetectionResult: OnLabelDetectionResult
    var imageUri : Uri

    constructor(context: Context, APIKey: String, onLabelDetectionResult: OnLabelDetectionResult, imageUri: Uri) {
        this.context = context
        this.APIKey = APIKey
        this.onLabelDetectionResult = onLabelDetectionResult
        this.imageUri = imageUri
        performImageAnalysiz(imageUri)
    }


    fun performImageAnalysiz(uri : Uri){
        val visionBuilder = Vision.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
        visionBuilder.setVisionRequestInitializer(VisionRequestInitializer(APIKey))
        val vision = visionBuilder.build()
        val executor: ExecutorService =  Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val inputStream =context.contentResolver.openInputStream(uri)
            try {
                val photoData = IOUtils.toByteArray(inputStream)
                inputStream!!.close()
                val inputImage = Image()
                inputImage.encodeContent(photoData)
                val desiredFeature = Feature()
                //desiredFeature.setType("FACE_DETECTION");
                desiredFeature.type = "LABEL_DETECTION"
                val request = AnnotateImageRequest()
                request.image = inputImage
                request.features = Arrays.asList(desiredFeature)
                val batchRequest = BatchAnnotateImagesRequest()
                batchRequest.requests = Arrays.asList(request)
                val batchResponse = vision.images().annotate(batchRequest).execute()
                //List<FaceAnnotation> faces = batchResponse.getResponses().get(0).getFaceAnnotations();
                val faces = batchResponse.responses[0].labelAnnotations
                var result = ""
                for (i in faces.indices) {
                    result = result +" "+faces[i].description
                    Log.e("***TAG", faces[i].description)
                }
                handler.post {
                    onLabelDetectionResult.onResult(result)
                }

            } catch (e: IOException) {
                e.printStackTrace()
                handler.post {
                    onLabelDetectionResult.onFailure()
                }
            }
        }
    }



}