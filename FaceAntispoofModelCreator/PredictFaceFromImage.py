import uuid
from pathlib import Path

import numpy as np
import argparse
import cv2
import os
import glob
from keras.models import load_model

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-m", "--model", type=str, required=True,
                help="path to trained model")
ap.add_argument("-inimage", "--inputimage", type=str, required=True,
                help="path to input image")
ap.add_argument("-width", "--width", type=int, required=True,
                help="images resize width")
ap.add_argument("-height", "--height", type=int, required=True,
                help="images resize height")
ap.add_argument("-f", "--flip", type=int, default=0,
                help="# flip cropped faces")
args = vars(ap.parse_args())

width_new = int(args["width"])
height_new = int(args["height"])
dim = (width_new, height_new)

# load our serialized face detector from disk
print("[INFO] loading face detector...")
faceDetector = './face_detector/'
confidenceThreshold = 0.5
protoPath = os.path.sep.join([faceDetector, "deploy.prototxt"])
modelPath = os.path.sep.join([faceDetector,
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)
model = load_model(args["model"])
imageSource = args["inputimage"]
print("image folder : " + imageSource)
for file in glob.glob(imageSource):

    img = cv2.imread(file, cv2.IMREAD_UNCHANGED)

    # img = Image.open(file, "r")
    width = img.shape[1]
    height = img.shape[0]

    # if (width < 250 or height < 250):
    # grab the frame dimensions and construct a blob from the frame

    blob = cv2.dnn.blobFromImage(cv2.resize(img, (300, 300)), 1.0,
                                 (300, 300), (104.0, 177.0, 123.0))

    # pass the blob through the network and obtain the detections and
    # predictions
    net.setInput(blob)
    detections = net.forward()

    # ensure at least one face was found
    if len(detections) > 0:
        # we're making the assumption that each image has only ONE
        # face, so find the bounding box with the largest probability
        i = np.argmax(detections[0, 0, :, 2])
        confidence = detections[0, 0, i, 2]
        # ensure that the detection with the largest probability also
        # means our minimum probability test (thus helping filter out
        # weak detections)
        # print(file + " confidence : " + str(confidence))
        if confidence > confidenceThreshold:
            # compute the (x, y)-coordinates of the bounding box for
            # the face and extract the face ROI
            box = detections[0, 0, i, 3:7] * np.array([width, height, width, height])
            (startX, startY, endX, endY) = box.astype("int")
            boxWidth = endX - startX
            boxHeight = endY - startY
            diff = boxHeight - boxWidth
            newStartX = int(startX - (diff / 2))
            newEndX = int(endX + (diff / 2))
            face = img[startY:endY, newStartX:newEndX]

            if args["flip"] != 0:
                _face = cv2.flip(face, 0)
            else:
                _face = face
            resized = cv2.resize(_face, dim, interpolation=cv2.INTER_AREA)
            img = resized.reshape(1, 224, 224, 3)
            preds = model.predict(img)

            livenessConf = preds[0, 0]
            print(file, livenessConf)

            # write the frame to disk
            # print(os.listdir(file.__str__()))
            # p = os.path.sep.join(["D:\\_kerja\\customDataset\\real",
            #                       "outSample{}.png".format(1)])

            # print(type(_face))
            # if resized.size != 0:
            # cv2.imwrite(p, resized)
            # saved += 1
            # print("[INFO] saved {} to disk".format(p))
