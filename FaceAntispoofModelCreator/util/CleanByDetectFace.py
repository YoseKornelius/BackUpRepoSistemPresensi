import numpy as np
import argparse
import cv2
import os
import glob
import shutil

ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input folder")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")

args = vars(ap.parse_args())

faceDetector = './face_detector/'
confidace = 0.9
print("[INFO] loading face detector...")
protoPath = os.path.sep.join([faceDetector, "deploy.prototxt"])
modelPath = os.path.sep.join([faceDetector,
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)

imageSource = args["input"]
outputDir = args["output"]
print("image folder : " + imageSource)
for file in glob.glob(imageSource):

    img = cv2.imread(file, cv2.IMREAD_UNCHANGED)
    width = img.shape[1]
    height = img.shape[0]
    filename = os.path.basename(file)
    blob = cv2.dnn.blobFromImage(cv2.resize(img, (300, 300)), 1.0,
                                 (300, 300), (104.0, 177.0, 123.0))

    # pass the blob through the network and obtain the detections and
    # predictions
    net.setInput(blob)
    detections = net.forward()

    # ensure at least one face was found
    if len(detections) > 0:
        i = np.argmax(detections[0, 0, :, 2])
        confidence = detections[0, 0, i, 2]
        # ensure that the detection with the largest probability also
        # means our minimum probability test (thus helping filter out
        # weak detections)
        # print(file + " confidance : " + str(confidence))
        #remove undetectedFace
        if confidence < confidace:
            newpath = os.path.sep.join([outputDir, filename])
            print(newpath)
            shutil.move(file, newpath)
