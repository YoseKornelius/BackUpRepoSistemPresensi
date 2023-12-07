# USAGE
# python retrain_liveness.py -dtrain record-mpad_224px\splitted\train -dvalidation record-mpad_224px\splitted\validation -imodel LivenessDetectionWorkBench\folder_h5\MobileNetv2_BS_16\h5_replaymobile_eksperimen_v4\retrain1_llc_fsad\liveness.h5 -omodel \retrain2_recod_mpad\epoch3\ -tepoch 3 -batchsize 16

# set the matplotlib backend so figures can be saved in the background
from collections import Counter

import matplotlib
from keras.optimizers import Adam
from keras.models import load_model

matplotlib.use("Agg")

# import the necessary packages
# from pyimagesearch.livenessnet import LivenessNet
# from densenet.Densenet import DenseLivenessNet
from classifier.LivenessMobileNet import LivenessMobileNet
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
from keras.preprocessing.image import ImageDataGenerator

from tensorflow.python.keras.utils import np_utils
from keras.applications.mobilenet import preprocess_input
from imutils import paths
import matplotlib.pyplot as plt
import numpy as np
import argparse
import pickle
# import cv2
import os
import tensorflow as tf

print("Num GPUs Available: ", len(tf.config.list_physical_devices('GPU')))

# construct the argument parser and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-dtrain", "--train_dataset", required=True,
                help="path to input train dataset")
ap.add_argument("-dvalidation", "--validation_dataset", required=True,
                help="path to input validation dataset")
ap.add_argument("-imodel", "--input_model", type=str, required=True,
                help="path to retrained model")
ap.add_argument("-omodel", "--output_model", type=str, required=True,
                help="path to retrained model output")
ap.add_argument("-tepoch", "--training_epoch", type=int, required=True,
                help="Training Epoch")
ap.add_argument("-batchsize", "--batchsize", type=int, required=True,
                help="batch size")
args = vars(ap.parse_args())

# initialize the initial learning rate, batch size, and number of
# epochs to train for
INIT_LR = 1e-4  # 0.0001
# Mini-batch sizes, commonly called “batch sizes” for brevity, are often tuned to an aspect of the computational architecture
# on which the implementation is being executed. Such as a power of two that fits the memory requirements of the GPU or
# CPU hardware like 32, 64, 128, 256, and so on.
BS = args["batchsize"]
EPOCHS = args["training_epoch"]

# grab the list of images in our dataset directory, then initialize
# the list of data (i.e., images) and class images

print("[INFO] loading train_dataset-----")
trainDatasetPath = args["train_dataset"]
train_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)
train_generator = train_datagen.flow_from_directory(trainDatasetPath,
                                                    target_size=(224, 224),
                                                    color_mode='rgb',
                                                    batch_size=BS,
                                                    class_mode='binary',
                                                    shuffle=True)
train_classes = train_generator.class_indices
numClass = len(train_classes)
print("Train Data classes is : ", train_classes)
print("Train Data num of class : ", numClass)
# https://stackoverflow.com/questions/65632501/keras-flow-from-directory-how-to-get-number-of-samples-in-each-category
print("Train Data class mapping : ", Counter(train_generator.classes).items())

# train_generator.labels
print("[INFO] loading validation dataset-----")
validationDatasetPath = args["validation_dataset"]
validation_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)
validation_generator = validation_datagen.flow_from_directory(validationDatasetPath,
                                                              target_size=(224, 224),
                                                              color_mode='rgb',
                                                              batch_size=BS,
                                                              class_mode='binary',
                                                              shuffle=False)

print("Validation Data classes is : ", validation_generator.class_indices)
print("Validation Data num of class : ", len(validation_generator.class_indices))
print("Validation Data class mapping : ", Counter(validation_generator.classes).items())

# initialize the optimizer and model #width and height changed from 32 to 112
print("[INFO] compiling model...")
opt = Adam(learning_rate=INIT_LR, decay=INIT_LR / EPOCHS)

modelsrc = args["input_model"]
# load model
model = load_model(modelsrc)
# model.compile(optimizer='Adam',loss='categorical_crossentropy',metrics=['accuracy'])
model.summary()

# train the network
print("[INFO] training network for {} epochs...".format(EPOCHS))
trainHistory = model.fit(train_generator,
                         steps_per_epoch=(train_generator.n // train_generator.batch_size),
                         epochs=EPOCHS,
                         validation_data=validation_generator)
# evaluate the network
print("[INFO] evaluating network...")
nb_samples = np.ceil(len(validation_generator.filenames) / BS)
predictions = model.predict(validation_generator, batch_size=nb_samples, verbose=1)
pred_data = np.where(predictions > 0.5, 1, 0)
test_data = validation_generator.labels

print(classification_report(test_data, pred_data, target_names=validation_generator.class_indices))

# save the network to disk
print("[INFO] serializing network to '{}'...".format(args["output_model"]))
model.save(args["output_model"] + "model")
model.save(args["output_model"] + "liveness.h5")

# save the label encoder to disk
labels = dict((v, k) for k, v in train_classes.items())
print("The classes is : ", labels)
with open(args["output_model"] + "label.txt", 'w') as f:
    for value in labels.values():
        f.write('{}\n'.format(value))

f = open(args["output_model"] + "le.pickle", "wb")
f.write(pickle.dumps(labels))
f.close()

# plot the training loss and accuracy
plt.style.use("ggplot")
plt.figure()
plt.plot(np.arange(0, EPOCHS), trainHistory.history["loss"], label="train_loss")
plt.plot(np.arange(0, EPOCHS), trainHistory.history["val_loss"], label="val_loss")
plt.plot(np.arange(0, EPOCHS), trainHistory.history["accuracy"], label="train_accuracy")
plt.plot(np.arange(0, EPOCHS), trainHistory.history["val_accuracy"], label="val_accuracy")
plt.title("Training Loss and Accuracy on Dataset")
plt.xlabel("Epoch #")
plt.ylabel("Loss/Accuracy")
plt.legend(loc="lower left")
plt.savefig(args["output_model"] + "plot.png")
