# USAGE
# python CaptureFaceFromVideoToImage.py -i \replay-mobile\train\attack\ -o \replay-mobile\train\attackImg224\ -width 224 -height 224 -s 16 -f 0

# import the necessary packages
import uuid
from pathlib import Path

import numpy as np
import argparse
import cv2
import os

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input video")
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

# load our serialized face detector from disk
print("[INFO] loading face detector...")
faceDetector = '../face_detector/'
confidenceThreshold = 0.5
protoPath = os.path.sep.join([faceDetector, "deploy.prototxt"])
modelPath = os.path.sep.join([faceDetector,
                              "res10_300x300_ssd_iter_140000.caffemodel"])
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)

# open a pointer to the video file stream and initialize the total
# number of frames read and saved thus far

videoSource = args["input"]
videoExtension = [".3g2", ".3gp", ".asf", ".asx", ".avi", ".flv", ".m2ts", ".mkv", ".mov", ".mp4", ".mpg", ".mpeg", ".rm", ".swf", ".vob", ".wmv"]
# loop over files
for file in os.listdir(videoSource):
    read = 0
    saved = 0
    if file.endswith(tuple(videoExtension)):
        path = os.path.join(videoSource, file)
        # imagePrefix = Path(path).stem
        #create uniqName
        imagePrefix = str(uuid.uuid4())[:8]
        vs = cv2.VideoCapture(path)
        # loop over frames from the video file stream
        while True:
            # grab the frame from the file
            (grabbed, frame) = vs.read()

            # if the frame was not grabbed, then we have reached the end
            # of the stream
            if not grabbed:
                break

            # increment the total number of frames read thus far
            read += 1

            # check to see if we should process this frame
            if read % args["skip"] != 0:
                continue

            # grab the frame dimensions and construct a blob from the frame
            (h, w) = frame.shape[:2]
            blob = cv2.dnn.blobFromImage(cv2.resize(frame, (300, 300)), 1.0,
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
                    face = frame[startY:endY, newStartX:newEndX]

                    # write the frame to disk
                    p = os.path.sep.join([args["output"],
                                          "{}.png".format(imagePrefix + "_" + str(saved))])
                    if args["flip"] != 0:
                        _face = cv2.flip(face, 0)
                    else:
                        _face = face

                    if face.size != 0:
                        resized = cv2.resize(_face, dim, interpolation=cv2.INTER_AREA)
                        cv2.imwrite(p, resized)
                        saved += 1
                        print("[INFO] saved {} to disk".format(p))

# do a bit of cleanup
vs.release()
cv2.destroyAllWindows()
