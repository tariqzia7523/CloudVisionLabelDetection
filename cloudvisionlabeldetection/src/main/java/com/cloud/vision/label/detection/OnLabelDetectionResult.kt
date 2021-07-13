package com.cloud.vision.label.detection

interface OnLabelDetectionResult {
    fun onResult(result: String?)
    fun onFailure()
}