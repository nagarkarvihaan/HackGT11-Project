from flask import Flask, request, jsonify
import base64
import numpy as np
import cv2
from deepface import DeepFace

app = Flask(__name__)

# Define the route to handle image analysis
@app.route("/analyze", methods=["POST"])
def analyze():
    try:
        data = request.json
        
        # Check if the image is in the request
        if "image" not in data:
            return jsonify({"error": "No image provided"}), 400

        # Decode the base64 image
        img_data = base64.b64decode(data["image"])
        np_img = np.frombuffer(img_data, np.uint8)
        img = cv2.imdecode(np_img, cv2.IMREAD_COLOR)

        # Use DeepFace to analyze the image and compare it against a dataset
        result = DeepFace.find(img_path=img, db_path="path/to/roboflow-dataset", model_name='VGG-Face')

        # Return the recognized person (if any)
        if len(result) > 0:
            person = result.iloc[0]["identity"]
            return jsonify({"person": person}), 200
        else:
            return jsonify({"error": "No match found"}), 404

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)