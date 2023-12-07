# USAGE
# python train_liveness.py -dtrain replay-mobile224\database\train -dvalidation replay-mobile224\database\devel -m model -l le.pickle

# set the matplotlib backend so figures can be saved in the background
from collections import Counter

import matplotlib
from keras.optimizers import Adam

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
# from imutils import paths
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
ap.add_argument("-m", "--model", type=str, required=True,
                help="path to trained model")
ap.add_argument("-iwidth", "--img_width", type=int, required=True,
                help="image input width")
ap.add_argument("-iheight", "--img_height", type=int, required=True,
                help="image input height")
ap.add_argument("-l", "--le", type=str, required=True,
                help="path to label encoder")
ap.add_argument("-p", "--plot", type=str, default="plot.png",
                help="path to output loss/accuracy plot")
args = vars(ap.parse_args())

# initialize the initial learning rate, batch size, and number of
# epochs to train for
INIT_LR = 1e-4  # 0.0001
# Mini-batch sizes, commonly called “batch sizes” for brevity, are often tuned to an aspect of the computational architecture
# on which the implementation is being executed. Such as a power of two that fits the memory requirements of the GPU or
# CPU hardware like 32, 64, 128, 256, and so on.
BS = 16
EPOCHS = 8

# grab the list of images in our dataset directory, then initialize
# the list of data (i.e., images) and class images

img_width = args["img_width"]
img_height = args["img_height"]

print("[INFO] loading train_dataset-----")
trainDatasetPath = args["train_dataset"]
train_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)
train_generator = train_datagen.flow_from_directory(trainDatasetPath,
                                                    target_size=(img_width, img_height),
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
                                                              target_size=(img_width, img_height),
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

model = LivenessMobileNet.build(width=img_width, height=img_height, depth=3, experiment=4)
model.compile(loss="binary_crossentropy", optimizer=opt, metrics=["accuracy"])
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
print("[INFO] serializing network to '{}'...".format(args["model"]))
model.save(args["model"])
model.save("liveness.h5")

# save the label encoder to disk
labels = dict((v, k) for k, v in train_classes.items())
print("The classes is : ", labels)
with open("label.txt", 'w') as f:
    for value in labels.values():
        f.write('{}\n'.format(value))

f = open(args["le"], "wb")
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
plt.savefig(args["plot"])
