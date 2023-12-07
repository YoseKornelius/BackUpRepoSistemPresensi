import tensorflow as tf
# https://towardsdatascience.com/installing-tensorflow-with-cuda-cudnn-and-gpu-support-on-windows-10-60693e46e781
print("Num GPUs Available: ", len(tf.config.list_physical_devices('GPU')))
print("is_built_with_cuda : ", tf.test.is_built_with_cuda())
