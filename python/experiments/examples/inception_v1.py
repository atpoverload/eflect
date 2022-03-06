from datetime import datetime

import tensorflow as tf
import tensorflow_hub as hub


def main():
    # grab model
    model = tf.keras.Sequential([
        hub.KerasLayer(
            "https://tfhub.dev/google/imagenet/inception_v1/classification/5")
    ])
    model.build([None, 224, 224, 3])  # Batch input shape.

    # grab data
    data_root = tf.keras.utils.get_file(
      'flower_photos',
      'https://storage.googleapis.com/download.tensorflow.org/example_images/flower_photos.tgz',
        untar=True)

    batch_size = 32
    img_height = 224
    img_width = 224

    train_ds = tf.keras.utils.image_dataset_from_directory(
      str(data_root),
      validation_split=0.2,
      subset="training",
      seed=123,
      image_size=(img_height, img_width),
      batch_size=batch_size
    )

    val_ds = tf.keras.utils.image_dataset_from_directory(
      str(data_root),
      validation_split=0.2,
      subset="validation",
      seed=123,
      image_size=(img_height, img_width),
      batch_size=batch_size
    )

    normalization_layer = tf.keras.layers.Rescaling(1./255)
    # Where x—images, y—labels.
    train_ds = train_ds.map(lambda x, y: (normalization_layer(x), y))
    # Where x—images, y—labels.
    val_ds = val_ds.map(lambda x, y: (normalization_layer(x), y))

    AUTOTUNE = tf.data.AUTOTUNE
    train_ds = train_ds.cache().prefetch(buffer_size=AUTOTUNE)
    val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)

    # tboard_callback = tf.keras.callbacks.TensorBoard(
    #     log_dir="/tmp/logs/inception_v1_" + datetime.now().strftime("%Y%m%d-%H%M%S"),
    #     histogram_freq=1,
    #     write_graph=False,
    #     write_images=False,
    #     write_steps_per_second=False,
    #     update_freq='batch',
    #     profile_batch=(500, 600),
    # )

    model.predict(train_ds) # , callbacks=[tboard_callback])


if __name__ == '__main__':
    main()
