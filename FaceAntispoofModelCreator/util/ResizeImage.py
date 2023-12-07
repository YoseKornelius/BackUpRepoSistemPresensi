import numpy as np
import argparse
import cv2
import os
import glob
from PIL import Image

#python ResizeImage.py -i E:\_kerja\lcc-fasd-casia\LCC_FASD\LCC_FASD_development\spoof\*.png  -o E:\_kerja\development_224px\spoof\ -width 224 -height 224

# python ResizeImage.py -i /media/user/Dataset/_kerja/lcc-fasd-casia/LCC_FASD/LCC_FASD_development/spoof -o /media/user/Dataset/_kerja/llc-fasd_112/llc_fasd_development/spoof -width 112 -height 112
# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True,
                help="path to input folder")
ap.add_argument("-o", "--output", type=str, required=True,
                help="path to output directory of cropped faces")
ap.add_argument("-width", "--width", type=int, required=True,
                help="resize width")
ap.add_argument("-height", "--height", type=int, required=True,
                help="resize height")

args = vars(ap.parse_args())
saved = 0

imageSource = args["input"]
print("image folder : " + imageSource)

width_new = int(args["width"])
height_new = int(args["height"])
dim = (width_new, height_new)

os.chdir(imageSource)
# for file in glob.glob(imageSource):
# numFile = len([name for name in os.listdir() if os.path.isfile(name)])
# print("numFIle : ", numFile)
for file in os.listdir():
    if os.path.isfile(file):
        img = cv2.imread(file, cv2.IMREAD_UNCHANGED)

        # img = Image.open(file, "r")
        # print(file)
        
        # width = img.shape[1]
        # height = img.shape[0]
        height, width, channels = img.shape

        print('Original Dimensions : ', img.shape)

        # scale_percent = 60  # percent of original size
        # width_new = int(img.shape[1] * scale_percent / 100)
        # height_new = int(img.shape[0] * scale_percent / 100)

        resized = cv2.resize(img, dim, interpolation=cv2.INTER_AREA)

        print('Resized Dimensions : ', resized.shape)
        p = os.path.sep.join([args["output"], "bf{}.png".format(saved)])

        cv2.imwrite(p, resized)
        saved += 1
        print("[INFO] saved {} to disk".format(p))
