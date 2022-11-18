""" Tests for the python processing script. """
import os
import unittest

import pandas as pd

import virtualization

INTERVAL = '1s'
FREQ = '50ms'
WINDOW = '5s'
TIMESTAMPS = pd.date_range(
    '1/1/1970',
    '1/1/1970 01:00:00',
    freq=FREQ,
    inclusive='left'
)


def data_file_path(path):
    return os.path.join(os.path.dirname(__file__), 'resources', path)


class TestProcessing(unittest.TestCase):
    def test_bucket_timestamps(self):
        buckets = virtualization.bucket_timestamps(pd.Series(TIMESTAMPS), INTERVAL)
        self.assertEqual(
            buckets.value_counts().max(),
            pd.to_timedelta(INTERVAL) / pd.to_timedelta(FREQ)
        )

    # def test_max_rolling_difference(self):
    #     s = pd.Series(index=TIMESTAMPS, data=list(range(len(TIMESTAMPS))))
    #     s.index.name = 'timestamp'
    #     s, ts = virtualization.max_rolling_difference(s, WINDOW)
    #
    #     self.assertEqual(
    #         (s / ts).dropna().astype(int).max(),
    #         pd.to_timedelta(INTERVAL) / pd.to_timedelta(FREQ)
    #     )
    #
    # def test_processing(self):
    #     data = DataSet()
    #     for i in range(2):
    #         reading = CpuReading()
    #         reading.cpu = 0
    #         reading.user = i
    #
    #         sample = CpuSample()
    #         sample.timestamp = TIMESTAMPS.astype(np.int64)[i] // 10**9
    #         sample.reading.append(reading)
    #
    #         data.cpu.append(sample)
    #
    #         # reading = CpuReading()
    #         # reading.cpu = 0
    #         # reading.user = 100
    #         #
    #         # sample = CpuSample()
    #         # sample.timestamp = TIMESTAMPS.astype(np.int64).max() // 10**9
    #         # sample.reading.append(reading)
    #         #
    #         # data.cpu.append(sample)
    #
    #     print(data.cpu)
    #
    #     df = virtualization.cpu_samples_to_df(data.cpu)
    #     print(df)
    #
    #     # print(TIMESTAMPS.astype(np.int64).min() // 10**9)
    #     # print(TIMESTAMPS.astype(np.int64).max() // 10**9)
    #     # with open(data_file_path('data_set.textproto')) as f:
    #     #     data = text_format.Parse(f.read(), DataSet())
    #     #
    #     #
    #     # print(df)
    #
    #     # print(data)
    #     # ts = pd.date_range(
    #     #     '1/1/1970',
    #     #     '1/1/1970 01:00:00',
    #     #     freq='50ms',
    #     #     inclusive='left'
    #     # )
    #     # df = pd.DataFrame(data={'ts': ts, 'x': len(ts) * [1]})
    #     #
    #     # INTERVAL = '1s'
    #     # self.assertEqual(
    #     #     virtualization.bucket_timestamps(
    #     #         df.ts, interval).value_counts().mean(),
    #     #     20
    #     # )
    #
    #     # window_size = '5s'
    #     # self.assertEqual(virtualization.max_rolling_difference(df, window_size))

if __name__ == '__main__':
    unittest.main()
