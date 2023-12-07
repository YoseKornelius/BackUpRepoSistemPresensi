import tensorflow

import pandas as pd
import numpy as np
import os
import keras
import random
import cv2
import math
import seaborn as sns

from sklearn.metrics import confusion_matrix
from sklearn.preprocessing import LabelBinarizer
from sklearn.model_selection import train_test_split

import matplotlib.pyplot as plt

from tensorflow.keras.layers import Dense,GlobalAveragePooling2D,Convolution2D,BatchNormalization
from tensorflow.keras.layers import Flatten,MaxPooling2D,Dropout

from tensorflow.keras.applications import DenseNet121
from tensorflow.keras.applications.densenet import preprocess_input

from tensorflow.keras.preprocessing import image
from tensorflow.keras.preprocessing.image import ImageDataGenerator,img_to_array

from tensorflow.keras.models import Model

from tensorflow.keras.optimizers import Adam

from tensorflow.keras.callbacks import ModelCheckpoint, ReduceLROnPlateau

import warnings

# https://www.pluralsight.com/guides/introduction-to-densenet-with-tensorflow

class DenseLivenessNet:
    @staticmethod
    def build(width, height, depth):
        model_d = DenseNet121(weights='imagenet', include_top=False, input_shape=(width, height, depth))
        x = model_d.output

        x = GlobalAveragePooling2D()(x)
        x = BatchNormalization()(x)
        x = Dropout(0.5)(x)
        x = Dense(1024, activation='relu')(x)
        x = Dense(512, activation='relu')(x)
        x = BatchNormalization()(x)
        x = Dropout(0.5)(x)

        preds = Dense(2, activation='softmax')(x)  # FC-layer
        model = Model(inputs=model_d.input, outputs=preds)
        # model.summary()

        # for layer in model.layers[:-8]:
        #     layer.trainable = False
        #
        # for layer in model.layers[-8:]:
        #     layer.trainable = True

        return model
#
# model_d = DenseNet121(weights='imagenet', include_top=False, input_shape=(112, 112, 3))
# x = model_d.output
#
# x = GlobalAveragePooling2D()(x)
# x = BatchNormalization()(x)
# x = Dropout(0.5)(x)
# x = Dense(1024, activation='relu')(x)
# x = Dense(512, activation='relu')(x)
# x = BatchNormalization()(x)
# x = Dropout(0.5)(x)
#
# preds = Dense(8, activation='softmax')(x)  # FC-layer
# model = Model(inputs=model_d.input, outputs=preds)
# model.summary()
#
# for layer in model.layers[:-8]:
#     layer.trainable=False
#
# for layer in model.layers[-8:]:
#     layer.trainable=True
#
# model.compile(optimizer='Adam',loss='categorical_crossentropy',metrics=['accuracy'])
# model.summary()