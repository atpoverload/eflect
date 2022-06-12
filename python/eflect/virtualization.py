""" processing and virtualization code for a DataSet using pandas """
import os

from argparse import ArgumentParser
from zipfile import ZipFile

import numpy as np
import pandas as pd

from pandas import to_datetime

from protos.sample.sample_pb2 import DataSet

# processing helpers
# TODO(timur): this is totally arbitrary. do we need metadata in the proto?
INTERVAL = '50ms'
WINDOW_SIZE = '501ms'


def bucket_timestamps(timestamps, interval=INTERVAL):
    """ Floors a series of timestamps to some interval for easy aggregates. """
    return to_datetime(timestamps).dt.floor(interval)


def max_rolling_difference(df, window_size=WINDOW_SIZE):
    """ Computes a rolling difference of points up to the window size. """
    values = df - df.rolling(window_size).min()

    timestamps = df.reset_index().timestamp.astype(int) / 10**9
    timestamps.index = df.index
    timestamps = timestamps - timestamps.rolling(window_size).min()

    return values, timestamps


# cpu jiffies processing
def parse_cpu_samples(samples):
    """ Converts a collection of CpuSample to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.cpu,
                stat.user,
                stat.nice,
                stat.system,
                stat.idle,
                stat.iowait,
                stat.irq,
                stat.softirq,
                stat.steal,
                stat.guest,
                stat.guest_nice
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'cpu',
        'user',
        'nice',
        'system',
        'idle',
        'iowait',
        'irq',
        'softirq',
        'steal',
        'guest',
        'guest_nice'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


# TODO(timur): it's not clear which of these are actually useful
ACTIVE_JIFFIES = [
  'cpu',
  'user',
  'nice',
  'system',
  # 'idle',
  # 'iowait',
  'irq',
  'softirq',
  'steal',
  'guest',
  'guest_nice',
]


def process_cpu_data(df):
    """ Computes the cpu jiffy rate of each 50ms bucket """
    df['jiffies'] = df[ACTIVE_JIFFIES].sum(axis=1)
    df.timestamp = bucket_timestamps(df.timestamp)

    jiffies = df.groupby(['timestamp', 'cpu']).jiffies.min().unstack()
    jiffies, ts = max_rolling_difference(jiffies)
    jiffies = jiffies.stack().reset_index()
    jiffies = jiffies.groupby(['timestamp', 'cpu']).sum().unstack()
    jiffies = jiffies.div(ts, axis=0).stack()[0]
    jiffies.name = 'jiffies'

    return jiffies


def cpu_samples_to_df(samples):
    """ Converts a collection of CpuSamples to a processed DataFrame. """
    return process_cpu_data(parse_cpu_samples(samples))


# task jiffies processing
def parse_task_samples(samples):
    """ Converts a collection of ProcessSamples to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.task_id,
                stat.name if stat.HasField('name') else '',
                stat.cpu,
                stat.user,
                stat.system
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'id',
        'thread_name',
        'cpu',
        'user',
        'system',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_task_data(df):
    """ Computes the app jiffy rate of each 50ms bucket """
    df['jiffies'] = df.user + df.system
    df = df[~df.thread_name.str.contains('eflect-')]
    df.timestamp = bucket_timestamps(df.timestamp)

    cpus = df.groupby(['timestamp', 'id']).cpu.max()
    jiffies, ts = max_rolling_difference(df.groupby([
        'timestamp',
        'id'
    ]).jiffies.min().unstack())
    jiffies = jiffies.stack().to_frame()
    jiffies['cpu'] = cpus
    jiffies = jiffies.groupby([
        'timestamp',
        'id',
        'cpu'
    ])[0].sum().unstack().unstack().div(ts, axis=0).stack().stack(0)
    jiffies.name = 'jiffies'

    return jiffies


def task_samples_to_df(samples):
    """ Converts a collection of ProcessSamples to a processed DataFrame. """
    return process_task_data(parse_task_samples(samples))


# battery_manager processing
def parse_battery_manager_samples(samples):
    """ Converts a collection of BatteryManagerSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.battery_property_energy_counter,
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'energy',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_battery_manager_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp']).min()

    energy, ts = max_rolling_difference(df)
    energy = energy.div(ts, axis=0)
    energy.name = 'energy'
    # TODO(timur): we're converting this to nanojoules for the moment
    energy = energy * 3600

    return energy


def battery_manager_samples_to_df(samples):
    """ Converts a collection of BatteryManagerSamples to a processed DataFrame. """
    return process_battery_manager_data(parse_battery_manager_samples(samples))


# nvml processing
def parse_nvml_samples(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.bus_id,
                reading.power_usage,
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'bus_id',
        'power_usage',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_nvml_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp', 'bus_id']).power_usage.min()

    # TODO(timur): this assumes the baseline is the smallest reading; we should do something else
    energy = df - df.groupby('bus_id').min()
    energy.name = 'energy'

    return energy


def nvml_samples_to_df(samples):
    """ Converts a collection of NvmlSamples to a processed DataFrame. """
    return process_nvml_data(parse_nvml_samples(samples))


# rapl processing
WRAP_AROUND_VALUE = 16384


def parse_rapl_samples(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.socket,
                reading.cpu,
                reading.package,
                reading.dram,
                reading.gpu
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'socket',
        'cpu',
        'package',
        'dram',
        'gpu'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


# TODO(timur): i've been told by alejandro that the value i'm using isn't
#   actually the wrap around. i'm not sure how to look it up properly however.
def maybe_apply_wrap_around(value):
    """ Checks if the value needs to be adjusted by the wrap around. """
    if value < 0:
        return value + WRAP_AROUND_VALUE
    else:
        return value


def process_rapl_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp', 'socket']).min()
    df.columns.name = 'component'

    energy, ts = max_rolling_difference(df.unstack())
    energy = energy.stack().stack().apply(maybe_apply_wrap_around)
    energy = energy.groupby([
        'timestamp',
        'socket',
        'component'
    ]).sum()
    energy = energy.div(ts, axis=0).dropna()
    energy.name = 'energy'

    return energy


def rapl_samples_to_df(samples):
    """ Converts a collection of RaplSamples to a processed DataFrame. """
    return process_rapl_data(parse_rapl_samples(samples))


# virtualization
def virtualize_jiffies(tasks, cpu):
    """ Returns the ratio of the jiffies with attribution corrections. """
    activity = (tasks / cpu).dropna().replace(np.inf, 1).clip(0, 1)
    activity = activity[activity > 0]
    activity.name = 'activity'
    return activity


def virtualize_energy(activity, energy):
    """ Computes the product of the data across shared indices. """
    try:
        df = activity * energy
        df.name = 'energy'
        return df
    except Exception as e:
        # TODO(timur): sometimes the data can't be aligned and i don't know why
        idx = list(set(activity.index.names) & set(energy.index.names))
        print('data could not be directly aligned: {}'.format(e))
        print('forcing merge on {} instead'.format(idx))
        energy = pd.merge(
            activity.reset_index(),
            energy.reset_index(),
            on=['timestamp']
        ).set_index(idx)
        energy = energy.activity * energy.energy
        energy.name = 'energy'
        return energy


def virtualize_battery_manager_energy(tasks, cpu, battery_manager):
    """ Returns the product of energy and activity. """
    activity = virtualize_jiffies(
        tasks.reset_index('cpu').jiffies,
        cpu.groupby(['timestamp']).sum()
    )

    battery_manager = battery_manager_samples_to_df(battery_manager)

    energy = virtualize_energy(activity, battery_manager)
    # TODO(timur): this is a crude post-virtualization clean up; we should pick
    #   something more formal
    energy = energy[energy > 0].dropna() / 1000000000
    return energy


# TODO(timur): this is an incomplete way of doing this. can we look up the
# thread positioning within the gpu with nvml?
def virtualize_nvml_energy(tasks, cpu, nvml):
    """ Returns the product of energy and activity. """
    activity = virtualize_jiffies(
        tasks.reset_index('cpu').jiffies,
        cpu.groupby(['timestamp']).sum()
    )

    nvml = nvml_samples_to_df(nvml)

    energy = virtualize_energy(activity, nvml)
    # TODO(timur): this is a crude post-virtualization clean up; we should pick
    #   something more formal
    energy = energy[energy > 0].dropna() / 1000
    return energy


# TODO(timur): find out if there's a way to abstract this
def RAPL_DOMAIN_CONVERSION(x): return 0 if int(x) < 20 else 1


def virtualize_rapl_energy(tasks, cpu, rapl):
    """ Returns the product of energy and activity by socket. """
    tasks = tasks.reset_index()
    tasks['socket'] = tasks.cpu.apply(RAPL_DOMAIN_CONVERSION)
    tasks = tasks.groupby(['timestamp', 'socket', 'id']).jiffies.sum()

    cpu = cpu.reset_index()
    cpu['socket'] = cpu.cpu.apply(RAPL_DOMAIN_CONVERSION)
    cpu = cpu.groupby(['timestamp', 'socket']).jiffies.sum()

    activity = virtualize_jiffies(tasks, cpu)

    rapl = rapl_samples_to_df(rapl)

    energy = virtualize_energy(activity, rapl)
    # TODO(timur): this is a crude post-virtualization clean up; we should pick
    #   something more formal
    energy = energy[energy > 0].dropna() / 1000000

    return energy


def virtualize_data(data):
    """ Produces energy virtualizations from a data set. """
    print('virtualizing application activity...')
    tasks = task_samples_to_df(data.process)
    cpu = cpu_samples_to_df(data.cpu)

    # TODO(timur): we need a virtualization proto for this
    virtualization = {}

    if len(data.battery_manager) > 0:
        print('virtualizing battery manager...')
        virtualization['battery_manager'] = virtualize_battery_manager_energy(
            tasks, cpu, data.battery_manager)

    if len(data.nvml) > 0:
        print('virtualizing nvml...')
        virtualization['nvml'] = virtualize_nvml_energy(tasks, cpu, data.nvml)

    if len(data.rapl) > 0:
        print('virtualizing rapl...')
        virtualization['rapl'] = virtualize_rapl_energy(tasks, cpu, data.rapl)

    return virtualization


# cli to process globs of files
def parse_args():
    """ Parses virtualization arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='files',
        nargs='*',
        default=None,
        help='files to process',
    )
    parser.add_argument(
        '-o',
        '--output_dir',
        dest='output',
        default=None,
        help='directory to write the processed data to',
    )
    return parser.parse_args()


def main():
    args = parse_args()
    for file in args.files:
        with open(file, 'rb') as f:
            data = DataSet()
            data.ParseFromString(f.read())

        if args.output:
            if os.path.exists(args.output) and not os.path.isdir(args.output):
                raise RuntimeError(
                    'output target {} already exists and is not a directory; aborting'.format(args.output))
            elif not os.path.exists(args.output):
                os.makedirs(args.output)

            path = os.path.join(args.output, os.path.splitext(
                os.path.basename(file))[0]) + '.zip'
        else:
            path = os.path.splitext(file)[0] + '.zip'
        footprints = virtualize_data(data)

        # TODO: this only spits out a single file. we should be able to write
        #   multiple files to the archive, but maybe not with pandas
        with ZipFile(path, 'w') as archive:
            for key in footprints:
                archive.writestr('{}.csv'.format(key), footprints[key].to_csv())


if __name__ == '__main__':
    main()
