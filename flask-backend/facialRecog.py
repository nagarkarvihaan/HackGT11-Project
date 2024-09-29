from flask import Flask, request, jsonify
import base64
import numpy as np
import cv2
from deepface import DeepFace
import os

app = Flask(__name__)

# Function to extract the person's name from the file path
def get_person_name(file_path):
    return os.path.basename(os.path.dirname(file_path))

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

        # Use DeepFace to analyze the image and compare it against the dataset
        result_list = DeepFace.find(img_path=img, db_path="/Users/adilhusain/Desktop/Dataset", model_name='VGG-Face')

        # Get the first DataFrame from the result list
        if isinstance(result_list, list) and len(result_list) > 0:
            result = result_list[0]  # Access the first DataFrame
        else:
            return jsonify({"message": "Sorry, I do not know who is there."}), 404

        # Set a threshold for recognition
        threshold = 0.4
        matches = result[result['distance'] <= threshold]

        # Check for matches and return the most likely person's name
        if len(matches) > 0:
            # Get the row with the smallest distance
            best_match = matches.iloc[0]
            person_name = get_person_name(best_match['identity'])
            return jsonify({"person": person_name}), 200
        else:
            return jsonify({"message": "Sorry, I do not know who is there."}), 404

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)