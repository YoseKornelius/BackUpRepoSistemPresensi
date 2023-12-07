import shutil

from sklearn.model_selection import train_test_split
from pathlib import Path

from imutils import paths
import argparse
import os

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--dataset", required=True,
                help="path to input dataset")
ap.add_argument("-o", "--output", required=True,
                help="path to output dataset")
ap.add_argument("-l", "--le", type=str, required=True,
                help="path to label encoder")
args = vars(ap.parse_args())


def CopyDatasetFile(srcImagePath, srcImageLabel, destFolder):
    for idx in range(len(srcImagePath)):
        destFolderName = os.path.sep.join([destFolder, srcImageLabel[idx]])
        #check if folder exist
        if not os.path.exists(destFolderName):
            os.mkdir(destFolderName)
        destFileName = os.path.sep.join([destFolderName,
                                         "{}.png".format(Path(srcImagePath[idx]).stem)])
        # print(srcImagePath[idx], " is ", srcImageLabel[idx])
        shutil.copyfile(srcImagePath[idx], destFileName)


imagePaths = list(paths.list_images(args["dataset"]))
# data = []
labels = []
outputPaths = args["output"]

for imagePath in imagePaths:
    # extract the class label from the filename, load the image and
    # resize it to be a fixed 32x32 pixels, ignoring aspect ratio
    label = imagePath.split(os.path.sep)[-2]
    print(label, " : ", imagePath)
    # update the data and labels lists, respectively
    # data.append(imagePath)
    labels.append(label)

# partition the data into training, validation and testing splits
# the data for training 60%, validation 20% and the remaining 20% for testing
# However, one approach to dividing the dataset into train, test, val with 0.6, 0.2, 0.2
# would be to use the train_test_split method twice.
(trainX, testX, trainY, testY) = train_test_split(imagePaths, labels, test_size=0.2, random_state=42)
(trainX, validationX, trainY, validationY) = train_test_split(trainX, trainY, test_size=0.25, random_state=42)

# copy the data into respected dataset
# trainData
print("Copy Train Data")
trainOutputFolder = os.path.sep.join([outputPaths, "{}".format("train")])
if not os.path.exists(trainOutputFolder):
    os.mkdir(trainOutputFolder)
CopyDatasetFile(trainX, trainY, trainOutputFolder)

print("Copy Validation Data")
valOutputFolder = os.path.sep.join([outputPaths, "{}".format("validation")])
if not os.path.exists(valOutputFolder):
    os.mkdir(valOutputFolder)
CopyDatasetFile(validationX, validationY, valOutputFolder)

print("Copy Test Data")
testOutputFolder = os.path.sep.join([outputPaths, "{}".format("test")])
if not os.path.exists(testOutputFolder):
    os.mkdir(testOutputFolder)
CopyDatasetFile(testX, testY, testOutputFolder)
print("EOF")
