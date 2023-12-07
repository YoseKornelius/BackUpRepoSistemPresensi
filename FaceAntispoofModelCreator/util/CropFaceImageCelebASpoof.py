import numpy as np
import argparse
import cv2
import os
import glob
from PIL import Image
from fnmatch import fnmatch


def detectFacesAndSave(listFileImage=[], OutputFolder=""):
    saved = 0
    for file in listFileImage:
        print(file)
        img = cv2.imread(file, cv2.IMREAD_UNCHANGED)

        width = img.shape[1]
        height = img.shape[0]

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
            print(file + " confidance : " + str(confidence))
            if confidence > 0.9:
                # compute the (x, y)-coordinates of the bounding box for
                # the face and extract the face ROI(Region of interest)
                box = detections[0, 0, i, 3:7] * np.array([width, height, width, height])
                (startX, startY, endX, endY) = box.astype("int")
                face = img[startY:endY, startX:endX]
                # roiWidth = endX - startX
                # roiHeight = endY - startY
                # face = cv2.resize(face, (112, 112))  # changed from 32 to 112
                # write the frame to disk
                p = os.path.sep.join([OutputFolder, "sp{}.png".format(saved)])
                if args["flip"] != 0:
                    _face = cv2.flip(face, 0)
                else:
                    _face = face
                # print(type(_face))
                if _face.size != 0:
                    cv2.imwrite(p, _face)
                    saved += 1
                    print("[INFO] saved {} to disk".format(p))


# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input folder")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")
ap.add_argument("-d", "--detector", type=str, required=True,
                help="path to OpenCV's deep learning face detector")
ap.add_argument("-c", "--confidence", type=float, default=0.5,
                help="minimum probability to filter weak detections")
ap.add_argument("-s", "--skip", type=int, default=16,
                help="# of frames to skip before applying face detection")
ap.add_argument("-f", "--flip", type=int, default=0,
                help="# flip cropped faces")
args = vars(ap.parse_args())
saved = 0

print("[INFO] loading face detector...")
protoPath = os.path.sep.join([args["detector"], "deploy.prototxt"])
modelPath = os.path.sep.join([args["detector"],
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)

imageSource = args["input"]
outputFolder = args["output"]
print("image folder : " + imageSource)
imagesFiles = []
# for file in glob.glob("*.txt"):
#     txtfiles.append(file)

print("create output folder")
realOutputFolder = os.path.join(outputFolder, "real")
fakeOutputFolder = os.path.join(outputFolder, "fake")
if (os.path.isdir(realOutputFolder) == False):
    os.mkdir(realOutputFolder)
if (os.path.isdir(fakeOutputFolder) == False):
    os.mkdir(fakeOutputFolder)

pattern = "*.jpg"
listLiveImage = []
listSpoofImage = []
# get all image file from CelebAspoof folder
for path, subdirs, files in os.walk(imageSource):
    for name in files:
        if fnmatch(name, pattern):
            print(path)
            # print(os.path.basename(path))
            if (os.path.basename(path) == 'spoof'):
                listSpoofImage.append(os.path.join(path, name))
            elif (os.path.basename(path) == 'live'):
                listLiveImage.append(os.path.join(path, name))

# print("SpoofIMage")
# print(listSpoofImage)
print("Live images")
# print(listLiveImage)
detectFacesAndSave(listLiveImage, realOutputFolder)
detectFacesAndSave(listSpoofImage, fakeOutputFolder)

# for file in listLiveImage:
#     print(file)
#     img = cv2.imread(file, cv2.IMREAD_UNCHANGED)
#
#     width = img.shape[1]
#     height = img.shape[0]
#
#     blob = cv2.dnn.blobFromImage(cv2.resize(img, (300, 300)), 1.0,
#                                  (300, 300), (104.0, 177.0, 123.0))
#
#     # pass the blob through the network and obtain the detections and
#     # predictions
#     net.setInput(blob)
#     detections = net.forward()
#
#     # ensure at least one face was found
#     if len(detections) > 0:
#         # we're making the assumption that each image has only ONE
#         # face, so find the bounding box with the largest probability
#         i = np.argmax(detections[0, 0, :, 2])
#         confidence = detections[0, 0, i, 2]
#         # ensure that the detection with the largest probability also
#         # means our minimum probability test (thus helping filter out
#         # weak detections)
#         print(file + " confidance : " + str(confidence))
#         if confidence > 0.9:
#             # compute the (x, y)-coordinates of the bounding box for
#             # the face and extract the face ROI(Region of interest)
#             box = detections[0, 0, i, 3:7] * np.array([width, height, width, height])
#             (startX, startY, endX, endY) = box.astype("int")
#             face = img[startY:endY, startX:endX]
#             # roiWidth = endX - startX
#             # roiHeight = endY - startY
#             face = cv2.resize(face, (112, 112))  # changed from 32 to 112
#             # write the frame to disk
#             p = os.path.sep.join([liveOutputFolder, "sp{}.png".format(saved)])
#             if args["flip"] != 0:
#                 _face = cv2.flip(face, 0)
#             else:
#                 _face = face
#             # print(type(_face))
#             if _face.size != 0:
#                 cv2.imwrite(p, _face)
#                 saved += 1
#                 print("[INFO] saved {} to disk".format(p))
