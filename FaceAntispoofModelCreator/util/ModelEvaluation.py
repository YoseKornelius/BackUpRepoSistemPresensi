# script for evaluate model using given dataset
#-m \retrain2_recod_mpad\epoch4\liveness.h5 -dtest \customDataset\testImg224px

import argparse
import itertools

import numpy as np
from sklearn.metrics import classification_report, confusion_matrix, roc_curve, auc, roc_auc_score, \
    ConfusionMatrixDisplay
from keras.preprocessing.image import ImageDataGenerator
import matplotlib
from keras.applications.mobilenet import preprocess_input
from keras.models import load_model
import matplotlib.pyplot as plt
import io

matplotlib.use("Agg")

ap = argparse.ArgumentParser()
ap.add_argument("-m", "--model", type=str, required=True,
                help="path to trained model")
ap.add_argument("-iwidth", "--img_width", type=int, required=True,
                help="image input width")
ap.add_argument("-iheight", "--img_height", type=int, required=True,
                help="image input height")
ap.add_argument("-dtest", "--test_dataset", required=True,
                help="path to input test dataset")

args = vars(ap.parse_args())

BS = 16

img_width = args["img_width"]
img_height = args["img_height"]

def get_model_summary(model):
    stream = io.StringIO()
    model.summary(print_fn=lambda x: stream.write(x + '\n'))
    summary_string = stream.getvalue()
    stream.close()
    return summary_string


testDatasetPath = args["test_dataset"]
test_datagen = ImageDataGenerator(preprocessing_function=preprocess_input)
test_generator = test_datagen.flow_from_directory(testDatasetPath,
                                                  target_size=(img_width, img_height),
                                                  color_mode='rgb',
                                                  batch_size=BS,
                                                  class_mode=None,
                                                  shuffle=False)

modelsrc = args["model"]
# load model
model = load_model(modelsrc)
finalReport = "Model Summary \n{}".format(get_model_summary(model))

filenames = test_generator.filenames
nb_samples = np.ceil(len(filenames) / BS)
predictions = model.predict(test_generator, steps=nb_samples)

# https://androidkt.com/get-class-labels-from-predict-method-in-keras/
# You have predicted class probabilities. Since you are doing binary classification. You have a dense layer consisting
# of one unit with an activation function of the sigmoid. Sigmoid function outputs a value in the range [0,1] which
# corresponds to the probability of the given sample belonging to a positive class (i.e. class one).
# To convert these to class labels you can take a threshold. Everything below 0.5 is labeled with Zero
# (i.e. negative class) and everything above 0.5 is labeled with One. So to find the predicted class you can do the
# following.
pred_label = np.where(predictions > 0.5, 1, 0)
test_label = test_generator.labels
test_classes = test_generator.class_indices
finalReport += "\nClassification Report \n{}".format(classification_report(test_label, pred_label, target_names=test_classes))

# https://colab.research.google.com/drive/1XYAXOHiXNWqmKedCj1WRcJ6mGpr4aVC0?usp=sharing#scrollTo=adIVN0BknWDt
# https://stackoverflow.com/questions/28339746/equal-error-rate-in-python
# far, tar, thresholds = roc_curve(test_generator.classes, predictions)
# far, tar, thresholds = roc_curve(test_label, pred_label)
# frr = 1 - tar
# eer_threshold = thresholds[np.nanargmin(np.absolute((frr - tar)))]
# hter = (frr + far) / 2.0
#
# roc_auc = auc(far, tar)
# score = roc_auc_score(test_label, pred_label)
#
# plt.figure()
# lw = 2
# plt.plot(far, tar, color='darkorange',
#          lw=lw, label='ROC curve (area = %0.2f)' % roc_auc)
# plt.plot([0, 1], [0, 1], color='navy', lw=lw, linestyle='--')
# plt.xlim([0.0, 1.0])
# plt.ylim([0.0, 1.05])
# plt.xlabel('False Acceptance Rate')
# plt.ylabel('True Acceptance Rate')
# plt.title('Receiver operating characteristic example')
# plt.legend(loc="lower right")
# plt.savefig("Model Evaluation.png")
# plt.clf()

# https://coderzcolumn.com/tutorials/machine-learning/model-evaluation-scoring-metrics-scikit-learn-sklearn
conf_mat = confusion_matrix(test_label, pred_label)
tp, fn, fp, tn = conf_mat.ravel()
with plt.style.context(('ggplot', 'seaborn')):
    fig = plt.figure(figsize=(6, 6), num=1)
    plt.imshow(conf_mat, interpolation='nearest', cmap=plt.cm.Blues)
    plt.xticks([0, 1], ['0.Accept', '1.Reject'])
    plt.yticks([0, 1], ['0.Real', '1.Spoof'])
    plt.xlabel('Predicted Label')
    plt.ylabel('Actual Label')
    plt.text(0, 1, "FP = {}".format(fp), horizontalalignment="center", color="red")
    plt.text(1, 0, "FN = {}".format(fn), horizontalalignment="center", color="red")
    plt.text(0, 0, "TP = {}".format(tp), horizontalalignment="center", color="red")
    plt.text(1, 1, "TN = {}".format(tn), horizontalalignment="center", color="red")
    # for i, j in itertools.product(range(conf_mat.shape[0]), range(conf_mat.shape[1])):
    #     plt.text(j, i, conf_mat[i, j], horizontalalignment="center", color="red")
    plt.grid(None)
    plt.title('Confusion Matrix')
    plt.savefig("Confusion Matrix.png")

plt.clf()
disp=ConfusionMatrixDisplay(confusion_matrix=conf_mat,display_labels=test_classes)
disp.plot()
plt.savefig("Confusion Matrix 2.png")


# https://www.recogtech.com/en/knowledge-base/security-level-versus-user-convenience
# If you try to reduce the FAR to the lowest possible level,
# the FRR is likely to rise sharply. In other words, the more secure your access control,
# the less convenient it will be, as users are falsely rejected by the system.
# The same also applies the other way round. Do you want to increase user convenience by reducing the FRR?
# In this case the system is likely to be less secure (higher FAR).
far = fp / (fp + tn)  # False Acceptance Rate
frr = fn / (fn + tp)  # False rejection Rate
hter = (frr + far) / 2.0  # Half Total Error Rate
finalReport += "\n True Negatif = {}".format(round(tn, 2))
finalReport += "\n False Positif = {}".format(round(fp, 2))
finalReport += "\n False Negatif = {}".format(round(fn, 2))
finalReport += "\n True Positif = {}".format(round(tp, 2))

finalReport += "\n FAR = {}".format(round(far, 2))
finalReport += "\n FRR = {}".format(round(frr, 2))
finalReport += "\n HTER = {}".format(round(hter, 2))

print(finalReport)
with open("report.txt", 'w') as f:
    f.write('{}\n'.format(finalReport))
# TBD https://github.com/YuanGongND/python-compute-eer
# https://github.com/mnikitin/Learn-Convolutional-Neural-Network-for-Face-Anti-Spoofing/blob/master/utils/statistics.py

# model.predict() returns the final output of the model, i.e. answer.
# While model.evaluate() returns the loss. The loss is used to train the model (via backpropagation)
# and it is not the answer.

## link (https://stackoverflow.com/questions/69917915/test-h5-model-on-test-dataset)
# result = model.evaluate(test_generator)

# tf.print('Accuracy: ', result[1] * 100)
