import numpy as np
import argparse
import cv2
import os
import glob
import shutil
from PIL import Image

ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input folder")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")

args = vars(ap.parse_args())

imageSource = args["input"]
outputDir = args["output"]
print("image folder : " + imageSource)
for file in glob.glob(imageSource):
    # print(file)
    filename = os.path.basename(file)
    img = Image.open(file, "r")
    width, height = img.size

    # img = Image.open(file, "r")
    # width = img.shape[1]
    # height = img.shape[0]
    filename = os.path.basename(file)
    img.close()
    if (width < 128 or height < 128):
        print(width, height)
        newpath = os.path.sep.join([outputDir, filename])
        print(newpath)
        shutil.move(file, newpath)
