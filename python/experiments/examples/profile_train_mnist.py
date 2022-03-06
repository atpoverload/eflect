from datetime import datetime

import tensorflow as tf


def main():
    mnist = tf.keras.datasets.mnist

    (x_train, y_train), (x_test, y_test) = mnist.load_data()
    x_train, x_test = x_train / 255.0, x_test / 255.0

    model = tf.keras.models.Sequential([
      tf.keras.layers.Flatten(input_shape=(28, 28)),
      tf.keras.layers.Dense(128, activation='relu'),
      tf.keras.layers.Dropout(0.2),
      tf.keras.layers.Dense(10)
    ])

    loss_fn = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)

    model.compile(
        optimizer='adam',
        loss=loss_fn,
        metrics=['accuracy']
    )

    tboard_callback = tf.keras.callbacks.TensorBoard(
        log_dir="/tmp/logs/" + datetime.now().strftime("%Y%m%d-%H%M%S"),
        histogram_freq=1,
        write_graph=False,
        write_images=False,
        write_steps_per_second=False,
        update_freq='batch',
        profile_batch=(500, 600),
    )

    model.fit(x_train, y_train, epochs=5, callbacks=[tboard_callback])
    # model.fit(x_train, y_train, epochs=5)
    model.evaluate(x_test,  y_test, verbose=2)


if __name__ == '__main__':
    main()
