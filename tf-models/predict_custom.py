import tensorflow as tf
import numpy as np
from PIL import Image
import matplotlib.pyplot as plt

# Load saved model
model = tf.keras.models.load_model('mnist_model.h5')

def predict_custom(image_path):
    # Preprocess image
    img = Image.open(image_path).convert('L')  # Convert to grayscale
    img = img.resize((28, 28))                # Resize to MNIST dimensions
    img_array = 1 - (np.array(img) / 255.0)   # Invert colors (black background)
    img_array = np.expand_dims(img_array, axis=0)  # Add batch dimension
    
    # Predict
    prediction = model.predict(img_array)
    predicted_digit = np.argmax(prediction)
    
    # Display
    plt.imshow(img_array[0], cmap='gray')
    plt.title(f"Predicted: {predicted_digit}")
    plt.show()
    return predicted_digit

# Predict your images
print("Prediction:", predict_custom('test_images/digit_2.png'))
