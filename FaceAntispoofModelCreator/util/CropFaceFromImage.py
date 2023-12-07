import numpy as np
import argparse
import cv2
import os
import glob
from PIL import Image

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input folder")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")

args = vars(ap.parse_args())
saved = 0

print("[INFO] loading face detector...")
faceDetector = './face_detector/'
confidace = 0.9
print("[INFO] loading face detector...")
protoPath = os.path.sep.join([faceDetector, "deploy.prototxt"])
modelPath = os.path.sep.join([faceDetector,
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)

imageSource = args["input"]
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
        print(file + " confidence : " + str(confidence))

        #copy detected face to othe folder
        if confidence > confidace:
            # compute the (x, y)-coordinates of the bounding box for
            # the face and extract the face ROI
            box = detections[0, 0, i, 3:7] * np.array([width, height, width, height])
            (startX, startY, endX, endY) = box.astype("int")
            face = img[startY:endY, startX:endX]

            # write the frame to disk
            p = os.path.sep.join([args["output"],
                                  "bf{}.png".format(saved)])

            # print(type(_face))
            if face.size != 0:
                cv2.imwrite(p, face)
                saved += 1
                print("[INFO] saved {} to disk".format(p))
