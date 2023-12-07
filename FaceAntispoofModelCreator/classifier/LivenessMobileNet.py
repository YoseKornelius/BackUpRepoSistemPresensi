# import the necessary packages
from keras.layers import Dense, GlobalAveragePooling2D, Activation, BatchNormalization, Dropout
from keras.models import Model, Sequential

from keras.applications.mobilenet import MobileNet

from keras.applications.mobilenet_v2 import MobileNetV2
from keras.applications.mobilenet_v3 import MobileNetV3
# https://github.com/ferhat00/Deep-Learning/blob/master/Transfer%20Learning%20CNN/Transfer%20Learning%20in%20Keras%20using%20MobileNet.ipynb
# from tensorflow.python.keras.layers import Flatten


class LivenessMobileNet:
    @staticmethod
    def build(width, height, depth, classes=1, experiment=2):
        model = Sequential()

        model.add(MobileNetV2(input_shape=(width, height, depth),
                            weights='imagenet',
                            include_top=False, pooling='avg'))
        # imports the mobilenet model and discards the last 1000 neuron layer.
        # net = MobileNet(input_shape=(width, height, depth),
        #                 weights='imagenet', classes=classes,
        #                 include_top=False)
        # res = net.output
        # model.add(GlobalAveragePooling2D())
        if (experiment == 1):
            # we add dense layers so that the model can learn more complex functions and classify for better results.
            model.add(Dense(1024, activation='relu'))
            # dense layer 2
            model.add(Dense(1024, activation='relu'))
            # dense layer 3
            model.add(Dense(512, activation='relu'))
            # final layer with sigmoid activation
            model.add(Dense(classes, activation='sigmoid'))

        elif (experiment == 2):
            # binary classification activation using sigmoid
            model.add(Dense(classes, activation='sigmoid'))

        elif (experiment == 3):
            # experiment 3 is reducing amount of dense unit to 512, 512 and 64
            model.add(Dense(512, activation='relu'))
            # dense layer 2
            model.add(Dense(512, activation='relu'))
            # dense layer 3
            model.add(Dense(64, activation='relu'))
            # final layer with sigmoid activation
            model.add(Dense(classes, activation='sigmoid'))

        elif (experiment == 4):
            model.add(Dense(64))
            model.add(BatchNormalization())
            model.add(Activation("relu"))
            model.add(Dropout(0.5))

            model.add(Dense(32))
            model.add(BatchNormalization())
            model.add(Activation("relu"))
            model.add(Dropout(0.5))
            # https://machinelearningmastery.com/batch-normalization-for-training-of-deep-neural-networks/
            # stabilizing the learning process and dramatically reducing
            # the number of training epochs required to train deep networks

            # https://towardsdatascience.com/10-minutes-to-building-a-binary-image-classifier-by-applying-transfer-learning-to-mobilenet-eab5a8719525
            # binary classification activation using sigmoid
            model.add(Dense(classes, activation="sigmoid"))
            # This is important: we must set our MobileNet layers’ trainable parameter to False
            # so that we don’t end up training the entire model
            # — we only need to train the last layer!
            model.layers[0].trainable = False
        # add more elif for other experiment

        elif (experiment == 5):
            # experiment 5 is reducing amount of dense unit to 512, 256 and 64
            model.add(Dense(512, activation='relu'))
            # dense layer 2
            model.add(Dense(256, activation='relu'))
            # dense layer 3
            model.add(Dense(64, activation='relu'))
            # final layer with softmax activation
            model.add(Dense(classes, activation='sigmoid'))

        elif (experiment == 6):
            # experiment 5 is reducing amount of dense unit to 512, 256 and 64
            model.add(Dense(1024, activation='relu'))
            # dense layer 2
            model.add(Dense(256, activation='relu'))
            # dense layer 3
            model.add(Dense(256, activation='relu'))
            # final layer with sigmoid activation
            model.add(Dense(classes, activation='sigmoid'))
        # return the constructed network architecture
        return model
