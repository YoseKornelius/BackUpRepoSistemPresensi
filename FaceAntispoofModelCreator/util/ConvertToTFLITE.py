import tensorflow as tf
from keras.models import load_model
import argparse
from keras.preprocessing.image import ImageDataGenerator
from keras.applications.mobilenet import preprocess_input
import os

# https://www.tensorflow.org/tutorials/keras/save_and_load

# saved_model_dir = "../model"
# new_model = load_model(saved_model_dir)
from keras.models import load_model
import tensorflow as tf

ap = argparse.ArgumentParser()

ap.add_argument("-m", "--model", type=str, required=True,
                help="path to trained model")

args = vars(ap.parse_args())

modelsrc = args["model"]
# load model
model = load_model(modelsrc)
# Check its architecture
model.summary()

# convert the model
print("----prepare conversion process -----")
# converter = tf.lite.TFLiteConverter.from_saved_model(modelsrc)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model.
print("----Doing conversion -----")
filename = '{0}/liveness.tflite'.format(os.path.dirname(os.path.abspath(modelsrc)))
with open(filename, 'wb') as f:
    f.write(tflite_model)
