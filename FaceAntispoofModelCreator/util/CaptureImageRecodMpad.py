import uuid
from pathlib import Path

import numpy as np
import argparse
import cv2
import os
import glob

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input images")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")
ap.add_argument("-width", "--width", type=int, required=True,
                help="images resize width")
ap.add_argument("-height", "--height", type=int, required=True,
                help="images resize height")
ap.add_argument("-s", "--skip", type=int, default=16,
                help="# of frames to skip before applying face detection")
ap.add_argument("-f", "--flip", type=int, default=0,
                help="# flip cropped faces")
args = vars(ap.parse_args())

width_new = int(args["width"])
height_new = int(args["height"])
dim = (width_new, height_new)

print("[INFO] loading face detector...")
faceDetector = '../face_detector/'
confidenceThreshold = 0.5
protoPath = os.path.sep.join([faceDetector, "deploy.prototxt"])
modelPath = os.path.sep.join([faceDetector,
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)

imageSource = args["input"]


def getListOfFiles(dirName):
    # create a list of file and sub directories
    # names in the given directory
    listOfFile = os.listdir(dirName)
    allFiles = list()
    # Iterate over all the entries
    for entry in listOfFile:
        # Create full path
        fullPath = os.path.join(dirName, entry)
        # If entry is a directory then get the list of files in this directory
        if os.path.isdir(fullPath):
            allFiles = allFiles + getListOfFiles(fullPath)
        else:
            allFiles.append(fullPath)

    return allFiles


listOfFiles = getListOfFiles(imageSource)
saved = 0
# Print the files
for file in listOfFiles:
#    or file.endswith(".png")
    if file.endswith(".jpg") :
        img = cv2.imread(file, cv2.IMREAD_UNCHANGED)
        # create uniqName
        imagePrefix = Path(file).stem
        # imagePrefix = str(uuid.uuid4())[:8]
        (h, w) = img.shape[:2]
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
            if confidence > confidenceThreshold:
                # compute the (x, y)-coordinates of the bounding box for
                # the face and extract the face ROI
                box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
                (startX, startY, endX, endY) = box.astype("int")
                # resize the width of bounding box to make sure the aspect ratio will be same
                boxWidth = endX - startX
                boxHeight = endY - startY
                diff = boxHeight - boxWidth
                newStartX = int(startX - (diff / 2))
                newEndX = int(endX + (diff / 2))
                face = img[startY:endY, newStartX:newEndX]

                # write the frame to disk
                p = os.path.sep.join([args["output"],
                                      "{}.png".format(imagePrefix)])
                if args["flip"] != 0:
                    _face = cv2.flip(face, 0)
                else:
                    _face = face

                if face.size != 0:
                    resized = cv2.resize(_face, dim, interpolation=cv2.INTER_AREA)
                    cv2.imwrite(p, resized)
                    saved += 1
                    print("[INFO] saved {} to disk".format(p))
                    print("{} remaining!".format((len(listOfFiles) - saved)))
